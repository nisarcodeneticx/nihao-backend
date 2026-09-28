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
@Table(name = "lessons")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @Column(name = "lesson_number", nullable = false)
    private Integer lessonNumber;

    @Column(name = "lesson_type", nullable = false, length = 20)
    private String lessonType;

    @Column(name = "urdu_title", nullable = false, length = 100)
    private String urduTitle;

    @Column(name = "instruction_text", columnDefinition = "TEXT")
    private String instructionText;

    @Column(length = 20)
    private String difficulty;

    @Column(name = "crowns")
    private Integer crowns;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "exercises_count")
    private Integer exercisesCount;

    @Column(name = "words_count")
    private Integer wordsCount;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Exercise> exercises = new ArrayList<>();
}
