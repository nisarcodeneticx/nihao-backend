package com.codeneticx.nihaobackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class VocabularyRequest {
    @NotBlank
    private String hanzi;

    @NotBlank
    private String pinyin;

    private Integer tone;

    @NotBlank
    private String urduTranslation;

    private String romanUrdu;
    private String literalGloss;
    private String partOfSpeech;
    private Integer hskLevel;
    private List<String> topics;
    private Integer frequency;
    private Integer strokeCount;
    private String radical;
    private String audioMaleUrl;
    private String audioFemaleUrl;
    private String illustrationUrl;
    private List<ExampleRequest> examples;
}
