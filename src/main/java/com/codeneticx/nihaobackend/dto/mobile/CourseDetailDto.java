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
public class CourseDetailDto {
    private String id;
    private LocalizedText title;
    private LocalizedText description;
    private String level;
    private int totalUnits;
    private int totalWords;
    private boolean premium;
    private float progress;
    private String version;
    private List<UnitSummaryDto> units;
}
