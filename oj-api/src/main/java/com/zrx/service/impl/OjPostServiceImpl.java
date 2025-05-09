package com.zrx.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.hutool.core.collection.CollUtil;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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



}
