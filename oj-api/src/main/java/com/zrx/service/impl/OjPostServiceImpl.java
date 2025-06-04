package com.zrx.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.util.StringUtils;
import com.google.common.collect.Lists;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
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
import com.zrx.model.entity.OjPostFavour;
import com.zrx.model.entity.OjPostThumb;
import com.zrx.model.vo.OjPostSimpleVo;
import com.zrx.model.vo.OjPostVo;
import com.zrx.security.utils.SecurityHelper;
import com.zrx.service.OjPostService;
import com.zrx.sys.mapper.SysUserMapper;
import com.zrx.sys.model.entity.SysUser;
import com.zrx.utils.PostUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import static com.zrx.model.entity.table.OjPostFavourTableDef.OJ_POST_FAVOUR;
import static com.zrx.model.entity.table.OjPostTableDef.OJ_POST;
import static com.zrx.model.entity.table.OjPostThumbTableDef.OJ_POST_THUMB;


/**
 * 帖子 服务层实现。
 *
 * @author zhang.rx
 * @since 2024/5/13
 */
@Service
public class OjPostServiceImpl extends ServiceImpl<OjPostMapper, OjPost> implements OjPostService {
    @Resource
    private OjPostMapper ojPostMapper;
    @Resource
    private SysUserMapper sysUserMapper;

    @Autowired
    private OjPostFavourMapper ojPostFavourMapper;

    @Autowired
    private OjPostThumbMapper ojPostThumbMapper;

