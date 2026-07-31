package com.study.kids.service;

import com.study.kids.dao.ChineseCharacterMapper;
import com.study.kids.entity.ChineseCharacter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChineseCharacterService {

    private final ChineseCharacterMapper mapper;

    public List<ChineseCharacter> listAll() {
        return mapper.findAll();
    }

    public ChineseCharacter getById(Long id) {
        return mapper.findById(id);
    }

    public ChineseCharacter create(ChineseCharacter entity) {
        if (entity.getSortOrder() == null) {
            entity.setSortOrder(0);
        }
        mapper.insert(entity);
        return entity;
    }

    public boolean update(ChineseCharacter entity) {
        return mapper.update(entity) > 0;
    }

    public boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }
}
