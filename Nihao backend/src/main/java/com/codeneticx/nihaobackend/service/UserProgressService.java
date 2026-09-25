package com.codeneticx.nihaobackend.service;

import com.codeneticx.nihaobackend.dto.mobile.CompleteLessonResultDto;
import com.codeneticx.nihaobackend.dto.mobile.CourseProgressDto;
import com.codeneticx.nihaobackend.dto.mobile.LeagueDto;
import com.codeneticx.nihaobackend.dto.mobile.LeagueEntryDto;
import com.codeneticx.nihaobackend.dto.mobile.UserProgressDto;
import com.codeneticx.nihaobackend.dto.request.CompleteLessonRequest;
import com.codeneticx.nihaobackend.exception.BadRequestException;
import com.codeneticx.nihaobackend.exception.ResourceNotFoundException;
import com.codeneticx.nihaobackend.model.*;
import com.codeneticx.nihaobackend.repository.*;
import com.codeneticx.nihaobackend.util.AppContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProgressService {

    private static final String STATE_LOCKED = "locked";
    private static final String STATE_ACTIVE = "active";
    private static final String STATE_COMPLETE = "complete";
    private static final String STATE_AVAILABLE = "available";

    private final UserProfileRepository userProfileRepository;
    private final UserCourseProgressRepository userCourseProgressRepository;
    private final UserUnitProgressRepository userUnitProgressRepository;
    private final UserLessonProgressRepository userLessonProgressRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UnitRepository unitRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public UserProfile getOrCreateProfile(String userId) {
        return userProfileRepository.findById(userId).orElseGet(() -> {
            UserProfile profile = UserProfile.builder()
                    .user(userRepository.getReferenceById(userId))
                    .totalXp(0)
                    .level(1)
                    .streakDays(0)
                    .totalCrowns(0)
                    .wordsLearned(0)
                    .completedLessons(0)
                    .build();
            return userProfileRepository.save(profile);
        });
    }

    @Transactional
    public void ensureCourseProgressInitialized(String userId, String courseId) {
        findPublishedCourse(courseId);
        if (userCourseProgressRepository.findByUserIdAndCourseId(userId, courseId).isPresent()) {
            return;
        }

        List<Unit> units = liveUnits(courseId);

        userCourseProgressRepository.save(UserCourseProgress.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .course(courseRepository.getReferenceById(courseId))
                .unlocked(true)
                .progressPercent(0f)
                .completedUnits(0)
                .completedWords(0)
                .totalCrowns(0)
                .build());

        UserProfile profile = getOrCreateProfile(userId);
        if (profile.getCurrentCourseId() == null && !units.isEmpty()) {
            profile.setCurrentCourseId(courseId);
            profile.setCurrentUnitNumber(units.get(0).getUnitNumber());
            profile.setCurrentLessonNumber(1);
            userProfileRepository.save(profile);
        }

        for (int unitIndex = 0; unitIndex < units.size(); unitIndex++) {
            Unit unit = units.get(unitIndex);
            List<Lesson> lessons = liveLessons(unit.getId());
            boolean unitUnlocked = unitIndex == 0;

            userUnitProgressRepository.save(UserUnitProgress.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .unit(unit)
                    .state(unitUnlocked ? STATE_ACTIVE : STATE_LOCKED)
                    .unlocked(unitUnlocked)
                    .completedLessons(0)
                    .completedItems(0)
                    .crowns(0)
                    .checkpointCompleted(false)
                    .build());

            for (int lessonIndex = 0; lessonIndex < lessons.size(); lessonIndex++) {
                Lesson lesson = lessons.get(lessonIndex);
                boolean lessonAvailable = unitUnlocked && lessonIndex == 0;
                userLessonProgressRepository.save(UserLessonProgress.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(userId)
                        .lesson(lesson)
                        .state(lessonAvailable ? STATE_AVAILABLE : STATE_LOCKED)
                        .complete(false)
                        .earnedCrowns(0)
                        .completedItems(0)
                        .xpEarned(0)
                        .build());
            }
        }
    }

    public UserProgressDto getUserProgressDto(String userId) {
        UserProfile profile = getOrCreateProfile(userId);
        int totalLessons = (int) lessonRepository.count();

        return UserProgressDto.builder()
                .currentUnit(defaultInt(profile.getCurrentUnitNumber(), 1))
                .currentLesson(defaultInt(profile.getCurrentLessonNumber(), 1))
                .totalXp(profile.getTotalXp())
                .streak(profile.getStreakDays())
                .crowns(profile.getTotalCrowns())
                .wordsLearned(profile.getWordsLearned())
                .completedLessons(profile.getCompletedLessons())
                .totalLessons(totalLessons)
                .build();
    }

    @Transactional
    public LeagueDto getLeague(String userId) {
        getOrCreateProfile(userId);
        List<UserProfile> profiles = userProfileRepository.findLeagueStandings();
        if (profiles.size() > 100) {
            profiles = profiles.subList(0, 100);
        }
        List<LeagueEntryDto> entries = new ArrayList<>();
        int yourRank = 0;
        int rank = 0;
        for (UserProfile profile : profiles) {
            rank++;
            boolean current = userId.equals(profile.getUserId());
            if (current) {
                yourRank = rank;
            }
            User user = profile.getUser();
            entries.add(LeagueEntryDto.builder()
                    .userId(profile.getUserId())
                    .name(displayName(user))
                    .xp(profile.getTotalXp())
                    .rank(rank)
                    .currentUser(current)
                    .build());
        }
        if (yourRank == 0) {
            UserProfile mine = getOrCreateProfile(userId);
            User user = mine.getUser() != null
                    ? mine.getUser()
                    : userRepository.findById(userId).orElse(null);
            yourRank = entries.size() + 1;
            entries.add(LeagueEntryDto.builder()
                    .userId(userId)
                    .name(displayName(user))
                    .xp(mine.getTotalXp())
                    .rank(yourRank)
                    .currentUser(true)
                    .build());
        }
        return LeagueDto.builder()
                .name("gold")
                .yourRank(yourRank)
                .entries(entries)
                .build();
    }

    public CourseProgressDto getCourseProgressDto(String userId, String courseId) {
        ensureCourseProgressInitialized(userId, courseId);
        UserProfile profile = getOrCreateProfile(userId);
        UserCourseProgress courseProgress = userCourseProgressRepository.findByUserIdAndCourseId(userId, courseId)
                .orElseThrow();

        int totalUnits = liveUnits(courseId).size();
        int totalWords = countWordsForCourse(courseId);

        return CourseProgressDto.builder()
                .xp(profile.getTotalXp())
                .level(profile.getLevel())
                .streak(profile.getStreakDays())
                .completedUnits(courseProgress.getCompletedUnits())
                .totalUnits(totalUnits)
                .completedWords(courseProgress.getCompletedWords())
                .totalWords(totalWords)
                .totalCrowns(courseProgress.getTotalCrowns())
                .build();
    }

    public float getCourseProgressPercent(String userId, String courseId) {
        ensureCourseProgressInitialized(userId, courseId);
        return userCourseProgressRepository.findByUserIdAndCourseId(userId, courseId)
                .map(UserCourseProgress::getProgressPercent)
                .orElse(0f);
    }

    public boolean isCourseUnlocked(String userId, String courseId, int courseIndex) {
        ensureCourseProgressInitialized(userId, courseId);
        return true;
    }

    public Map<String, UserUnitProgress> getUnitProgressMap(String userId, List<Unit> units) {
        if (units.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> unitIds = units.stream().map(Unit::getId).toList();
        return userUnitProgressRepository.findByUserIdAndUnitIdIn(userId, unitIds).stream()
                .collect(Collectors.toMap(p -> p.getUnit().getId(), Function.identity()));
    }

    public Map<String, UserLessonProgress> getLessonProgressMap(String userId, List<Lesson> lessons) {
        if (lessons.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> lessonIds = lessons.stream().map(Lesson::getId).toList();
        return userLessonProgressRepository.findByUserIdAndLessonIdIn(userId, lessonIds).stream()
                .collect(Collectors.toMap(p -> p.getLesson().getId(), Function.identity()));
    }

    @Transactional
    public CompleteLessonResultDto completeLesson(String userId, String lessonId, CompleteLessonRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .filter(item -> AppContent.isLive(item.getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + lessonId));

        Unit unit = lesson.getUnit();
        Course course = unit.getCourse();
        ensureCourseProgressInitialized(userId, course.getId());

        UserLessonProgress lessonProgress = userLessonProgressRepository.findByUserIdAndLessonId(userId, lessonId)
                .orElseThrow(() -> new BadRequestException("Lesson progress not initialized"));

        if (lessonProgress.isComplete()) {
            throw new BadRequestException("Lesson already completed");
        }
        if (!STATE_AVAILABLE.equalsIgnoreCase(lessonProgress.getState())) {
            throw new BadRequestException("Lesson is not available yet");
        }

        int earnedCrowns = clamp(request.getEarnedCrowns(), 0, defaultInt(lesson.getCrowns(), 3));
        int xpEarned = Math.max(0, defaultInt(request.getXpEarned(), 10));
        int completedItems = defaultInt(lesson.getWordsCount(), 0);

        lessonProgress.setComplete(true);
        lessonProgress.setState(STATE_COMPLETE);
        lessonProgress.setEarnedCrowns(earnedCrowns);
        lessonProgress.setXpEarned(xpEarned);
        lessonProgress.setCompletedItems(completedItems);
        lessonProgress.setCompletedAt(LocalDateTime.now());
        userLessonProgressRepository.save(lessonProgress);

        UserUnitProgress unitProgress = userUnitProgressRepository.findByUserIdAndUnitId(userId, unit.getId())
                .orElseThrow();
        unitProgress.setCompletedLessons(unitProgress.getCompletedLessons() + 1);
        unitProgress.setCompletedItems(unitProgress.getCompletedItems() + completedItems);
        unitProgress.setCrowns(unitProgress.getCrowns() + earnedCrowns);
        if ("CHECKPOINT".equalsIgnoreCase(lesson.getLessonType())) {
            unitProgress.setCheckpointCompleted(true);
        }

        List<Lesson> unitLessons = liveLessons(unit.getId());
        Map<String, UserLessonProgress> lessonProgressMap = getLessonProgressMap(userId, unitLessons);
        boolean unitComplete = unitLessons.stream()
                .filter(l -> !"CHECKPOINT".equalsIgnoreCase(l.getLessonType()))
                .allMatch(l -> {
                    UserLessonProgress progress = lessonProgressMap.get(l.getId());
                    return progress != null && progress.isComplete();
                });

        if (unitComplete) {
            unitProgress.setState(STATE_COMPLETE);
        } else if (STATE_LOCKED.equalsIgnoreCase(unitProgress.getState())) {
            unitProgress.setState(STATE_ACTIVE);
        }
        userUnitProgressRepository.save(unitProgress);

        unlockNextLesson(userId, unitLessons, lesson, lessonProgressMap);
        if (unitComplete) {
            unlockNextUnit(userId, course.getId(), unit.getUnitNumber());
        }

        UserProfile profile = getOrCreateProfile(userId);
        profile.setTotalXp(profile.getTotalXp() + xpEarned);
        profile.setLevel(calculateLevel(profile.getTotalXp()));
        profile.setTotalCrowns(profile.getTotalCrowns() + earnedCrowns);
        profile.setWordsLearned(profile.getWordsLearned() + completedItems);
        profile.setCompletedLessons(profile.getCompletedLessons() + 1);
        profile.setCurrentCourseId(course.getId());
        profile.setCurrentUnitNumber(unit.getUnitNumber());
        profile.setCurrentLessonNumber(lesson.getLessonNumber());
        updateStreak(profile);
        userProfileRepository.save(profile);

        UserCourseProgress courseProgress = userCourseProgressRepository.findByUserIdAndCourseId(userId, course.getId())
                .orElseThrow();
        courseProgress.setCompletedWords(courseProgress.getCompletedWords() + completedItems);
        courseProgress.setTotalCrowns(courseProgress.getTotalCrowns() + earnedCrowns);
        if (unitComplete) {
            courseProgress.setCompletedUnits(courseProgress.getCompletedUnits() + 1);
        }
        int totalUnits = liveUnits(course.getId()).size();
        courseProgress.setProgressPercent(totalUnits == 0 ? 0f : (courseProgress.getCompletedUnits() * 100f) / totalUnits);
        userCourseProgressRepository.save(courseProgress);

        CourseProgressDto progressDto = getCourseProgressDto(userId, course.getId());

        return CompleteLessonResultDto.builder()
                .lessonId(lessonId)
                .xpEarned(xpEarned)
                .earnedCrowns(earnedCrowns)
                .totalXp(profile.getTotalXp())
                .streak(profile.getStreakDays())
                .level(profile.getLevel())
                .courseProgress(progressDto)
                .build();
    }

    private void unlockNextLesson(String userId, List<Lesson> unitLessons, Lesson completedLesson,
                                    Map<String, UserLessonProgress> lessonProgressMap) {
        for (int i = 0; i < unitLessons.size(); i++) {
            if (!unitLessons.get(i).getId().equals(completedLesson.getId())) {
                continue;
            }
            if (i + 1 < unitLessons.size()) {
                Lesson nextLesson = unitLessons.get(i + 1);
                UserLessonProgress nextProgress = userLessonProgressRepository.findByUserIdAndLessonId(userId, nextLesson.getId())
                        .orElseThrow();
                if (STATE_LOCKED.equalsIgnoreCase(nextProgress.getState())) {
                    nextProgress.setState(STATE_AVAILABLE);
                    userLessonProgressRepository.save(nextProgress);
                }
            }
            break;
        }
    }

    private void unlockNextUnit(String userId, String courseId, int completedUnitNumber) {
        List<Unit> units = liveUnits(courseId);
        for (int i = 0; i < units.size(); i++) {
            Unit current = units.get(i);
            if (!Objects.equals(current.getUnitNumber(), completedUnitNumber) || i + 1 >= units.size()) {
                continue;
            }

            Unit nextUnit = units.get(i + 1);
            UserUnitProgress nextUnitProgress = userUnitProgressRepository.findByUserIdAndUnitId(userId, nextUnit.getId())
                    .orElseThrow();
            nextUnitProgress.setUnlocked(true);
            nextUnitProgress.setState(STATE_ACTIVE);
            userUnitProgressRepository.save(nextUnitProgress);

            List<Lesson> nextLessons = liveLessons(nextUnit.getId());
            if (!nextLessons.isEmpty()) {
                UserLessonProgress firstLessonProgress = userLessonProgressRepository
                        .findByUserIdAndLessonId(userId, nextLessons.get(0).getId())
                        .orElseThrow();
                if (STATE_LOCKED.equalsIgnoreCase(firstLessonProgress.getState())) {
                    firstLessonProgress.setState(STATE_AVAILABLE);
                    userLessonProgressRepository.save(firstLessonProgress);
                }
            }

            UserProfile profile = getOrCreateProfile(userId);
            profile.setCurrentUnitNumber(nextUnit.getUnitNumber());
            profile.setCurrentLessonNumber(nextLessons.isEmpty() ? 1 : nextLessons.get(0).getLessonNumber());
            userProfileRepository.save(profile);
            break;
        }
    }

    private void updateStreak(UserProfile profile) {
        LocalDate today = LocalDate.now();
        LocalDate lastActivity = profile.getLastActivityDate();

        if (lastActivity == null) {
            profile.setStreakDays(1);
        } else if (lastActivity.equals(today)) {
            // same day, keep streak
        } else if (lastActivity.plusDays(1).equals(today)) {
            profile.setStreakDays(profile.getStreakDays() + 1);
        } else {
            profile.setStreakDays(1);
        }
        profile.setLastActivityDate(today);
    }

    private int calculateLevel(int totalXp) {
        return Math.max(1, (totalXp / 100) + 1);
    }

    private int countWordsForCourse(String courseId) {
        return liveUnits(courseId).stream()
                .flatMap(unit -> liveLessons(unit.getId()).stream())
                .mapToInt(lesson -> defaultInt(lesson.getWordsCount(), 0))
                .sum();
    }

    private Course findPublishedCourse(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
        if (!AppContent.isLive(course.getStatus())) {
            throw new ResourceNotFoundException("Course not found: " + courseId);
        }
        return course;
    }

    private List<Unit> liveUnits(String courseId) {
        return AppContent.liveUnits(unitRepository.findByCourseIdOrderByUnitNumberAsc(courseId));
    }

    private List<Lesson> liveLessons(String unitId) {
        return AppContent.liveLessons(lessonRepository.findByUnitIdOrderByLessonNumberAsc(unitId));
    }

    private int clamp(Integer value, int min, int max) {
        int actual = value == null ? min : value;
        return Math.max(min, Math.min(max, actual));
    }

    private int defaultInt(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private String displayName(User user) {
        if (user == null) {
            return "طالب علم";
        }
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName();
        }
        return user.getUsername();
    }
}
