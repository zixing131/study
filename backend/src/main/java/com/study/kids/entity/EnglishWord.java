package com.study.kids.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EnglishWord {
    private Long id;
    private String word;
    private String phonetic;
    private String meaning;
    /** letter / word */
    private String category;
    private String example;
    private String emoji;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
