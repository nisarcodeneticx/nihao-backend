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
public class UnitJourneyNodeDto {
    private String id;
    private int index;
    private LocalizedText title;
    private String hanziTitle;
    private String state;
    private int completedLessons;
    private int totalLessons;
    private int completedItems;
    private int totalItems;
    private int crowns;
    private boolean hasCheckpoint;
    private boolean checkpointCompleted;
    private boolean isUnlocked;
    private boolean premium;
}
