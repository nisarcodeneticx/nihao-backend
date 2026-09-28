package com.codeneticx.nihaobackend.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseResponse {
    private String id;
    private String exerciseType;
    private JsonNode exerciseData;
    private int exerciseOrder;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
