package com.zrx.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import com.zrx.exception.BusinessException;
import com.zrx.mapper.CourseMapper;
import com.zrx.mapper.CourseReservationMapper;
import com.zrx.model.common.Paging;
import com.zrx.model.dto.course.CourseReCondition;
import com.zrx.model.dto.course.CourseReservationQuery;
import com.zrx.model.entity.Course;
import com.zrx.model.entity.CourseReservation;
import com.zrx.model.vo.CourseReservationVo;
import com.zrx.model.vo.OjPostVo;
import com.zrx.service.CourseReservationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author : KaMiGui
 * @date 2025/6/18 10:33
 * Description:
 */
@Service
@Slf4j
public class CourseReservationServiceImpl extends ServiceImpl<CourseReservationMapper, CourseReservation>
        implements CourseReservationService {

    @Autowired
    private CourseReservationMapper courseReservationMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Resource
    private RedisTemplate<String, List<String>> redisTemplate;

    /**
     *  查看分页集合
      * @param condition
     * @return
     */
    @Override
    public Page<CourseReservationVo> getList(CourseReCondition condition) {
        QueryWrapper wrapper = new QueryWrapper();
        //获取课程id
        if (condition.getCourseName()!=null){
            List<Integer> ids = this.getCourseId(condition.getCourseName());
            if (ids.isEmpty()){
                wrapper.eq("course_id",-1);
            }else {
                wrapper.in("course_id",ids);
            }
        }
        // 2. 学生姓名模糊查询
        if (StringUtils.hasText(condition.getStudentName())) {
            wrapper.like("student_name", condition.getStudentName());
        }
        // 3. 校区模糊查询
        if (StringUtils.hasText(condition.getSchool())) {
            wrapper.like("school", condition.getSchool());
        }
        // 获取所有符合条件的数据
        long totalNum = courseReservationMapper.selectCountByQuery(wrapper);
        //添加分页限制
        wrapper.limit((condition.getPageNum()-1)*condition.getPageSize(),condition.getPageSize());
        List<CourseReservation> courseReservations = courseReservationMapper.selectListByQuery(wrapper);
        if (courseReservations==null||courseReservations.isEmpty()){
            return null;
        }
        List<CourseReservationVo> courseReVo = BeanUtil.copyToList(courseReservations, CourseReservationVo.class);
        Map<Integer, Course> courseName = this.getCourseName(courseReservations);   //查询课程名称
        for (CourseReservationVo co : courseReVo) {
            co.setCourse(courseName.get(Integer.valueOf(co.getCourseId())).getName()); //补充课程名称
        }
        //获取总页数
        long totalPage =  (long)Math.ceil((double)totalNum / condition.getPageSize());
        Page<CourseReservationVo> pageVo = new Page<>();
        pageVo.setRecords(courseReVo);
        pageVo.setTotalRow(totalNum);
        pageVo.setPageSize(condition.getPageSize());
        pageVo.setPageNumber(condition.getPageNum());
        pageVo.setTotalPage(totalPage);
        return pageVo;
    }

    /**
     *  根据课程id查询课程名称
     */
    private Map<Integer, Course> getCourseName(List<CourseReservation> courseReservations){
        ArrayList<Integer> ids = new ArrayList<>();
        for (CourseReservation co : courseReservations) {
            ids.add(Integer.valueOf(co.getCourseId()));
        }
        List<Course> courses = courseMapper.selectListByIds(ids);
        Map<Integer,Course> course = courses.stream()
                .collect(Collectors.toMap(
                        c -> Integer.parseInt(c.getId()),
                        Function.identity()
                ));
        return course;
    }

    /**
     * 模糊查询获取课程id
     */
    private List<Integer> getCourseId(String courseName){
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.like("name",courseName);
        List<Course> courses = courseMapper.selectListByQuery(wrapper);
        ArrayList<Integer> ids = new ArrayList<>();
        for (Course cours : courses) {
           if (!ids.contains(Integer.valueOf(cours.getId()))){
               ids.add(Integer.valueOf(cours.getId()));
           }
        }
        return ids;
    }

    /**
     *  新增请求
     *  1.检查是否存在（课程）
     *  2.查重（一个学生只能预约一个课程
     */
    @Override
    public Boolean add(CourseReservationQuery courseReservationQuery) {
        QueryWrapper wrapper = new QueryWrapper()
                .eq("name",courseReservationQuery.getCourseName());
        Course course = courseMapper.selectOneByQuery(wrapper);
        if (course==null){
            throw new BusinessException("不存在添加的课程");
        }
        QueryWrapper wrapper1 = new QueryWrapper()
                .eq("student_name",courseReservationQuery.getStudentName())
                .eq("Course_id",course.getId()); //课程id
        CourseReservation courseReservation = courseReservationMapper.selectOneByQuery(wrapper1);
        if (courseReservation!=null){
            throw new BusinessException("同一学生只能预约一个课程");
        }
        //课程存在且学生没有预约过可以添加记录
        CourseReservation insertEntity = BeanUtil.copyProperties(courseReservationQuery, CourseReservation.class);
        insertEntity.setCourseId(course.getId()); //补充课程id
        return courseReservationMapper.insert(insertEntity) > 0;
    }

    /**
     * 根据id删除
     * @param id
     * @return
     */
    @Override
    public Boolean deleteById(String id) {
        CourseReservation courseReservation = courseReservationMapper.selectOneById(id);
        if (courseReservation==null){
            throw new BusinessException("暂无当前选中记录");
        }
        return courseReservationMapper.deleteById(id) > 0;
    }

    /**
     * 修改数据
     * 1.检查是否存在
     * 2.id，学生名字和课程不能修改
     *
     */
    @Override
    public Boolean modify(CourseReservationQuery courseReservationQuery) {
        CourseReservation coId = courseReservationMapper.selectOneById(courseReservationQuery.getId());
        if (coId==null){
            throw new BusinessException("选中要修改的记录不存在");
        }
        //补充课程id
        QueryWrapper wrapper = new QueryWrapper()
                .eq("name",courseReservationQuery.getCourseName());
        Course course = courseMapper.selectOneByQuery(wrapper);
        //修改部分合理
        CourseReservation updateEntity = BeanUtil.copyProperties(courseReservationQuery, CourseReservation.class);
        updateEntity.setCourseId(course.getId());
        return courseReservationMapper.update(updateEntity) > 0;
    }

    /**
     * 获取课程名称列表
     * @return
     */
    @Override
    public List<String> courseNameList() {
        List<String> courseName = redisTemplate.opsForValue().get("courseName");
        if (courseName!=null){
            return courseName;  //如果redis有缓存就返回给前端
        }
        List<String> names = new ArrayList<>();
        List<Course> courses = courseMapper.selectAll();
        for (Course cours : courses) {
            names.add(cours.getName());
        }
        redisTemplate.opsForValue().set("courseName",names,1, TimeUnit.DAYS);  //设置缓存时间为 1 天
        return names;
    }


}
