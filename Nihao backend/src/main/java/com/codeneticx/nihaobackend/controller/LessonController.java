package com.codeneticx.nihaobackend.controller;

import com.codeneticx.nihaobackend.dto.request.ApiResponse;
import com.codeneticx.nihaobackend.dto.request.LessonRequest;
import com.codeneticx.nihaobackend.dto.response.LessonResponse;
import com.codeneticx.nihaobackend.service.CurrentUserService;
import com.codeneticx.nihaobackend.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;
    private final CurrentUserService currentUserService;

    @GetMapping("/unit/{unitId}")
    public ApiResponse<List<LessonResponse>> getByUnit(@PathVariable String unitId) {
        return ApiResponse.ok(lessonService.getByUnit(unitId));
    }

    @GetMapping("/{id}")
    public ApiResponse<LessonResponse> getById(@PathVariable String id) {
        return ApiResponse.ok(lessonService.getById(id));
    }

    @PostMapping("/unit/{unitId}")
    public ApiResponse<LessonResponse> create(@PathVariable String unitId, @Valid @RequestBody LessonRequest request) {
        return ApiResponse.ok("Lesson created", lessonService.create(unitId, request, currentUserService.getCurrentUserId()));
    }

    @PutMapping("/{id}")
    public ApiResponse<LessonResponse> update(@PathVariable String id, @Valid @RequestBody LessonRequest request) {
        return ApiResponse.ok("Lesson updated", lessonService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        lessonService.delete(id);
        return ApiResponse.ok("Lesson deleted", null);
    }
}
