package com.codeneticx.nihaobackend.service;

import com.codeneticx.nihaobackend.dto.request.LessonRequest;
import com.codeneticx.nihaobackend.dto.response.LessonResponse;
import com.codeneticx.nihaobackend.exception.ResourceNotFoundException;
import com.codeneticx.nihaobackend.mapper.AdminMapper;
import com.codeneticx.nihaobackend.model.Lesson;
import com.codeneticx.nihaobackend.model.Unit;
import com.codeneticx.nihaobackend.repository.LessonRepository;
import com.codeneticx.nihaobackend.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final UnitRepository unitRepository;
    private final ContentDeleteService contentDeleteService;
    private final AdminMapper adminMapper;

    @Transactional(readOnly = true)
    public List<LessonResponse> getByUnit(String unitId) {
        if (!unitRepository.existsById(unitId)) {
            return List.of();
        }
        return lessonRepository.findByUnitIdOrderByLessonNumberAsc(unitId).stream()
                .map(lesson -> adminMapper.toLessonResponse(lesson, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public LessonResponse getById(String id) {
        Lesson lesson = findLesson(id);
        return adminMapper.toLessonResponse(lesson, true);
    }

    @Transactional
    public LessonResponse create(String unitId, LessonRequest request, String userId) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + unitId));

        int lessonNumber = request.getLessonNumber() != null
                ? request.getLessonNumber()
                : (int) lessonRepository.findByUnitIdOrderByLessonNumberAsc(unitId).size() + 1;

        Lesson lesson = Lesson.builder()
                .id(UUID.randomUUID().toString())
                .unit(unit)
                .lessonNumber(lessonNumber)
                .lessonType(normalizeLessonType(request.getLessonType()))
                .urduTitle(request.getUrduTitle())
                .instructionText(defaultString(request.getInstructionText(), ""))
                .difficulty(defaultString(request.getDifficulty(), "Easy"))
                .crowns(defaultInt(request.getCrowns(), 3))
                .status(defaultString(request.getStatus(), "PUBLISHED").toUpperCase())
                .exercisesCount(defaultInt(request.getExercisesCount(), 0))
                .wordsCount(defaultInt(request.getWordsCount(), 0))
                .createdBy(userId)
                .build();

        lessonRepository.save(lesson);
        return adminMapper.toLessonResponse(lesson, false);
    }

    @Transactional
    public LessonResponse update(String id, LessonRequest request) {
        Lesson lesson = findLesson(id);
        if (request.getLessonNumber() != null) {
            lesson.setLessonNumber(request.getLessonNumber());
        }
        lesson.setLessonType(normalizeLessonType(request.getLessonType()));
        lesson.setUrduTitle(request.getUrduTitle());
        if (request.getInstructionText() != null) {
            lesson.setInstructionText(request.getInstructionText());
        }
        if (request.getDifficulty() != null) {
            lesson.setDifficulty(request.getDifficulty());
        }
        if (request.getCrowns() != null) {
            lesson.setCrowns(request.getCrowns());
        }
        if (request.getStatus() != null) {
            lesson.setStatus(request.getStatus());
        }
        if (request.getExercisesCount() != null) {
            lesson.setExercisesCount(request.getExercisesCount());
        }
        if (request.getWordsCount() != null) {
            lesson.setWordsCount(request.getWordsCount());
        }
        lessonRepository.save(lesson);
        return adminMapper.toLessonResponse(lesson, false);
    }

    @Transactional
    public void delete(String id) {
        contentDeleteService.deleteLesson(id);
    }

    private void ensureUnitExists(String unitId) {
        if (!unitRepository.existsById(unitId)) {
            throw new ResourceNotFoundException("Unit not found: " + unitId);
        }
    }

    private Lesson findLesson(String id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + id));
    }

    private String normalizeLessonType(String lessonType) {
        if (lessonType == null) {
            return "NORMAL";
        }
        return switch (lessonType.toUpperCase()) {
            case "CHECKPOINT" -> "CHECKPOINT";
            case "REVIEW" -> "REVIEW";
            default -> "NORMAL";
        };
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private int defaultInt(Integer value, int fallback) {
        return value == null ? fallback : value;
    }
}
