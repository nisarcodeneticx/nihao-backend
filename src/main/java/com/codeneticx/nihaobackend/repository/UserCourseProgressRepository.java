package com.codeneticx.nihaobackend.repository;

import com.codeneticx.nihaobackend.model.UserCourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCourseProgressRepository extends JpaRepository<UserCourseProgress, String> {
    Optional<UserCourseProgress> findByUserIdAndCourseId(String userId, String courseId);

    List<UserCourseProgress> findByUserIdOrderByStartedAtAsc(String userId);

    void deleteByCourse_Id(String courseId);
}
