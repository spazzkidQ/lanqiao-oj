package com.zrx.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.StringUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zrx.exception.BusinessException;
import com.zrx.mapper.OjPostCommentMapper;
import com.zrx.mapper.OjPostMapper;
import com.zrx.mapstruct.OjPostCommentConverter;
import com.zrx.model.dto.postComment.PostCommentRequest;
import com.zrx.model.entity.OjPostComment;
import com.zrx.model.vo.OjPostVo;
import com.zrx.model.vo.PostCommentVo;
import com.zrx.security.utils.SecurityHelper;
import com.zrx.service.OjPostCommentService;
import com.zrx.service.OjPostService;
import com.zrx.sys.mapper.SysUserMapper;
import com.zrx.sys.model.entity.SysUser;
import com.zrx.sys.service.SysUserService;
import jakarta.annotation.Resource;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static com.zrx.model.entity.table.OjPostCommentTableDef.OJ_POST_COMMENT;


/**
 * 帖子评论 服务层实现。
 *
 * @author zhang.rx
 * @since 2024/5/20
 */
@Service
public class OjPostCommentServiceImpl extends ServiceImpl<OjPostCommentMapper, OjPostComment> implements OjPostCommentService {

    @Autowired
    private OjPostService ojPostService;

    @Autowired
    private OjPostCommentMapper postCommentMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 根据帖子id获取评论数量
     * @param postId
     * @return
     */
    @Override
    public Long getCountNum(Long postId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select(OJ_POST_COMMENT.ALL_COLUMNS)
                .from(OJ_POST_COMMENT)
                .where(OJ_POST_COMMENT.POST_ID.eq(postId));
        return (long)postCommentMapper.selectListByQuery(queryWrapper).size();
    }

    /**
     *  1.评论不能为null
     *  2.查询文章是否存在
     *  3.如果存在父id，验证父id是否存在于记录中，查询 id 字段
     *  4.添加评论后查询该文章的所有评论
     */
    @Override
    @Transactional(rollbackFor = Exception.class)  //开启事务
    public Boolean addComment(OjPostComment ojPostComment) {
        if (ojPostComment.getContent()==null || ojPostComment.getContent().isEmpty()){
            throw new BusinessException("不能发送空评论");
        }
        OjPostVo ojPostVo = ojPostService.getPostById(ojPostComment.getPostId());
        if (ojPostVo==null){
            throw new BusinessException("该帖子不存在");
        }
        int insert = 0;
        synchronized (this){
            if (ojPostComment.getParentId()!=null){
                //父id存在，需要判断记录中是否有 父id,并且记录的帖子id 为 当前传入帖子id
                OjPostComment ojParent = postCommentMapper.selectOneById(ojPostComment.getParentId());
                if (ojParent==null){
                    throw new BusinessException("回复评论并不存在");
                }
                if (!ojParent.getId().equals(ojPostComment.getParentId())){
                    throw new BusinessException("回复评论不存在");
                }
            }
            ojPostComment.setAuthorId(SecurityHelper.getUser().getId());
            insert = postCommentMapper.insert(ojPostComment);
        }
        return insert > 0;
    }

    /**
     * 根据帖子id查询评论记录
     */
    @Override
    public List<OjPostComment> selectByPostId(Long postId) {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("post_id",postId);
        List<OjPostComment> ojComment = postCommentMapper.selectListByQuery(wrapper);
        if (ojComment == null || ojComment.isEmpty()){
            throw new BusinessException("该帖子暂无评论");
        }
        return ojComment;
    }


    /**
     *  根据id查询完整评论
     *  1.校验帖子 id
     *  2.查询帖子评论
     *  3.set children元素内容
     *  4.提前去user表中查询 用户头像和昵称
     *  A.构建 tree 集合 ，parents 集合
     *  B.查找 顶级评论 add到 parents 集合
     *  C.构建 children 集合，查找 顶级评论下的子评论
     *  D.将 children 集合 add 到对应的 parents.children
     *  E.将parents 集合 add 到tree，返回 tree
     */
    @Override
    public List<PostCommentVo> findList(Long postId) {
        OjPostVo ojPostVo = ojPostService.getPostById(postId);
        if (ojPostVo==null){
            throw new BusinessException("该帖子不存在");
        }
        List<PostCommentVo> postCommentVos = null;
        synchronized (this){
            //根据 id 查询所有评论
            List<OjPostComment> ojPostComments = this.selectByPostId(postId);
            List<PostCommentVo> ojs = BeanUtil.copyToList(ojPostComments, PostCommentVo.class);
            postCommentVos = this.buildTree(ojs);
        }

        return postCommentVos;
    }


    /**
     *  递归构建 Tree 数据
     */
    private List<PostCommentVo> buildTree(List<PostCommentVo> ojs){
        // 构建 Tree集合，用于存放返回前端的数据 构建 parents 集合 用于存放 顶级评论
        ArrayList<PostCommentVo> Tree = new ArrayList<>();
        ArrayList<PostCommentVo> parents = new ArrayList<>();
        // 查找 顶级评论
        for (PostCommentVo oj : ojs) {
            // parent 就是此次循环的顶级评论
            PostCommentVo parent = this.buildParent(oj, ojs);
            //将 parent 添加到 parents集合中，还要排除重复的
            if (parent != null && !parents.contains(parent)){
                parents.add(parent);
            }
        }
        //查找 子评论
        for (PostCommentVo parent : parents) {
            PostCommentVo parentTree = buildSon(parent,ojs);
            Tree.add(parentTree);
        }
        return Tree;
    }

    /**
     *  查找顶级评论
     */
    private PostCommentVo buildParent(PostCommentVo oj, List<PostCommentVo> ojs) {
        // 使用 hashMap，避免重复循环。
        Map<Long,PostCommentVo> parentMap = new HashMap<>();
        for (PostCommentVo comment : ojs) {
            // id 为 key，PostCommit对象为 值。
            parentMap.put(comment.getId(), comment);
        }
        PostCommentVo parent = oj;
        while (oj.getParentId()!=null){
            //使用 链表查询替代递归，时间复杂度降低。
            parent = parentMap.get(parent.getParentId());
            if (parent == null){
                break;
            }
        }
        return parent;
    }


    /**
     *  构建 子评论
     */
    private PostCommentVo buildSon(PostCommentVo parent, List<PostCommentVo> ojs) {
        //存储子评论，一个父评可能有多个子评论，所以用 List 集合
        ArrayList<PostCommentVo> children = new ArrayList<>();
        for (PostCommentVo oj : ojs) {
            // 根据评论的AuthorId查询AuthorName 和 authorAvatar
            SysUser sysUser = sysUserMapper.selectOneById(oj.getAuthorId());
            if (sysUser==null){
                throw new BusinessException("出错了，用户不存在");
            }
            if (sysUser.getAvatar()==null || sysUser.getNickName()==null){
                throw new BusinessException("部分用户信息未完善，请稍后查看");
            }
            // 如果用户没有头像或者昵称，就显示null头像和真实名字
            oj.setAuthorAvatar(sysUser.getAvatar());
            oj.setAuthorName(sysUser.getNickName());
            if (oj.getParentId() == null){
                continue; //跳过此次循环
            }
            // 判断 该评论是否有子评论
            if(parent.getId().equals(oj.getParentId())){
                children.add(buildSon(oj,ojs));
            }
        }
        //父评论 添加 子评论
        parent.setChildren(children);
        return parent;
    }



}
