package com.zrx.utils;/*
@author 比巴卜
@date  2025/5/15 上午11:52
@Description 返回结果类
*/

import lombok.Data;

@Data
public class CommonResult<T> {
    private int code;
    private String message;
    private T data;

    public CommonResult() {
    }

    public CommonResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 快捷静态方法
    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<>(200, "ok", data);
    }

    public static <T> CommonResult<T> fail(String message) {
        return new CommonResult<>(500, message, null);
    }
}

