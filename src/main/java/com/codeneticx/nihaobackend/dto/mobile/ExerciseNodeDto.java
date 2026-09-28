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
public class ExerciseNodeDto {
    private String id;
    private String type;
    private LocalizedText prompt;
    private List<String> items;
    private List<String> options;
    private String answer;
    private List<LocalizedText> gloss;
    private String audioId;
    private LocalizedText hint;
    private String pinyin;
}
