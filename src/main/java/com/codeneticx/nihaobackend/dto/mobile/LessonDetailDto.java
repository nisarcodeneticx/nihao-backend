package com.codeneticx.nihaobackend.dto.mobile;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LessonDetailDto {
    private String id;
    private int lessonNumber;
    private String lessonType;
    private String urduTitle;
    private String instructionText;
    private String difficulty;
    private int crowns;
    private int exercisesCount;
    private int wordsCount;
    private List<LessonExerciseDto> exercises;
}
