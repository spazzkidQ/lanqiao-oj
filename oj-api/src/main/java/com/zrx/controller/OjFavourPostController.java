package com.zrx.controller;

import com.mybatisflex.core.paginate.Page;
import com.zrx.model.common.Paging;
import com.zrx.model.vo.OjPostVo;
import com.zrx.reuslt.Result;
import com.zrx.service.OjPostFavourService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子收藏接口
 *
 * @author zhang.rx
 * @since 2024/5/13
 */
@RestController
@Tag(name = "OjFavourPost", description = "帖子收藏接口")
@RequestMapping("/oj/post/favour")
public class OjFavourPostController {


}
