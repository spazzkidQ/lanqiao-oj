package com.zrx.controller;

import com.zrx.model.common.Paging;
import com.zrx.model.dto.course.CourseReCondition;
import com.zrx.model.dto.course.CourseReservationQuery;
import com.zrx.reuslt.Result;
import com.zrx.service.CourseReservationService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author : KaMiGui
 * @date 2025/6/18 10:02
 * Description:
 */
@RestController
@Tag(name = "OjFavourPost", description = "课程-学生接口")
@RequestMapping("/course/courseReservation")
public class CourseReservationController {
    @Autowired
    private CourseReservationService courseReservationService;

    // 查看分页数据
    @PostMapping("/getList")
    public Result getList(@RequestBody CourseReCondition condition){
        return Result.success(courseReservationService.getList(condition));
    }

    // 新增数据
    @PostMapping("/add")
    public Result add(@RequestBody CourseReservationQuery courseReservationQuery){
        System.out.println(courseReservationQuery);
        return Result.success(courseReservationService.add(courseReservationQuery));
    }

    // 删除数据
    @DeleteMapping("/deleteById")
    public Result deleteById(String id){
        return Result.success(courseReservationService.deleteById(id));
    }

    // 修改数据
    @PostMapping("/modify")
    public Result modify(@RequestBody CourseReservationQuery courseReservationQuery){
        System.out.println(courseReservationQuery);
        return Result.success( courseReservationService.modify(courseReservationQuery));
    }

    // 新增时获取课程名称列表
    @GetMapping("/courseNameList")
    public Result courseNameList(){
        return Result.success(courseReservationService.courseNameList());
    }

}
