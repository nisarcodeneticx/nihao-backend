package com.codeneticx.nihaobackend.repository;

import com.codeneticx.nihaobackend.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, String> {
    List<Lesson> findByUnitIdAndStatusOrderByLessonNumberAsc(String unitId, String status);

    Optional<Lesson> findByIdAndStatus(String id, String status);

    long countByUnitIdAndStatus(String unitId, String status);

    List<Lesson> findByUnitIdOrderByLessonNumberAsc(String unitId);
}
