package com.zrx.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.zrx.enums.ProblemDifficultyEnum;
import com.zrx.exception.BusinessException;
import com.zrx.mapper.OjProblemMapper;
import com.zrx.model.dto.problem.OjProblemAddRequest;
import com.zrx.model.dto.problem.OjProblemQueryRequest;
import com.zrx.model.entity.OjProblem;
import com.zrx.model.vo.OjProblemPageVo;
import com.zrx.service.OjProblemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目 服务实现类。
 *
 * @author zhang.rx
 * @since 2024/3/20
 */
@Slf4j
@Service
public class OjProblemServiceImpl extends ServiceImpl<OjProblemMapper, OjProblem> implements OjProblemService {

    @Autowired
    private OjProblemMapper ojProblemMapper;

    @Override
    public boolean saveProblem(OjProblemAddRequest req) {
        // 参数校验
        if (req == null) {
            throw new BusinessException("请求参数不能为空");
        }
        if (StrUtil.isBlank(req.getTitle())) {
            throw new BusinessException("题目标题不能为空");
        }

        // 检查题目是否已存在
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("title = ?", req.getTitle())
                .and("del_flag = ?", 0);
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException("该题目已存在");
        }

        if (StrUtil.isBlank(req.getContent())) {
            throw new BusinessException("题目内容不能为空");
        }
        if (req.getDifficulty() == null) {
            throw new BusinessException("题目难度不能为空");
        }

        // 将请求体中的数据转换为 OjProblem 实体对象
        OjProblem ojProblem = new OjProblem();
        ojProblem.setTitle(req.getTitle());
        ojProblem.setContent(req.getContent());
        ojProblem.setDifficulty(req.getDifficulty());

        // 使用 JSONUtil 进行正确的 JSON 转换
        if (CollUtil.isNotEmpty(req.getTags())) {
            ojProblem.setTags(JSONUtil.toJsonStr(req.getTags()));
        }
        ojProblem.setAnsLanguage(req.getAnsLanguage());
        ojProblem.setAnswer(req.getAnswer());
        if (CollUtil.isNotEmpty(req.getJudgeCase())) {
            ojProblem.setJudgeCase(JSONUtil.toJsonStr(req.getJudgeCase()));
        }
        if (ObjectUtil.isNotEmpty(req.getJudgeConfig())) {
            ojProblem.setJudgeConfig(JSONUtil.toJsonStr(req.getJudgeConfig()));
        }

        // 使用 mybatis-flex 提供的 save 方法进行保存
        return this.save(ojProblem);
    }

    @Override
    public Page<OjProblemPageVo> page(Page<OjProblem> page, OjProblemQueryRequest req) {
        // 构建查询条件
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(
                        "id", "title", "content", "tags", "difficulty",
                        "thumb_num", "favour_num"
                )
                .where("del_flag = ?", 0); // 只查询未删除的题目

        // 执行分页查询
        Page<OjProblem> problemPage = this.page(page, queryWrapper);

        // 转换为VO对象
        List<OjProblemPageVo> voList = problemPage.getRecords().stream()
                .map(problem -> {
                    OjProblemPageVo vo = new OjProblemPageVo();
                    vo.setId(problem.getId());
                    vo.setTitle(problem.getTitle());
                    vo.setContent(problem.getContent());

                    // 转换难度为文字描述
                    ProblemDifficultyEnum difficultyEnum = ProblemDifficultyEnum.getEnum(problem.getDifficulty());
                    String difficultyText = difficultyEnum != null ? difficultyEnum.getName() : "简单";
                    vo.setDifficulty(difficultyText);
                    // 处理标签
                    if (StrUtil.isNotBlank(problem.getTags())) {
                        try {
                            vo.setTags(JSONUtil.toList(problem.getTags(), String.class));
                        } catch (Exception e) {
                            vo.setTags(new ArrayList<>());
                            log.error("解析题目标签 JSON 失败, problemId: {}", problem.getId(), e);
                        }
                    } else {
                        vo.setTags(new ArrayList<>());
                    }

                    // 获取提交数和通过数
                    Integer submitNum = ojProblemMapper.getSubmitNum(problem.getId());
                    Integer acceptedNum = ojProblemMapper.getAcceptedNum(problem.getId());

                    // 更新题目的提交数和通过数
                    ojProblemMapper.updateSubmitAndAcceptedNum(problem.getId(), submitNum, acceptedNum);

                    vo.setSubmitNum(submitNum);
                    vo.setAcceptedNum(acceptedNum);
                    vo.setThumbNum(problem.getThumbNum());
                    vo.setFavourNum(problem.getFavourNum());

                    return vo;
                })
                .collect(Collectors.toList());

        // 构建新的分页对象
        Page<OjProblemPageVo> voPage = new Page<>();
        voPage.setRecords(voList);
        voPage.setPageNumber(problemPage.getPageNumber());
        voPage.setPageSize(problemPage.getPageSize());
        voPage.setTotalRow(problemPage.getTotalRow());

        return voPage;
    }

    @Override
    public Integer getSubmitNum(Long problemId) {
        return ojProblemMapper.getSubmitNum(problemId);
    }

    @Override
    public Integer getAcceptedNum(Long problemId) {
        return ojProblemMapper.getAcceptedNum(problemId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSubmitAndAcceptedNum(Long problemId, Integer submitNum, Integer acceptedNum) {
        ojProblemMapper.updateSubmitAndAcceptedNum(problemId, submitNum, acceptedNum);
    }
}