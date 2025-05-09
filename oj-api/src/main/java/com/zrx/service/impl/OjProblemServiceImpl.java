package com.zrx.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.StringUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zrx.config.flex.MybatisFlexFunc;
import com.zrx.exception.BusinessException;
import com.zrx.mapper.OjProblemMapper;
import com.zrx.mapstruct.OjProblemConverter;
import com.zrx.model.common.Paging;
import com.zrx.model.dto.problem.OjProblemAddRequest;
import com.zrx.model.dto.problem.OjProblemQueryRequest;
import com.zrx.model.dto.problem.OjProblemUpdateRequest;
import com.zrx.model.entity.OjProblem;
import com.zrx.model.vo.OjProblemPageVo;
import com.zrx.model.vo.OjProblemVo;
import com.zrx.service.OjProblemService;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

import static com.zrx.model.entity.table.OjProblemTableDef.OJ_PROBLEM;

/**
 * 题目 服务层实现。
 *
 * @author zhang.rx
 * @since 2024/3/20
 */
@Service
public class OjProblemServiceImpl extends ServiceImpl<OjProblemMapper, OjProblem> implements OjProblemService {

}
