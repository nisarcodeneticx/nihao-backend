package com.codeneticx.nihaobackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseListItemResponse {
    private String id;
    private String lessonId;
    private String lessonTitle;
    private String unitTitle;
    private String courseName;
    private String exerciseType;
    private int exerciseOrder;
}
