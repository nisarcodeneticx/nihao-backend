package com.codeneticx.nihaobackend.repository;

import com.codeneticx.nihaobackend.model.Unit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<Unit, String> {
    List<Unit> findByCourseIdAndStatusOrderByUnitNumberAsc(String courseId, String status);

    List<Unit> findByCourseIdAndStatusOrderByUnitNumberAsc(String courseId, String status, Pageable pageable);

    long countByCourseIdAndStatus(String courseId, String status);

    Optional<Unit> findByIdAndStatus(String id, String status);

    List<Unit> findByCourseIdOrderByUnitNumberAsc(String courseId);

    long countByCourseId(String courseId);
}