    @Autowired
    private PostUtil postUtil;
    @Resource
    private OjPostConverter postConverter;
    @Resource
    private OjPostMapper postMapper;
    /**
     * 分页查询
     * @param page
     * @param req
     * @return
     */
    @Override
    public Page<OjPostVo> pageSelfOrPage(Paging page, OjPostQueryRequest req) {
        String title = req.getTitle();
        String zone = req.getZone();
        List<String> tags = req.getTags();
        // 创建QueryWrapper
        QueryWrapper queryWrapper = new QueryWrapper();
        // 使用工具类来获取当前用户信息 这里可以能缓存需要重要查询用户信息
        SysUser user = SecurityHelper.getUser();
        // 根据用户id查询用户信息
        SysUser sysUser = sysUserMapper.selectOneById(user.getId());
        // 根据条件查询
        queryWrapper.select(OJ_POST.ALL_COLUMNS) // 查询OJ_post表中所有字段
                .where(OJ_POST.TITLE.like(title))
                .and(OJ_POST.ZONE.like(zone))
                .and(OJ_POST.CREATOR.eq(user.getId())) // 创建者的id 必须与用户的id一致
                .orderBy(OJ_POST.VIEW_NUM.desc()); // 根据观看数降序排序
        // 使用Hutool 的工具类的判断标签是否为空，为其添加模糊查询条件
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.and(OJ_POST.TAGS.like("\"" + tag + "\""));
            }
        }

        // 使用page获得分页的数据
        Page<OjPost> ojPostPage = mapper.paginate(Page.of(page.getPageNum(),page.getPageSize()),queryWrapper);
        System.err.println(ojPostPage);
        // 将查询出的数据 转换为VO对象
        List<OjPostVo> collect = ojPostPage.getRecords().stream().map(post -> {
            // 创建vo对象
            OjPostVo ojPostVo = new OjPostVo();
            // 设置参数
            ojPostVo.setThumbNum(post.getThumbNum()); // 点赞数
            ojPostVo.setFavourNum(post.getFavourNum()); // 收藏数
            ojPostVo.setTitle(post.getTitle()); // 标题
            ojPostVo.setAvatar(user.getAvatar()); // 用户头像
            ojPostVo.setId(post.getId());//id
            ojPostVo.setCreateTime(post.getCreateTime()); // 创建时间
            ojPostVo.setContent(post.getContent()); // 内容
            ojPostVo.setCreatorName(user.getNickName()); // 用户名
            ojPostVo.setCreator(user.getId()); // 用户 id
            ojPostVo.setIntroduce(user.getIntroduce()); // 用户简介
            ojPostVo.setViewNum(post.getViewNum()); // 观看数

            // 设置标签的显示格式
            String tags1 = post.getTags();
            String plainTags = tags1.replaceAll("[\\[\\]\"]", "");// 移除[]和,
            ojPostVo.setTags(Collections.singletonList(plainTags));// 标签
            ojPostVo.setZone(post.getZone());
            // 使用分区的枚举来查询出对应的中文信息
            ojPostVo.setZoneName(PostZoneEnums.getTextByValue(post.getZone())); // 分区
            ojPostVo.setViewNum(post.getViewNum()); // 观看数
            return ojPostVo; // 返回
        }).collect(Collectors.toList());

        // 创建返回的Page数据
        Page<OjPostVo> resultPage = new Page<>();
        resultPage.setPageNumber(ojPostPage.getPageNumber());// 设置当前页数
        resultPage.setPageSize(ojPostPage.getPageSize());//  设置当前分页的一页显示的页数
        resultPage.setTotalPage(ojPostPage.getTotalPage());// 查询出的总页数
        resultPage.setTotalRow(ojPostPage.getTotalRow());// 查询出的总行数
        resultPage.setRecords(collect);// 结果集
        System.err.println(collect);
        return resultPage;
    }

    //根据id来获取帖子
    @Override
    public OjPostVo getInfoById(String id) {

        SysUser user = SecurityHelper.getUser();
        OjPostVo vo = new OjPostVo();
        OjPost post = ojPostMapper.selectOneById(id);
//        System.out.println(post);
        vo.setId(post.getId()); //id
        vo.setTitle(post.getTitle()); //标题
        vo.setContent(post.getContent()); //内容
        vo.setThumbNum(post.getThumbNum()); // 点赞数
        vo.setFavourNum(post.getFavourNum()); // 收藏数
        vo.setAvatar(user.getAvatar()); // 用户头像
        vo.setZone(post.getZone());  //分区
        vo.setViewNum(post.getViewNum());   // 浏览数

        ojPostMapper.incrementViewNum(id);  // 增加浏览量
        vo.setViewNum(post.getViewNum() + 1);  // 显示更新后的值

        vo.setCreateTime(post.getCreateTime());     // 创建时间
        vo.setZoneName(PostZoneEnums.getTextByValue(post.getZone())); // 分区
        String tags1 = post.getTags(); //标签
        String plainTags = tags1.replaceAll("[\\[\\]\"]", "");// 移除[]和,
        vo.setTags(Collections.singletonList(plainTags));// 标签
        vo.setCreatorName(user.getNickName()); //作者
        vo.setCreator(user.getId());  //作者id
        vo.setIntroduce(user.getIntroduce());//作者简介
        vo.setAvatar(user.getAvatar());  //作者头像
        // 默认未点赞收藏
        vo.setThumbFlag(false);
        vo.setFavourFlag(false);

        //校验帖子是否收藏 获取到登录id
        Long userId = SecurityHelper.getUser().getId();
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("post_id",id)
                .eq("user_id",userId);
        OjPostFavour ojPFavour = ojPostFavourMapper.selectOneByQuery(wrapper);
        if (ojPFavour!=null){
            //说明有数据，已经收藏
            vo.setFavourFlag(true);
        }
        //校验帖子是否收藏 获取到登录id
        OjPostThumb ojThumb = ojPostThumbMapper.selectOneByQuery(wrapper);
        if (ojThumb!=null){
            //说明有数据，已经点赞
            vo.setThumbFlag(true);
        }

        return vo;
    }

    @Override
    public Boolean updateById(OjPostUpdateRequest req) {
        OjPost post = ojPostMapper.selectOneById(req.getId());//原数据
        post.setId(req.getId());
        post.setTitle(req.getTitle());
        post.setContent(req.getContent());
        post.setZone(req.getZone());
        post.setTags(req.getTags().toString());

        return ojPostMapper.update(post)==1;

    }

    @Override
    public Boolean removeById(Long id) {

        return ojPostMapper.deleteById(id)==1;
    }



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
                OjPost::getId,        // 帖子id
                OjPost::getTitle,     // 帖子标题
                OjPost::getZone,      // 帖子分区
                OjPost::getContent,   // 帖子内容
                OjPost::getViewNum,   // 帖子观看数
                OjPost::getThumbNum,  // 帖子点赞数
                OjPost::getFavourNum, // 帖子收藏数
                OjPost::getCreator,   // 关联用户头像
                OjPost::getCreateTime

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


    @Override
    public OjPostVo getPostById(Long id) {
        OjPost ojPost = ojPostMapper.selectOneById(id);
        if (ojPost==null){
            return null;
        }
        return BeanUtil.copyProperties(ojPost,OjPostVo.class);
    }

    /**
     * 用户添加点赞和取消点赞
     */
    @Override
    public Boolean postIsThumb(Long postId, Boolean isThumb) {
        OjPostVo ojPostVo = this.getPostById(postId);
        if (ojPostVo==null){
            throw new BusinessException("该文章不存在");
        }
        if (isThumb){
            //添加点赞
            ojPostVo.setThumbNum(ojPostVo.getThumbNum()+1);
        }else {
            //取消点赞
            ojPostVo.setThumbNum(ojPostVo.getThumbNum()-1);
        }
        int result = ojPostMapper.postIsThumb(postId, ojPostVo.getThumbNum());
        return result > 0;
    }


    /**
     * 用户添加收藏和取消收藏
     */
    @Override
    public Boolean postIsFavour(Long postId, Boolean isFavour) {
        OjPostVo ojPostVo = this.getPostById(postId);
        if (ojPostVo==null){
            throw new BusinessException("该文章不存在");
        }
        if (isFavour){
            //添加收藏
            ojPostVo.setFavourNum(ojPostVo.getFavourNum()+1);
        }else {
            //取消收藏
            ojPostVo.setFavourNum(ojPostVo.getFavourNum()-1);
        }
        int result = ojPostMapper.postIsFavour(postId, ojPostVo.getFavourNum());
        return result > 0;
    }



    /**
     * 根据帖子ID获取帖子详细信息
     * @param id 帖子ID
     * @return 帖子详细信息
     */
    @Override
    public OjPostVo getInfoByIdDetail(String id) {
        // 1. 根据被点击的ID查询帖子信息
        OjPost post = postMapper.selectOneById(Long.parseLong(id));
        if (post == null) {
            throw new BusinessException("未找到该帖子");
        }

        // 2. 更新帖子浏览量
        boolean updated = UpdateChain.of(OjPost.class)
                .set(OjPost::getViewNum, post.getViewNum() + 1)
                .where(OjPost::getId).eq(Long.parseLong(id))
                .update();

        if (updated) {
            post.setViewNum(post.getViewNum() + 1);
        }

        // 3. 转换为VO对象
        OjPostVo vo = postConverter.toVo(post);

        // 4. 设置帖子作者信息
        postUtil.setPostAuthor(Lists.newArrayList(vo));

        // 5. 设置分区名称
        setZoneName(vo);

        return vo;
    }

    /**
     * 设置帖子的分区名称
     * 根据分区代码获取对应的分区名称
     * @param ojPostVo 帖子VO对象
     */
    private void setZoneName(OjPostVo ojPostVo) {
        ojPostVo.setZoneName(PostZoneEnums.getTextByValue(ojPostVo.getZone()));
    }

    /**
     *  保存帖子和发送帖子
     * @param req
     * @return
     */
    @Override
    public Boolean save(OjPostAddRequest req) {
        OjPost ojPost = new OjPost();
        ojPost.setTitle(req.getTitle());
        ojPost.setContent(req.getContent());
        ojPost.setZone(req.getZone());
        ojPost.setTags(req.getTags().toString());
        if(mapper.insert(ojPost) < 0) return false;
        else return true;
    }


}
