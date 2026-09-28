package com.codeneticx.nihaobackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {
    private String id;
    private String name;
    private String hskLevel;
    private String description;
    private String difficulty;
    private String coverImageUrl;
    private String status;
    private int units;
    private int vocabulary;
    private List<UnitResponse> unitsList;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
}
