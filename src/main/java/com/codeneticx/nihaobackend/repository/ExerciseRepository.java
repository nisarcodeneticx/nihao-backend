package com.codeneticx.nihaobackend.repository;

import com.codeneticx.nihaobackend.model.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, String> {
    List<Exercise> findByLessonIdOrderByExerciseOrderAsc(String lessonId);

    void deleteByLessonId(String lessonId);
}
