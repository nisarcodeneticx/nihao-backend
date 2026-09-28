package com.codeneticx.nihaobackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleAuthRequest {
    @NotBlank
    private String idToken;

    private String deviceId;
    private String deviceName;
    private String appVersion;
}
