package com.zrx.model.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


/**
 * 
 * @TableName notice_table
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "notice_table")
public class NoticeTable {
    private Integer id;
    /**
     * 类型
     */
    private String type;

    /**
     * 时间
     */
    @Column(value="datetime",onInsertValue = "CURRENT_TIMESTAMP")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date datetime;  // 改用 java.util.Date



    /**
     * 内容
     */
    private String content;

    @Override
    public String toString() {
        return "NoticeTable{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", datetime=" + datetime +
                ", content='" + content + '\'' +
                '}';
    }

}