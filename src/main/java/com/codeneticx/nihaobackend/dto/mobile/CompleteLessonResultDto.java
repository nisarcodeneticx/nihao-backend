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
public class CompleteLessonResultDto {
    private String lessonId;
    private int xpEarned;
    private int earnedCrowns;
    private int totalXp;
    private int streak;
    private int level;
    private CourseProgressDto courseProgress;
}
