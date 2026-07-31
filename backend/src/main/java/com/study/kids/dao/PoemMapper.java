package com.study.kids.dao;

import com.study.kids.entity.Poem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PoemMapper {
    List<Poem> findAll();

    Poem findById(@Param("id") Long id);

    int insert(Poem entity);

    int update(Poem entity);

    int deleteById(@Param("id") Long id);
}
