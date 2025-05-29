package com.zrx.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import com.zrx.model.common.Paging;
import com.zrx.model.dto.problem.OjProblemAddRequest;
import com.zrx.model.dto.problem.OjProblemQueryRequest;
import com.zrx.model.entity.OjProblem;
import com.zrx.model.vo.OjProblemPageVo;
import com.zrx.model.vo.OjProblemVo;


/**
 * 题目 服务层。
 *
 * @author zhang.rx
 * @since 2024/3/20
 */
public interface OjProblemService extends IService<OjProblem> {

    /**
     * 添加题目
     * @param req 题目添加请求
     * @return 是否添加成功
     */
    boolean saveProblem(OjProblemAddRequest req);

    /**
     * 分页查询题目
     *
     * @param page 分页参数
     * @param req 查询条件
     * @return 分页结果
     */
    Page<OjProblemPageVo> page(Page<OjProblem> page, OjProblemQueryRequest req);

    /**
     * 获取题目的提交数
     *
     * @param problemId 题目ID
     * @return 提交数
     */
    Integer getSubmitNum(Long problemId);

    /**
     * 获取题目的通过数
     *
     * @param problemId 题目ID
     * @return 通过数
     */
    Integer getAcceptedNum(Long problemId);

    /**
     * 更新题目的提交数和通过数
     *
     * @param problemId 题目ID
     * @param submitNum 提交数
     * @param acceptedNum 通过数
     */
    void updateSubmitAndAcceptedNum(Long problemId, Integer submitNum, Integer acceptedNum);

    // 根据 登录id 查看已做题目。  Luo
    Page<OjProblemVo> findListById(Paging page);

}