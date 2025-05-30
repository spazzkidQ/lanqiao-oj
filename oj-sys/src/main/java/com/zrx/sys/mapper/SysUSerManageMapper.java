package com.zrx.sys.mapper;

import com.zrx.sys.model.vo.SysUSerManage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysUSerManageMapper {
    @Select("select * from sys_manage where userId=#{userId}")
    SysUSerManage translate(String bb);
    @Update("update sys_manage set question=#{question},answer=#{answer} where userId=#{userId}")
    int update(SysUSerManage sysUSerManage);
    @Insert("insert into sys_manage(userId,question,answer) values(#{userId},#{question},#{answer})")
    int insert(SysUSerManage sysUSerManage);

}