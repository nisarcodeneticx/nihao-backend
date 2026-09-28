package com.codeneticx.nihaobackend.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExerciseRequest {
    @NotBlank
    private String exerciseType;

    @NotNull
    private JsonNode exerciseData;

    private Integer exerciseOrder;
}
