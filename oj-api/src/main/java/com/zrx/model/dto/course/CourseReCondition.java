package com.zrx.model.dto.course;

import com.mybatisflex.annotation.Column;
import lombok.Data;

/**
 * @author : KaMiGui
 * @date 2025/6/26 23:53
 * Description:
 */
@Data
public class CourseReCondition {
    /**
     * 课程名称
     */
    private String courseName;
    /**
     * 学生姓名
     */
    private String studentName;
    /**
     * 预约校区
     */
    private String school;
    // 为分页参数提供默认值，防止空指针
    @Column(ignore = true)
    private Integer pageNum;
    @Column(ignore = true)
    private Integer pageSize;
}
