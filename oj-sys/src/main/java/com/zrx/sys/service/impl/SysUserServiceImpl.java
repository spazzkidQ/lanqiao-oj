package com.zrx.sys.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.mybatisflex.core.mask.MaskManager;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.core.util.StringUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zrx.exception.BusinessException;
import com.zrx.model.common.Paging;
import com.zrx.reuslt.ResultCode;
import com.zrx.security.model.dto.LoginRequest;
import com.zrx.security.model.dto.RegisterRequest;
import com.zrx.security.model.vo.LoginResponse;
import com.zrx.security.satoken.AuthConst;
import com.zrx.security.service.CaptchaService;
import com.zrx.security.utils.Md5Util;
import com.zrx.security.utils.SecurityHelper;
import com.zrx.sys.enums.UserStatusEnum;
import com.zrx.sys.mapper.SysRoleMapper;
import com.zrx.sys.mapper.SysRoleUserMapper;
import com.zrx.sys.mapper.SysUSerManageMapper;
import com.zrx.sys.mapper.SysUserMapper;
import com.zrx.sys.mapstruct.SysUserMap;
import com.zrx.sys.model.dto.SysUserRequest;
import com.zrx.sys.model.dto.SysUserRoleBo;
import com.zrx.sys.model.dto.SysUserUpdateRequest;
import com.zrx.sys.model.dto.ChangePasswordRequest;
import com.zrx.sys.model.entity.SysRole;
import com.zrx.sys.model.entity.SysRoleUser;
import com.zrx.sys.model.entity.SysUser;
import com.zrx.sys.model.vo.ForgotPasswordResponse;
import com.zrx.sys.model.vo.SysUSerManage;
import com.zrx.sys.model.vo.SysUserResponse;
import com.zrx.sys.model.vo.SysUserSimpleResponse;
import com.zrx.sys.service.SysFileService;
import com.zrx.sys.service.SysUserService;
import com.zrx.sys.utils.RoleUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.zrx.sys.model.entity.table.SysRoleTableDef.SYS_ROLE;
import static com.zrx.sys.model.entity.table.SysRoleUserTableDef.SYS_ROLE_USER;
import static com.zrx.sys.model.entity.table.SysUserTableDef.SYS_USER;

