package com.zrx.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.mybatisflex.core.paginate.Page;
import com.zrx.model.common.Paging;
import com.zrx.model.common.SaveGroup;
import com.zrx.model.common.UpdateGroup;
import com.zrx.model.dto.problem.OjProblemAddRequest;
import com.zrx.model.dto.problem.OjProblemQueryRequest;
import com.zrx.model.dto.problem.OjProblemUpdateRequest;
import com.zrx.model.vo.OjProblemPageVo;
import com.zrx.model.vo.OjProblemVo;
import com.zrx.reuslt.Result;
import com.zrx.reuslt.ResultCode;
import com.zrx.security.satoken.AuthConst;
import com.zrx.service.OjProblemService;
import com.zrx.utils.ExcelUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.Serializable;

/**
 * 题目 控制层。
 *
 * @author zhang.rx
 * @since 2024/3/20
 */
@RestController
@Tag(name = "OjProblem", description = "题目管理")
@RequestMapping("/ojProblem")
public class OjProblemController {

	@Resource
	private OjProblemService ojProblemService;

	/**
	 * 添加题目。
	 * @param req 题目
	 * @return {@code true} 添加成功，{@code false} 添加失败
	 */
	@PostMapping("/save")
	@Operation(summary = "保存题目")
	@SaCheckRole(AuthConst.SUPER_ADMIN)
	public Result<Boolean> save(@Validated({ SaveGroup.class }) @RequestBody @Parameter OjProblemAddRequest req) {
		return null;
	}

	/**
	 * 根据主键删除题目。
	 * @param id 主键
	 * @return {@code true} 删除成功，{@code false} 删除失败
	 */
	@DeleteMapping("/remove/{id}")
	@Operation(summary = "根据主键删除题目")
	@SaCheckRole(AuthConst.SUPER_ADMIN)
	public Result<String> remove(@PathVariable @Parameter(description = "题目主键") Serializable id) {
		return null;
	}

	/**
	 * 根据主键更新题目。
	 * @param req 题目
	 * @return {@code true} 更新成功，{@code false} 更新失败
	 */
	@PutMapping("/update")
	@Operation(summary = "根据主键更新题目")
	@SaCheckRole(AuthConst.SUPER_ADMIN)
	public Result<Boolean> update(@Validated({ UpdateGroup.class }) @RequestBody @Parameter(
			description = "题目主键") OjProblemUpdateRequest req) {
		return null;
	}

	/**
	 * 根据题目主键获取详细信息。
	 * @param id 题目主键
	 * @return 题目详情
	 */
	@GetMapping("/getInfo/{id}")
	@Operation(summary = "根据主键获取题目")
	public Result<OjProblemVo> getInfo(@PathVariable Serializable id) {
		return null;
	}

	/**
	 * 分页查询题目。
	 * @param page 分页对象
	 * @return 分页对象
	 */
	@GetMapping("/page")
	@Operation(summary = "分页查询题目")
	public Result<Page<OjProblemPageVo>> page(@Parameter(description = "分页信息") Paging page,
			@Parameter(description = "查询条件") OjProblemQueryRequest req) {
		return null;
	}

	/**
	 * 导出题目信息
	 * @param req 请求信息
	 * @param response 响应
	 * @throws IOException 异常
	 */
	@GetMapping("/export")
	@Operation(summary = "导出题目信息")
	public void export(@Parameter(description = "分页信息") Paging page,
			@Parameter(description = "查询条件") OjProblemQueryRequest req, HttpServletResponse response)
			throws IOException {
	}

}
