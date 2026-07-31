package com.study.kids.controller;

import com.study.kids.common.ApiResponse;
import com.study.kids.entity.EnglishWord;
import com.study.kids.service.EnglishWordService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EnglishController {

    private final EnglishWordService service;

    @GetMapping("/api/english")
    public ApiResponse<List<EnglishWord>> listForUser(@RequestParam(required = false) String category) {
        if (category == null || category.isBlank()) {
            return ApiResponse.ok(service.listAll());
        }
        return ApiResponse.ok(service.listByCategory(category));
    }

    @GetMapping("/api/english/{id}")
    public ApiResponse<EnglishWord> detail(@PathVariable Long id) {
        EnglishWord item = service.getById(id);
        if (item == null) {
            return ApiResponse.fail("单词不存在");
        }
        return ApiResponse.ok(item);
    }

    @GetMapping("/api/admin/english")
    public ApiResponse<List<EnglishWord>> listForAdmin(@RequestParam(required = false) String category) {
        if (category == null || category.isBlank()) {
            return ApiResponse.ok(service.listAll());
        }
        return ApiResponse.ok(service.listByCategory(category));
    }

    @PostMapping("/api/admin/english")
    public ApiResponse<EnglishWord> create(@Valid @RequestBody EnglishRequest request) {
        return ApiResponse.ok(service.create(toEntity(request, null)));
    }

    @PutMapping("/api/admin/english/{id}")
    public ApiResponse<EnglishWord> update(@PathVariable Long id, @Valid @RequestBody EnglishRequest request) {
        EnglishWord entity = toEntity(request, id);
        if (!service.update(entity)) {
            return ApiResponse.fail("更新失败");
        }
        return ApiResponse.ok(service.getById(id));
    }

    @DeleteMapping("/api/admin/english/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (!service.delete(id)) {
            return ApiResponse.fail("删除失败");
        }
        return ApiResponse.ok();
    }

    private EnglishWord toEntity(EnglishRequest request, Long id) {
        EnglishWord word = new EnglishWord();
        word.setId(id);
        word.setWord(request.getWord());
        word.setPhonetic(request.getPhonetic());
        word.setMeaning(request.getMeaning());
        word.setCategory(request.getCategory());
        word.setExample(request.getExample());
        word.setEmoji(request.getEmoji());
        word.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        return word;
    }

    @Data
    public static class EnglishRequest {
        @NotBlank
        private String word;
        private String phonetic;
        @NotBlank
        private String meaning;
        @NotBlank
        private String category;
        private String example;
        private String emoji;
        private Integer sortOrder;
    }
}
