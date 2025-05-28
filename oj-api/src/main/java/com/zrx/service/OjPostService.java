package com.zrx.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.zrx.model.common.Paging;
import com.zrx.model.dto.post.OjPostAddRequest;
import com.zrx.model.dto.post.OjPostQueryRequest;
import com.zrx.model.dto.post.OjPostUpdateRequest;
import com.zrx.model.entity.OjPost;
import com.zrx.model.vo.OjPostSimpleVo;
import com.zrx.model.vo.OjPostVo;

import java.util.List;

/**
 * 帖子 服务层。
 *
 * @author zhang.rx
 * @since 2024/5/13
 */
public interface OjPostService extends IService<OjPost> {

    //帖子首页  屈
    //分页条件查询
    Page<OjPostVo> getList(Paging page, OjPostQueryRequest req, Boolean selfFlag);

    //获取五个热力帖子
    List<OjPostSimpleVo> getFiveHotPost();
}
