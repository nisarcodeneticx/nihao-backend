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
public class LessonJourneyNodeDto {
    private String id;
    private int index;
    private String type;
    private LocalizedText title;
    private int items;
    private int crowns;
    private int earnedCrowns;
    private boolean isComplete;
    private boolean premium;
    private String state;
}
