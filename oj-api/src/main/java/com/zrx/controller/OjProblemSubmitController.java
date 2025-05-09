package com.zrx.controller;

import com.mybatisflex.core.paginate.Page;
import com.zrx.model.common.Paging;
import com.zrx.model.dto.problemSubmit.OjProblemSubmitQueryRequest;
import com.zrx.model.dto.problemSubmit.OjProblemSubmitVo;
import com.zrx.model.dto.problemSubmit.ProblemSubmitAddRequest;
import com.zrx.reuslt.Result;
import com.zrx.security.utils.SecurityHelper;
import com.zrx.service.OjProblemSubmitService;
import com.zrx.sys.model.entity.SysUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "OjProblemSubmit", description = "题目提交管理")
@RequestMapping("/submit")
public class OjProblemSubmitController {


}
