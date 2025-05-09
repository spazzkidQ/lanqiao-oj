package com.zrx.controller;

import com.zrx.model.dto.problemComment.ProblemCommentRequest;
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

}
