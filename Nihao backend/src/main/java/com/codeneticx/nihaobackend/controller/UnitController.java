package com.codeneticx.nihaobackend.controller;

import com.codeneticx.nihaobackend.dto.request.ApiResponse;
import com.codeneticx.nihaobackend.dto.request.UnitRequest;
import com.codeneticx.nihaobackend.dto.response.UnitResponse;
import com.codeneticx.nihaobackend.service.CurrentUserService;
import com.codeneticx.nihaobackend.service.UnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;
    private final CurrentUserService currentUserService;

    @GetMapping("/course/{courseId}")
    public ApiResponse<List<UnitResponse>> getByCourse(@PathVariable String courseId) {
        return ApiResponse.ok(unitService.getByCourse(courseId));
    }

    @GetMapping("/{id}")
    public ApiResponse<UnitResponse> getById(@PathVariable String id) {
        return ApiResponse.ok(unitService.getById(id));
    }

    @PostMapping("/course/{courseId}")
    public ApiResponse<UnitResponse> create(@PathVariable String courseId, @Valid @RequestBody UnitRequest request) {
        return ApiResponse.ok("Unit created", unitService.create(courseId, request, currentUserService.getCurrentUserId()));
    }

    @PutMapping("/{id}")
    public ApiResponse<UnitResponse> update(@PathVariable String id, @Valid @RequestBody UnitRequest request) {
        return ApiResponse.ok("Unit updated", unitService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        unitService.delete(id);
        return ApiResponse.ok("Unit deleted", null);
    }

    @PatchMapping("/reorder")
    public ApiResponse<List<UnitResponse>> reorder(@RequestBody List<String> unitIds) {
        return ApiResponse.ok("Units reordered", unitService.reorder(unitIds));
    }
}
