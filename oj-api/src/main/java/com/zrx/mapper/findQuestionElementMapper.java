package com.zrx.mapper;

import com.mybatisflex.core.BaseMapper;
import com.zrx.model.entity.NoticeTable;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author 土豆儿
* @description 针对表【notice_table】的数据库操作Mapper
* @createDate 2025-05-13 19:55:29
* @Entity com.zrx.model.entity.NoticeTable
*/
@Mapper
public interface findQuestionElementMapper extends BaseMapper<NoticeTable> {
    // 获取用户权限
    @Select("SELECT sort from sys_role where id = (SELECT role_id FROM sys_role_user where user_id = #{id})")
    int getPermission(Long id);
    // 发布公告
    @Insert("INSERT INTO notice_table (type, content) VALUES (#{type}, #{content})")
    int insertNotice(@Param("type") String type, @Param("content") String content);
    // 获取公告
    @Select("SELECT * FROM notice_table ORDER BY date DESC")
    List<NoticeTable> getNotice();
}




