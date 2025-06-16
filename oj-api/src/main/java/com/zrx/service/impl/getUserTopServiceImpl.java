package com.zrx.service.impl;/*
@author 比巴卜
@date  2025/5/21 上午9:38
@Description 获取用户热力榜
*/

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zrx.model.vo.UserRankingVO;
import com.zrx.mapper.OjProblemSubmitMapper;
import com.zrx.mapper.getUserTopMapper;
import com.zrx.service.getUserTopService;
import com.zrx.sys.mapper.SysUserMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class getUserTopServiceImpl extends ServiceImpl<com.zrx.mapper.getUserTopMapper, UserRankingVO> implements getUserTopService {
    @Resource
    private getUserTopMapper getUserTopMapper;
    @Resource
    private OjProblemSubmitMapper ojProblemSubmitMapper;
    @Resource
    private SysUserMapper sysUserMapper;

    // 获取前十名用户排行榜
    @Override
    public List<UserRankingVO> getUserTop() {
        QueryWrapper query = QueryWrapper.create()
                .select(
                        "sys_user.avatar AS avatar",
                        "sys_user.nick_name AS nickname",
                        "COUNT(oj_problem_submit.id) AS totalSubmissions",
                        "COUNT(CASE WHEN DATE(oj_problem_submit.create_time) = CURRENT_DATE THEN 1 END) AS todaySubmissions"
                )
                .from("sys_user")
                .leftJoin("oj_problem_submit").on("sys_user.id = oj_problem_submit.user_id")
                .where("sys_user.status = ?", 0)
                .groupBy("sys_user.id")
                .orderBy("totalSubmissions", false)
                .limit(10);

        return getUserTopMapper.selectListByQuery(query);
    }

    // 获取当前用户的排名的其他信息
    @Override
    public UserRankingVO getCurrentUserRankOther(Long id) {
        // 参数校验
        if (id == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        try {
            QueryWrapper query = QueryWrapper.create()
                    .select(
                            "sys_user.avatar AS avatar",
                            "sys_user.nick_name AS nickName",
                            "COUNT(oj_problem_submit.id) AS totalSubmissions",
                            "SUM(CASE WHEN oj_problem_submit.create_time >= CURRENT_DATE THEN 1 ELSE 0 END) AS todaySubmissions"
                    )
                    .from("sys_user")
                    .leftJoin("oj_problem_submit").on("sys_user.id = oj_problem_submit.user_id")
                    .where("sys_user.status = ?", 0)
                    .and("sys_user.id = ?", id)
                    .groupBy("sys_user.id", "sys_user.avatar", "sys_user.nick_name");

            // 执行查询
            UserRankingVO userRank = sysUserMapper.selectOneByQueryAs(query, UserRankingVO.class);
            return userRank;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取用户信息失败: " + e.getMessage());
        }
    }

    @Override
    public int getCurrentUserRank(Long id) {
        // 查出总用户数
        QueryWrapper totalUsers = QueryWrapper.create()
                .select("COUNT(DISTINCT id) as totalUsers")
                .from("sys_user")
                .where("status = ?", 0);
        // 查询当前用户的排名
        QueryWrapper userRank = QueryWrapper.create()
                .select("RANK() OVER (ORDER BY total_submissions DESC) AS ranking")
                .from(
                        QueryWrapper.create()
                                .select("user_id, COUNT(*) AS total_submissions")
                                .from("oj_problem_submit")
                                .where("is_delete = 0")
                                .and("(status = 2 OR status = 3)")
                                .groupBy("user_id")
                )
                .as("t") // 给子查询添加别名 "t"
                .where("user_id = ?", id);

        // 执行查询获取结果
        Integer totalUsersSum = sysUserMapper.selectOneByQueryAs(totalUsers, Integer.class);
        if (userRank != null) {
            Integer userRankSum = ojProblemSubmitMapper.selectOneByQueryAs(userRank, Integer.class);
            if(userRankSum == null){
                return -1;
            }
            BigDecimal result = new BigDecimal(userRankSum - 1).divide(new BigDecimal(totalUsersSum - 1), 2, RoundingMode.DOWN);
            // 计算百分比
            return result.multiply(new BigDecimal(100)).intValue();
        }else{
            return -1;
        }

    }
}