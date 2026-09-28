package com.codeneticx.nihaobackend.repository;

import com.codeneticx.nihaobackend.model.UserLessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserLessonProgressRepository extends JpaRepository<UserLessonProgress, String> {
    Optional<UserLessonProgress> findByUserIdAndLessonId(String userId, String lessonId);

    List<UserLessonProgress> findByUserIdAndLessonIdIn(String userId, Collection<String> lessonIds);

    long countByUserIdAndCompleteTrue(String userId);

    void deleteByLesson_Id(String lessonId);
}
