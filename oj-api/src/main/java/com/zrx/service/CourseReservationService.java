package com.zrx.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.zrx.model.common.Paging;
import com.zrx.model.dto.course.CourseReCondition;
import com.zrx.model.dto.course.CourseReservationQuery;
import com.zrx.model.entity.CourseReservation;
import com.zrx.model.vo.CourseReservationVo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author : KaMiGui
 * @date 2025/6/18 10:31
 * Description:
 */
@Service
public interface CourseReservationService extends IService<CourseReservation> {
    Page<CourseReservationVo> getList(CourseReCondition condition);

    Boolean add(CourseReservationQuery courseReservationQuery);

    Boolean deleteById(String id);

    Boolean modify(CourseReservationQuery courseReservationQuery);

    List<String> courseNameList();
}
