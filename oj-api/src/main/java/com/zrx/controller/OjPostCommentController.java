package com.zrx.controller;

import com.zrx.model.dto.postComment.PostCommentRequest;
import com.zrx.model.entity.OjPostComment;
import com.zrx.model.vo.PostCommentVo;
import com.zrx.reuslt.Result;
import com.zrx.service.OjPostCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
 * 帖子评论 控制层。
 *
 * @author zhang.rx
 * @since 2024/5/20
 */
@RestController
@Tag(name = "OjPostComment", description = "帖子评论接口")
@RequestMapping("/oj/post/comment")
public class OjPostCommentController {

    @Autowired
    private OjPostCommentService ojPostCommentService;

    /**
     * 根据帖子id获取评论数量
     * @param postId
     * @return
     */
    @GetMapping("/get/num")
    public Result<Long> getNum(Long postId){
        return Result.success(ojPostCommentService.getCountNum(postId));
    }


    /**
     *  1.查询文章是否存在
     *  2.如果存在父id，先验证父id是否存在
     *  3.评论不能为null
     *  4.添加评论后查询该文章的所有评论
     */
    @PostMapping("/save")
    public Result addComment(@RequestBody OjPostComment ojPostComment){
        if (!ojPostCommentService.addComment(ojPostComment)){
            return Result.fail("评论失败");
        }
        return Result.success(true);
    }

    /**
     *     查看评论请求
     *     1.校验帖子 id 是否存在
     *     2.查询帖子评论
     *     3.set children元素信息 (递归 )
     */
    @GetMapping("/list")
    public Result findList(Long postId){
        // System.out.println(postId);
        List<PostCommentVo> list = ojPostCommentService.findList(postId);
        if (list.isEmpty()){
            return Result.fail("查看评论失败");
        }
        return Result.success(list);
    }
}
