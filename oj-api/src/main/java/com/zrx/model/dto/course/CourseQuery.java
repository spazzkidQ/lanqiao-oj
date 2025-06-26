package com.zrx.model.dto.course;/*
@author 比巴卜
@date  2025/6/21 下午4:38
@Description 
*/
import com.mybatisflex.annotation.Column;
import lombok.Data;

@Data
public class CourseQuery {
    private String name;
    private String type;
    private String difficultyLevel;
    // 为分页参数提供默认值，防止空指针
    @Column(ignore = true)
    private Integer page = 1;
    @Column(ignore = true)
    private Integer pageSize = 5;
}
