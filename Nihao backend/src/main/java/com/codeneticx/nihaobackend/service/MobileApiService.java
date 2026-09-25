package com.codeneticx.nihaobackend.service;

import com.codeneticx.nihaobackend.dto.mobile.*;
import com.codeneticx.nihaobackend.exception.ResourceNotFoundException;
import com.codeneticx.nihaobackend.model.*;
import com.codeneticx.nihaobackend.repository.*;
import com.codeneticx.nihaobackend.util.AppContent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MobileApiService {

    private final CourseRepository courseRepository;
    private final UnitRepository unitRepository;
    private final LessonRepository lessonRepository;
    private final VocabularyRepository vocabularyRepository;
    private final ExerciseRepository exerciseRepository;
    private final ObjectMapper objectMapper;
    private final UserProgressService userProgressService;

    public List<CourseListItemDto> getCourses(String userId) {
        List<Course> courses = AppContent.liveCourses(courseRepository.findAll());
        List<CourseListItemDto> items = new ArrayList<>();
        for (int i = 0; i < courses.size(); i++) {
            items.add(toCourseListItem(userId, courses.get(i), i));
        }
        return items;
    }

    public CourseDetailDto getCourseDetail(String userId, String courseId) {
        userProgressService.ensureCourseProgressInitialized(userId, courseId);
        Course course = findPublishedCourse(courseId);
        List<Unit> units = liveUnits(courseId);

        return CourseDetailDto.builder()
                .id(course.getId())
                .title(courseTitle(course))
                .description(courseDescription(course))
                .level(defaultString(course.getDifficulty(), "Beginner"))
                .totalUnits(units.size())
                .totalWords(countWordsForCourse(courseId))
                .premium(false)
                .progress(userProgressService.getCourseProgressPercent(userId, courseId))
                .version("1.0.0")
                .units(units.stream().map(this::toUnitSummary).collect(Collectors.toList()))
                .build();
    }

    public CourseProgressDto getCourseProgress(String userId, String courseId) {
        return userProgressService.getCourseProgressDto(userId, courseId);
    }

    public LeagueDto getLeague(String userId) {
        return userProgressService.getLeague(userId);
    }

    public List<UnitJourneyNodeDto> getCourseUnits(String userId, String courseId, int limit, int offset) {
        userProgressService.ensureCourseProgressInitialized(userId, courseId);
        findPublishedCourse(courseId);
        List<Unit> allUnits = liveUnits(courseId);
        int from = Math.min(Math.max(offset, 0), allUnits.size());
        int to = Math.min(from + Math.max(limit, 1), allUnits.size());
        List<Unit> units = allUnits.subList(from, to);
        Map<String, UserUnitProgress> progressMap = userProgressService.getUnitProgressMap(userId, units);

        List<UnitJourneyNodeDto> nodes = new ArrayList<>();
        for (int i = 0; i < units.size(); i++) {
            nodes.add(toUnitJourneyNode(units.get(i), offset + i, progressMap.get(units.get(i).getId())));
        }
        return nodes;
    }

    public UnitDetailDto getUnitDetail(String userId, String unitId) {
        Unit unit = findPublishedUnit(unitId);
        userProgressService.ensureCourseProgressInitialized(userId, unit.getCourse().getId());
        List<Lesson> lessons = liveLessons(unitId);

        return UnitDetailDto.builder()
                .id(unit.getId())
                .title(LocalizedText.builder().ur(unit.getUrduTitle()).en(unit.getHanziTitle()).build())
                .hanziTitle(unit.getHanziTitle())
                .grammarPoint(grammarPoint(unit.getGrammarPoint()))
                .totalLessons(lessons.size())
                .totalItems(lessons.stream().mapToInt(l -> defaultInt(l.getWordsCount(), 0)).sum())
                .premium(false)
                .packVersion("v" + defaultInt(unit.getVersion(), 1) + ".0")
                .lessons(lessons.stream().map(this::toLessonBrief).collect(Collectors.toList()))
                .build();
    }

    public List<LessonJourneyNodeDto> getUnitLessons(String userId, String unitId) {
        Unit unit = findPublishedUnit(unitId);
        userProgressService.ensureCourseProgressInitialized(userId, unit.getCourse().getId());
        List<Lesson> lessons = liveLessons(unitId);
        Map<String, UserLessonProgress> progressMap = userProgressService.getLessonProgressMap(userId, lessons);

        return lessons.stream()
                .map(lesson -> toLessonJourneyNode(lesson, progressMap.get(lesson.getId())))
                .collect(Collectors.toList());
    }

    public List<ExerciseNodeDto> getLessonExercises(String lessonId) {
        Lesson lesson = findPublishedLesson(lessonId);
        return teachThenQuiz(exerciseRepository.findByLessonIdOrderByExerciseOrderAsc(lessonId)).stream()
                .map(exercise -> toExerciseNode(exercise, lesson))
                .collect(Collectors.toList());
    }

    public LessonDetailDto getLessonDetail(String lessonId) {
        Lesson lesson = findPublishedLesson(lessonId);
        List<Exercise> exercises = teachThenQuiz(
                exerciseRepository.findByLessonIdOrderByExerciseOrderAsc(lessonId));

        return LessonDetailDto.builder()
                .id(lesson.getId())
                .lessonNumber(lesson.getLessonNumber())
                .lessonType(normalizeLessonType(lesson.getLessonType()))
                .urduTitle(lesson.getUrduTitle())
                .instructionText(defaultString(lesson.getInstructionText(), ""))
                .difficulty(defaultString(lesson.getDifficulty(), "Easy"))
                .crowns(defaultInt(lesson.getCrowns(), 3))
                .exercisesCount(defaultInt(lesson.getExercisesCount(), exercises.size()))
                .wordsCount(defaultInt(lesson.getWordsCount(), 0))
                .exercises(exercises.stream().map(this::toLessonExercise).collect(Collectors.toList()))
                .build();
    }

    public VocabularyItemDetailDto getItemDetail(String itemId) {
        Vocabulary vocabulary = vocabularyRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary item not found: " + itemId));
        return toVocabularyItemDetail(vocabulary);
    }

    public List<VocabularyListItemDto> getVocabulary(Integer hskLevel, String search) {
        String normalizedSearch = (search == null || search.isBlank()) ? null : search.trim();
        return vocabularyRepository.searchVocabulary(hskLevel, normalizedSearch).stream()
                .map(this::toVocabularyListItem)
                .collect(Collectors.toList());
    }

    private Course findPublishedCourse(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
        if (!AppContent.isLive(course.getStatus())) {
            throw new ResourceNotFoundException("Course not found: " + courseId);
        }
        return course;
    }

    private Unit findPublishedUnit(String unitId) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + unitId));
        if (!AppContent.isLive(unit.getStatus())) {
            throw new ResourceNotFoundException("Unit not found: " + unitId);
        }
        return unit;
    }

    private Lesson findPublishedLesson(String lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));
        if (!AppContent.isLive(lesson.getStatus())) {
            throw new ResourceNotFoundException("Lesson not found: " + lessonId);
        }
        return lesson;
    }

    private List<Unit> liveUnits(String courseId) {
        return AppContent.liveUnits(unitRepository.findByCourseIdOrderByUnitNumberAsc(courseId));
    }

    private List<Lesson> liveLessons(String unitId) {
        return AppContent.liveLessons(lessonRepository.findByUnitIdOrderByLessonNumberAsc(unitId));
    }

    private CourseListItemDto toCourseListItem(String userId, Course course, int courseIndex) {
        userProgressService.ensureCourseProgressInitialized(userId, course.getId());
        int totalUnits = liveUnits(course.getId()).size();
        return CourseListItemDto.builder()
                .id(course.getId())
                .title(courseTitle(course))
                .level(defaultString(course.getDifficulty(), "Beginner"))
                .totalUnits(totalUnits)
                .totalWords(countWordsForCourse(course.getId()))
                .premium(false)
                .isUnlocked(userProgressService.isCourseUnlocked(userId, course.getId(), courseIndex))
                .progress(userProgressService.getCourseProgressPercent(userId, course.getId()))
                .coverImageUrl(course.getCoverImageUrl())
                .build();
    }

    private UnitSummaryDto toUnitSummary(Unit unit) {
        List<Lesson> lessons = liveLessons(unit.getId());
        return UnitSummaryDto.builder()
                .id(unit.getId())
                .unitNumber(unit.getUnitNumber())
                .urduTitle(unit.getUrduTitle())
                .hanziTitle(unit.getHanziTitle())
                .grammarPoint(grammarPoint(unit.getGrammarPoint()))
                .hskLevel(defaultString(unit.getHskLevel(), "1"))
                .topics(unit.getTopics() != null ? unit.getTopics() : Collections.emptyList())
                .estimatedTime(defaultInt(unit.getEstimatedTime(), 20))
                .difficulty(defaultString(unit.getDifficulty(), "Easy"))
                .learningObjectives(unit.getObjectives() != null ? unit.getObjectives() : Collections.emptyList())
                .lessonCount(lessons.size())
                .wordCount(lessons.stream().mapToInt(l -> defaultInt(l.getWordsCount(), 0)).sum())
                .build();
    }

    private UnitJourneyNodeDto toUnitJourneyNode(Unit unit, int index, UserUnitProgress progress) {
        List<Lesson> lessons = liveLessons(unit.getId());
        int totalItems = lessons.stream().mapToInt(l -> defaultInt(l.getWordsCount(), 0)).sum();
        boolean hasCheckpoint = lessons.stream().anyMatch(l -> "CHECKPOINT".equalsIgnoreCase(l.getLessonType()));

        if (progress == null) {
            return UnitJourneyNodeDto.builder()
                    .id(unit.getId())
                    .index(index)
                    .title(LocalizedText.builder().ur(unit.getUrduTitle()).en(unit.getHanziTitle()).build())
                    .hanziTitle(unit.getHanziTitle())
                    .state(index == 0 ? "active" : "locked")
                    .completedLessons(0)
                    .totalLessons(lessons.size())
                    .completedItems(0)
                    .totalItems(totalItems)
                    .crowns(0)
                    .hasCheckpoint(hasCheckpoint)
                    .checkpointCompleted(false)
                    .isUnlocked(index == 0)
                    .premium(false)
                    .build();
        }

        return UnitJourneyNodeDto.builder()
                .id(unit.getId())
                .index(index)
                .title(LocalizedText.builder().ur(unit.getUrduTitle()).en(unit.getHanziTitle()).build())
                .hanziTitle(unit.getHanziTitle())
                .state(progress.getState())
                .completedLessons(progress.getCompletedLessons())
                .totalLessons(lessons.size())
                .completedItems(progress.getCompletedItems())
                .totalItems(totalItems)
                .crowns(progress.getCrowns())
                .hasCheckpoint(hasCheckpoint)
                .checkpointCompleted(progress.isCheckpointCompleted())
                .isUnlocked(progress.isUnlocked())
                .premium(false)
                .build();
    }

    private LessonJourneyNodeDto toLessonJourneyNode(Lesson lesson, UserLessonProgress progress) {
        String type = normalizeLessonType(lesson.getLessonType()).toLowerCase();
        if (progress == null) {
            return LessonJourneyNodeDto.builder()
                    .id(lesson.getId())
                    .index(lesson.getLessonNumber())
                    .type(type)
                    .title(LocalizedText.builder().ur(lesson.getUrduTitle()).en(lesson.getUrduTitle()).build())
                    .items(defaultInt(lesson.getWordsCount(), 0))
                    .crowns(defaultInt(lesson.getCrowns(), 3))
                    .earnedCrowns(0)
                    .isComplete(false)
                    .premium(false)
                    .state("locked")
                    .build();
        }

        return LessonJourneyNodeDto.builder()
                .id(lesson.getId())
                .index(lesson.getLessonNumber())
                .type(type)
                .title(LocalizedText.builder().ur(lesson.getUrduTitle()).en(lesson.getUrduTitle()).build())
                .items(defaultInt(lesson.getWordsCount(), 0))
                .crowns(defaultInt(lesson.getCrowns(), 3))
                .earnedCrowns(progress.getEarnedCrowns())
                .isComplete(progress.isComplete())
                .premium(false)
                .state(progress.getState())
                .build();
    }

    private LessonBriefDto toLessonBrief(Lesson lesson) {
        return LessonBriefDto.builder()
                .id(lesson.getId())
                .lessonNumber(lesson.getLessonNumber())
                .lessonType(normalizeLessonType(lesson.getLessonType()))
                .urduTitle(lesson.getUrduTitle())
                .instructionText(defaultString(lesson.getInstructionText(), ""))
                .difficulty(defaultString(lesson.getDifficulty(), "Easy"))
                .crowns(defaultInt(lesson.getCrowns(), 3))
                .exercisesCount(defaultInt(lesson.getExercisesCount(), 0))
                .wordsCount(defaultInt(lesson.getWordsCount(), 0))
                .build();
    }

    private LessonExerciseDto toLessonExercise(Exercise exercise) {
        JsonNode data = parseExerciseData(exercise.getExerciseData());
        return LessonExerciseDto.builder()
                .id(exercise.getId())
                .exerciseType(normalizeMobileExerciseType(exercise.getExerciseType()))
                .exerciseData(data)
                .exerciseOrder(exercise.getExerciseOrder())
                .build();
    }

    private ExerciseNodeDto toExerciseNode(Exercise exercise, Lesson lesson) {
        JsonNode data = parseExerciseData(exercise.getExerciseData());
        String type = normalizeMobileExerciseType(exercise.getExerciseType());

        return ExerciseNodeDto.builder()
                .id(exercise.getId())
                .type(type)
                .prompt(extractPrompt(data, lesson))
                .items(extractStringList(data, "items"))
                .options(extractStringList(data, "options"))
                .answer(extractText(data, "answer", "correctAnswer", "word"))
                .gloss(extractGloss(data))
                .audioId(extractText(data, "audioId", "audio"))
                .hint(extractLocalizedHint(data))
                .pinyin(extractText(data, "pinyin"))
                .build();
    }

    private VocabularyItemDetailDto toVocabularyItemDetail(Vocabulary vocabulary) {
        List<ExampleSentenceDto> examples = vocabulary.getExampleSentences() == null
                ? Collections.emptyList()
                : vocabulary.getExampleSentences().stream()
                .map(es -> ExampleSentenceDto.builder()
                        .hanzi(es.getHanzi())
                        .pinyin(es.getPinyin())
                        .urdu(es.getUrdu())
                        .audioId(null)
                        .build())
                .collect(Collectors.toList());

        return VocabularyItemDetailDto.builder()
                .id(vocabulary.getId())
                .hanzi(vocabulary.getHanzi())
                .pinyin(vocabulary.getPinyin())
                .urdu(vocabulary.getUrduTranslation())
                .romanUrdu(defaultString(vocabulary.getRomanUrdu(), ""))
                .glossUr(vocabulary.getLiteralGloss())
                .audioIds(AudioIdsDto.builder()
                        .male(vocabulary.getAudioMaleUrl())
                        .female(vocabulary.getAudioFemaleUrl())
                        .build())
                .illustration(vocabulary.getIllustrationUrl())
                .exampleSentences(examples)
                .hskTag(vocabulary.getHskLevel() != null ? "HSK " + vocabulary.getHskLevel() : "HSK 1")
                .topicTags(vocabulary.getTopics() != null ? vocabulary.getTopics() : Collections.emptyList())
                .build();
    }

    private VocabularyListItemDto toVocabularyListItem(Vocabulary vocabulary) {
        List<ExampleDto> examples = vocabulary.getExampleSentences() == null
                ? Collections.emptyList()
                : vocabulary.getExampleSentences().stream()
                .map(es -> ExampleDto.builder()
                        .hanzi(es.getHanzi())
                        .pinyin(es.getPinyin())
                        .urdu(es.getUrdu())
                        .build())
                .collect(Collectors.toList());

        return VocabularyListItemDto.builder()
                .id(vocabulary.getId())
                .hanzi(vocabulary.getHanzi())
                .pinyin(vocabulary.getPinyin())
                .tone(defaultInt(vocabulary.getTone(), 0))
                .urduTranslation(vocabulary.getUrduTranslation())
                .romanUrdu(defaultString(vocabulary.getRomanUrdu(), ""))
                .partOfSpeech(defaultString(vocabulary.getPartOfSpeech(), ""))
                .hskLevel(defaultInt(vocabulary.getHskLevel(), 1))
                .topics(vocabulary.getTopics() != null ? vocabulary.getTopics() : Collections.emptyList())
                .examples(examples)
                .audioMaleUrl(vocabulary.getAudioMaleUrl())
                .audioFemaleUrl(vocabulary.getAudioFemaleUrl())
                .illustrationUrl(vocabulary.getIllustrationUrl())
                .build();
    }

    private LocalizedText courseTitle(Course course) {
        return LocalizedText.builder()
                .ur(defaultString(course.getDescription(), course.getName()))
                .en(course.getName())
                .build();
    }

    private LocalizedText courseDescription(Course course) {
        return LocalizedText.builder()
                .ur(defaultString(course.getDescription(), course.getName()))
                .en(defaultString(course.getDescription(), course.getName()))
                .build();
    }

    private LocalizedText grammarPoint(String grammarPoint) {
        String value = defaultString(grammarPoint, "");
        return LocalizedText.builder().ur(value).en(value).build();
    }

    private int countWordsForCourse(String courseId) {
        return liveUnits(courseId).stream()
                .flatMap(unit -> liveLessons(unit.getId()).stream())
                .mapToInt(lesson -> defaultInt(lesson.getWordsCount(), 0))
                .sum();
    }

    private JsonNode parseExerciseData(String exerciseData) {
        try {
            return objectMapper.readTree(exerciseData);
        } catch (Exception ex) {
            return objectMapper.createObjectNode();
        }
    }

    private LocalizedText extractPrompt(JsonNode data, Lesson lesson) {
        String ur = extractText(data, "promptUr", "prompt");
        String en = extractText(data, "promptEn", "prompt", "instructionText");
        if (ur == null && en == null) {
            ur = lesson.getUrduTitle();
            en = lesson.getInstructionText();
        }
        return LocalizedText.builder()
                .ur(defaultString(ur, ""))
                .en(defaultString(en, ur))
                .build();
    }

    private List<LocalizedText> extractGloss(JsonNode data) {
        JsonNode glossNode = data.get("gloss");
        if (glossNode == null) {
            String urdu = extractText(data, "urdu", "urduTranslation");
            if (urdu != null) {
                return List.of(LocalizedText.builder().ur(urdu).en(urdu).build());
            }
            return null;
        }
        if (glossNode.isArray()) {
            List<LocalizedText> gloss = new ArrayList<>();
            glossNode.forEach(node -> gloss.add(LocalizedText.builder()
                    .ur(node.path("ur").asText(node.asText()))
                    .en(node.path("en").asText(node.path("ur").asText(node.asText())))
                    .build()));
            return gloss;
        }
        return List.of(LocalizedText.builder().ur(glossNode.asText()).en(glossNode.asText()).build());
    }

    private LocalizedText extractLocalizedHint(JsonNode data) {
        JsonNode hintNode = data.get("hint");
        if (hintNode == null) {
            return null;
        }
        if (hintNode.isObject()) {
            return LocalizedText.builder()
                    .ur(hintNode.path("ur").asText())
                    .en(hintNode.path("en").asText(hintNode.path("ur").asText()))
                    .build();
        }
        return LocalizedText.builder().ur(hintNode.asText()).en(hintNode.asText()).build();
    }

    private List<String> extractStringList(JsonNode data, String field) {
        JsonNode node = data.get(field);
        if (node == null || !node.isArray()) {
            return null;
        }
        List<String> values = new ArrayList<>();
        node.forEach(item -> values.add(item.asText()));
        return values;
    }

    private String extractText(JsonNode data, String... fields) {
        for (String field : fields) {
            JsonNode node = data.get(field);
            if (node != null && !node.isNull() && !node.asText().isBlank()) {
                return node.asText();
            }
        }
        return null;
    }

    private String normalizeLessonType(String lessonType) {
        if (lessonType == null) {
            return "normal";
        }
        return switch (lessonType.toUpperCase()) {
            case "CHECKPOINT" -> "checkpoint";
            case "REVIEW" -> "review";
            default -> "normal";
        };
    }

    private List<Exercise> teachThenQuiz(List<Exercise> exercises) {
        List<Exercise> teaches = new ArrayList<>();
        List<Exercise> quizzes = new ArrayList<>();
        for (Exercise exercise : exercises) {
            if (isTeachExercise(exercise.getExerciseType())) {
                teaches.add(exercise);
            } else {
                quizzes.add(exercise);
            }
        }
        if (teaches.isEmpty() || quizzes.isEmpty()) {
            return exercises;
        }
        List<Exercise> ordered = new ArrayList<>(teaches.size() + quizzes.size());
        ordered.addAll(teaches);
        ordered.addAll(quizzes);
        return ordered;
    }

    private boolean isTeachExercise(String exerciseType) {
        if (exerciseType == null || exerciseType.isBlank()) {
            return false;
        }
        String normalized = exerciseType.trim().toUpperCase();
        return "TEACH_FRAME".equals(normalized) || "TEACH".equals(normalized);
    }

    private String normalizeMobileExerciseType(String exerciseType) {
        if (exerciseType == null) {
            return "teach";
        }
        return switch (exerciseType.toUpperCase()) {
            case "TEACH_FRAME", "TEACH" -> "teach";
            case "PICTURE_MATCH", "PIC" -> "pic";
            case "LISTENING_CHOICE", "LISTEN" -> "listen";
            case "TONE_DRILL", "TONE" -> "tone";
            case "TAP_TO_BUILD", "BUILD" -> "build";
            case "FILL_IN_THE_BLANK", "FILL" -> "fill";
            default -> exerciseType.toLowerCase();
        };
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private int defaultInt(Integer value, int fallback) {
        return value == null ? fallback : value;
    }
}
