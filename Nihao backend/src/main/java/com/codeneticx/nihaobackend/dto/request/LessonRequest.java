package com.codeneticx.nihaobackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LessonRequest {
    private Integer lessonNumber;

    @NotBlank
    private String lessonType;

    @NotBlank
    private String urduTitle;

    private String instructionText;
    private String difficulty;
    private Integer crowns;
    private String status;
    private Integer exercisesCount;
    private Integer wordsCount;
}
