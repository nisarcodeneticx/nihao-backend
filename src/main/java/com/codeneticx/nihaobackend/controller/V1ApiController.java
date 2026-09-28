package com.codeneticx.nihaobackend.controller;

import com.codeneticx.nihaobackend.dto.mobile.CompleteLessonResultDto;
import com.codeneticx.nihaobackend.dto.mobile.LessonDetailDto;
import com.codeneticx.nihaobackend.dto.mobile.VocabularyListItemDto;
import com.codeneticx.nihaobackend.dto.request.ApiResponse;
import com.codeneticx.nihaobackend.dto.request.CompleteLessonRequest;
import com.codeneticx.nihaobackend.dto.request.GoogleAuthRequest;
import com.codeneticx.nihaobackend.dto.request.MobileLoginRequest;
import com.codeneticx.nihaobackend.dto.request.MobileRegisterRequest;
import com.codeneticx.nihaobackend.dto.response.MobileAuthResponse;
import com.codeneticx.nihaobackend.service.AuthService;
import com.codeneticx.nihaobackend.service.CurrentUserService;
import com.codeneticx.nihaobackend.service.MobileApiService;
import com.codeneticx.nihaobackend.service.UserProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api")
@RequiredArgsConstructor
public class V1ApiController {

    private final AuthService authService;
    private final MobileApiService mobileApiService;
    private final UserProgressService userProgressService;
    private final CurrentUserService currentUserService;

    @PostMapping("/login")
    public ApiResponse<MobileAuthResponse> login(@Valid @RequestBody MobileLoginRequest request) {
        return ApiResponse.ok("Login successful", authService.mobileLogin(request));
    }

    @PostMapping("/register")
    public ApiResponse<MobileAuthResponse> register(@Valid @RequestBody MobileRegisterRequest request) {
        return ApiResponse.ok("Account created", authService.mobileRegister(request));
    }

    @PostMapping("/auth/google")
    public ApiResponse<MobileAuthResponse> googleAuth(@Valid @RequestBody GoogleAuthRequest request) {
        return ApiResponse.ok("Google sign-in successful", authService.googleLogin(request));
    }

    @GetMapping("/me")
    public ApiResponse<MobileAuthResponse> me() {
        return ApiResponse.ok("Session restored", authService.currentMobileSession());
    }

    @GetMapping("/lessons/{lessonId}")
    public ApiResponse<LessonDetailDto> getLessonDetail(@PathVariable String lessonId) {
        return ApiResponse.ok("Lesson fetched successfully", mobileApiService.getLessonDetail(lessonId));
    }

    @PostMapping("/lessons/{lessonId}/complete")
    public ApiResponse<CompleteLessonResultDto> completeLesson(
            @PathVariable String lessonId,
            @Valid @RequestBody CompleteLessonRequest request) {
        String userId = currentUserService.getCurrentUserId();
        return ApiResponse.ok("Lesson completed successfully",
                userProgressService.completeLesson(userId, lessonId, request));
    }

    @GetMapping("/vocabulary")
    public ApiResponse<List<VocabularyListItemDto>> getVocabulary(
            @RequestParam(required = false) Integer hskLevel,
            @RequestParam(required = false) String search) {
        return ApiResponse.ok("Vocabulary fetched successfully", mobileApiService.getVocabulary(hskLevel, search));
    }
}
