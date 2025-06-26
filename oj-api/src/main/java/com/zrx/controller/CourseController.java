package com.zrx.controller;

import com.mybatisflex.core.paginate.Page;
import com.zrx.model.dto.course.CourseQuery;
import com.zrx.model.entity.Course;
import com.zrx.reuslt.Result;
import com.zrx.service.CourseService;
import com.zrx.sys.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Tag(name = "course", description = "课程相关接口")
@RequestMapping("/course") // 修正根路径以匹配前端
public class CourseController {
    @Resource
    private CourseService courseService;

    /**
     * 分页和条件查询所有课程
     *
     * @param query 查询参数 (Spring Boot 会自动从 URL Query String 中匹配字段)
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<Page<Course>> getPaginatedList(CourseQuery query) {
        return Result.success(courseService.getPaginatedCourses(query));
    }

    /**
     * 新增课程
     *
     * @param course 课程实体
     * @return 是否成功
     */
    @PostMapping("/add")
    public Result<Course> addCourse(@RequestBody Course course) {
        return Result.success(courseService.addCourse(course));
    }

    /**
     * 编辑课程 (部分更新)
     *
     * @param course 课程实体 (只需传入id和需要修改的字段)
     * @return 是否成功
     */
    @PutMapping("/edit")
    public Result<Boolean> updateCourse(@RequestBody Course course) {
        return Result.success(courseService.editCourse(course));
    }

    /**
     * 根据ID获取课程详情
     *
     * @param id 课程ID
     * @return 课程详情
     */
    @GetMapping("/{id}")
    public Result<Course> getCourseById(@PathVariable String id) {
        Course course = courseService.getById(id);
        if (course == null) {
            // 您可以根据需要自定义返回的错误码和信息
            return Result.fail(404, "课程不存在");
        }
        return Result.success(course);
    }

    /**
     * 删除课程
     *
     * @param id 课程ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}") // 修正路径以匹配前端和RESTful风格
    public Result<Boolean> deleteCourse(@PathVariable String id) {
        return Result.success(courseService.deleteCourse(id));
    }

    /**
     * 上传指定课程的教师头像
     *
     * @param courseId 课程ID
     * @param file     头像文件
     * @return 新头像的URL
     */
    @PostMapping("/{id}/upload-avatar")
    public Result<String> uploadCourseAvatar(@PathVariable("id") String courseId, @RequestParam("file") MultipartFile file) {
        // 调用Service层的方法来处理业务逻辑
        String avatarUrl = courseService.uploadCourseAvatar(courseId, file);
        return Result.success(avatarUrl);
    }
}
