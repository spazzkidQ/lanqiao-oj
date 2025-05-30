package com.zrx.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zrx.exception.BusinessException;
import com.zrx.mapper.OjProblemMapper;
import com.zrx.mapper.ProblemCommentMapper;
import com.zrx.model.dto.problemComment.ProblemCommentRequest;
import com.zrx.model.entity.OjProblem;
import com.zrx.model.entity.ProblemComment;
import com.zrx.model.vo.ProblemCommentVo;
import com.zrx.security.utils.SecurityHelper;
import com.zrx.service.ProblemCommentService;
import com.zrx.sys.mapper.SysUserMapper;
import com.zrx.sys.model.entity.SysUser;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.zrx.model.entity.table.ProblemCommentTableDef.PROBLEM_COMMENT;

/**
 * 题目评论 服务层实现。
 *
 * @author zhang.rx
 * @since 2024/4/18
 */
@Service
public class ProblemCommentServiceImpl extends ServiceImpl<ProblemCommentMapper, ProblemComment>
		implements ProblemCommentService {
	@Resource
	private ProblemCommentMapper problemCommentMapper;
	@Resource
	private OjProblemMapper ojProblemMapper;
	@Resource
	private SysUserMapper sysUserMapper;

	/**
	 * 	保存题目评论
	 * @param req
	 * @return
	 */
	@Override
	public Boolean save(ProblemCommentRequest req) {
		ProblemComment problemComment = new ProblemComment();
		// 根据题目的id查询出题目的信息
		OjProblem ojProblem = ojProblemMapper.selectOneById(req.getProblemId());
		// 查询出当前用户的信息  使用工具类来查询
		SysUser user = SecurityHelper.getUser();
		problemComment.setTags(ojProblem.getTags());
		problemComment.setParentId(req.getParentId());
		problemComment.setProblemId(req.getProblemId());
		problemComment.setContent(req.getContent());
		problemComment.setAuthorId(user.getId());
		return problemCommentMapper.insert(problemComment) == 1;
	}

	/**
	 * 	根据当前题目的id查询出当前题目的评论
	 * @param problemId
	 * @return
	 */
	@Override
	public List<ProblemCommentVo> getProblemCommentByProblemId(String problemId) {
		if(problemId == null){
			throw new BusinessException(""); // 当前题目没有查询到任何评论内容
		}
		// 1.创建查询条件
		QueryWrapper queryWrapper = new QueryWrapper();
		queryWrapper.select(PROBLEM_COMMENT.ALL_COLUMNS)
				.where(PROBLEM_COMMENT.PROBLEM_ID.eq(problemId))
				.and(PROBLEM_COMMENT.PARENT_ID.isNull()); // 查询出当前题目的所有的评论父（首评）
		// 2.查询出所有的当前题目的首评
		List<ProblemComment> problemComments = problemCommentMapper.selectListByQuery(queryWrapper);

		// 3. 查询出所有当前题目评论的所有子回复，封装成我们所需的vo
		List<ProblemCommentVo> resultList = problemComments.stream().map(pro -> {
			ProblemCommentVo returnVo = new ProblemCommentVo();
			// 查询出所有子回复评论的用户信息
			SysUser user = sysUserMapper.selectOneById(pro.getAuthorId());
			returnVo.setId(pro.getId());
			returnVo.setTags(pro.getTags());
			returnVo.setParentId(pro.getParentId());
			returnVo.setProblemId(pro.getProblemId());
			returnVo.setContent(pro.getContent());
			returnVo.setAuthorId(user.getId());
			returnVo.setAuthorAvatar(user.getAvatar());
			returnVo.setAuthorName(user.getNickName());
			returnVo.setCreateTime(pro.getCreateTime());

			// 第二个查询条件
			QueryWrapper newQuery = new QueryWrapper();
			newQuery.select(PROBLEM_COMMENT.ALL_COLUMNS).where(PROBLEM_COMMENT.PROBLEM_ID.eq(pro.getProblemId())).and(PROBLEM_COMMENT.PARENT_ID.eq(pro.getId()));
			// 查询出所有的子回复
			List<ProblemCommentVo> collect = problemCommentMapper.selectListByQuery(newQuery).stream().map(po -> {
				// 创建vo
				ProblemCommentVo Vo = new ProblemCommentVo();
				// 查询出所有子回复评论的用户信息
				SysUser childUser = sysUserMapper.selectOneById(po.getAuthorId());
				Vo.setId(po.getId());
				Vo.setTags(po.getTags());
				Vo.setParentId(po.getParentId());
				Vo.setProblemId(po.getProblemId());
				Vo.setContent(po.getContent());
				Vo.setAuthorId(childUser.getId());
				Vo.setAuthorAvatar(childUser.getAvatar());
				Vo.setAuthorName(childUser.getNickName());
				Vo.setCreateTime(po.getCreateTime());
				return Vo;
			}).collect(Collectors.toList());

			returnVo.setChildren(collect);
			return returnVo;
		}).collect(Collectors.toList());
		return resultList;
	}

	/**
	 * 根据父节点id获取题目子评论
	 * @param problemId
	 * @return
	 */
	@Override
	public List<ProblemCommentVo> getListChildren(Long problemId) {
		// 创建查询
		QueryWrapper queryWrapper = new QueryWrapper();
		queryWrapper.select(PROBLEM_COMMENT.ALL_COLUMNS)
				.where(PROBLEM_COMMENT.PARENT_ID.eq(problemId));
		// 查询出所有父id与请求problemId的评论
		List<ProblemComment> problemComments = problemCommentMapper.selectListByQuery(queryWrapper);
		//
		List<ProblemCommentVo> resultVo = problemComments.stream().map(pro -> {
			// 创建每个子回复
			// 查询出当前用户的所有信息
			SysUser user = sysUserMapper.selectOneById(pro.getAuthorId());
			ProblemCommentVo vo = new ProblemCommentVo();
			vo.setId(pro.getId());
			vo.setTags(pro.getTags());
			vo.setParentId(pro.getParentId());
			vo.setProblemId(pro.getProblemId());
			vo.setContent(pro.getContent());
			vo.setAuthorId(user.getId());
			vo.setAuthorAvatar(user.getAvatar());
			vo.setAuthorName(user.getNickName());
			vo.setCreateTime(pro.getCreateTime());

			// 子查询
			QueryWrapper childQuery = new QueryWrapper();
			childQuery.select(PROBLEM_COMMENT.ALL_COLUMNS).where(PROBLEM_COMMENT.PROBLEM_ID.eq(pro.getProblemId())).and(PROBLEM_COMMENT.PARENT_ID.eq(pro.getId()));
			List<ProblemCommentVo> childList = problemCommentMapper.selectListByQuery(childQuery).stream().map(po -> {
				ProblemCommentVo childVo = new ProblemCommentVo();
				// 查询出当前子回复的用户信息
				SysUser childUser = sysUserMapper.selectOneById(po.getAuthorId());
				childVo.setId(po.getId());
				childVo.setTags(po.getTags());
				childVo.setParentId(po.getParentId());
				childVo.setProblemId(po.getProblemId());
				childVo.setContent(po.getContent());
				childVo.setAuthorId(childUser.getId());
				childVo.setAuthorAvatar(childUser.getAvatar());
				childVo.setAuthorName(childUser.getNickName());
				childVo.setCreateTime(po.getCreateTime());
				return childVo;
			}).collect(Collectors.toList());
			vo.setChildren(childList);
			return vo;
		}).collect(Collectors.toList());
		return resultVo;
	}

	/**
	 * 根据主键删除题目评论
	 * @param id
	 * @return
	 */
	@Override
	public Boolean deleteByProblemId(String id) {
		return problemCommentMapper.deleteById(id) == 1;
	}


}
