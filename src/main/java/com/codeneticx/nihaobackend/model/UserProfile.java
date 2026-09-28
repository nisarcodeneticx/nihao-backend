package com.codeneticx.nihaobackend.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @Column(name = "user_id", length = 36)
    private String userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "total_xp", nullable = false)
    private int totalXp;

    @Column(nullable = false)
    private int level;

    @Column(name = "streak_days", nullable = false)
    private int streakDays;

    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;

    @Column(name = "total_crowns", nullable = false)
    private int totalCrowns;

    @Column(name = "words_learned", nullable = false)
    private int wordsLearned;

    @Column(name = "completed_lessons", nullable = false)
    private int completedLessons;

    @Column(name = "current_course_id", length = 36)
    private String currentCourseId;

    @Column(name = "current_unit_number")
    private Integer currentUnitNumber;

    @Column(name = "current_lesson_number")
    private Integer currentLessonNumber;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
