package com.codeneticx.nihaobackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExampleResponse {
    private String id;
    private String hanzi;
    private String pinyin;
    private String urdu;
    private LocalDateTime createdAt;
}
