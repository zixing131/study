package com.study.kids.controller;

import com.study.kids.common.ApiResponse;
import com.study.kids.entity.Poem;
import com.study.kids.service.PoemService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PoemController {

    private final PoemService service;

    @GetMapping("/api/poems")
    public ApiResponse<List<Poem>> listForUser() {
        return ApiResponse.ok(service.listAll());
    }

    @GetMapping("/api/poems/{id}")
    public ApiResponse<Poem> detail(@PathVariable Long id) {
        Poem poem = service.getById(id);
        if (poem == null) {
            return ApiResponse.fail("古诗不存在");
        }
        return ApiResponse.ok(poem);
    }

    @GetMapping("/api/admin/poems")
    public ApiResponse<List<Poem>> listForAdmin() {
        return ApiResponse.ok(service.listAll());
    }

    @PostMapping("/api/admin/poems")
    public ApiResponse<Poem> create(@Valid @RequestBody PoemRequest request) {
        return ApiResponse.ok(service.create(toEntity(request, null)));
    }

    @PutMapping("/api/admin/poems/{id}")
    public ApiResponse<Poem> update(@PathVariable Long id, @Valid @RequestBody PoemRequest request) {
        Poem entity = toEntity(request, id);
        if (!service.update(entity)) {
            return ApiResponse.fail("更新失败");
        }
        return ApiResponse.ok(service.getById(id));
    }

    @DeleteMapping("/api/admin/poems/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (!service.delete(id)) {
            return ApiResponse.fail("删除失败");
        }
        return ApiResponse.ok();
    }

    private Poem toEntity(PoemRequest request, Long id) {
        Poem poem = new Poem();
        poem.setId(id);
        poem.setTitle(request.getTitle());
        poem.setAuthor(request.getAuthor());
        poem.setDynasty(request.getDynasty() == null ? "" : request.getDynasty());
        poem.setLines(request.getLines());
        poem.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        return poem;
    }

    @Data
    public static class PoemRequest {
        @NotBlank
        private String title;
        @NotBlank
        private String author;
        private String dynasty;
        @NotEmpty
        private List<String> lines;
        private Integer sortOrder;
    }
}
