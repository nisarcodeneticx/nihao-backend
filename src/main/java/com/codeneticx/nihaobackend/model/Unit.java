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
@Table(name = "units")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Unit {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "unit_number", nullable = false)
    private Integer unitNumber;

    @Column(name = "urdu_title", nullable = false, length = 100)
    private String urduTitle;

    @Column(name = "hanzi_title", nullable = false, length = 100)
    private String hanziTitle;

    @Column(name = "grammar_point", columnDefinition = "TEXT")
    private String grammarPoint;

    @Column(name = "hsk_level", length = 20)
    private String hskLevel;

    @Column(name = "estimated_time")
    private Integer estimatedTime;

    @Column(length = 20)
    private String difficulty;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "version")
    private Integer version;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ElementCollection
    @CollectionTable(name = "unit_topics", joinColumns = @JoinColumn(name = "unit_id"))
    @Column(name = "topic")
    @Builder.Default
    private List<String> topics = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "unit_objectives", joinColumns = @JoinColumn(name = "unit_id"))
    @Column(name = "objective", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> objectives = new ArrayList<>();

    @OneToMany(mappedBy = "unit", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Lesson> lessons = new ArrayList<>();
}
