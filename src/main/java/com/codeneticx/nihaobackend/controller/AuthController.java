package com.codeneticx.nihaobackend.controller;

import com.codeneticx.nihaobackend.dto.request.AdminLoginRequest;
import com.codeneticx.nihaobackend.dto.request.ApiResponse;
import com.codeneticx.nihaobackend.dto.response.AdminAuthResponse;
import com.codeneticx.nihaobackend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<AdminAuthResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.ok("Login successful", authService.adminLogin(request));
    }
}
