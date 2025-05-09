package com.zrx.controller;

import com.mybatisflex.core.paginate.Page;
import com.zrx.model.common.Paging;
import com.zrx.model.vo.OjPostVo;
import com.zrx.reuslt.Result;
import com.zrx.service.OjPostThumbService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子点赞接口
 *
 * @author zhang.rx
 * @since 2024/5/13
 */
@RestController
@Tag(name = "OjThumbPost", description = "帖子点赞接口")
@RequestMapping("/oj/post/thumb")
public class OjThumbPostController {

}
