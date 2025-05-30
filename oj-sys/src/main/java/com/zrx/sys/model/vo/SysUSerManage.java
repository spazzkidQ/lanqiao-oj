package com.zrx.sys.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "设置密保")
public class SysUSerManage {
    private String userId;
    private String question;
    private String Answer;
}
