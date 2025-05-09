package com.zrx.controller;

import com.mybatisflex.core.paginate.Page;
import com.zrx.model.common.Paging;
import com.zrx.model.dto.post.OjPostAddRequest;
import com.zrx.model.dto.post.OjPostQueryRequest;
import com.zrx.model.dto.post.OjPostUpdateRequest;
import com.zrx.model.vo.OjPostSimpleVo;
import com.zrx.model.vo.OjPostVo;
import com.zrx.reuslt.Result;
import com.zrx.service.OjPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 帖子 控制层。
 *
 * @author zhang.rx
 * @since 2024/5/13
 */
@RestController
@Tag(name = "OjPost", description = "帖子接口")
@RequestMapping("/oj/post")
public class OjPostController {


}
