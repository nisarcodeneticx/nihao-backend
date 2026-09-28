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
public class UnitResponse {
    private String id;
    private int unitNumber;
    private String urduTitle;
    private String hanziTitle;
    private String grammarPoint;
    private String hskLevel;
    private List<String> topics;
    private int estimatedTime;
    private String difficulty;
    private List<String> learningObjectives;
    private String status;
    private int version;
    private LocalDateTime publishedAt;
    private String createdBy;
    private List<LessonResponse> lessons;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
