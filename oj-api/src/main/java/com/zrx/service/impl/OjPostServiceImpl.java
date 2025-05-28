package com.zrx.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.util.StringUtils;
import com.google.common.collect.Lists;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.core.util.StringUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zrx.enums.PostZoneEnums;
import com.zrx.exception.BusinessException;
import com.zrx.mapper.OjPostFavourMapper;
import com.zrx.mapper.OjPostMapper;
import com.zrx.mapper.OjPostThumbMapper;
import com.zrx.mapstruct.OjPostConverter;
import com.zrx.model.common.Paging;
import com.zrx.model.dto.post.OjPostAddRequest;
import com.zrx.model.dto.post.OjPostQueryRequest;
import com.zrx.model.dto.post.OjPostUpdateRequest;
import com.zrx.model.entity.OjPost;
import com.zrx.model.vo.OjPostSimpleVo;
import com.zrx.model.vo.OjPostVo;
import com.zrx.security.utils.SecurityHelper;
import com.zrx.service.OjPostService;
import com.zrx.utils.PostUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * 帖子 服务层实现。
 *
 * @author zhang.rx
 * @since 2024/5/13
 */
@Service
public class OjPostServiceImpl extends ServiceImpl<OjPostMapper, OjPost> implements OjPostService {

    @Autowired
    private PostUtil postUtil;
    @Resource
    private OjPostConverter postConverter;
    @Resource
    private OjPostMapper postMapper;


    //帖子首页  屈  分页条件查询
    @Override
    public Page<OjPostVo> getList(Paging page, OjPostQueryRequest req, Boolean selfFlag) {
        // 校验前端传递的分类数据
        if (!StringUtils.isBlank(req.getZone()) && !PostZoneEnums.isValid(req.getZone())) {

            // req.getZone() != null ;  // Zone("")
            // 返回空列表表示分类无效
            return new Page<>();
        }

        // 获取帖子列表，使用集合存储
        List<String> tags = req.getTags();
        if (!CollUtil.isEmpty(tags)) {
            return new Page<>();
        }
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select(
                OjPost::getTitle,     // 帖子标题
                OjPost::getContent,   // 帖子内容
                OjPost::getViewNum,   // 帖子观看数
                OjPost::getThumbNum,  // 帖子点赞数
                OjPost::getFavourNum, // 帖子收藏数
                OjPost::getCreator    // 关联用户头像
        );

        // 区域查询条件——标签和分区是链接在一起的，不可以单独实现
        if (StringUtils.isNotBlank(req.getZone())) {
            queryWrapper.eq(OjPost::getZone, req.getZone());
        }

        // 标签查询条件
        if (CollUtil.isNotEmpty(tags)) {
            queryWrapper.and(wrapper -> {
                for (String tag : req.getTags()) {
                    wrapper.like(OjPost::getTags, tag);
                }
            });
        }
        Page<OjPost> paginate = postMapper.paginate(Page.of(page.getPageNum(), page.getPageSize()), queryWrapper);
        Page<OjPostVo> voPage = postConverter.toVoPage(paginate);
        postUtil.setPostAuthor(voPage.getRecords());
        postUtil.setPostZoneName(voPage.getRecords());
        return voPage;
    }

    //帖子首页 屈  获取五个热门帖子
    @Override
    public List<OjPostSimpleVo> getFiveHotPost() {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select(
                        OjPost::getId,
                        OjPost::getTitle
                ).orderBy(OjPost::getViewNum, true)//根据观看数进行排序
                .limit(5);//返回5个数据
        List<OjPost> ojPosts = mapper.selectListByQuery(queryWrapper);
        return postConverter.toSimpleVoList(ojPosts);
    }

}
