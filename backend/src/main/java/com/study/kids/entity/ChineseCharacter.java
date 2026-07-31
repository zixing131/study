package com.study.kids.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChineseCharacter {
    private Long id;
    private String charText;
    private String pinyin;
    private String strokeOrder;
    private String words;
    private String sentence;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
