package com.study.kids.service;

import com.study.kids.dao.EnglishWordMapper;
import com.study.kids.entity.EnglishWord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnglishWordService {

    private final EnglishWordMapper mapper;

    public List<EnglishWord> listAll() {
        return mapper.findAll();
    }

    public List<EnglishWord> listByCategory(String category) {
        return mapper.findByCategory(category);
    }

    public EnglishWord getById(Long id) {
        return mapper.findById(id);
    }

    public EnglishWord create(EnglishWord entity) {
        if (entity.getSortOrder() == null) {
            entity.setSortOrder(0);
        }
        if (entity.getPhonetic() == null) {
            entity.setPhonetic("");
        }
        if (entity.getExample() == null) {
            entity.setExample("");
        }
        if (entity.getEmoji() == null) {
            entity.setEmoji("");
        }
        mapper.insert(entity);
        return entity;
    }

    public boolean update(EnglishWord entity) {
        return mapper.update(entity) > 0;
    }

    public boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }
}
