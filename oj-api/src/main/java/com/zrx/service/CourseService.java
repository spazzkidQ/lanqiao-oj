package com.zrx.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.zrx.model.dto.course.CourseQuery;
import com.zrx.model.entity.Course;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author 土豆儿
 * @description 针对表【course】的数据库操作Service
 * @createDate 2025-06-18 14:20:21
 */
@Service
public interface CourseService extends IService<Course> {

    /**
     * 新增课程
     * @param course 课程实体
     * @return 是否成功
     */
    Course addCourse(Course course);

    /**
     * 编辑课程 (部分更新)
     * @param course 课程实体 (只需传入id和需要修改的字段)
     * @return 是否成功
     */
    boolean editCourse(Course course);

    /**
     * 删除课程
     * @param courseId 课程ID
     * @return 是否成功
     */
    boolean deleteCourse(String courseId);

    /**
     * 分页查询课程
     * @param query 查询条件
     * @return 分页结果
     */
    Page<Course> getPaginatedCourses(CourseQuery query);

    /**
     * 上传课程教师头像
     * @param courseId 课程ID
     * @param file 文件
     * @return 头像URL
     */
    String uploadCourseAvatar(String courseId, MultipartFile file);

}