package com.zrx.model.vo;/*
@author 比巴卜
@date  2025/5/19 下午9:44
@Description 热力用户返回类
*/

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRankingVO {
    // 用户头像
    private String avatar;
    // 用户别名
    private String nickname;
    // 用户总提交数
    private int totalSubmissions;
    // 用户当天提交数
    private int todaySubmissions;
}
