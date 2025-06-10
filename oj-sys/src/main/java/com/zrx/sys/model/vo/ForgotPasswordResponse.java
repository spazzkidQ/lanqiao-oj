package com.zrx.sys.model.vo;

import lombok.Data;

@Data
public class ForgotPasswordResponse {
    private Long id;
    private String mobile;
    private String email;
    private String question;
} 