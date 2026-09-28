package com.codeneticx.nihaobackend.dto.response;

import com.codeneticx.nihaobackend.dto.mobile.UserProgressDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MobileAuthResponse {
    private String token;
    private String refreshToken;
    private String userId;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private String status;
    private long expiresIn;
    private UserProgressDto progress;
}
