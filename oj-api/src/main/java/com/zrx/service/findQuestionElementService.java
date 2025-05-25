package com.zrx.service;

import com.mybatisflex.core.service.IService;
import com.zrx.model.entity.NoticeTable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 土豆儿
 * @description 针对表【notice_table】的数据库操作Service
 * @createDate 2025-05-13 19:55:29
 */
@Service
public interface findQuestionElementService extends IService<NoticeTable> {
    // 获取用户权限
    int getPermission(Long id);

    // 发布公告
    int insertNotice(String type, String content);

    // 获取公告
    List<NoticeTable> getNotice();

    // 查询单个公告
    NoticeTable getNoticeById(Integer id);


}
