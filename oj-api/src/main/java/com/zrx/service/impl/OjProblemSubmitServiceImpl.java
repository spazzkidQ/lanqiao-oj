package com.zrx.service.impl;

import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.StringUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zrx.codesandbox.model.JudgeInfo;
import com.zrx.enums.ProblemJudgeResultEnum;
import com.zrx.enums.ProblemSubmitStatusEnum;
import com.zrx.exception.BusinessException;
import com.zrx.execudeCode.JudgeService;
import com.zrx.mapper.OjPostMapper;
import com.zrx.mapper.OjProblemMapper;
import com.zrx.mapper.OjProblemSubmitMapper;
import com.zrx.mapstruct.ProblemSubmitConverter;
import com.zrx.model.common.Paging;
import com.zrx.model.dto.problemSubmit.OjProblemSubmitQueryRequest;
import com.zrx.model.dto.problemSubmit.OjProblemSubmitVo;
import com.zrx.model.dto.problemSubmit.ProblemSubmitAddRequest;
import com.zrx.model.entity.OjPost;
import com.zrx.model.entity.OjProblem;
import com.zrx.model.entity.OjProblemSubmit;
import com.zrx.model.vo.OjPostVo;
import com.zrx.model.vo.OjProblemPageVo;
import com.zrx.security.utils.SecurityHelper;
import com.zrx.service.OjProblemSubmitService;
import com.zrx.sys.model.entity.SysUser;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static cn.dev33.satoken.SaManager.log;
import static com.zrx.model.entity.table.OjProblemSubmitTableDef.OJ_PROBLEM_SUBMIT;

