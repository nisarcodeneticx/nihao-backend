package com.codeneticx.nihaobackend.controller;

import com.codeneticx.nihaobackend.dto.mobile.*;
import com.codeneticx.nihaobackend.dto.request.ApiResponse;
import com.codeneticx.nihaobackend.service.CurrentUserService;
import com.codeneticx.nihaobackend.service.MobileApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class V1Controller {

    private final MobileApiService mobileApiService;
    private final CurrentUserService currentUserService;

    @GetMapping("/courses")
    public ApiResponse<List<CourseListItemDto>> getCourses() {
        String userId = currentUserService.getCurrentUserId();
        return ApiResponse.ok("Courses fetched successfully", mobileApiService.getCourses(userId));
    }

    @GetMapping("/courses/{courseId}")
    public ApiResponse<CourseDetailDto> getCourseDetail(@PathVariable String courseId) {
        String userId = currentUserService.getCurrentUserId();
        return ApiResponse.ok("Course detail fetched successfully", mobileApiService.getCourseDetail(userId, courseId));
    }

    @GetMapping("/courses/{courseId}/progress")
    public ApiResponse<CourseProgressDto> getCourseProgress(@PathVariable String courseId) {
        String userId = currentUserService.getCurrentUserId();
        return ApiResponse.ok("Course progress fetched successfully", mobileApiService.getCourseProgress(userId, courseId));
    }

    @GetMapping("/league")
    public ApiResponse<LeagueDto> getLeague() {
        String userId = currentUserService.getCurrentUserId();
        return ApiResponse.ok("League fetched successfully", mobileApiService.getLeague(userId));
    }

    @GetMapping("/courses/{courseId}/units")
    public ApiResponse<List<UnitJourneyNodeDto>> getCourseUnits(
            @PathVariable String courseId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        String userId = currentUserService.getCurrentUserId();
        return ApiResponse.ok("Units fetched successfully", mobileApiService.getCourseUnits(userId, courseId, limit, offset));
    }

    @GetMapping("/units/{unitId}")
    public ApiResponse<UnitDetailDto> getUnitDetail(@PathVariable String unitId) {
        String userId = currentUserService.getCurrentUserId();
        return ApiResponse.ok("Unit detail fetched successfully", mobileApiService.getUnitDetail(userId, unitId));
    }

    @GetMapping("/units/{unitId}/lessons")
    public ApiResponse<List<LessonJourneyNodeDto>> getUnitLessons(@PathVariable String unitId) {
        String userId = currentUserService.getCurrentUserId();
        return ApiResponse.ok("Lessons fetched successfully", mobileApiService.getUnitLessons(userId, unitId));
    }

    @GetMapping("/lessons/{lessonId}/exercises")
    public ApiResponse<List<ExerciseNodeDto>> getLessonExercises(@PathVariable String lessonId) {
        return ApiResponse.ok("Exercises fetched successfully", mobileApiService.getLessonExercises(lessonId));
    }

    @GetMapping("/items/{itemId}")
    public ApiResponse<VocabularyItemDetailDto> getItemDetail(@PathVariable String itemId) {
        return ApiResponse.ok("Item details fetched successfully", mobileApiService.getItemDetail(itemId));
    }
}
