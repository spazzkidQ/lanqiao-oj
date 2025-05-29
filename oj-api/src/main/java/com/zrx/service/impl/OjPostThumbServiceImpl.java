package com.zrx.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zrx.exception.BusinessException;
import com.zrx.mapper.OjPostMapper;
import com.zrx.mapper.OjPostThumbMapper;
import com.zrx.mapstruct.OjPostConverter;
import com.zrx.model.common.Paging;
import com.zrx.model.entity.OjPost;
import com.zrx.model.entity.OjPostThumb;
import com.zrx.model.vo.OjPostVo;
import com.zrx.reuslt.Result;
import com.zrx.security.utils.SecurityHelper;
import com.zrx.service.OjPostService;
import com.zrx.service.OjPostThumbService;
import com.zrx.sys.mapper.SysUserMapper;
import com.zrx.sys.model.entity.SysUser;
import com.zrx.utils.PostUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 帖子点赞 服务层实现。
 *
 * @author zhang.rx
 * @since 2024/5/13
 */
@Service
public class OjPostThumbServiceImpl extends ServiceImpl<OjPostThumbMapper, OjPostThumb> implements OjPostThumbService {

    @Autowired
    private OjPostThumbMapper ojPostThumbMapper;

    @Autowired
    private OjPostService ojPostService;

    @Autowired
    private OjPostMapper ojPostMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)  //开启事务
    public Boolean addThumb(Long id) {
        // 先去查询是否有该帖子，再去查询是否有点赞记录
        OjPostVo ojPostVo = ojPostService.getPostById(id);
        if (ojPostVo==null){
            throw new BusinessException("该帖子不存在！");
        }
        //获取登录id
        Long userId = SecurityHelper.getUser().getId();
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("post_id",id)
                .eq("user_id",userId);
        OjPostThumb ojThumb = ojPostThumbMapper.selectOneByQuery(wrapper);
        if (ojThumb!=null){
            //点赞记录存在，就不需要添加
            throw new BusinessException("已有点赞记录,无法重复操作");
        }
        Boolean addThumb = false;  //记录文章点赞数加一 是否成功
        synchronized (this){
            OjPostThumb ojPostThumb = new OjPostThumb();
            ojPostThumb.setPostId(id);
            ojPostThumb.setUserId(userId);
            //添加点赞记录
            int result = ojPostThumbMapper.insert(ojPostThumb);
            if (!(result > 0)){
                throw new BusinessException("添加点赞记录失败");
            }
            // 文章点赞数 加一
            addThumb = ojPostService.postIsThumb(id, true);
        }

        return addThumb;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)  //开启事务
    public Boolean removeThumb(Long id) {
        // 先去查询是否有该帖子，再去查询是否有点赞记录
        OjPostVo ojPostVo = ojPostService.getPostById(id);
        if (ojPostVo==null){
            throw new BusinessException("该帖子不存在！");
        }
        //获取登录id
        Long userId = SecurityHelper.getUser().getId();
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("post_id",id)
                .eq("user_id",userId);
        OjPostThumb ojThumb = ojPostThumbMapper.selectOneByQuery(wrapper);
        if (ojThumb==null){
            //点赞记录不存在，不需要删除。
            throw new BusinessException("没有点赞记录,无需取消点赞");
        }
        Boolean removeThumb = false;
        synchronized (this){
            //删除点赞记录
            int result = ojPostThumbMapper.deleteByQuery(wrapper);
            if (!(result>0)){
                throw new BusinessException("删除点赞记录失败");
            }
            removeThumb = ojPostService.postIsThumb(id, false);
        }
        return removeThumb;
    }

    @Override
    public Page<OjPostVo> findThumbList(Long id, Paging page) {
        List<Long> ids = new ArrayList<>();
        //根据 id 查询点赞帖子集合  id从前端发送过来的
        List<OjPost> ojPosts = thumbList(id, page);
        if (ojPosts==null){
            return null;  //没有点赞帖子
        }
        List<OjPostVo> ojs = BeanUtil.copyToList(ojPosts, OjPostVo.class); // 还需要将list集合转化成page
        //补充vo内容,根据ojs中的帖子作者id查询信息
        for (OjPostVo oj : ojs) {
            if (!ids.contains(oj.getCreator())){
                ids.add(oj.getCreator());
            }
        }
        //根据ids查询作者信息
        Map<Long, SysUser> authorMap = findAuthor(ids);
        //为每个ojs补充信息
        for (OjPostVo oj : ojs) {
            SysUser user = authorMap.get(oj.getCreator());
            if (user!=null){
                oj.setCreatorName(user.getNickName());
                oj.setIntroduce(user.getIntroduce());
                oj.setAvatar(user.getAvatar());
            }
        }
        // 获取总页数
        int totalNum = (int) Math.ceil((double) ojs.size() / page.getPageSize());
        //搭建page信息
        Page<OjPostVo> pageVo = new Page<>();
        pageVo.setPageNumber(page.getPageNum());
        pageVo.setPageSize(page.getPageSize());
        pageVo.setRecords(ojs);
        pageVo.setTotalPage(totalNum);
        pageVo.setTotalRow(ojs.size());
        return pageVo;
    }

    /**
     *  根据 ids查询到作者信息
     */
    @Override
    public Map<Long,SysUser> findAuthor(List<Long> ids) {
        Map<Long, SysUser> authorMap = new HashMap<>();
        List<SysUser> user = userMapper.selectListByIds(ids);
        if (user==null){
            throw new BusinessException("帖子作者不存在");
        }
        //将结果存储于Map中
        for (SysUser u : user) {
            authorMap.put(u.getId(), u);
        }
        return authorMap;
    }

    /**
     *  根据 id 查询点赞帖子集合
     */
    private List<OjPost> thumbList(Long id,Paging page) {
        List<Long> ids = new ArrayList<>();
        QueryWrapper wrapper = new QueryWrapper()
                .eq("user_id", id)
                .limit((page.getPageNum() - 1) * page.getPageSize(), page.getPageSize());
        List<OjPostThumb> ojs = ojPostThumbMapper.selectListByQuery(wrapper);
        if (ojs==null){
            return null; //没有点赞内容
        }
        for (OjPostThumb oj : ojs) {
            ids.add(oj.getPostId());
        }
        if (ids.isEmpty()){
            return null;
        }
        // 根据id集合查询帖子
        List<OjPost> ojPosts = ojPostMapper.selectListByIds(ids);
        for (OjPost ojPost : ojPosts) {
            System.out.println(ojPost);
        }
        return ojPosts;
    }
}
