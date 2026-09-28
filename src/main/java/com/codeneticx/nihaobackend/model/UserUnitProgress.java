package com.codeneticx.nihaobackend.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_unit_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "unit_id"})
)
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUnitProgress {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @Column(nullable = false, length = 20)
    private String state;

    @Column(name = "is_unlocked", nullable = false)
    private boolean unlocked;

    @Column(name = "completed_lessons", nullable = false)
    private int completedLessons;

    @Column(name = "completed_items", nullable = false)
    private int completedItems;

    @Column(nullable = false)
    private int crowns;

    @Column(name = "checkpoint_completed", nullable = false)
    private boolean checkpointCompleted;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
