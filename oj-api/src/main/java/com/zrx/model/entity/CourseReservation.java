package com.zrx.model.entity;


import java.io.Serializable;
import java.time.LocalDateTime;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Table(value = "course_reservation")
public class CourseReservation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @Id(keyType = KeyType.Auto)
    private String id;
    /*
    * Course的Id;
    * */
    private String CourseId;
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
     * 备注
     */
    private String remark;
    /**
     * 校区所在城市
     */
    private String city;

    /**
     *  预约时间
     */
    @Column(onInsertValue = "now()")
    @Schema(description = "预约时间")
    private LocalDateTime createTime;
}
