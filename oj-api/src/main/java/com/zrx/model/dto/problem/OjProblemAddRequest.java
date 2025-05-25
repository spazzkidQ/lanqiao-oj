package com.zrx.model.dto.problem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 题目添加请求体
 *
 * @author zhang.rx
 * @since 2024/3/20
 */
@Data
@Schema(description = "题目添加请求体")
public class OjProblemAddRequest {

    /**
     * 标题
     */
    @Schema(description = "标题")
    private String title;

    /**
     * 内容
     */
    @Schema(description = "内容")
    private String content;

    /**
     * 标签列表
     */
    @Schema(description = "标签列表")
    private List<String> tags;

    /**
     * 题目难度，0简单，1中等，2困难
     */
    @Schema(description = "题目难度，0简单，1中等，2困难")
    private Integer difficulty;

    /**
     * 题目答案语言
     */
    @Schema(description = "题目答案语言")
    private String ansLanguage;

    /**
     * 题目答案
     */
    @Schema(description = "题目答案")
    private String answer;

    /**
     * 判题用例
     */
    @Schema(description = "判题用例")
    private List<JudgeCase> judgeCase;

    /**
     * 判题配置
     */
    @Schema(description = "判题配置")
    private JudgeConfig judgeConfig;
}
