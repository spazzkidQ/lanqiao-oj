package com.zrx.service.impl;

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
import com.zrx.security.utils.SecurityHelper;
import com.zrx.service.OjPostThumbService;
import com.zrx.utils.PostUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.zrx.model.entity.table.OjPostTableDef.OJ_POST;
import static com.zrx.model.entity.table.OjPostThumbTableDef.OJ_POST_THUMB;

/**
 * 帖子点赞 服务层实现。
 *
 * @author zhang.rx
 * @since 2024/5/13
 */
@Service
public class OjPostThumbServiceImpl extends ServiceImpl<OjPostThumbMapper, OjPostThumb> implements OjPostThumbService {



}
