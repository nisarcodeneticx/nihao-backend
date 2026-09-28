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
public class VocabularyItemDetailDto {
    private String id;
    private String hanzi;
    private String pinyin;
    private String urdu;
    private String romanUrdu;
    private String glossUr;
    private AudioIdsDto audioIds;
    private String illustration;
    private List<ExampleSentenceDto> exampleSentences;
    private String hskTag;
    private List<String> topicTags;
}
