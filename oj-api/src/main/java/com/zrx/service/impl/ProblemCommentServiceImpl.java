package com.zrx.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zrx.exception.BusinessException;
import com.zrx.mapper.OjProblemMapper;
import com.zrx.mapper.ProblemCommentMapper;
import com.zrx.mapstruct.ProblemCommentMapstruct;
import com.zrx.model.dto.problemComment.ProblemCommentRequest;
import com.zrx.model.entity.ProblemComment;
import com.zrx.model.vo.ProblemCommentVo;
import com.zrx.security.utils.SecurityHelper;
import com.zrx.service.ProblemCommentService;
import com.zrx.sys.mapper.SysUserMapper;
import com.zrx.sys.model.entity.SysUser;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.zrx.model.entity.table.OjProblemTableDef.OJ_PROBLEM;
import static com.zrx.model.entity.table.ProblemCommentTableDef.PROBLEM_COMMENT;
import static com.zrx.sys.model.entity.table.SysUserTableDef.SYS_USER;

/**
 * 题目评论 服务层实现。
 *
 * @author zhang.rx
 * @since 2024/4/18
 */
@Service
public class ProblemCommentServiceImpl extends ServiceImpl<ProblemCommentMapper, ProblemComment>
		implements ProblemCommentService {


}
