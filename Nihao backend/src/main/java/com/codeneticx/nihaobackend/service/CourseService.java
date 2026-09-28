package com.codeneticx.nihaobackend.service;

import com.codeneticx.nihaobackend.dto.request.CourseRequest;
import com.codeneticx.nihaobackend.dto.response.CourseResponse;
import com.codeneticx.nihaobackend.dto.response.PaginatedResponse;
import com.codeneticx.nihaobackend.exception.BadRequestException;
import com.codeneticx.nihaobackend.exception.ResourceNotFoundException;
import com.codeneticx.nihaobackend.mapper.AdminMapper;
import com.codeneticx.nihaobackend.model.Course;
import com.codeneticx.nihaobackend.repository.CourseRepository;
import com.codeneticx.nihaobackend.repository.UnitRepository;
import com.codeneticx.nihaobackend.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UnitRepository unitRepository;
    private final VocabularyRepository vocabularyRepository;
    private final ContentDeleteService contentDeleteService;
    private final AdminMapper adminMapper;

    @Transactional(readOnly = true)
    public PaginatedResponse<CourseResponse> getAll(String search, String status, int page, int size,
                                                    String sortBy, String sortDirection) {
        Sort sort = Sort.by("DESC".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortBy == null || sortBy.isBlank() ? "createdAt" : sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Course> courses = courseRepository.searchCourses(emptyToNull(search), emptyToNull(status), pageable);

        Page<CourseResponse> mapped = courses.map(course -> adminMapper.toCourseResponse(
                course,
                (int) unitRepository.countByCourseId(course.getId()),
                (int) vocabularyRepository.count()
        ));

        return PaginatedResponse.from(mapped);
    }

    @Transactional(readOnly = true)
    public CourseResponse getById(String id) {
        Course course = findCourse(id);
        return adminMapper.toCourseResponse(
                course,
                (int) unitRepository.countByCourseId(course.getId()),
                (int) vocabularyRepository.count(),
                true
        );
    }

    @Transactional
    public CourseResponse create(CourseRequest request, String userId) {
        Course course = Course.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .hskLevel(request.getHskLevel())
                .description(request.getDescription())
                .difficulty(defaultString(request.getDifficulty(), "Beginner"))
                .coverImageUrl(request.getCoverImageUrl())
                .status(defaultString(request.getStatus(), "PUBLISHED").toUpperCase())
                .createdBy(userId)
                .build();

        if ("PUBLISHED".equals(course.getStatus())) {
            course.setPublishedAt(LocalDateTime.now());
        }

        courseRepository.save(course);
        return adminMapper.toCourseResponse(course, 0, (int) vocabularyRepository.count());
    }

    @Transactional
    public CourseResponse update(String id, CourseRequest request) {
        Course course = findCourse(id);
        course.setName(request.getName());
        course.setHskLevel(request.getHskLevel());
        course.setDescription(request.getDescription());
        if (request.getDifficulty() != null) {
            course.setDifficulty(request.getDifficulty());
        }
        if (request.getCoverImageUrl() != null) {
            course.setCoverImageUrl(request.getCoverImageUrl());
        }
        if (request.getStatus() != null) {
            updateStatusInternal(course, request.getStatus());
        }
        courseRepository.save(course);
        return adminMapper.toCourseResponse(
                course,
                (int) unitRepository.countByCourseId(course.getId()),
                (int) vocabularyRepository.count()
        );
    }

    @Transactional
    public void delete(String id) {
        contentDeleteService.deleteCourse(id);
    }

    @Transactional
    public CourseResponse updateStatus(String id, String status) {
        Course course = findCourse(id);
        updateStatusInternal(course, status);
        courseRepository.save(course);
        return adminMapper.toCourseResponse(
                course,
                (int) unitRepository.countByCourseId(course.getId()),
                (int) vocabularyRepository.count()
        );
    }

    private void updateStatusInternal(Course course, String status) {
        if (status == null || status.isBlank()) {
            throw new BadRequestException("Status is required");
        }
        course.setStatus(status.toUpperCase());
        if ("PUBLISHED".equals(course.getStatus()) && course.getPublishedAt() == null) {
            course.setPublishedAt(LocalDateTime.now());
        }
    }

    private Course findCourse(String id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + id));
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
