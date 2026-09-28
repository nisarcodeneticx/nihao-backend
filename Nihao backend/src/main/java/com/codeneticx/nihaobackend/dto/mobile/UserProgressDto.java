package com.codeneticx.nihaobackend.dto.mobile;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProgressDto {
    private int currentUnit;
    private int currentLesson;
    private int totalXp;
    private int streak;
    private int crowns;
    private int wordsLearned;
    private int completedLessons;
    private int totalLessons;
}
