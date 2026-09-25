package com.codeneticx.nihaobackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExampleRequest {
    @NotBlank
    private String hanzi;

    @NotBlank
    private String pinyin;

    @NotBlank
    private String urdu;
}
