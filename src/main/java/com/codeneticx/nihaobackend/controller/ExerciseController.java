package com.codeneticx.nihaobackend.controller;

import com.codeneticx.nihaobackend.dto.request.ApiResponse;
import com.codeneticx.nihaobackend.dto.request.ExerciseRequest;
import com.codeneticx.nihaobackend.dto.response.ExerciseListItemResponse;
import com.codeneticx.nihaobackend.dto.response.ExerciseResponse;
import com.codeneticx.nihaobackend.dto.response.PaginatedResponse;
import com.codeneticx.nihaobackend.service.CurrentUserService;
import com.codeneticx.nihaobackend.service.ExerciseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<PaginatedResponse<ExerciseListItemResponse>> getAll(
            @RequestParam(required = false) String lessonId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(exerciseService.getAll(lessonId, page, size));
    }

    @GetMapping("/lesson/{lessonId}")
    public ApiResponse<List<ExerciseResponse>> getByLesson(@PathVariable String lessonId) {
        return ApiResponse.ok(exerciseService.getByLesson(lessonId));
    }

    @GetMapping("/{id}")
    public ApiResponse<ExerciseResponse> getById(@PathVariable String id) {
        return ApiResponse.ok(exerciseService.getById(id));
    }

    @PostMapping("/lesson/{lessonId}")
    public ApiResponse<ExerciseResponse> create(
            @PathVariable String lessonId,
            @Valid @RequestBody ExerciseRequest request) {
        return ApiResponse.ok("Exercise created", exerciseService.create(lessonId, request, currentUserService.getCurrentUserId()));
    }

    @PutMapping("/{id}")
    public ApiResponse<ExerciseResponse> update(@PathVariable String id, @Valid @RequestBody ExerciseRequest request) {
        return ApiResponse.ok("Exercise updated", exerciseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        exerciseService.delete(id);
        return ApiResponse.ok("Exercise deleted", null);
    }
}
