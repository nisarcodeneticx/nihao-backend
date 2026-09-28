package com.codeneticx.nihaobackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class UnitRequest {
    private Integer unitNumber;

    @NotBlank
    private String urduTitle;

    @NotBlank
    private String hanziTitle;

    private String grammarPoint;
    private String hskLevel;
    private List<String> topics;
    private Integer estimatedTime;
    private String difficulty;
    private List<String> learningObjectives;
    private String status;
    private Integer version;
}
