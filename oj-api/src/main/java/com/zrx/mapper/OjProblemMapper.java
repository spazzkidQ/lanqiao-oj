package com.zrx.mapper;

import com.mybatisflex.core.BaseMapper;
import com.zrx.model.entity.OjProblem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


/**
 * 题目 映射层。
 *
 * @author zhang.rx
 * @since 2024/3/20
 */
@Mapper
public interface OjProblemMapper extends BaseMapper<OjProblem> {
    //获取提交总数（所有未删除的提交）
    @Select("SELECT COUNT(*) FROM oj_problem_submit s " +
            "INNER JOIN oj_problem p ON s.question_id = p.id " +
            "WHERE p.id = #{problemId} AND p.del_flag = 0")
    Integer getSubmitNum(Long problemId);

    //获取通过数（只统计判题成功的提交）
    @Select("SELECT COUNT(*) FROM oj_problem_submit s " +
            "INNER JOIN oj_problem p ON s.question_id = p.id " +
            "WHERE p.id = #{problemId} AND p.del_flag = 0 AND s.status = 2")
    Integer getAcceptedNum(Long problemId);

    @Update("UPDATE oj_problem SET submit_num = #{submitNum}, accepted_num = #{acceptedNum} WHERE id = #{problemId}")
    void updateSubmitAndAcceptedNum(Long problemId, Integer submitNum, Integer acceptedNum);

}
