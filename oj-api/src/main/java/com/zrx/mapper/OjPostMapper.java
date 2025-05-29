package com.zrx.mapper;

import com.mybatisflex.core.BaseMapper;
import com.zrx.model.entity.OjPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 帖子 映射层。
 *
 *
 * @since 2024/5/13
 */
@Mapper
public interface OjPostMapper extends BaseMapper<OjPost> {


    //LuoTaoLing
    @Update("update oj_post set thumb_num = #{thumbNum} where id = #{postId}")
    int postIsThumb(Long postId, Integer thumbNum);

    @Update("update oj_post set favour_num = #{favourNum} where id = #{postId}")
    int postIsFavour(Long postId, Integer favourNum);

}
