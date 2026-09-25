package com.codeneticx.nihaobackend.repository;

import com.codeneticx.nihaobackend.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, String> {
    List<Course> findByStatusOrderByCreatedAtAsc(String status);

    @Query("SELECT c FROM Course c WHERE " +
            "(:search IS NULL OR :search = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:status IS NULL OR :status = '' OR c.status = :status)")
    Page<Course> searchCourses(@Param("search") String search, @Param("status") String status, Pageable pageable);
}
