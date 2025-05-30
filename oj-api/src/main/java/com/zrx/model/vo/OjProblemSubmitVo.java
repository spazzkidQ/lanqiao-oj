package com.zrx.model.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.core.keygen.KeyGenerators;
import com.zrx.codesandbox.model.JudgeInfo;
import com.zrx.config.excel.StringListConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "题目响应体")
@ExcelIgnoreUnannotated
public class OjProblemSubmitVo {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    @Schema(description = "id")
    private Long id;
    /**
     * 标题
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "标题", index = 0)
    @Schema(description = "标题")
    private String title;

    /**
     * 内容
     */
    @ColumnWidth(10)
    @ExcelProperty(value = "内容", index = 1)
    @Schema(description = "内容")
    private String content;

    /**
     * 标签列表（json 数组）
     */
    @ExcelProperty(value = "标签", index = 2, converter = StringListConverter.class)
    @Schema(description = "标签列表（json 数组）")
    private List<String> tags;

    /**
     * 题目难度，简单，中等，困难
     */
    @ColumnWidth(15)
    @ExcelProperty(value = "题目难度", index = 3)
    @Schema(description = "题目难度，简单，中等，困难")
    private String difficulty;


    @Schema(description = "编程语言")
    private String language;

    @Schema(description = "用户代码")
    private String code;

    @Schema(description = "代码执行状态（详见ProblemJudgeResultEnum）")
    private String codeStatus;

    @Schema(description = "题目实际输出（json）")
    private List<String> outputList;

    @Schema(description = "判题信息（json 对象）")
    private JudgeInfo judgeInfo;

    @Schema(description = "判题状态（0 - 待判题、1 - 判题中、2 - 成功、3 - 失败）")
    private String status;

    @Schema(description = "题目 id")
    private Long questionId;

    @Schema(description = "创建用户id")
    private Long userId;

}
