package com.zrx.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.alibaba.excel.EasyExcel;
import com.mybatisflex.core.paginate.Page;
import com.zrx.mapstruct.OjProblemConverter;
import com.zrx.model.common.Paging;
import com.zrx.model.common.SaveGroup;
import com.zrx.model.common.UpdateGroup;
import com.zrx.model.dto.problem.OjProblemAddRequest;
import com.zrx.model.dto.problem.OjProblemQueryRequest;
import com.zrx.model.dto.problem.OjProblemUpdateRequest;
import com.zrx.model.entity.OjProblem;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger log = LoggerFactory.getLogger(OjProblemController.class);

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
		// 调用服务层保存题目
		boolean success = ojProblemService.saveProblem(req);
		// 返回结果
		return success ? Result.success(true) : Result.success(false);
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
	 *
	 * @param req 题目
	 * @return {@code true} 更新成功，{@code false} 更新失败
	 */
	@PutMapping("/update")
	@Operation(summary = "根据主键更新题目")
	@SaCheckRole(AuthConst.SUPER_ADMIN)
	public Result<Boolean> update(@Validated({UpdateGroup.class}) @RequestBody @Parameter(
			description = "题目主键") OjProblemUpdateRequest req) {
		if (req == null || req.getId() == null) {
			return Result.fail(ResultCode.PARAM_ERROR);
		}

		OjProblem ojProblem = OjProblemConverter.updateDto2Entity(req);
		boolean success = ojProblemService.updateById(ojProblem);
		return success ? Result.success(true) : Result.success(false);
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
	 * @param paging 分页对象
	 * @param req 查询条件
	 * @return 分页对象
	 */
	@GetMapping("/page")
	@Operation(summary = "分页查询题目")
	public Result<Page<OjProblemPageVo>> page(@Parameter(description = "分页信息") Paging paging,
											  @Parameter(description = "查询条件") OjProblemQueryRequest req) {
		// 将 Paging 转换为 Page<OjProblem>
		Page<OjProblem> page = new Page<>(paging.getPageNum(), paging.getPageSize());
		Page<OjProblemPageVo> result = ojProblemService.page(page, req);
		return Result.success(result);
	}

	/**
	 * 导出题目信息
	 *
	 * @param page     分页信息
	 * @param req      请求信息
	 * @param response 响应
	 * @throws IOException 异常
	 */
	@GetMapping("/export")
	@Operation(summary = "导出题目信息")
	public void export(@Parameter(description = "分页信息") Paging page,
					   @Parameter(description = "查询条件") OjProblemQueryRequest req, HttpServletResponse response)
			throws IOException {
		// 设置响应头
		ExcelUtil.setExcelResponseProp(response, "题目列表");

		// 查询所有数据（不分页）
		Page<OjProblem> problemPage = new Page<>(1, Integer.MAX_VALUE);
		Page<OjProblemPageVo> result = ojProblemService.page(problemPage, req);

		// 导出数据
		EasyExcel.write(response.getOutputStream(), OjProblemPageVo.class)
				.sheet("题目列表")
				.doWrite(result.getRecords());
	}
}
