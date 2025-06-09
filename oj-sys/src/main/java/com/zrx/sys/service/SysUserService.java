package com.zrx.sys.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.zrx.model.common.Paging;
import com.zrx.security.model.dto.LoginRequest;
import com.zrx.security.model.dto.RegisterRequest;
import com.zrx.security.model.vo.LoginResponse;
import com.zrx.sys.model.dto.ChangePasswordRequest;
import com.zrx.sys.model.dto.SysUserRequest;
import com.zrx.sys.model.dto.SysUserUpdateRequest;
import com.zrx.sys.model.entity.SysUser;
import com.zrx.sys.model.vo.SysUSerManage;
import com.zrx.sys.model.vo.SysUserResponse;
import com.zrx.sys.model.vo.SysUserSimpleResponse;
import com.zrx.sys.model.vo.ForgotPasswordResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 系统用户 服务层。
 *
 * @author zhang.rx
 * @since 2024/2/18
 */
public interface SysUserService extends IService<SysUser> {

	SysUser getByUsername(String username);

	Boolean update(SysUserUpdateRequest request);

	LoginResponse login(LoginRequest loginRequest);

	String register(RegisterRequest registerRequest);

	Boolean disable(String id);

	Boolean enable(String id);

	SysUserResponse getInfo();

	SysUserSimpleResponse getInfoById(String userId);

	Page<SysUserResponse> page(Paging page, SysUserRequest request);

	String uploadAvatar(MultipartFile file);

	Boolean kick(Long id);

	/**
	 * 修改密码
	 * @param request 修改密码请求体
	 * @return 是否成功
	 */
	String changePassword(ChangePasswordRequest request);
	/**
	 * 设置密保
	 * @param request 密保请求体
	 * @return 是否成功
	 */
	SysUSerManage setSecurityQuestion(SysUSerManage request);
	/**
	 * 获取密保
	 * @param userId 用户ID
	 * @return 密保
	 */
	int getSecurityQuestion(SysUSerManage userId);
	/**
	 * 验证密保
	 * @param request 密保请求体
	 * @return 是否成功
	 */
	int verifySecurityQuestion(SysUSerManage request);

	/**
	 * 发送手机验证码
	 * @param userId 用户ID
	 * @param mobile 手机号
	 * @return 是否发送成功
	 */
	String sendMobileCode(String userId, String mobile);

	/**
	 * 绑定手机号
	 * @param userId 用户ID
	 * @param mobile 手机号
	 * @param code 验证码
	 * @return 绑定结果
	 */
	String bindMobile(String userId, String mobile, String code);

	/**
	 * 绑定邮箱
	 * @param userId 用户ID
	 * @param email 邮箱
	 * @param code 验证码
	 * @return 绑定结果
	 */
	String bindEmail(String userId, String email, String code);

	/**
	 * 本地上传头像（存储到 user-avatars 文件夹）
	 * @param file 头像文件
	 * @return 头像访问路径
	 */
	String uploadAvatarToUserAvatars(org.springframework.web.multipart.MultipartFile file);

	/**
	 * 忘记密码-根据用户名查找用户id
	 */
	Long forgotPassword(String username);

	/**
	 * 忘记密码-根据用户名查找用户信息
	 */
	ForgotPasswordResponse forgotPasswordInfo(String username);

	/**
	 * 重置密码-根据userId修改密码
	 */
	String resetPassword(String userId, String password);

	/**
	 * 校验密保答案
	 */
	boolean verifyQuestion(String userId, String answer);
}
