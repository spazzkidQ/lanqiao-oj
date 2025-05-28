package com.zrx.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.zrx.model.common.Paging;
import com.zrx.model.common.SaveGroup;
import com.zrx.model.common.UpdateGroup;
import com.zrx.model.dto.problem.OjProblemAddRequest;
import com.zrx.model.dto.problem.OjProblemQueryRequest;
import com.zrx.model.dto.problem.OjProblemUpdateRequest;
import com.zrx.model.entity.NoticeTable;
import com.zrx.model.entity.OjProblem;
import com.zrx.model.vo.OjProblemPageVo;
import com.zrx.model.vo.OjProblemVo;
import com.zrx.model.vo.UserRankingVO;
import com.zrx.reuslt.Result;
import com.zrx.reuslt.ResultCode;
import com.zrx.security.satoken.AuthConst;
import com.zrx.service.OjProblemService;
import com.zrx.mapstruct.OjProblemConverter;
import com.zrx.utils.CommonResult;
import com.zrx.utils.ExcelUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zrx.service.getUserTopService;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import com.zrx.service.findQuestionElementService;
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

	@Resource
	private OjProblemConverter ojProblemConverter;

	@Resource
	private findQuestionElementService findQuestionElementService;
	@Resource
	private getUserTopService getUserTopService;
	/**
	 * 添加题目。
	 *
	 * @param req 题目
	 * @return {@code true} 添加成功，{@code false} 添加失败
	 */
	@PostMapping("/save")
	@Operation(summary = "保存题目")
	@SaCheckRole(AuthConst.SUPER_ADMIN)
	public Result<Boolean> save(@Validated({SaveGroup.class}) @RequestBody @Parameter OjProblemAddRequest req) {
		boolean success = ojProblemService.saveProblem(req);
		return success ? Result.success(true) : Result.success(false);
	}

	/**
	 * 根据主键删除题目。
	 *
	 * @param id 主键
	 * @return {@code true} 删除成功，{@code false} 删除失败
	 */
	@DeleteMapping("/remove/{id}")
	@Operation(summary = "根据主键删除题目")
	@SaCheckRole(AuthConst.SUPER_ADMIN)
	public Result<String> remove(@PathVariable @Parameter(description = "题目主键") String id) {
		if (id == null) {
			return Result.fail(ResultCode.PARAM_ERROR);
		}

		try {
			Long problemId = Long.parseLong(id);
			OjProblem ojProblem = new OjProblem();
			ojProblem.setId(problemId);
			ojProblem.setDelFlag(1);

			boolean success = ojProblemService.updateById(ojProblem);
			return success ? Result.success("删除成功") : Result.fail(ResultCode.FAIL);
		} catch (NumberFormatException e) {
			return Result.fail(ResultCode.PARAM_ERROR);
		}
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
	 *
	 * @param id 题目主键
	 * @return 题目详情
	 */
	@GetMapping("/getInfo/{id}")
	@Operation(summary = "根据主键获取题目")
	public Result<OjProblemVo> getInfo(@PathVariable @Parameter(description = "题目主键") Serializable id) {
		OjProblem ojProblem = ojProblemService.getById(id);
		if (ojProblem == null) {
			return Result.fail(ResultCode.NOT_FOUND);
		}
		OjProblemVo ojProblemVo = OjProblemConverter.entity2Vo(ojProblem);
		return Result.success(ojProblemVo);
	}

	/**
	 * 分页查询题目。
	 *
	 * @param paging 分页参数
	 * @param req    查询条件
	 * @return 分页结果
	 */
	@GetMapping("/page")
	@Operation(summary = "分页查询题目")
	public Result<Page<OjProblemPageVo>> page(@Parameter(description = "分页信息") Paging paging,
											  @Parameter(description = "查询条件") OjProblemQueryRequest req) {
		log.info("Received pagination request: pageNumber = {}, pageSize = {}, title = {}, difficulty = {}, tags = {}",
				paging.getPageNum(), paging.getPageSize(), req.getTitle(), req.getDifficulty(), req.getTags());

		// 将 Paging 转换为 Page<OjProblem>
		Page<OjProblem> page = new Page<>(paging.getPageNum(), paging.getPageSize());

		// 执行分页查询
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
	@GetMapping("/getNotice")
	public CommonResult<List<NoticeTable>> getNotice() {
		List<NoticeTable> list = findQuestionElementService.getNotice();
		return CommonResult.success(list);
	}
	@GetMapping("/getPermission")
	public CommonResult<Integer> getPermission(@RequestParam Long id) {
		int permission =  findQuestionElementService.getPermission(id);
		return CommonResult.success(permission);
	}

	@PostMapping("/insertNotice")
	public CommonResult<Integer> insertNotice(@RequestBody NoticeTable noticeTable) {
		int result = findQuestionElementService.insertNotice(
				noticeTable.getType(),
				noticeTable.getContent()
		);
		return CommonResult.success(result);
	}
	@GetMapping("/getNoticeById")//根据id获取通知
	public CommonResult<NoticeTable> getNoticeById(@RequestParam Integer id) {
		NoticeTable noticeTable = findQuestionElementService.getNoticeById(id);
		System.out.println(noticeTable.getDatetime());
		System.out.println(noticeTable.getContent());
		return CommonResult.success(noticeTable);
	}
	@GetMapping("/getHotUser")
	public CommonResult<List<UserRankingVO>> getHotUser() {
		List<UserRankingVO> list = getUserTopService.getUserTop();
		System.out.println(list);
		return CommonResult.success(list);
	}
	@GetMapping("/getCurrentUserRankingOther")
	public CommonResult<UserRankingVO> getCurrentUserRankOther(@RequestParam Long id) {
		UserRankingVO userRankVO = getUserTopService.getCurrentUserRankOther(id);
		System.out.println("userRankingVO???????"+userRankVO);
		return CommonResult.success(userRankVO);
	}
	@GetMapping("/getCurrentUserRanking")
	public CommonResult<Integer> getCurrentUserRank(@RequestParam Long id) {
		Integer userRank = getUserTopService.getCurrentUserRank(id);
		System.out.println("userRank???????"+userRank);
		return CommonResult.success(userRank);
	}
}
