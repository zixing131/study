package com.study.kids.controller;

import com.study.kids.common.ApiResponse;
import com.study.kids.entity.ChineseCharacter;
import com.study.kids.service.ChineseCharacterService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CharacterController {

    private final ChineseCharacterService service;

    @GetMapping("/api/characters")
    public ApiResponse<List<ChineseCharacter>> listForUser() {
        return ApiResponse.ok(service.listAll());
    }

    @GetMapping("/api/characters/{id}")
    public ApiResponse<ChineseCharacter> detail(@PathVariable Long id) {
        ChineseCharacter item = service.getById(id);
        if (item == null) {
            return ApiResponse.fail("汉字不存在");
        }
        return ApiResponse.ok(item);
    }

    @GetMapping("/api/admin/characters")
    public ApiResponse<List<ChineseCharacter>> listForAdmin() {
        return ApiResponse.ok(service.listAll());
    }

    @PostMapping("/api/admin/characters")
    public ApiResponse<ChineseCharacter> create(@Valid @RequestBody CharacterRequest request) {
        return ApiResponse.ok(service.create(toEntity(request, null)));
    }

    @PutMapping("/api/admin/characters/{id}")
    public ApiResponse<ChineseCharacter> update(@PathVariable Long id, @Valid @RequestBody CharacterRequest request) {
        ChineseCharacter entity = toEntity(request, id);
        if (!service.update(entity)) {
            return ApiResponse.fail("更新失败");
        }
        return ApiResponse.ok(service.getById(id));
    }

    @DeleteMapping("/api/admin/characters/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (!service.delete(id)) {
            return ApiResponse.fail("删除失败");
        }
        return ApiResponse.ok();
    }

    private ChineseCharacter toEntity(CharacterRequest request, Long id) {
        ChineseCharacter entity = new ChineseCharacter();
        entity.setId(id);
        entity.setCharText(request.getCharText());
        entity.setPinyin(request.getPinyin());
        entity.setStrokeOrder(request.getStrokeOrder());
        entity.setWords(request.getWords());
        entity.setSentence(request.getSentence());
        entity.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        return entity;
    }

    @Data
    public static class CharacterRequest {
        @NotBlank
        private String charText;
        @NotBlank
        private String pinyin;
        @NotBlank
        private String strokeOrder;
        @NotBlank
        private String words;
        @NotBlank
        private String sentence;
        private Integer sortOrder;
    }
}
