package com.zrx.service.impl;


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
import com.zrx.model.vo.PostCommentVo;
import com.zrx.security.utils.SecurityHelper;
import com.zrx.service.OjPostCommentService;
import com.zrx.sys.mapper.SysUserMapper;
import com.zrx.sys.model.entity.SysUser;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.zrx.model.entity.table.OjPostCommentTableDef.OJ_POST_COMMENT;
import static com.zrx.model.entity.table.OjPostTableDef.OJ_POST;
import static com.zrx.sys.model.entity.table.SysUserTableDef.SYS_USER;

/**
 * 帖子评论 服务层实现。
 *
 * @author zhang.rx
 * @since 2024/5/20
 */
@Service
public class OjPostCommentServiceImpl extends ServiceImpl<OjPostCommentMapper, OjPostComment> implements OjPostCommentService {

}
