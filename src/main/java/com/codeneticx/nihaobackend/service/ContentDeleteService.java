package com.codeneticx.nihaobackend.service;

import com.codeneticx.nihaobackend.exception.ResourceNotFoundException;
import com.codeneticx.nihaobackend.model.Lesson;
import com.codeneticx.nihaobackend.model.Unit;
import com.codeneticx.nihaobackend.repository.CourseRepository;
import com.codeneticx.nihaobackend.repository.ExerciseRepository;
import com.codeneticx.nihaobackend.repository.LessonRepository;
import com.codeneticx.nihaobackend.repository.UnitRepository;
import com.codeneticx.nihaobackend.repository.UserCourseProgressRepository;
import com.codeneticx.nihaobackend.repository.UserLessonProgressRepository;
import com.codeneticx.nihaobackend.repository.UserProfileRepository;
import com.codeneticx.nihaobackend.repository.UserUnitProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentDeleteService {

    private final CourseRepository courseRepository;
    private final UnitRepository unitRepository;
    private final LessonRepository lessonRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserCourseProgressRepository userCourseProgressRepository;
    private final UserUnitProgressRepository userUnitProgressRepository;
    private final UserLessonProgressRepository userLessonProgressRepository;
    private final UserProfileRepository userProfileRepository;

    @Transactional
    public void deleteCourse(String courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found: " + courseId);
        }
        List<Unit> units = unitRepository.findByCourseIdOrderByUnitNumberAsc(courseId);
        for (Unit unit : units) {
            deleteUnitInternal(unit);
        }
        userCourseProgressRepository.deleteByCourse_Id(courseId);
        userProfileRepository.clearCurrentCourse(courseId);
        courseRepository.deleteById(courseId);
    }

    @Transactional
    public void deleteUnit(String unitId) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + unitId));
        deleteUnitInternal(unit);
    }

    @Transactional
    public void deleteLesson(String lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));
        deleteLessonInternal(lesson);
    }

    private void deleteUnitInternal(Unit unit) {
        List<Lesson> lessons = lessonRepository.findByUnitIdOrderByLessonNumberAsc(unit.getId());
        for (Lesson lesson : lessons) {
            deleteLessonInternal(lesson);
        }
        userUnitProgressRepository.deleteByUnit_Id(unit.getId());
        if (unit.getTopics() != null) {
            unit.getTopics().clear();
        }
        if (unit.getObjectives() != null) {
            unit.getObjectives().clear();
        }
        unitRepository.save(unit);
        unitRepository.delete(unit);
    }

    private void deleteLessonInternal(Lesson lesson) {
        exerciseRepository.deleteByLessonId(lesson.getId());
        userLessonProgressRepository.deleteByLesson_Id(lesson.getId());
        lessonRepository.delete(lesson);
    }
}
