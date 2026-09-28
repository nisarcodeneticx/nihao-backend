package com.codeneticx.nihaobackend.controller;

import com.codeneticx.nihaobackend.dto.request.ApiResponse;
import com.codeneticx.nihaobackend.dto.response.PaginatedResponse;
import com.codeneticx.nihaobackend.dto.response.UserResponse;
import com.codeneticx.nihaobackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<PaginatedResponse<UserResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        return ApiResponse.ok(userService.getAll(search, status, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getById(@PathVariable String id) {
        return ApiResponse.ok(userService.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> update(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        return ApiResponse.ok("User updated", userService.update(id, updates));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        userService.delete(id);
        return ApiResponse.ok("User deleted", null);
    }

    @PatchMapping("/{id}/ban")
    public ApiResponse<UserResponse> ban(@PathVariable String id) {
        return ApiResponse.ok("User banned", userService.ban(id));
    }

    @PatchMapping("/{id}/premium")
    public ApiResponse<UserResponse> grantPremium(@PathVariable String id) {
        return ApiResponse.ok("Premium granted", userService.grantPremium(id));
    }
}
