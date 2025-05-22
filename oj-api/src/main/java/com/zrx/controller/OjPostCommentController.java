package com.zrx.controller;

import com.zrx.model.dto.postComment.PostCommentRequest;
import com.zrx.model.vo.PostCommentVo;
import com.zrx.reuslt.Result;
import com.zrx.service.OjPostCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
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
    }
