package com.zrx.service.impl;

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
import com.zrx.utils.PostUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.zrx.model.entity.table.OjPostFavourTableDef.OJ_POST_FAVOUR;
import static com.zrx.model.entity.table.OjPostTableDef.OJ_POST;

/**
 * 帖子收藏 服务层实现。
 *
 * @author zhang.rx
 * @since 2024/5/13
 */
@Service
public class OjPostFavourServiceImpl extends ServiceImpl<OjPostFavourMapper, OjPostFavour>
		implements OjPostFavourService {



}
