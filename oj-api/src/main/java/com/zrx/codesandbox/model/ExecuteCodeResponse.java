package com.zrx.codesandbox.model;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ExecuteCodeResponse {

	private List<String> ouputList;   // 输出的答案;

	private String message;   // 消息;

	private Integer status;   // 判题的状态；

	private JudgeInfo judgeInfo;   // 执行信息的大小;  时间 + 内存;

}
