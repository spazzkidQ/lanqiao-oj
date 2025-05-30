package com.zrx.controller;

import com.zrx.model.dto.problemComment.ProblemCommentRequest;
import com.zrx.model.vo.PostCommentVo;
import com.zrx.model.vo.ProblemCommentVo;
import com.zrx.reuslt.Result;
import com.zrx.service.ProblemCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
 * 题目评论 控制层。
 *
 * @author zhang.rx
 * @since 2024/4/18
 */
@RestController
@Tag(name = "OjProblemComment", description = "题目评论接口")
@RequestMapping("/problem/comment")
public class OjProblemCommentController {
    @Resource
    private ProblemCommentService problemCommentService;

    /**
     * 保存题目评论
     * @param req
     * @return
     */
    @PostMapping("/save")
    public Result<Boolean> saveComment(@RequestBody ProblemCommentRequest req){
        return Result.success(problemCommentService.save(req));
    }

    /**
     *  获取根节点评论(当前题目的id)
     * @param problemId
     * @return
     */
    @GetMapping("/list")
    public Result<List<ProblemCommentVo>> getProblemCommentByProblemId(String problemId){
        return Result.success(problemCommentService.getProblemCommentByProblemId(problemId));
    }

    /**
     *  根据父节点id获取题目子评论
     * @param problemId
     * @return
     */
    @GetMapping("/listChildren")
    public Result<List<ProblemCommentVo>> getListChildren(Long problemId) {
        return Result.success(problemCommentService.getListChildren(problemId));
    }


    /**
     * 根据主键删除题目评论
     * @return
     */
    @DeleteMapping("/remove/{id}")
    public Result<Boolean> deleteByProblemId(@PathVariable String id){
        return Result.success(problemCommentService.deleteByProblemId(id));
    }

}
