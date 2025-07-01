package com.zrx.controller;

import com.mybatisflex.core.paginate.Page;
import com.zrx.exception.BusinessException;
import com.zrx.model.common.Paging;
import com.zrx.model.entity.OjPostThumb;
import com.zrx.model.vo.OjPostVo;
import com.zrx.reuslt.Result;
import com.zrx.security.utils.SecurityHelper;
import com.zrx.service.OjPostService;
import com.zrx.service.OjPostThumbService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private OjPostService ojPostService;

    @Autowired
    private OjPostThumbService ojPostThumbService;


    /**
     * 帖子点赞
     * 先拿到点赞id去数据库内查询是否有该文章
     * 再查询数据库内是否含有登录用户点赞该文章记录
     * 如果没有就添加上
     */
    @PostMapping("/do/{id}")
    public Result doThumb(@PathVariable Long id){
        if (!ojPostThumbService.addThumb(id)){
            //点赞失败
            return Result.fail("点赞失败");
        }
        return Result.success(true);
    }


    /**
     * 帖子取消点赞
     * 先拿到点赞id去数据库内查询是否有该文章
     * 再查询数据库内是否含有登录用户点赞该文章记录
     * 如果有就删除掉
     */
    @PostMapping("/cancel/{id}")
    public Result cancelThumb(@PathVariable Long id){
        if (!ojPostThumbService.removeThumb(id)){
            //删除失败
            return Result.fail("取消点赞失败");
        }
        return Result.success(true);
    }


    /**
     *  查看登录账号点赞的帖子
     *  根据 id 查询点赞帖子id 限制 pageSize
     *  根据 帖子id 查询文章内容
     */
    @GetMapping("/get/post/{id}")
    public Result getThumbPost(@PathVariable Long id, @Parameter Paging page){
        Page<OjPostVo> thumbList = ojPostThumbService.findThumbList(id, page);
        return Result.success(thumbList);
    }

}
