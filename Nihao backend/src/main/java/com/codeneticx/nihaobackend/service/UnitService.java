package com.codeneticx.nihaobackend.service;

import com.codeneticx.nihaobackend.dto.request.UnitRequest;
import com.codeneticx.nihaobackend.dto.response.UnitResponse;
import com.codeneticx.nihaobackend.exception.ResourceNotFoundException;
import com.codeneticx.nihaobackend.mapper.AdminMapper;
import com.codeneticx.nihaobackend.model.Course;
import com.codeneticx.nihaobackend.model.Unit;
import com.codeneticx.nihaobackend.repository.CourseRepository;
import com.codeneticx.nihaobackend.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UnitService {

    private final UnitRepository unitRepository;
    private final CourseRepository courseRepository;
    private final ContentDeleteService contentDeleteService;
    private final AdminMapper adminMapper;

    @Transactional(readOnly = true)
    public List<UnitResponse> getByCourse(String courseId) {
        if (!courseRepository.existsById(courseId)) {
            return List.of();
        }
        List<Unit> units = unitRepository.findByCourseIdOrderByUnitNumberAsc(courseId);
        units.forEach(this::initializeCollections);
        return units.stream()
                .map(unit -> adminMapper.toUnitResponse(unit, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public UnitResponse getById(String id) {
        Unit unit = findUnit(id);
        return adminMapper.toUnitResponse(unit, true);
    }

    @Transactional
    public UnitResponse create(String courseId, UnitRequest request, String userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));

        int unitNumber = request.getUnitNumber() != null
                ? request.getUnitNumber()
                : (int) unitRepository.countByCourseId(courseId) + 1;

        Unit unit = Unit.builder()
                .id(UUID.randomUUID().toString())
                .course(course)
                .unitNumber(unitNumber)
                .urduTitle(request.getUrduTitle())
                .hanziTitle(request.getHanziTitle())
                .grammarPoint(request.getGrammarPoint())
                .hskLevel(defaultString(request.getHskLevel(), course.getHskLevel()))
                .estimatedTime(defaultInt(request.getEstimatedTime(), 20))
                .difficulty(defaultString(request.getDifficulty(), "Easy"))
                .status(defaultString(request.getStatus(), "PUBLISHED").toUpperCase())
                .version(defaultInt(request.getVersion(), 1))
                .topics(request.getTopics() != null ? new ArrayList<>(request.getTopics()) : new ArrayList<>())
                .objectives(request.getLearningObjectives() != null
                        ? new ArrayList<>(request.getLearningObjectives()) : new ArrayList<>())
                .createdBy(userId)
                .build();

        if ("PUBLISHED".equals(unit.getStatus())) {
            unit.setPublishedAt(LocalDateTime.now());
        }

        unitRepository.save(unit);
        return adminMapper.toUnitResponse(unit, false);
    }

    @Transactional
    public UnitResponse update(String id, UnitRequest request) {
        Unit unit = findUnit(id);
        if (request.getUnitNumber() != null) {
            unit.setUnitNumber(request.getUnitNumber());
        }
        unit.setUrduTitle(request.getUrduTitle());
        unit.setHanziTitle(request.getHanziTitle());
        if (request.getGrammarPoint() != null) {
            unit.setGrammarPoint(request.getGrammarPoint());
        }
        if (request.getHskLevel() != null) {
            unit.setHskLevel(request.getHskLevel());
        }
        if (request.getTopics() != null) {
            unit.setTopics(new ArrayList<>(request.getTopics()));
        }
        if (request.getEstimatedTime() != null) {
            unit.setEstimatedTime(request.getEstimatedTime());
        }
        if (request.getDifficulty() != null) {
            unit.setDifficulty(request.getDifficulty());
        }
        if (request.getLearningObjectives() != null) {
            unit.setObjectives(new ArrayList<>(request.getLearningObjectives()));
        }
        if (request.getStatus() != null) {
            unit.setStatus(request.getStatus());
            if ("PUBLISHED".equals(unit.getStatus()) && unit.getPublishedAt() == null) {
                unit.setPublishedAt(LocalDateTime.now());
            }
        }
        if (request.getVersion() != null) {
            unit.setVersion(request.getVersion());
        }
        unitRepository.save(unit);
        return adminMapper.toUnitResponse(unit, false);
    }

    @Transactional
    public void delete(String id) {
        contentDeleteService.deleteUnit(id);
    }

    @Transactional
    public List<UnitResponse> reorder(List<String> unitIds) {
        for (int i = 0; i < unitIds.size(); i++) {
            Unit unit = findUnit(unitIds.get(i));
            unit.setUnitNumber(i + 1);
            unitRepository.save(unit);
        }
        if (unitIds.isEmpty()) {
            return List.of();
        }
        Unit first = findUnit(unitIds.get(0));
        return getByCourse(first.getCourse().getId());
    }

    private void ensureCourseExists(String courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found: " + courseId);
        }
    }

    private Unit findUnit(String id) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + id));
        initializeCollections(unit);
        return unit;
    }

    private void initializeCollections(Unit unit) {
        if (unit.getTopics() != null) {
            unit.getTopics().size();
        }
        if (unit.getObjectives() != null) {
            unit.getObjectives().size();
        }
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private int defaultInt(Integer value, int fallback) {
        return value == null ? fallback : value;
    }
}
