package com.codeneticx.nihaobackend.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_course_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id"})
)
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCourseProgress {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "is_unlocked", nullable = false)
    private boolean unlocked;

    @Column(name = "progress_percent", nullable = false)
    private float progressPercent;

    @Column(name = "completed_units", nullable = false)
    private int completedUnits;

    @Column(name = "completed_words", nullable = false)
    private int completedWords;

    @Column(name = "total_crowns", nullable = false)
    private int totalCrowns;

    @CreatedDate
    @Column(name = "started_at", updatable = false)
    private LocalDateTime startedAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
