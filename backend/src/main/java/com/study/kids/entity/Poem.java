package com.study.kids.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Poem {
    private Long id;
    private String title;
    private String author;
    private String dynasty;
    /** 数据库中的 JSON 字符串 */
    private String linesJson;
    /** 接口返回用的诗句列表 */
    private List<String> lines;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
