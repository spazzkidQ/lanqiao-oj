package com.zrx.sys.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.annotation.SaMode;
import com.mybatisflex.core.paginate.Page;
import com.zrx.exception.BusinessException;
import com.zrx.model.common.Paging;
import com.zrx.reuslt.Result;
import com.zrx.security.satoken.AuthConst;
import com.zrx.sys.model.dto.ChangePasswordRequest;
import com.zrx.sys.model.dto.SysUserRequest;
import com.zrx.sys.model.dto.SysUserUpdateRequest;
import com.zrx.sys.model.entity.SysUser;
import com.zrx.sys.model.vo.SysUSerManage;
import com.zrx.sys.model.vo.SysUserResponse;
import com.zrx.sys.model.vo.SysUserSimpleResponse;
import com.zrx.sys.service.SysUserService;
import com.zrx.sys.utils.EmailUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 系统用户 控制层。
 *
 * @author zhang.rx
 * @since 2024/2/18
 */
@RestController
@Tag(name = "SysUser", description = "系统用户管理接口")
@RequestMapping("/sys/sysUser")
public class SysUserController {

	@Resource
	private SysUserService sysUserService;

	@Resource
	private EmailUtil emailUtil;

	/**
	 * 上传头像
	 * @param file 文件
	 * @return url
	 */
	@PostMapping("/upload/avatar")
	@Operation(summary = "上传头像")
	public Result<String> upload(@RequestParam("file") MultipartFile file) {
		return Result.success(sysUserService.uploadAvatarToUserAvatars(file));
	}

	/**
	 * 修改用户信息--开放给个人用户使用
	 * @param request 请求参数
	 * @return boolean
	 */
	@PutMapping("/update")
	@Operation(summary = "修改用户信息--用户使用")
	public Result<Boolean> updateUserInfo(@RequestBody SysUserUpdateRequest request) {
		return Result.success(sysUserService.update(request));
	}

	/**
	 * 禁用用户
	 * @param id 主键
	 * @return 禁用结果
	 */
	@PutMapping("/disable/{id}")
	@Operation(summary = "禁用用户")
	@SaCheckRole(AuthConst.SUPER_ADMIN)
	public Result<String> disable(@PathVariable @Parameter(description = "主键") String id) {
		if (!sysUserService.disable(id)) {
			throw new BusinessException("禁用失败");
		}
		return Result.ok();
	}

	/**
	 * 启用用户
	 * @param id 主键
	 * @return 启用结果
	 */
	@PutMapping("/enable/{id}")
	@Operation(summary = "启用用户")
	@SaCheckRole(AuthConst.SUPER_ADMIN)
	public Result<Boolean> enable(@PathVariable @Parameter(description = "主键") String id) {
		if (!sysUserService.enable(id)) {
			throw new BusinessException("启用失败");
		}
		return Result.ok();
	}

	/**
	 * 根据主键获取系统用户详细信息。
	 * @return 详情
	 */
	@GetMapping("/getInfo")
	@Operation(summary = "获取已登录的当前用户信息--用户使用")
	public Result<SysUserResponse> getInfo() {
		return Result.success(sysUserService.getInfo());
	}

	/**
	 * 根据主键获取系统用户信息。
	 * @return 详情
	 */
	@GetMapping("/getInfoById/{id}")
	@Operation(summary = "根据主键获取系统用户信息--用户使用")
	public Result<SysUserSimpleResponse> getInfoById(@PathVariable("id") String userId) {
		return Result.success(sysUserService.getInfoById(userId));
	}

	/**
	 * 分页查询。
	 * @param page 分页对象
	 * @return 分页对象
	 */
	@GetMapping("/page")
	@Operation(summary = "分页查询")
	@SaCheckRole(value = { AuthConst.SUPER_ADMIN, AuthConst.ADMIN }, mode = SaMode.OR)
	public Result<Page<SysUserResponse>> page(@Parameter(description = "分页条件") Paging page,
											  @Parameter(description = "查询条件") SysUserRequest request) {
		return Result.success(sysUserService.page(page, request));
	}

	/**
	 * 获取已登录的当前用户信息。
	 * @return 当前用户信息
	 */
	@GetMapping("/info")
	@Operation(summary = "根据主键获取系统用户详细信息--admin使用")
	@SaCheckRole(AuthConst.SUPER_ADMIN)
	public Result<SysUser> infoById(String id) {
		return Result.success(sysUserService.getById(id));
	}

