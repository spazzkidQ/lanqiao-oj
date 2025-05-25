package com.zrx.mapper;/*
@author 比巴卜
@date  2025/5/21 上午9:34
@Description 获取热力用户排行榜
*/

import com.mybatisflex.core.BaseMapper;
import com.zrx.model.vo.UserRankingVO;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface getUserTopMapper extends BaseMapper<UserRankingVO> {

}
