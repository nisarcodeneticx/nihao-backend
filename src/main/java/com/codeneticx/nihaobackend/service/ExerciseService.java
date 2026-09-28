package com.codeneticx.nihaobackend.service;

import com.codeneticx.nihaobackend.dto.request.ExerciseRequest;
import com.codeneticx.nihaobackend.dto.response.ExerciseListItemResponse;
import com.codeneticx.nihaobackend.dto.response.ExerciseResponse;
import com.codeneticx.nihaobackend.dto.response.PaginatedResponse;
import com.codeneticx.nihaobackend.exception.ResourceNotFoundException;
import com.codeneticx.nihaobackend.mapper.AdminMapper;
import com.codeneticx.nihaobackend.model.Exercise;
import com.codeneticx.nihaobackend.model.Lesson;
import com.codeneticx.nihaobackend.repository.ExerciseRepository;
import com.codeneticx.nihaobackend.repository.LessonRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final LessonRepository lessonRepository;
    private final AdminMapper adminMapper;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<ExerciseResponse> getByLesson(String lessonId) {
        ensureLessonExists(lessonId);
        return exerciseRepository.findByLessonIdOrderByExerciseOrderAsc(lessonId).stream()
                .map(adminMapper::toExerciseResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<ExerciseListItemResponse> getAll(String lessonId, int page, int size) {
        List<Exercise> exercises = lessonId == null || lessonId.isBlank()
                ? exerciseRepository.findAll()
                : exerciseRepository.findByLessonIdOrderByExerciseOrderAsc(lessonId);

        exercises = exercises.stream()
                .sorted(Comparator.comparing(Exercise::getExerciseOrder))
                .toList();

        int from = Math.min(page * size, exercises.size());
        int to = Math.min(from + size, exercises.size());
        List<ExerciseListItemResponse> pageContent = exercises.subList(from, to).stream()
                .map(this::toListItem)
                .collect(Collectors.toList());

        Page<ExerciseListItemResponse> result = new PageImpl<>(
                pageContent,
                PageRequest.of(page, size),
                exercises.size()
        );
        return PaginatedResponse.from(result);
    }

    @Transactional(readOnly = true)
    public ExerciseResponse getById(String id) {
        return adminMapper.toExerciseResponse(findExercise(id));
    }

    @Transactional
    public ExerciseResponse create(String lessonId, ExerciseRequest request, String userId) {
        Lesson lesson = findLesson(lessonId);
        int order = request.getExerciseOrder() != null
                ? request.getExerciseOrder()
                : exerciseRepository.findByLessonIdOrderByExerciseOrderAsc(lessonId).size() + 1;

        Exercise exercise = Exercise.builder()
                .id(UUID.randomUUID().toString())
                .lesson(lesson)
                .exerciseType(normalizeExerciseType(request.getExerciseType()))
                .exerciseData(writeExerciseData(request.getExerciseData()))
                .exerciseOrder(order)
                .createdBy(userId)
                .build();

        exerciseRepository.save(exercise);
        refreshExerciseCount(lesson);
        return adminMapper.toExerciseResponse(exercise);
    }

    @Transactional
    public ExerciseResponse update(String id, ExerciseRequest request) {
        Exercise exercise = findExercise(id);
        exercise.setExerciseType(normalizeExerciseType(request.getExerciseType()));
        exercise.setExerciseData(writeExerciseData(request.getExerciseData()));
        if (request.getExerciseOrder() != null) {
            exercise.setExerciseOrder(request.getExerciseOrder());
        }
        exerciseRepository.save(exercise);
        refreshExerciseCount(exercise.getLesson());
        return adminMapper.toExerciseResponse(exercise);
    }

    @Transactional
    public void delete(String id) {
        Exercise exercise = findExercise(id);
        Lesson lesson = exercise.getLesson();
        exerciseRepository.delete(exercise);
        refreshExerciseCount(lesson);
    }

    private ExerciseListItemResponse toListItem(Exercise exercise) {
        Lesson lesson = exercise.getLesson();
        return ExerciseListItemResponse.builder()
                .id(exercise.getId())
                .lessonId(lesson.getId())
                .lessonTitle(lesson.getUrduTitle())
                .unitTitle(lesson.getUnit().getUrduTitle())
                .courseName(lesson.getUnit().getCourse().getName())
                .exerciseType(exercise.getExerciseType())
                .exerciseOrder(defaultInt(exercise.getExerciseOrder(), 0))
                .build();
    }

    private void refreshExerciseCount(Lesson lesson) {
        int count = exerciseRepository.findByLessonIdOrderByExerciseOrderAsc(lesson.getId()).size();
        lesson.setExercisesCount(count);
        lessonRepository.save(lesson);
    }

    private void ensureLessonExists(String lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new ResourceNotFoundException("Lesson not found: " + lessonId);
        }
    }

    private Lesson findLesson(String lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));
    }

    private Exercise findExercise(String id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found: " + id));
    }

    private String normalizeExerciseType(String exerciseType) {
        if (exerciseType == null) {
            return "TEACH_FRAME";
        }
        return switch (exerciseType.toUpperCase()) {
            case "TEACH", "TEACH_FRAME" -> "TEACH_FRAME";
            case "PIC", "PICTURE_MATCH" -> "PICTURE_MATCH";
            case "LISTEN", "LISTENING_CHOICE" -> "LISTENING_CHOICE";
            case "TONE", "TONE_DRILL" -> "TONE_DRILL";
            case "BUILD", "TAP_TO_BUILD" -> "TAP_TO_BUILD";
            case "FILL", "FILL_IN_THE_BLANK" -> "FILL_IN_THE_BLANK";
            default -> exerciseType.toUpperCase();
        };
    }

    private String writeExerciseData(com.fasterxml.jackson.databind.JsonNode data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private int defaultInt(Integer value, int fallback) {
        return value == null ? fallback : value;
    }
}
