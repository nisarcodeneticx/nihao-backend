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
public class CourseProgressDto {
    private int xp;
    private int level;
    private int streak;
    private int completedUnits;
    private int totalUnits;
    private int completedWords;
    private int totalWords;
    private int totalCrowns;
}
