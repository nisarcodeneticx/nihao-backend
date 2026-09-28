package com.codeneticx.nihaobackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MobileLoginRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    private String deviceId;
    private String deviceName;
    private String appVersion;
}
