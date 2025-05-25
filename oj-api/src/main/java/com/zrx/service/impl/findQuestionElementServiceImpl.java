package com.zrx.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zrx.model.entity.NoticeTable;
import com.zrx.mapper.findQuestionElementMapper;
import com.zrx.service.findQuestionElementService;
import com.zrx.sys.mapper.SysRoleMapper;
import com.zrx.sys.mapper.SysRoleUserMapper;
import com.zrx.sys.model.entity.SysRole;
import com.zrx.sys.model.entity.SysRoleUser;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 土豆儿
 * @description 针对表【notice_table】的数据库操作Service实现
 * @createDate 2025-05-13 19:55:29
 */
@Service
@Slf4j
public class findQuestionElementServiceImpl extends ServiceImpl<findQuestionElementMapper, NoticeTable> implements findQuestionElementService {
    @Resource
    private findQuestionElementMapper noticeTableMapper;
    @Resource
    private SysRoleUserMapper sysRoleUserMapper;
    @Resource
    private SysRoleMapper sysRoleMapper;


    public List<NoticeTable> getNotice() {
        // 按 datetime 字段倒序
        QueryWrapper query = QueryWrapper.create().orderBy("datetime", false);
        return noticeTableMapper.selectListByQuery(query);
    }

    @Override
    public NoticeTable getNoticeById(Integer id) {
        QueryWrapper query = QueryWrapper.create()
                .select("id", "type", "datetime", "content")  // 明确指定要查询的字段
                .where("id = ?", id);
        return noticeTableMapper.selectOneByQuery(query);
    }


    public int insertNotice(String type, String content) {
        NoticeTable notice = new NoticeTable();
        notice.setType(type);
        notice.setContent(content);
        return noticeTableMapper.insert(notice);
    }


    public int getPermission(Long userId) {
        // 先查 sys_role_user 表，获取 roleId
        QueryWrapper roleUserQuery = QueryWrapper.create().where("user_id = ?", userId);
        SysRoleUser sysRoleUser = sysRoleUserMapper.selectOneByQuery(roleUserQuery);
        if (sysRoleUser == null) {
            return -1;
        }
        // 再查 sys_role 表，获取 sort
        QueryWrapper roleQuery = QueryWrapper.create().where("id = ?", sysRoleUser.getRoleId());
        SysRole sysRole = sysRoleMapper.selectOneByQuery(roleQuery);
        return sysRole != null ? sysRole.getSort() : -1;
    }

}




