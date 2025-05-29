package com.zrx.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zrx.exception.BusinessException;
import com.zrx.mapper.OjPostFavourMapper;
import com.zrx.mapper.OjPostMapper;
import com.zrx.mapstruct.OjPostConverter;
import com.zrx.model.common.Paging;
import com.zrx.model.entity.OjPost;
import com.zrx.model.entity.OjPostFavour;
import com.zrx.model.vo.OjPostVo;
import com.zrx.security.utils.SecurityHelper;
import com.zrx.service.OjPostFavourService;
import com.zrx.service.OjPostService;
import com.zrx.service.OjPostThumbService;
import com.zrx.sys.model.entity.SysUser;
import com.zrx.utils.PostUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 帖子收藏 服务层实现。
 *
 * @author zhang.rx
 * @since 2024/5/13
 */
@Service
public class OjPostFavourServiceImpl extends ServiceImpl<OjPostFavourMapper, OjPostFavour>
		implements OjPostFavourService {

	@Autowired
	private OjPostService ojPostService;

	@Autowired
	private OjPostFavourMapper ojPostFavourMapper;

	@Autowired
	private OjPostThumbService ojPostThumbService;

	@Autowired
	private OjPostMapper ojPostMapper;

	@Override
	@Transactional(rollbackFor = Exception.class)  //开启事务
	public Boolean addFavour(Long id) {
		OjPostVo ojPostVo = ojPostService.getPostById(id);
		if (ojPostVo==null){
			//没有帖子
			throw new BusinessException("该帖子不存在");
		}
		//获取到登录id
		Long userId = SecurityHelper.getUser().getId();
		QueryWrapper wrapper = new QueryWrapper();
		wrapper.eq("post_id",id)
				.eq("user_id",userId);
		OjPostFavour ojPFavour = ojPostFavourMapper.selectOneByQuery(wrapper);
		if (ojPFavour!=null){
			//帖子已经收藏
			throw new BusinessException("帖子已收藏，请勿重复操作");
		}
		Boolean addFavour = false;
		synchronized (this){
			//帖子存在并且未收藏
			OjPostFavour ojPostFavour = new OjPostFavour();
			ojPostFavour.setPostId(id);
			ojPostFavour.setUserId(userId);
			int result = ojPostFavourMapper.insert(ojPostFavour);
			if (!(result>0)){
				throw new BusinessException("添加收藏记录失败");
			}
			addFavour = ojPostService.postIsFavour(id, true);
		}
		return addFavour;
	}


	@Override
	@Transactional(rollbackFor = Exception.class)  //开启事务
	public Boolean removeFavour(Long id) {
		OjPostVo ojPostVo = ojPostService.getPostById(id);
		if (ojPostVo==null){
			//没有帖子
			throw new BusinessException("该帖子不存在");
		}
		//获取到登录id
		Long userId = SecurityHelper.getUser().getId();
		QueryWrapper wrapper = new QueryWrapper();
		wrapper.eq("post_id",id)
				.eq("user_id",userId);
		OjPostFavour ojPFavour = ojPostFavourMapper.selectOneByQuery(wrapper);
		if (ojPFavour==null){
			//帖子未收藏
			throw new BusinessException("未收藏该帖子，无法取消收藏");
		}
		Boolean removeFavour = false;
		synchronized (this){
			//帖子存在并收藏
			int result = ojPostFavourMapper.deleteByQuery(wrapper);
			if (!(result>0)){
				throw new BusinessException("删除收藏记录失败");
			}
			removeFavour = ojPostService.postIsFavour(id, false);
		}
		return removeFavour;
	}

	/**
	 * 根据id查询用户收藏的帖子集合
	 * 根据帖子id查询到帖子内容
	 * 根据作者补全返回类
	 * 搭建返回 page
	 */
	@Override
	public Page<OjPostVo> findFavourList(Long id, Paging page) {
		List<Long> ids = new ArrayList<>();
		// 根据用户 id 查询到 帖子内容
		List<OjPost> ojPosts = favourList(id, page);
		if (ojPosts==null){
			return null;  //没有收藏帖子
		}
		List<OjPostVo> ojs = BeanUtil.copyToList(ojPosts, OjPostVo.class);
		for (OjPostVo oj : ojs) {
			if (!ids.contains(oj.getCreator())){
				ids.add(oj.getCreator());
			}
		}
		//根据帖子作者查询信息  然后补充ojs
		Map<Long, SysUser> author = ojPostThumbService.findAuthor(ids);
		for (OjPostVo oj : ojs) {
			SysUser user = author.get(oj.getCreator());
			if (user!=null){
				oj.setCreatorName(user.getNickName());
				oj.setIntroduce(user.getIntroduce());
				oj.setAvatar(user.getAvatar());
			}
		}
		// 搭建page信息 获取总页数
		int totalNum = (int)Math.ceil ((double) ojs.size() / page.getPageSize());
		Page<OjPostVo> pageVo = new Page<>();
		pageVo.setPageSize(page.getPageSize());
		pageVo.setPageNumber(page.getPageNum());
		pageVo.setTotalPage(totalNum);
		pageVo.setTotalRow(ojs.size());
		pageVo.setRecords(ojs);
		return pageVo;
	}


	// 查询用户收藏的帖子内容
	private List<OjPost> favourList(Long id, Paging page) {
		List<Long> ids = new ArrayList<>();
		QueryWrapper wrapper = new QueryWrapper()
				.eq("user_id",id)
				.limit((page.getPageNum()-1)*page.getPageSize(),page.getPageSize());
		List<OjPostFavour> ojs = ojPostFavourMapper.selectListByQuery(wrapper);
		if (ojs==null){
			return null; //当前没有收藏帖子
		}
		for (OjPostFavour oj : ojs) {
			ids.add(oj.getPostId());
		}
		if (ids.isEmpty()){
			return null;
		}
		return ojPostMapper.selectListByIds(ids);
	}


}

