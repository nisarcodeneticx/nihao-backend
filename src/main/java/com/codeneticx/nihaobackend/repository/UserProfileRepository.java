package com.codeneticx.nihaobackend.repository;

import com.codeneticx.nihaobackend.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {

    @Query("""
            select p from UserProfile p
            join fetch p.user u
            where u.status = 'ACTIVE'
              and upper(u.role) = 'STUDENT'
              and lower(u.email) <> 'student@nihao-urdu.com'
            order by p.totalXp desc, p.completedLessons desc, u.createdAt asc
            """)
    List<UserProfile> findLeagueStandings();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UserProfile p set p.currentCourseId = null, p.currentUnitNumber = null, p.currentLessonNumber = null where p.currentCourseId = :courseId")
    int clearCurrentCourse(@Param("courseId") String courseId);
}
