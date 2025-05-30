package com.zrx.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.zrx.model.common.Paging;
import com.zrx.model.dto.problemSubmit.OjProblemSubmitQueryRequest;
import com.zrx.model.dto.problemSubmit.OjProblemSubmitVo;
import com.zrx.model.dto.problemSubmit.ProblemSubmitAddRequest;
import com.zrx.model.entity.OjProblemSubmit;
import com.zrx.model.vo.OjProblemPageVo;
import com.zrx.sys.model.entity.SysUser;

public interface OjProblemSubmitService extends IService<OjProblemSubmit> {

    Long doQuestion(ProblemSubmitAddRequest problemSubmitAddRequest, SysUser user);
    // 根据主键id获取题目提交信息
    OjProblemSubmitVo getInfoById(Long id);
    // 根据用户id分页查询题目提交信息列表
    Page<OjProblemSubmitVo> pageInfoByUserIdById(Long id, Paging page);
}
