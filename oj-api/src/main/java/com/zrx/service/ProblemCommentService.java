package com.zrx.service;

import com.mybatisflex.core.service.IService;
import com.zrx.model.dto.problemComment.ProblemCommentRequest;
import com.zrx.model.entity.ProblemComment;
import com.zrx.model.vo.PostCommentVo;
import com.zrx.model.vo.ProblemCommentVo;

import java.io.Serializable;
import java.util.List;

/**
 * 题目评论 服务层。
 *
 * @author zhang.rx
 * @since 2024/4/18
 */
public interface ProblemCommentService extends IService<ProblemComment> {

    // 保存题目评论
    Boolean save(ProblemCommentRequest req);
    // 根据当前题目的id查询出该题目的评论
    List<ProblemCommentVo> getProblemCommentByProblemId(String problemId);
    // 根据父节点id获取题目子评论
    List<ProblemCommentVo> getListChildren(Long problemId);
    // 根据主键删除题目评论
    Boolean deleteByProblemId(String id);
}
