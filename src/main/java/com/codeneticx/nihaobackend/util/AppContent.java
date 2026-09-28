package com.codeneticx.nihaobackend.util;

import com.codeneticx.nihaobackend.model.Course;
import com.codeneticx.nihaobackend.model.Lesson;
import com.codeneticx.nihaobackend.model.Unit;

import java.util.Comparator;
import java.util.List;

public final class AppContent {
    private AppContent() {}

    public static boolean isLive(String status) {
        return status != null && !"ARCHIVED".equalsIgnoreCase(status.trim());
    }

    public static List<Course> liveCourses(List<Course> courses) {
        return courses.stream()
                .filter(course -> isLive(course.getStatus()))
                .sorted(Comparator.comparing(Course::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public static List<Unit> liveUnits(List<Unit> units) {
        return units.stream().filter(unit -> isLive(unit.getStatus())).toList();
    }

    public static List<Lesson> liveLessons(List<Lesson> lessons) {
        return lessons.stream().filter(lesson -> isLive(lesson.getStatus())).toList();
    }
}