/**
 * 系统用户 服务层实现。
 *
 * @author zhang.rx
 * @since 2024/2/18
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

	private final CaptchaService captchaService;

	private final SysUserMap sysUserMap;

	private final SysFileService sysFileService;

	private final SysRoleUserMapper roleUserMapper;

	private final SysRoleMapper roleMapper;

	private final RoleUtil roleUtil;

	private final SysUSerManageMapper sysUSerManageMapper;

	@Override
	public String uploadAvatar(MultipartFile file) {
		String url = sysFileService.upload(file);
		SysUser user = SecurityHelper.getUser();
		user.setAvatar(url);
		boolean updateFlag = updateById(user);
		if (!updateFlag) {
			throw new BusinessException("上传头像失败");
		}
		return url;
	}

	@Override
	public Boolean update(SysUserUpdateRequest request) {
		SysUser user = SecurityHelper.getUser();
		if (!user.getId().equals(request.getId())) {
			throw new BusinessException("无权限");
		}
		SysUser sysUser = sysUserMap.toEntityByUpdate(request);
		return mapper.update(sysUser) == 1;
	}

	@Override
	public SysUser getByUsername(String username) {
		if (StringUtils.isBlank(username)) {
			throw new BusinessException("用户名为空");
		}
		return mapper.selectOneByQuery(new QueryWrapper().where(SYS_USER.USERNAME.eq(username)));
	}

	@Override
	public LoginResponse login(LoginRequest loginRequest) {
		// 验证码是否正确
		boolean flag = captchaService.validate(loginRequest.getUuid(), loginRequest.getCaptcha());
		if (!flag) {
			throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "验证码错误");
		}
		// 用户信息
		SysUser user = getByUsername(loginRequest.getUsername());
		// 用户不存在
		if (user == null) {
			throw new BusinessException(ResultCode.FAIL.getCode(), "用户不存在");
		}
		// 密码错误
		if (!StringUtils.equals(Md5Util.inputPassToDBPass(loginRequest.getPassword(), user.getSalt()),
				user.getPassword())) {
			throw new BusinessException(ResultCode.FAIL.getCode(), "密码错误");
		}
		// 账号停用
		if (user.getStatus() == UserStatusEnum.DISABLE.getValue()) {
			throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "当前用户已被系统停用");
		}
		// 删除之前的token信息
		StpUtil.logout(user.getId());
		String loginId = String.valueOf(user.getId());
		StpUtil.login(loginId);
		// 存储已经登录的用户信息
		StpUtil.getSession().set(loginId, user);
		return new LoginResponse(StpUtil.getTokenInfo().getTokenValue(), loginId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public String register(RegisterRequest registerRequest) {
		String username = registerRequest.getUsername();
		String password = registerRequest.getPassword();
		String checkPassword = registerRequest.getCheckPassword();
		String mobile = registerRequest.getMobile();
		if (!StringUtils.equals(password, checkPassword)) {
			throw new BusinessException("两次输入的密码不一致");
		}
		SysUser sysUser = new SysUser();
		String salt = Md5Util.generateSalt();
		sysUser.setUsername(registerRequest.getUsername());
		sysUser.setPassword(Md5Util.inputPassToDBPass(password, salt));
		sysUser.setSalt(salt);
		sysUser.setStatus(UserStatusEnum.ENABLED.getValue());
		sysUser.setMobile(mobile);

		// 是否有相同用户名的用户
		if (exists(new QueryWrapper().where(SYS_USER.USERNAME.eq(username)))) {
			throw new BusinessException("用户名重复");
		}
		boolean saveFlag = save(sysUser);
		// 设置最初角色：普通用户
		SysRoleUser sysRoleUser = new SysRoleUser();
		sysRoleUser.setUserId(sysUser.getId());
		sysRoleUser.setRoleId(QueryChain.of(roleMapper)
			.select(SYS_ROLE.ID)
			.where(SYS_ROLE.ROLE_CODE.eq(AuthConst.USER))
			.oneAs(Long.class));
		int insertChildFlag = roleUserMapper.insert(sysRoleUser);
		if (!saveFlag && insertChildFlag != 1) {
			throw new BusinessException("注册失败");
		}
		return "注册成功";
	}

	@Override
	public Boolean disable(String id) {
		boolean updateFlag = UpdateChain.of(mapper)
			.where(SYS_USER.ID.eq(id))
			.set(SYS_USER.STATUS, UserStatusEnum.DISABLE.getValue())
			.update();
		if (!updateFlag) {
			throw new BusinessException("停用失败");
		}
		// 强制踢下线
		StpUtil.kickout(id);
		return true;
	}

	@Override
	public Boolean enable(String id) {
		return UpdateChain.of(mapper)
			.where(SYS_USER.ID.eq(id))
			.set(SYS_USER.STATUS, UserStatusEnum.ENABLED.getValue())
			.update();
	}

	@Override
	public SysUserResponse getInfo() {
		Long userId = SecurityHelper.getUser().getId();
		// TODO: 统一拦截脱敏、解除脱敏
		SysUser sysUser;
		try {
			MaskManager.skipMask();
			sysUser = mapper.selectOneByQuery(new QueryWrapper().where(SYS_USER.ID.eq(userId)));
		}
		finally {
			MaskManager.restoreMask();
		}
		if (sysUser == null) {
			throw new BusinessException("未找到该用户信息");
		}
		SysUserResponse resp = sysUserMap.toResp(sysUser);
		SysRole sysRole = roleUtil.selectMaxRoleByUserId(userId);
		resp.setRole(sysRole.getRoleCode());
		return resp;
	}

	@Override
	public SysUserSimpleResponse getInfoById(String userId) {
		SysUser sysUser = mapper.selectOneByCondition(SYS_USER.ID.eq(userId, StringUtil::isNotBlank));
		return sysUserMap.toSimpleResp(sysUser);
	}

	@Override
	public Page<SysUserResponse> page(Paging page, SysUserRequest request) {
		QueryWrapper queryWrapper = new QueryWrapper().select(SYS_USER.DEFAULT_COLUMNS)
			.where(SYS_USER.USERNAME.like(request.getUsername(), StringUtil::isNotBlank))
			.and(SYS_USER.REAL_NAME.like(request.getRealName(), StringUtil::isNotBlank))
			.and(SYS_USER.GENDER.eq(request.getGender()))
			.and(SYS_USER.EMAIL.like(request.getEmail(), StringUtil::isNotBlank))
			.and(SYS_USER.MOBILE.like(request.getMobile(), StringUtil::isNotBlank))
			.and(SYS_USER.STATUS.eq(request.getStatus()))
			.orderBy(SYS_USER.CREATE_TIME.asc());
		Page<SysUserResponse> respPage = mapper.paginateAs(Page.of(page.getPageNum(), page.getPageSize()), queryWrapper,
				SysUserResponse.class);
		// 获取用户角色
		getUserRole(respPage.getRecords());
		// 获取当前用户是否在线
		respPage.getRecords().forEach(item -> {
			SaSession session = StpUtil.getSessionByLoginId(item.getId(), false);
			item.setOnline(session != null);
		});
		return respPage;
	}

	/**
	 * 获取用户角色并填充
	 * @param respList respList
	 */
	private void getUserRole(List<SysUserResponse> respList) {
		List<Long> userIdList = respList.stream().map(SysUserResponse::getId).toList();
		QueryWrapper queryWrapper = new QueryWrapper().select(SYS_ROLE_USER.USER_ID.as("userId"), SYS_ROLE.REMARK)
			.from(SYS_ROLE)
			.leftJoin(SYS_ROLE_USER)
			.on(SYS_ROLE_USER.ROLE_ID.eq(SYS_ROLE.ID))
			.where(SYS_ROLE_USER.USER_ID.in(userIdList));
		Map<Long, List<String>> userRoleMap = roleUserMapper.selectListByQueryAs(queryWrapper, SysUserRoleBo.class)
			.stream()
			.collect(Collectors.toMap(SysUserRoleBo::getUserId, SysUserRoleBo::getRemark));
		respList.forEach(item -> {
			if (userRoleMap.containsKey(item.getId())) {
				item.setRoles(userRoleMap.get(item.getId()));
			}
		});
	}

	@Override
	public Boolean kick(Long id) {
		StpUtil.kickout(id);
		return true;
	}

	@Override
	public String changePassword(ChangePasswordRequest request) {
		SysUser user;
		try {
			MaskManager.skipMask();
			user = mapper.selectOneByQuery(new QueryWrapper().where(SYS_USER.ID.eq(request.getUserId())));
			if (user == null) {
				return "用户不存在";
			}
			// 校验旧密码
			String oldPwdDb = Md5Util.inputPassToDBPass(request.getOldPassword(), user.getSalt());
			System.out.println(oldPwdDb);
			System.out.println(user.getPassword());
			if (!oldPwdDb.equals(user.getPassword())) {
				return "旧密码错误";
			}
			// 校验新密码格式（可选）
			String newPwdDb = Md5Util.inputPassToDBPass(request.getNewPassword(), user.getSalt());
			user.setPassword(newPwdDb);
			int update = mapper.update(user);
			if (update != 1) {
				return "密码修改失败";
			}
			return "密码修改成功";
		} catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	public SysUSerManage setSecurityQuestion(SysUSerManage request) {
		SysUSerManage translate = sysUSerManageMapper.translate(request.getUserId());
		if (translate == null){
			return null;
		}
		return translate;

	}

	@Override
	public int getSecurityQuestion(SysUSerManage userId) {
		int update = sysUSerManageMapper.update(userId);
		if (update != 1){
			return 0;
		}
		return 1;
	}

	@Override
	public int verifySecurityQuestion(SysUSerManage request) {
		System.out.println(request);
		int insert = sysUSerManageMapper.insert(request);
		if (insert != 1){
			return 0;
		}
		return 1;
	}

	@Override
	public String sendMobileCode(String userId, String mobile) {
		// 这里可以集成短信服务商SDK，现用简单模拟
		if (StringUtils.isBlank(userId) || StringUtils.isBlank(mobile)) {
			throw new BusinessException("参数不能为空");
		}
		// 生成验证码（6位数字）
		String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
		// TODO: 调用短信服务商API发送验证码
		System.out.println("向手机号 " + mobile + " 发送验证码: " + code);
		// 你可以将验证码存入缓存（如Redis），此处略
		return code;
	}

	@Override
	public String bindMobile(String userId, String mobile, String code) {
		if (StringUtils.isBlank(userId) || StringUtils.isBlank(mobile) || StringUtils.isBlank(code)) {
			return "参数不能为空";
		}
		SysUser user = mapper.selectOneByQuery(new QueryWrapper().where(SYS_USER.ID.eq(userId)));
		if (user == null) {
			return "用户不存在";
		}
		user.setMobile(mobile);
		int update = mapper.update(user);
		if (update != 1) {
			return "绑定失败";
		}
		return "绑定成功";
	}

	@Override
	public String bindEmail(String userId, String email, String code) {
		if (StringUtils.isBlank(userId) || StringUtils.isBlank(email) || StringUtils.isBlank(code)) {
			return "参数不能为空";
		}
		SysUser user = mapper.selectOneByQuery(new QueryWrapper().where(SYS_USER.ID.eq(userId)));
		if (user == null) {
			return "用户不存在";
		}
		// TODO: 校验验证码，后续可补充
		user.setEmail(email);
		int update = mapper.update(user);
		if (update != 1) {
			return "绑定失败";
		}
		return "绑定成功";
	}


	@Override
	public String uploadAvatarToUserAvatars(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException("文件不能为空");
		}
		// 修改为 D:/IT/ITCase/Vue/WS/lanqiao-oj/user-avatars/ 目录
		String uploadDir = "D:/IT/ITCase/Vue/WS/lanqiao-oj/oj-sys/src/main/resources/user-avatars/";
		File dir = new File(uploadDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		SysUser user = com.zrx.security.utils.SecurityHelper.getUser();
		String userId = String.valueOf(user.getId());
		String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
		String fileName = userId + ext;
		File dest = new File(uploadDir + fileName);
		try {
			file.transferTo(dest);
		} catch (IOException e) {
			throw new BusinessException("文件保存失败");
		}
		String avatarPath = "/user-avatars/" + fileName;
		user.setAvatar(avatarPath);
		updateById(user);
		return avatarPath;
	}

	@Override
	public Long forgotPassword(String username) {
		SysUser user = getByUsername(username);
		return user != null ? user.getId() : null;
	}

	@Override
	public ForgotPasswordResponse forgotPasswordInfo(String username) {
		SysUser user = getByUsername(username);
		if (user == null) {
			return null;
		}
		ForgotPasswordResponse resp = new ForgotPasswordResponse();
		resp.setId(user.getId());
		resp.setMobile(user.getMobile());
		resp.setEmail(user.getEmail());
		SysUSerManage manage = sysUSerManageMapper.translate(String.valueOf(user.getId()));
		if (manage != null) {
			resp.setQuestion(manage.getQuestion());
		} else {
			resp.setQuestion(null);
		}
		return resp;
	}

	@Override
	public String resetPassword(String userId, String password) {
		SysUser user = mapper.selectOneByQuery(new QueryWrapper().where(SYS_USER.ID.eq(userId)));
		if (user == null) {
			return "用户不存在";
		};
		String dbPass = com.zrx.security.utils.Md5Util.inputPassToDBPass(password, user.getSalt());
		int update = ((com.zrx.sys.mapper.SysUserMapper)mapper).resetPassword(userId, dbPass);
		if (update != 1) {
			return "密码重置失败";
		}
		return "密码重置成功";
	}

	@Override
	public boolean verifyQuestion(String userId, String answer) {
		SysUSerManage manage = sysUSerManageMapper.translate(userId);
		System.out.println(manage);
		if (manage == null || !manage.getAnswer().equals(answer) ) {
			return false;
		}
		return true;
	}

}
