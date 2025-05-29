package com.zrx.service;


import com.mybatisflex.core.service.IService;
import com.zrx.model.dto.postComment.PostCommentRequest;
import com.zrx.model.entity.OjPostComment;
import com.zrx.model.vo.PostCommentVo;

import java.io.Serializable;
import java.util.List;

/**
 * 帖子评论 服务层。
 *
 * @author zhang.rx
 * @since 2024/5/20
 */
public interface OjPostCommentService extends IService<OjPostComment> {
    //  添加评论
    Boolean addComment(OjPostComment ojPostComment);

    // 根据文章id查询评论记录
    List<OjPostComment> selectByPostId(Long postId);

    //根据 id 查询完整树型结构评论
    List<PostCommentVo> findList(Long postId);
}
