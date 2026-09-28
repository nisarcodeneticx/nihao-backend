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
public class LessonResponse {
    private String id;
    private int lessonNumber;
    private String lessonType;
    private String urduTitle;
    private String instructionText;
    private String difficulty;
    private int crowns;
    private String status;
    private int exercisesCount;
    private int wordsCount;
    private String createdBy;
    private List<ExerciseResponse> exercises;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