	/**
	 * 强制踢人下线
	 */
	@PutMapping("/kick/{id}")
	@Operation(summary = "强制踢人下线")
	@SaCheckRole(AuthConst.SUPER_ADMIN)
	public Result<Boolean> kick(@PathVariable @Parameter(description = "主键") Long id) {
		return Result.success(sysUserService.kick(id));
	}
	/**
	 * 修改密码
	 */
	@PostMapping("/changePassword")
	@Operation(summary = "修改密码")
	public Result<String> changePassword(@RequestBody ChangePasswordRequest request) {
		return Result.success(sysUserService.changePassword(request));
	}
	/**
	 * 设置密保
	 */
	@PostMapping("/setSecurityQuestion")
	@Operation(summary = "设置密保")
	public Result<String> changePassword(@RequestBody SysUSerManage request) {
		SysUSerManage sysUSerManage = sysUserService.setSecurityQuestion(request);
		if (sysUSerManage != null) {
			int securityQuestion = sysUserService.getSecurityQuestion(request);
			if (securityQuestion == 1) {
				return Result.success("设置密保成功");
			} else {
				return Result.fail("设置密保失败");
			}
		}else {
			int i = sysUserService.verifySecurityQuestion(request);
			if (i == 1) {
				return Result.success("密保设置成功");
			} else {
				return Result.fail("密保设置失败");
			}
		}
	}
	/*
	 * 查看密保
	 * */
	@PostMapping("/viewThePassword")
	@Operation(summary = "查看密保")
	public Result<String> viewThePassword(@RequestBody SysUSerManage request) {
		SysUSerManage sysUSerManage = sysUserService.setSecurityQuestion(request);
		if (sysUSerManage != null){
			System.out.println("                           断行专用                  ");
			System.out.println(sysUSerManage.getQuestion());
			return Result.success(sysUSerManage.getQuestion());
		}else {
			return null;
		}
	}
	/*
	 * 给手机号发送短信
	 * */
	@PostMapping("/sendMobileCode")
	@Operation(summary = "发送手机验证码")
	public Result<String> sendMobileCode(@RequestBody Map<String, String> payload) {
		String userId = payload.get("userId");
		String mobile = payload.get("mobile");
		if (userId == null || mobile == null) {
			return Result.fail("参数不完整");
		}
		String success = sysUserService.sendMobileCode(userId, mobile);
		if (success == null){
			return Result.fail("发送失败");
		}
		return Result.success(success);
	}

	@PostMapping("/bindMobile")
	@Operation(summary = "绑定手机号")
	public Result<String> bindMobile(@RequestBody Map<String, String> payload) {
		String userId = payload.get("userId");
		String mobile = payload.get("mobile");
		String code = payload.get("code");
		String result = sysUserService.bindMobile(userId, mobile, code);
		if ("绑定成功".equals(result)) {
			return Result.success(result);
		} else {
			return Result.fail(result);
		}
	}

	@PostMapping("/sendEmailCode")
	@Operation(summary = "发送验证码")
	public Result<String> sendEmailCode(@RequestBody Map<String, String> payload){
		String email = payload.get("email");
		// 邮件标题
		String title = "验证码";
		// 生成6位随机验证码
		String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
		// 邮件正文
		String content = "您的验证码为：" + code + "，有效期5分钟。";
		try {
			emailUtil.sendSimpleMail(email, title, content);
			// 这里可以将验证码存入缓存（如Redis），略
			return Result.success(code);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.fail("发送邮件失败");
		}
	}

	@PostMapping("/bindEmail")
	@Operation(summary = "绑定邮箱")
	public Result<String> bindEmail(@RequestBody Map<String, String> payload) {
		String userId = payload.get("userId");
		String email = payload.get("email");
		String code = payload.get("code");
		String result = sysUserService.bindEmail(userId, email, code);
		if ("绑定成功".equals(result)) {
			return Result.success(result);
		} else {
			return Result.fail(result);
		}
	}

	@PostMapping("/resetPassword")
	@Operation(summary = "重置密码-根据userId修改密码")
	public Result<String> resetPassword(@RequestBody Map<String, String> body) {
		String userId = body.get("userId");
		String password = body.get("password");
		String result = sysUserService.resetPassword(userId, password);
		return Result.success(result);
	}

}