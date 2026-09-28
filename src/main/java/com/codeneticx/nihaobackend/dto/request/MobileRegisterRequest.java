package com.codeneticx.nihaobackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MobileRegisterRequest {
    @NotBlank
    @Size(max = 100)
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 72)
    private String password;

    private String deviceId;
    private String deviceName;
    private String appVersion;
}
