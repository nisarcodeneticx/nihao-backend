package com.codeneticx.nihaobackend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CompleteLessonRequest {
    @Min(0)
    @Max(3)
    private Integer earnedCrowns = 1;

    @Min(0)
    private Integer xpEarned = 10;

    private Boolean perfect = false;
}
