package com.zrx.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.zrx.model.common.Paging;
import com.zrx.model.entity.OjPost;
import com.zrx.model.entity.OjPostThumb;
import com.zrx.model.vo.OjPostVo;
import com.zrx.sys.model.entity.SysUser;

import java.util.List;
import java.util.Map;

/**
 * 帖子点赞 服务层。
 *
 * @author zhang.rx
 * @since 2024/5/13
 */
public interface OjPostThumbService extends IService<OjPostThumb> {

    //根据条件查询点赞记录是否存在
    // Boolean getThumb(OjPostThumb ojPostThumb);

    //添加点赞记录
    Boolean addThumb(Long id);

    //取消点赞记录
    Boolean removeThumb(Long id);

    // 查询用户点赞帖子
    Page<OjPostVo> findThumbList(Long id, Paging page);

    // 根据ids查询用户信息
    Map<Long,SysUser> findAuthor(List<Long> ids);
}