@Service
public class OjProblemSubmitServiceImpl extends ServiceImpl<OjProblemSubmitMapper, OjProblemSubmit>
		implements OjProblemSubmitService {
	@Resource
	private OjProblemMapper ojProblemMapper;
	@Resource
	private OjProblemSubmitMapper ojProblemSubmitMapper;
	@Resource
	private JudgeService judgeService;


	/**
	 * 	做题
	 * @param request
	 * @param user
	 * @return
	 *
	 */
	@Override
	public Long doQuestion(ProblemSubmitAddRequest request, SysUser user) {
		// 1. 校验题目是否存在
		Long questionId = request.getQuestionId();
		OjProblem ojProblem = ojProblemMapper.selectOneById(questionId);
		if (ojProblem == null) throw new BusinessException("题目不存在");

		// 2. 保存提交记录
		OjProblemSubmit submit = new OjProblemSubmit();
		submit.setLanguage(request.getLanguage());
		submit.setCode(request.getCode());
		submit.setQuestionId(questionId);
		submit.setUserId(user.getId());
		submit.setStatus(ProblemSubmitStatusEnum.Submitting.getKey());
		if (!this.save(submit)) throw new BusinessException("提交保存失败");

		// 3. 更新题目提交数
		ojProblem.setSubmitNum(ojProblem.getSubmitNum() == null ? 1 : ojProblem.getSubmitNum() + 1);
		ojProblemMapper.update(ojProblem);

		Long submitId = submit.getId();
		// 异步
		CompletableFuture.runAsync(() -> {
			try {
				judgeService.doJudge(submitId);
			} catch (Exception e) {
				log.error("判题失败，提交ID: {}", submitId, e);
				// 可能需要更新提交记录的状态为失败
				OjProblemSubmit updatedSubmit = new OjProblemSubmit();
				updatedSubmit.setId(submitId);
				updatedSubmit.setStatus(ProblemSubmitStatusEnum.Completed.getKey()); // 设置状态
				ojProblemSubmitMapper.update(updatedSubmit);
			}
		});
		return submitId;
	}

	/**
	 * 	根据主键id获取题目提交信息
	 * @param id
	 * @return
	 */
	@Override
	public OjProblemSubmitVo getInfoById(Long id) {
		// 创建 查询
		QueryWrapper queryWrapper = new QueryWrapper();
		queryWrapper.select(OJ_PROBLEM_SUBMIT.ALL_COLUMNS)
				.where(OJ_PROBLEM_SUBMIT.ID.eq(id));
		// 查询出当前用户提交当前题目的信息
		OjProblemSubmit ojProblemSubmit = ojProblemSubmitMapper.selectOneByQuery(queryWrapper);
		// 创建返回实例
		OjProblemSubmitVo ojProblemSubmitVo = new OjProblemSubmitVo();
		ojProblemSubmitVo.setLanguage(ojProblemSubmit.getLanguage()); // 设置语言
		ojProblemSubmitVo.setCode(ojProblemSubmit.getCode()); // 设置用户的代码
		ojProblemSubmitVo.setCodeStatus(String.valueOf(ojProblemSubmit.getCodeStatus())); // 设置代码执行的状态
		ojProblemSubmitVo.setOutput(ojProblemSubmit.getOutput()); // 设置输出的结果集

		JudgeInfo judgeInfo = JSONUtil.toBean(ojProblemSubmit.getJudgeInfo(), JudgeInfo.class);
		// 模拟的数据
		JudgeInfo judgeInfo1 = new JudgeInfo();
		judgeInfo1.setMessage("23");
		judgeInfo1.setMemory(40l);
		judgeInfo1.setTime(20l);

		ojProblemSubmitVo.setJudgeInfo(judgeInfo); // 提交的信息
		ojProblemSubmitVo.setStatus(String.valueOf(ojProblemSubmit.getStatus())); // 设置当前状态
		ojProblemSubmitVo.setQuestionId(String.valueOf(ojProblemSubmit.getQuestionId())); // 设置题目id
		ojProblemSubmitVo.setCreateTime(ojProblemSubmit.getCreateTime()); // 设置创建时间
		return ojProblemSubmitVo;
	}

	/**
	 * 	根据用户id分页查询题目提交信息列表
	 * @param id
	 * @param page
	 * @return
	 */
	@Override
	public Page<OjProblemSubmitVo> pageInfoByUserIdById(Long id, Paging page) {
		QueryWrapper queryWrapper = new QueryWrapper();
		queryWrapper.select(OJ_PROBLEM_SUBMIT.ALL_COLUMNS)
				.where(OJ_PROBLEM_SUBMIT.USER_ID.eq(id));
		// 查询出的所有当前用户的提交题目的信息
		List<OjProblemSubmit> ojProblemSubmits = ojProblemSubmitMapper.selectListByQuery(queryWrapper);

		// 转换为我们所需的vo
		List<OjProblemSubmitVo> collect = ojProblemSubmits.stream().map(pos -> {
			OjProblemSubmitVo vo = new OjProblemSubmitVo();
			vo.setId(vo.getId());
			vo.setLanguage(vo.getLanguage());
			vo.setCode(vo.getCode());
			vo.setCodeStatus(vo.getCodeStatus());
			vo.setOutput(vo.getOutput());
			vo.setJudgeInfo(vo.getJudgeInfo());
			vo.setStatus(vo.getStatus());
			vo.setQuestionId(vo.getQuestionId());
			vo.setCreateTime(vo.getCreateTime());
			return vo;
		}).collect(Collectors.toList());

		// 分页查询出数据
		Page<OjProblemSubmit> paginate = mapper.paginate(Page.of(page.getPageNum(), page.getPageSize()), queryWrapper);


		// 创建返回的Page数据
		Page<OjProblemSubmitVo> resultPage = new Page<>();
		resultPage.setPageNumber(paginate.getPageNumber());
		resultPage.setPageSize(paginate.getPageSize());
		resultPage.setTotalPage(paginate.getTotalPage());
		resultPage.setTotalRow(paginate.getTotalRow());
		resultPage.setRecords(collect);
		return resultPage;

	}

}
