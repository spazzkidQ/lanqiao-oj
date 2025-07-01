package com.zrx.service.impl;

import cn.hutool.core.lang.UUID;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zrx.exception.BusinessException;
import com.zrx.mapper.CourseMapper;
import com.zrx.model.dto.course.CourseQuery;
import com.zrx.model.entity.Course;
import com.zrx.security.utils.SecurityHelper;
import com.zrx.service.CourseService;
import com.zrx.sys.model.entity.SysUser;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course>
        implements CourseService {

    @Resource
    private RedisTemplate<String, List<String>> redisTemplate;

    private static final Logger log = LoggerFactory.getLogger(CourseServiceImpl.class);
    String property = System.getProperty("user.dir");
    String uploadDir = property.replace("\\","/") + "/oj-sys/src/main/resources/user-avatars/";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Course addCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("课程信息不能为空");
        }
        if (StringUtils.isBlank(course.getName()) || StringUtils.isBlank(course.getTeacher())) {
            throw new IllegalArgumentException("课程名称和教师不能为空");
        }
        save(course);
        redisTemplate.delete("courseName");
        return course;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean editCourse(Course course) {
        if (course == null || StringUtils.isBlank(course.getId())) {
            throw new IllegalArgumentException("要更新的课程ID不能为空");
        }
        redisTemplate.delete("courseName");
        return updateById(course, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCourse(String courseId) {
        if (StringUtils.isBlank(courseId)) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        redisTemplate.delete("courseName");
        return removeById(courseId);
    }

    @Override
    public Page<Course> getPaginatedCourses(CourseQuery query) {
        try {
            Page<Course> page = new Page<>(query.getPage(), query.getPageSize());

            // **修正后的查询逻辑**
            QueryWrapper queryWrapper = QueryWrapper.create()
                    // 使用标准的 like 方法进行模糊查询
                    .where(Course::getName).like(query.getName(), StringUtils.isNotBlank(query.getName()))
                    .and(Course::getType).eq(query.getType(), StringUtils.isNotBlank(query.getType()))
                    .and(Course::getDifficultyLevel).eq(query.getDifficultyLevel(), StringUtils.isNotBlank(query.getDifficultyLevel()));

            log.info("Executing paginated query for courses with params: {}", query);

            Page<Course> resultPage = mapper.paginate(page, queryWrapper);

            log.info("Query successful, found {} records.", resultPage.getTotalRow());
            return resultPage;

        } catch (Exception e) {
            log.error("Error during getPaginatedCourses", e);
            throw new RuntimeException("Error during course pagination", e);
        }
    }

    @Override
    @Transactional // 开启事务，确保数据库操作的原子性
    public String uploadCourseAvatar(String courseId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 1. 根据 courseId 从数据库中查出对应的课程记录
        Course course = this.getById(courseId);
        if (course == null) {
            throw new BusinessException("操作失败，对应的课程不存在");
        }

        // 2. 定义文件保存目录，如果不存在则创建
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 3. 生成一个独一无二的文件名，防止重名覆盖 (使用 UUID)
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            throw new BusinessException("Uploaded file must have a name to prevent errors.");
        }
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        File dest = new File(dir, uniqueFileName);

        // 4. 将上传的文件保存到目标位置
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            // Log the error
            throw new BusinessException("文件保存失败，请稍后重试");
        }

        // 5. 生成需要存入数据库的相对路径 (需要后端配置静态资源映射)
        // 假设 /avatars/ 路径已映射到您的上传目录
        String avatarRelativePath = "/user-avatars/" + uniqueFileName;

        // 6. 更新课程实体中的头像字段
        course.setAvatar(avatarRelativePath); // 假设课程实体中存储头像的字段是 avatar

        // 7. 将更新后的课程实体保存回数据库
        boolean isSuccess = this.updateById(course);
        if (!isSuccess) {
            throw new BusinessException("数据库更新失败，请稍后重试");
        }
        // 8. 将新的头像相对路径返回给前端
        return avatarRelativePath;
    }

}