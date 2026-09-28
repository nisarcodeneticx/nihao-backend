package com.codeneticx.nihaobackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyResponse {
    private String id;
    private String hanzi;
    private String pinyin;
    private int tone;
    private String urduTranslation;
    private String romanUrdu;
    private String literalGloss;
    private String partOfSpeech;
    private int hskLevel;
    private List<String> topics;
    private int frequency;
    private Integer strokeCount;
    private String radical;
    private String audioMaleUrl;
    private String audioFemaleUrl;
    private String illustrationUrl;
    private List<ExampleResponse> examples;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
