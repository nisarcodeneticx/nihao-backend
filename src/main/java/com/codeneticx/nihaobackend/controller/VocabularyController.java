package com.codeneticx.nihaobackend.controller;

import com.codeneticx.nihaobackend.dto.request.ApiResponse;
import com.codeneticx.nihaobackend.dto.request.VocabularyRequest;
import com.codeneticx.nihaobackend.dto.response.PaginatedResponse;
import com.codeneticx.nihaobackend.dto.response.VocabularyResponse;
import com.codeneticx.nihaobackend.service.CurrentUserService;
import com.codeneticx.nihaobackend.service.VocabularyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vocabulary")
@RequiredArgsConstructor
public class VocabularyController {

    private final VocabularyService vocabularyService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<PaginatedResponse<VocabularyResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer hskLevel) {
        return ApiResponse.ok(vocabularyService.getAll(search, hskLevel, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<VocabularyResponse> getById(@PathVariable String id) {
        return ApiResponse.ok(vocabularyService.getById(id));
    }

    @PostMapping
    public ApiResponse<VocabularyResponse> create(@Valid @RequestBody VocabularyRequest request) {
        return ApiResponse.ok("Vocabulary created", vocabularyService.create(request, currentUserService.getCurrentUserId()));
    }

    @PutMapping("/{id}")
    public ApiResponse<VocabularyResponse> update(@PathVariable String id, @Valid @RequestBody VocabularyRequest request) {
        return ApiResponse.ok("Vocabulary updated", vocabularyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        vocabularyService.delete(id);
        return ApiResponse.ok("Vocabulary deleted", null);
    }

    @PostMapping("/bulk")
    public ApiResponse<List<VocabularyResponse>> bulkImport(@Valid @RequestBody List<VocabularyRequest> requests) {
        return ApiResponse.ok("Vocabulary imported", vocabularyService.bulkImport(requests, currentUserService.getCurrentUserId()));
    }
}
