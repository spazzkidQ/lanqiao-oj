package com.zrx.codesandbox.model;

import lombok.Data;

/**
 * 进程执行信息
 */

@Data
public class ExecuteMessage {

    private Integer exitValue;   //执行的值;
    private String message;   // 执行的消息;
    private String errorMessage; // 执行错误的信息;
    private Long time;   // 时间;
    private Long memory;  // 内存;

}
