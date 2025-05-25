package com.zrx.service;/*
@author 比巴卜
@date  2025/5/21 上午9:38
@Description 获取用户热力榜
*/

import com.mybatisflex.core.service.IService;
import com.zrx.model.vo.UserRankingVO;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface getUserTopService extends IService<UserRankingVO> {
    // 获取前10名用户排行榜
    List<UserRankingVO> getUserTop();
    // 获取当前用户的排名的其他信息
    UserRankingVO getCurrentUserRankOther(Long id);
    // 获取用户排行榜的所有信息
    int getCurrentUserRank(Long id);
}
