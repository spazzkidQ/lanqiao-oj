package com.zrx.judge;

import cn.hutool.json.JSONUtil;
import com.zrx.codesandbox.enums.QuestionExecuteResultEnum;
import com.zrx.codesandbox.model.ExecuteCodeResponse;
import com.zrx.codesandbox.model.JudgeInfo;
import com.zrx.enums.ProblemJudgeResultEnum;
import com.zrx.model.dto.problem.JudgeCase;
import com.zrx.model.dto.problem.JudgeConfig;
import com.zrx.model.entity.OjProblem;
import com.zrx.model.entity.OjProblemSubmit;
import com.zrx.model.judge.JudgeTask;

import java.util.List;
import java.util.stream.Collectors;

public class HandleJudgeTask {

    public OjProblemSubmit doHandle(JudgeTask judgeTask) {
        ExecuteCodeResponse executeCodeResponse = judgeTask.getExecuteCodeResponse(); // 获取到我们代码执行的结果;
        JudgeInfo judgeInfo = executeCodeResponse.getJudgeInfo();  // 执行信息的大小 + 时间 + 内存;

        OjProblem ojProblem = judgeTask.getOjProblem();  // 获取题目的信息;

        OjProblemSubmit ojProblemSubmit = judgeTask.getOjProblemSubmit();  // 获取用户提交的信息;

        Integer status = executeCodeResponse.getStatus();  // 判题的状态;

        QuestionExecuteResultEnum enumByValue = QuestionExecuteResultEnum.getEnumByValue(status);

        if (enumByValue != QuestionExecuteResultEnum.ExecuteSuccess) {  // 当前执行结果不成功的案例;
            JudgeInfo judgeInfoRes = new JudgeInfo();
            judgeInfoRes.setMessage(executeCodeResponse.getMessage());
            ojProblemSubmit.setJudgeInfo(JSONUtil.toJsonStr(judgeInfoRes));
            if (enumByValue == QuestionExecuteResultEnum.CompileFailure) {
                ojProblemSubmit.setCodeStatus(ProblemJudgeResultEnum.COMPILE_ERROR.getKey());
                return ojProblemSubmit;
            }
            if (enumByValue == QuestionExecuteResultEnum.OutOfTimeException) {
                ojProblemSubmit.setCodeStatus(ProblemJudgeResultEnum.TIME_LIMIT_EXCEEDED.getKey());
                return ojProblemSubmit;
            }
            if (enumByValue == QuestionExecuteResultEnum.RunException) {
                ojProblemSubmit.setCodeStatus(ProblemJudgeResultEnum.RUNTIME_ERROR.getKey());
                return ojProblemSubmit;
            }
        }
        // 往后就是执行成功的案例;
        String judgeConfigStr = ojProblem.getJudgeConfig();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);  // 时间 + 内存  + 堆栈的配置信息;
        // 系统异常;
        if (judgeInfo.getTime() == null || judgeInfo.getMemory() == null) {
            ojProblemSubmit.setCodeStatus(ProblemJudgeResultEnum.SYSTEM_ERROR.getKey());
            return ojProblemSubmit;
        }
        // 时间超时异常;
        if (judgeInfo.getTime() > judgeConfig.getTimeLimit()) {
            ojProblemSubmit.setCodeStatus(ProblemJudgeResultEnum.TIME_LIMIT_EXCEEDED.getKey());
            return ojProblemSubmit;
        }
        // 堆栈异常;
        if (judgeInfo.getMemory() > judgeConfig.getMemoryLimit()) {
            ojProblemSubmit.setCodeStatus(ProblemJudgeResultEnum.MEMORY_LIMIT_EXCEEDED.getKey());
            return ojProblemSubmit;
        }
        //
        List<String> outputList = executeCodeResponse.getOuputList();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(ojProblem.getJudgeCase(), JudgeCase.class);
        List<String> expertOutputList = judgeCaseList.stream().map(JudgeCase::getOutput).collect(Collectors.toList());
        // 答案错误;
        if (outputList.size() != expertOutputList.size()) {
            ojProblemSubmit.setCodeStatus(ProblemJudgeResultEnum.WRONG_ANSWER.getKey());
            return ojProblemSubmit;
        }
        // 答案错误;
        for (int i = 0, len = outputList.size(); i < len; i++) {
            if (!outputList.get(i).equals(expertOutputList.get(i))) {
                ojProblemSubmit.setCodeStatus(ProblemJudgeResultEnum.WRONG_ANSWER.getKey());
                return ojProblemSubmit;
            }
        }
        // 答案正确;
        ojProblemSubmit.setCodeStatus(ProblemJudgeResultEnum.ACCEPTED.getKey());
        return ojProblemSubmit;
    }

}
