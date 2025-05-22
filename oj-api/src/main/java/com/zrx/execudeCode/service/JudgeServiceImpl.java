package com.zrx.execudeCode.service;

import cn.hutool.json.JSONUtil;
import com.zrx.codesandbox.CodeSandBox;
import com.zrx.codesandbox.model.ExecuteCodeRequest;
import com.zrx.codesandbox.model.ExecuteCodeResponse;
import com.zrx.enums.ProblemJudgeResultEnum;
import com.zrx.enums.ProblemSubmitStatusEnum;
import com.zrx.execudeCode.CodeSandboxManager;
import com.zrx.execudeCode.JudgeService;
import com.zrx.judge.HandleJudgeTask;
import com.zrx.mapper.OjProblemSubmitMapper;
import com.zrx.model.dto.problem.JudgeCase;
import com.zrx.model.entity.OjProblem;
import com.zrx.model.entity.OjProblemSubmit;
import com.zrx.model.judge.JudgeTask;
import com.zrx.service.OjProblemService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {

	@Resource
	private OjProblemService ojProblemService;

	@Resource
	private OjProblemSubmitMapper submitMapper;

	@Value("${codesandbox.type:remote}")
	private String type;

	@Override
	public void doJudge(Long ojProblemSubmitId) throws Exception {

		OjProblemSubmit ojProblemSubmit = submitMapper.selectOneById(ojProblemSubmitId);  // 根据提交题目的id进行查询我们的提交信息;
		OjProblem ojProblem = ojProblemService.getById(ojProblemSubmit.getQuestionId());  // 通过题目Id进行查询到我们的题目详情信息;
		System.err.println(ojProblem.getJudgeCase());
		ExecuteCodeRequest executeCodeRequest = getExecuteCodeRequest(ojProblemSubmit, ojProblem); // 输入 + 用户代码 + 语言;

		CodeSandBox codeSandbox = CodeSandboxManager.instance("local");   // 获取我们判题的对象;决定判断的是本地的判题的机制还是远程的判题机制;
		ojProblemSubmit.setStatus(ProblemSubmitStatusEnum.Waiting.getKey());  // 设置我们的状态为判题中;
		submitMapper.update(ojProblemSubmit);  // 修改我们的数据库;


		ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);   // 进行判题获取到我们的输出用例'

		JudgeTask judgeTask = setJudgeTask(executeCodeResponse, ojProblem, ojProblemSubmit); // 提交的数据 + 题目信息 + 执行后信息;
		ojProblemSubmit.setStatus(ProblemSubmitStatusEnum.Judging.getKey());   // 到这里就是执行成功的数据;
		submitMapper.update(ojProblemSubmit);
		// JudgeTask 包含提交的数据 + 题目信息 + 执行后信息;
		HandleJudgeTask handleJudgeTask = new HandleJudgeTask();
		ojProblemSubmit.setOutput(JSONUtil.toJsonStr(executeCodeResponse.getOuputList()));  // 设置我们执行出来的输出用例;
		ojProblemSubmit.setJudgeInfo(JSONUtil.toJsonStr(executeCodeResponse.getJudgeInfo()));  // 设置我们执行出来的信息大小 + 时间 + 内存;
		ojProblemSubmit = handleJudgeTask.doHandle(judgeTask);
		boolean saveFlag = submitMapper.update(ojProblemSubmit) == 1;  // 继续修改我们的OjProblemSubmit的数据;
		Integer codeStatus = ojProblemSubmit.getCodeStatus();
		if (codeStatus.equals(ProblemJudgeResultEnum.ACCEPTED.getKey())) {
			ojProblem.setAcceptedNum(ojProblem.getAcceptedNum() == null ? 1 : ojProblem.getAcceptedNum() + 1);
			ojProblemService.updateById(ojProblem);   // 成功数进行对应的 + 1的操作;
		}
		if (!saveFlag) {
			throw new Exception("代码提交保存失败");
		}
	}

	private ExecuteCodeRequest getExecuteCodeRequest(OjProblemSubmit ojProblemSubmit, OjProblem ojProblem) {
		// 传入我们的提交的信息和题目的信息;
		ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
		executeCodeRequest.setCode(ojProblemSubmit.getCode());  // 设置我们需要的用户提交的代码;
		executeCodeRequest.setLanguage(ojProblemSubmit.getLanguage());  // 用户提交的语言;
		List<JudgeCase> judgeCaseList = JSONUtil.toList(ojProblem.getJudgeCase(), JudgeCase.class);  // 输入 + 输出用列的json转换为List;
		List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());  // 输入用列;
		executeCodeRequest.setInputList(inputList);  // 设置我们的Code + Language + 输入用列;
		return executeCodeRequest;
	}

	private JudgeTask setJudgeTask(ExecuteCodeResponse executeCodeResponse, OjProblem ojProblem,
			OjProblemSubmit ojProblemSubmit) {
		JudgeTask judgeTask = new JudgeTask();
		judgeTask.setOjProblem(ojProblem);
		judgeTask.setOjProblemSubmit(ojProblemSubmit);
		judgeTask.setExecuteCodeResponse(executeCodeResponse);
		return judgeTask;
	}

}
