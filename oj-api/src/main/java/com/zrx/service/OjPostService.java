package com.zrx.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.zrx.model.common.Paging;
import com.zrx.model.dto.post.OjPostQueryRequest;
import com.zrx.model.dto.post.OjPostUpdateRequest;
import com.zrx.model.entity.OjPost;
import com.zrx.model.vo.OjPostVo;

/**
 * 帖子 服务层。
 *
 * @author zhang.rx
 * @since 2024/5/13
 */
public interface OjPostService extends IService<OjPost> {
    /*
   分页查询
   */
    Page<OjPostVo> pageSelfOrPage(Paging page, OjPostQueryRequest req);

   /*
   根据id查询
    */
    OjPostVo getInfoById(String id);

    /*
    编辑功能
     */
     Boolean updateById(OjPostUpdateRequest req);

     /*
     删除
      */
    Boolean removeById(Long id);

    //帖子首页  屈
    //分页条件查询
    Page<OjPostVo> getList(Paging page, OjPostQueryRequest req, Boolean selfFlag);

    //获取五个热力帖子
    List<OjPostSimpleVo> getFiveHotPost();
}
