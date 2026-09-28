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
public class UnitSummaryDto {
    private String id;
    private int unitNumber;
    private String urduTitle;
    private String hanziTitle;
    private LocalizedText grammarPoint;
    private String hskLevel;
    private List<String> topics;
    private int estimatedTime;
    private String difficulty;
    private List<String> learningObjectives;
    private int lessonCount;
    private int wordCount;
}
