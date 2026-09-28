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
public class VocabularyListItemDto {
    private String id;
    private String hanzi;
    private String pinyin;
    private int tone;
    private String urduTranslation;
    private String romanUrdu;
    private String partOfSpeech;
    private int hskLevel;
    private List<String> topics;
    private List<ExampleDto> examples;
    private String audioMaleUrl;
    private String audioFemaleUrl;
    private String illustrationUrl;
}
