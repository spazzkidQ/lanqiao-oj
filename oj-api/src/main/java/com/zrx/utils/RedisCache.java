package com.zrx.utils;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisCache {
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存题目标签
     * @param tags 标签列表
     */
    public void cacheProblemTags(List<String> tags) {
        try {
            String key = "problem:tags";
            String value = JSON.toJSONString(tags);
            // 设置1小时过期
            redisTemplate.opsForValue().set(key, value, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("缓存题目标签失败", e);
        }
    }

    /**
     * 获取缓存的题目标签
     * @return 标签列表
     */
    public List<String> getProblemTags() {
        try {
            String key = "problem:tags";
            String value = (String) redisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(value)) {
                return JSONArray.parseArray(value, String.class);
            }
        } catch (Exception e) {
            log.error("获取缓存的题目标签失败", e);
        }
        return null;
    }

    /**
     * 删除题目标签缓存
     */
    public void deleteProblemTags() {
        try {
            String key = "problem:tags";
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("删除题目标签缓存失败", e);
        }
    }
} 