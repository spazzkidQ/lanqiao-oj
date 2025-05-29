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
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private  OjPostFavourService ojPostFavourService;


    /**
     *  帖子收藏
     *  先去查询是否存在该帖子
     *  查询是否有收藏记录
     *  没有就添加记录
     */
    @PostMapping("/do/{id}")
    public Result addFavour(@PathVariable Long id){
        // System.out.println(id);
        if (!ojPostFavourService.addFavour(id)){
            return Result.fail("帖子收藏失败");
        }
        return Result.success(true);
    }


    /**
     *  取消收藏
     *  先去查询是否存在该帖子
     *  查询是否有收藏记录
     *  存在就删除记录
     */
    @PostMapping("/cancel/{id}")
    public Result removeFavour(@PathVariable Long id){
        // System.out.println(id);
        if (!ojPostFavourService.removeFavour(id)){
            return Result.fail("取消收藏失败");
        }
        return Result.success(true);
    }


    /**
     *  查看登录账号点赞的帖子
     *  根据 id 查询点赞帖子id 限制 pageSize
     *  根据 帖子id 查询文章内容
     */
    @GetMapping("/get/post/{id}")
    public Result getFavourPost(@PathVariable Long id, @Parameter Paging page){
        Page<OjPostVo> favourList = ojPostFavourService.findFavourList(id, page);
        if (favourList==null){
            return Result.fail("当前账号无收藏帖子");
        }
        return Result.success(favourList);
    }


}
