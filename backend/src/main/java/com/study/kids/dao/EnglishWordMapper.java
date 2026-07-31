package com.study.kids.dao;

import com.study.kids.entity.EnglishWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EnglishWordMapper {
    List<EnglishWord> findAll();

    List<EnglishWord> findByCategory(@Param("category") String category);

    EnglishWord findById(@Param("id") Long id);

    int insert(EnglishWord entity);

    int update(EnglishWord entity);

    int deleteById(@Param("id") Long id);
}
