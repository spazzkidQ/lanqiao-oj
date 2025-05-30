package com.zrx.controller;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.zrx.model.common.Paging;
import com.zrx.model.dto.problemSubmit.OjProblemSubmitQueryRequest;
import com.zrx.model.dto.problemSubmit.OjProblemSubmitVo;
import com.zrx.model.dto.problemSubmit.ProblemSubmitAddRequest;
import com.zrx.model.vo.OjProblemPageVo;
import com.zrx.reuslt.Result;
import com.zrx.security.utils.SecurityHelper;
import com.zrx.service.OjProblemService;
import com.zrx.service.OjProblemSubmitService;
import com.zrx.sys.model.entity.SysUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "OjProblemSubmit", description = "题目提交管理")
@RequestMapping("/submit")
public class OjProblemSubmitController {
    @Resource
    private OjProblemSubmitService ojProblemSubmitService;
    @PostMapping("/")
    @Operation(summary = "提交题目")
    public Result<Long> submitQuestion(@RequestBody ProblemSubmitAddRequest problemSubmitAddRequest){
        /**
         *  提交题目：
         *      1.需要提交用户的信息
         *      2.用户提交的代码信息（语言，代码，题目id)
         *      3.
         *
         */
        // 输出用户输入的用例
        // 利用工具类来获取当前登录用户的信息
        SysUser user = SecurityHelper.getUser();
        return Result.success(ojProblemSubmitService.doQuestion(problemSubmitAddRequest,user));
    }

    /**
     * 根据主键获取题目提交信息
     * @return
     */
    @GetMapping("/getInfo/{id}")
    @Operation(summary = "根据主键获取题目提交信息")
    public Result<OjProblemSubmitVo> getInfoById(@PathVariable Long id){
        return Result.success(ojProblemSubmitService.getInfoById(id));
    }

    // 根据用户id分页查询题目提交信息列表
    @PostMapping("/pageInfoByUserId/{id}")
    @Operation(summary = "根据用户id分页查询题目提交信息列表")
    public Result<Page<OjProblemSubmitVo>> pageInfoByUserIdById(@PathVariable Long id,@RequestBody Paging page){
        return Result.success(ojProblemSubmitService.pageInfoByUserIdById(id,page));
    }


}
