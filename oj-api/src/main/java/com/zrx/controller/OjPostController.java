package com.zrx.controller;
import com.zrx.model.dto.post.*;
import com.zrx.utils.PageHelperUtil;
import com.zrx.utils.ResponseUtil;
import com.mybatisflex.core.paginate.Page;
import com.zrx.model.common.Paging;
import com.zrx.model.entity.OjPost;
import com.zrx.model.vo.OjPostSimpleVo;
import com.zrx.model.vo.OjPostVo;
import com.zrx.reuslt.Result;
import com.zrx.service.OjPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
//import org.apache.tomcat.util.http.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpRequest;
import java.util.List;

/**
 * 帖子 控制层。
 *
 *
 *
 */
@RestController
@Tag(name = "OjPost", description = "帖子接口")
@RequestMapping("/oj/post")
public class OjPostController {
    @Resource
    private  OjPostService ojPostService;

    //分页查询功能
        @PostMapping("/pageSelf")
    public Result<Page<OjPostVo>> pageSelf(Paging page,@RequestBody OjPostQueryRequest req){
        return Result.success(ojPostService.pageSelfOrPage(page,req));
    }

    //编辑功能,根据id进行查询修改
    @GetMapping("/getInfo/{id}")
    public Result<OjPostVo> getInfo(@PathVariable("id") String id) {
//        System.out.println(id  + "---------");
        return Result.success(ojPostService.getInfoById(id));
    }

    //编辑
    @PutMapping("/update")
    public Result<Boolean> update(@Valid @RequestBody OjPostUpdateRequest req){
            return Result.success(ojPostService.updateById(req));
    }

    //删除
    @DeleteMapping("/remove/{id}")
    public Result<Boolean> remove(@PathVariable Long id){
            return Result.success(ojPostService.removeById(id));
    }
    @PostMapping("/page")
    public Result getPage(){
        return null;
    }



    @Resource
    private OjPostService ojPostService;

    //帖子首页 屈 分页条件查询
    @PostMapping("/page")
    @Operation(summary = "分页查询帖子")
    public Result<Page<OjPostVo>> page(@Parameter(description = "分页信息") Paging page,
                                       @RequestBody OjPostQueryRequest req) {
        return Result.success(ojPostService.getList(page, req, false));
    }

    /**
     * 帖子首页 屈  获取五个热门帖子
     * @return 帖子简单信息Vo
     */
    @GetMapping("/get/fiveHotPost")
    @Operation(summary = "获取五个热门帖子")
    public Result<List<OjPostSimpleVo>> getFiveHotPost() {
        return Result.success(ojPostService.getFiveHotPost());
    }
}
