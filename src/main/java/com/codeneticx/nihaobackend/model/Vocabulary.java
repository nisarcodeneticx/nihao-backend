package com.codeneticx.nihaobackend.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vocabulary")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vocabulary {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 50)
    private String hanzi;

    @Column(nullable = false, length = 100)
    private String pinyin;

    @Column
    private Integer tone;

    @Column(name = "urdu_translation", nullable = false, columnDefinition = "TEXT")
    private String urduTranslation;

    @Column(name = "roman_urdu", length = 100)
    private String romanUrdu;

    @Column(name = "literal_gloss", columnDefinition = "TEXT")
    private String literalGloss;

    @Column(name = "part_of_speech", length = 50)
    private String partOfSpeech;

    @Column(name = "hsk_level")
    private Integer hskLevel;

    @Column
    private Integer frequency;

    @Column(name = "stroke_count")
    private Integer strokeCount;

    @Column(length = 50)
    private String radical;

    @Column(name = "audio_male_url")
    private String audioMaleUrl;

    @Column(name = "audio_female_url")
    private String audioFemaleUrl;

    @Column(name = "illustration_url")
    private String illustrationUrl;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ElementCollection
    @CollectionTable(name = "vocabulary_topics", joinColumns = @JoinColumn(name = "vocabulary_id"))
    @Column(name = "topic")
    @Builder.Default
    private List<String> topics = new ArrayList<>();

    @OneToMany(mappedBy = "vocabulary", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExampleSentence> exampleSentences = new ArrayList<>();
}
