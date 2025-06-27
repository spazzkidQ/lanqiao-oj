package com.zrx.model.dto.course;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import lombok.Data;
import net.bytebuddy.implementation.bytecode.assign.TypeCasting;

/**
 * @author : KaMiGui
 * @date 2025/6/26 21:18
 * Description:
 */

@Data
public class CourseReservationQuery {
    /**
     * 主键id
     */
    @Id(keyType = KeyType.Auto)
    private String id;
    /**
     * 课程名称
     */
    private String courseName;
    /**
     * 学生姓名
     */
    private String studentName;

    /**
     * 联系方式
     */
    private String contactInfo;

    /**
     * 预约校区
     */
    private String school;
    /**
     * 学校所在地
     */
    private String city;

    /**
     * 备注
     */
    private String remark;
}
