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
public class UnitDetailDto {
    private String id;
    private LocalizedText title;
    private String hanziTitle;
    private LocalizedText grammarPoint;
    private int totalLessons;
    private int totalItems;
    private boolean premium;
    private String packVersion;
    private List<LessonBriefDto> lessons;
}
