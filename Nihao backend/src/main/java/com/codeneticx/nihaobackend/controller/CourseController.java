package com.codeneticx.nihaobackend.controller;

import com.codeneticx.nihaobackend.dto.request.ApiResponse;
import com.codeneticx.nihaobackend.dto.request.CourseRequest;
import com.codeneticx.nihaobackend.dto.response.CourseResponse;
import com.codeneticx.nihaobackend.dto.response.PaginatedResponse;
import com.codeneticx.nihaobackend.service.CourseService;
import com.codeneticx.nihaobackend.service.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public ApiResponse<PaginatedResponse<CourseResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        return ApiResponse.ok(courseService.getAll(search, status, page, size, sortBy, sortDirection));
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseResponse> getById(@PathVariable String id) {
        return ApiResponse.ok(courseService.getById(id));
    }

    @PostMapping
    public ApiResponse<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        return ApiResponse.ok("Course created", courseService.create(request, currentUserService.getCurrentUserId()));
    }

    @PutMapping("/{id}")
    public ApiResponse<CourseResponse> update(@PathVariable String id, @Valid @RequestBody CourseRequest request) {
        return ApiResponse.ok("Course updated", courseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        courseService.delete(id);
        return ApiResponse.ok("Course deleted", null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<CourseResponse> updateStatus(@PathVariable String id, @RequestParam String status) {
        return ApiResponse.ok("Status updated", courseService.updateStatus(id, status));
    }
}
