package com.zrx.sys.mapper;

import com.mybatisflex.core.BaseMapper;
import com.zrx.sys.model.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 系统用户 映射层。
 *
 * @author zhang.rx
 * @since 2024/2/18
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

	@Update("update sys_user set password=#{password} where id=#{userId}")
	int resetPassword(@Param("userId") String userId, @Param("password") String password);

}
