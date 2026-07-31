package com.study.kids.service;

import com.study.kids.common.JsonLines;
import com.study.kids.dao.PoemMapper;
import com.study.kids.entity.Poem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PoemService {

    private final PoemMapper mapper;

    public List<Poem> listAll() {
        List<Poem> list = mapper.findAll();
        list.forEach(this::fillLines);
        return list;
    }

    public Poem getById(Long id) {
        Poem poem = mapper.findById(id);
        fillLines(poem);
        return poem;
    }

    public Poem create(Poem entity) {
        normalizeLines(entity);
        if (entity.getSortOrder() == null) {
            entity.setSortOrder(0);
        }
        if (entity.getDynasty() == null) {
            entity.setDynasty("");
        }
        mapper.insert(entity);
        fillLines(entity);
        return entity;
    }

    public boolean update(Poem entity) {
        normalizeLines(entity);
        boolean ok = mapper.update(entity) > 0;
        fillLines(entity);
        return ok;
    }

    public boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }

    private void fillLines(Poem poem) {
        if (poem == null) {
            return;
        }
        poem.setLines(JsonLines.parse(poem.getLinesJson()));
    }

    private void normalizeLines(Poem poem) {
        if (poem.getLines() != null) {
            poem.setLinesJson(JsonLines.stringify(poem.getLines()));
        } else if (poem.getLinesJson() == null) {
            poem.setLinesJson("[]");
        }
    }
}
