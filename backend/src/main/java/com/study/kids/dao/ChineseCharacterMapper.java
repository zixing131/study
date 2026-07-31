package com.study.kids.dao;

import com.study.kids.entity.ChineseCharacter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChineseCharacterMapper {
    List<ChineseCharacter> findAll();

    ChineseCharacter findById(@Param("id") Long id);

    int insert(ChineseCharacter entity);

    int update(ChineseCharacter entity);

    int deleteById(@Param("id") Long id);
}
