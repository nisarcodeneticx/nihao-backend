package com.codeneticx.nihaobackend.mapper;

import com.codeneticx.nihaobackend.dto.response.*;
import com.codeneticx.nihaobackend.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AdminMapper {

    private final ObjectMapper objectMapper;

    public CourseResponse toCourseResponse(Course course, int unitCount, int vocabularyCount) {
        return toCourseResponse(course, unitCount, vocabularyCount, false);
    }

    public CourseResponse toCourseResponse(Course course, int unitCount, int vocabularyCount, boolean includeChildren) {
        CourseResponse.CourseResponseBuilder builder = CourseResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .hskLevel(course.getHskLevel())
                .description(course.getDescription())
                .difficulty(course.getDifficulty())
                .coverImageUrl(course.getCoverImageUrl())
                .status(course.getStatus())
                .units(unitCount)
                .vocabulary(vocabularyCount)
                .createdBy(course.getCreatedBy())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .publishedAt(course.getPublishedAt());

        if (includeChildren && course.getUnits() != null) {
            builder.unitsList(course.getUnits().stream()
                    .map(unit -> toUnitResponse(unit, true))
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }

    public UnitResponse toUnitResponse(Unit unit, boolean includeLessons) {
        UnitResponse.UnitResponseBuilder builder = UnitResponse.builder()
                .id(unit.getId())
                .unitNumber(defaultInt(unit.getUnitNumber(), 0))
                .urduTitle(unit.getUrduTitle())
                .hanziTitle(unit.getHanziTitle())
                .grammarPoint(unit.getGrammarPoint())
                .hskLevel(unit.getHskLevel())
                .topics(copyStrings(unit.getTopics()))
                .estimatedTime(defaultInt(unit.getEstimatedTime(), 0))
                .difficulty(unit.getDifficulty())
                .learningObjectives(copyStrings(unit.getObjectives()))
                .status(unit.getStatus())
                .version(defaultInt(unit.getVersion(), 1))
                .publishedAt(unit.getPublishedAt())
                .createdBy(unit.getCreatedBy())
                .createdAt(unit.getCreatedAt())
                .updatedAt(unit.getUpdatedAt());

        if (includeLessons && unit.getLessons() != null) {
            builder.lessons(unit.getLessons().stream()
                    .map(lesson -> toLessonResponse(lesson, true))
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }

    public LessonResponse toLessonResponse(Lesson lesson, boolean includeExercises) {
        LessonResponse.LessonResponseBuilder builder = LessonResponse.builder()
                .id(lesson.getId())
                .lessonNumber(defaultInt(lesson.getLessonNumber(), 0))
                .lessonType(lesson.getLessonType())
                .urduTitle(lesson.getUrduTitle())
                .instructionText(lesson.getInstructionText())
                .difficulty(lesson.getDifficulty())
                .crowns(defaultInt(lesson.getCrowns(), 0))
                .status(lesson.getStatus())
                .exercisesCount(defaultInt(lesson.getExercisesCount(), 0))
                .wordsCount(defaultInt(lesson.getWordsCount(), 0))
                .createdBy(lesson.getCreatedBy())
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt());

        if (includeExercises && lesson.getExercises() != null) {
            builder.exercises(lesson.getExercises().stream()
                    .map(this::toExerciseResponse)
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }

    public ExerciseResponse toExerciseResponse(Exercise exercise) {
        return ExerciseResponse.builder()
                .id(exercise.getId())
                .exerciseType(exercise.getExerciseType())
                .exerciseData(parseExerciseData(exercise.getExerciseData()))
                .exerciseOrder(defaultInt(exercise.getExerciseOrder(), 0))
                .createdBy(exercise.getCreatedBy())
                .createdAt(exercise.getCreatedAt())
                .updatedAt(exercise.getUpdatedAt())
                .build();
    }

    public VocabularyResponse toVocabularyResponse(Vocabulary vocabulary) {
        List<ExampleResponse> examples = vocabulary.getExampleSentences() == null
                ? Collections.emptyList()
                : vocabulary.getExampleSentences().stream()
                .map(this::toExampleResponse)
                .collect(Collectors.toList());

        return VocabularyResponse.builder()
                .id(vocabulary.getId())
                .hanzi(vocabulary.getHanzi())
                .pinyin(vocabulary.getPinyin())
                .tone(defaultInt(vocabulary.getTone(), 0))
                .urduTranslation(vocabulary.getUrduTranslation())
                .romanUrdu(vocabulary.getRomanUrdu())
                .literalGloss(vocabulary.getLiteralGloss())
                .partOfSpeech(vocabulary.getPartOfSpeech())
                .hskLevel(defaultInt(vocabulary.getHskLevel(), 1))
                .topics(copyStrings(vocabulary.getTopics()))
                .frequency(defaultInt(vocabulary.getFrequency(), 0))
                .strokeCount(vocabulary.getStrokeCount())
                .radical(vocabulary.getRadical())
                .audioMaleUrl(vocabulary.getAudioMaleUrl())
                .audioFemaleUrl(vocabulary.getAudioFemaleUrl())
                .illustrationUrl(vocabulary.getIllustrationUrl())
                .examples(examples)
                .createdBy(vocabulary.getCreatedBy())
                .createdAt(vocabulary.getCreatedAt())
                .updatedAt(vocabulary.getUpdatedAt())
                .build();
    }

    public ExampleResponse toExampleResponse(ExampleSentence example) {
        return ExampleResponse.builder()
                .id(example.getId())
                .hanzi(example.getHanzi())
                .pinyin(example.getPinyin())
                .urdu(example.getUrdu())
                .createdAt(example.getCreatedAt())
                .build();
    }

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .status(user.getStatus())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }

    private JsonNode parseExerciseData(String exerciseData) {
        try {
            return objectMapper.readTree(exerciseData);
        } catch (Exception ex) {
            return objectMapper.createObjectNode();
        }
    }

    private int defaultInt(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private List<String> copyStrings(List<String> values) {
        return values == null ? List.of() : new ArrayList<>(values);
    }
}
