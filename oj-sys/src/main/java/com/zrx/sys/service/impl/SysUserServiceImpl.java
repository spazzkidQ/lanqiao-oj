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
import com.zrx.sys.mapper.SysUserMapper;
import com.zrx.sys.mapstruct.SysUserMap;
import com.zrx.sys.model.dto.SysUserRequest;
import com.zrx.sys.model.dto.SysUserRoleBo;
import com.zrx.sys.model.dto.SysUserUpdateRequest;
import com.zrx.sys.model.entity.SysRole;
import com.zrx.sys.model.entity.SysRoleUser;
import com.zrx.sys.model.entity.SysUser;
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

import java.util.List;
import java.util.Map;
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



	@Override
	public SysUser getByUsername(String username) {
		return null;
	}

	@Override
	public LoginResponse login(LoginRequest loginRequest) {

		// 只是为了程序不报错;写的时候注释掉就行;
		SysUser user = new SysUser();

		// 删除之前的token信息
		StpUtil.logout(user.getId());
		String loginId = String.valueOf(user.getId());
		StpUtil.login(loginId);
		// 存储已经登录的用户信息
		StpUtil.getSession().set(loginId, user);
		return new LoginResponse(StpUtil.getTokenInfo().getTokenValue(), loginId);
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

}
