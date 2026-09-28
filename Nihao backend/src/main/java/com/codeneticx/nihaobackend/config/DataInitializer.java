package com.codeneticx.nihaobackend.config;

import com.codeneticx.nihaobackend.model.*;
import com.codeneticx.nihaobackend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UnitRepository unitRepository;
    private final LessonRepository lessonRepository;
    private final VocabularyRepository vocabularyRepository;
    private final ExerciseRepository exerciseRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        if (courseRepository.count() == 0) {
            log.info("Seeding mobile API sample data...");
            seedSampleContent();
        }
    }

    private void seedUsers() {
        if (userRepository.findByEmail("admin@nihao-urdu.com").isEmpty()) {
            userRepository.save(User.builder()
                    .id(UUID.randomUUID().toString())
                    .username("admin")
                    .email("admin@nihao-urdu.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .fullName("Admin User")
                    .role("ADMIN")
                    .status("ACTIVE")
                    .authProvider("PASSWORD")
                    .build());
        }

        if (userRepository.findByEmail("student@nihao-urdu.com").isEmpty()) {
            userRepository.save(User.builder()
                    .id(UUID.randomUUID().toString())
                    .username("student")
                    .email("student@nihao-urdu.com")
                    .passwordHash(passwordEncoder.encode("student123"))
                    .fullName("Test Student")
                    .role("STUDENT")
                    .status("ACTIVE")
                    .authProvider("PASSWORD")
                    .build());
        }
    }

    private void seedSampleContent() {
        User admin = userRepository.findByEmail("admin@nihao-urdu.com").orElseThrow();

        Course course = courseRepository.save(Course.builder()
                .id(UUID.randomUUID().toString())
                .name("Basic Chinese (HSK 1)")
                .hskLevel("HSK 1")
                .description("یہ کورس آپ کو چینی زبان کی بنیادی مہارتیں سکھائے گا، بشمول روزمرہ کے جملے اور پinin۔")
                .difficulty("Beginner")
                .status("PUBLISHED")
                .createdBy(admin.getId())
                .build());

        Unit unit1 = unitRepository.save(Unit.builder()
                .id(UUID.randomUUID().toString())
                .course(course)
                .unitNumber(1)
                .urduTitle("تعارف اور سلام")
                .hanziTitle("你好")
                .grammarPoint("استعمال '是' / Using 'SHI'")
                .hskLevel("1")
                .estimatedTime(20)
                .difficulty("Easy")
                .status("PUBLISHED")
                .version(1)
                .createdBy(admin.getId())
                .topics(List.of("Greetings", "Numbers"))
                .objectives(List.of("Say hello", "Count to 10"))
                .build());

        Unit unit2 = unitRepository.save(Unit.builder()
                .id(UUID.randomUUID().toString())
                .course(course)
                .unitNumber(2)
                .urduTitle("نمبر اور گنتی")
                .hanziTitle("一二三")
                .grammarPoint("Numbers / اعداد")
                .hskLevel("1")
                .estimatedTime(25)
                .difficulty("Easy")
                .status("PUBLISHED")
                .version(1)
                .createdBy(admin.getId())
                .topics(List.of("Numbers"))
                .objectives(List.of("Count to 10"))
                .build());

        Lesson lesson1 = lessonRepository.save(Lesson.builder()
                .id(UUID.randomUUID().toString())
                .unit(unit1)
                .lessonNumber(1)
                .lessonType("NORMAL")
                .urduTitle("ہیلو اور شکریہ")
                .instructionText("Learn basic greetings")
                .difficulty("Easy")
                .crowns(3)
                .status("PUBLISHED")
                .exercisesCount(2)
                .wordsCount(6)
                .createdBy(admin.getId())
                .build());

        Lesson lesson2 = lessonRepository.save(Lesson.builder()
                .id(UUID.randomUUID().toString())
                .unit(unit1)
                .lessonNumber(2)
                .lessonType("NORMAL")
                .urduTitle("آپ کا نام کیا ہے؟")
                .instructionText("Learn to ask names")
                .difficulty("Easy")
                .crowns(3)
                .status("PUBLISHED")
                .exercisesCount(1)
                .wordsCount(8)
                .createdBy(admin.getId())
                .build());

        lessonRepository.save(Lesson.builder()
                .id(UUID.randomUUID().toString())
                .unit(unit1)
                .lessonNumber(3)
                .lessonType("CHECKPOINT")
                .urduTitle("چیک پوائنٹ 1")
                .instructionText("Checkpoint review")
                .difficulty("Medium")
                .crowns(0)
                .status("PUBLISHED")
                .exercisesCount(0)
                .wordsCount(0)
                .createdBy(admin.getId())
                .build());

        lessonRepository.save(Lesson.builder()
                .id(UUID.randomUUID().toString())
                .unit(unit2)
                .lessonNumber(1)
                .lessonType("NORMAL")
                .urduTitle("1 سے 5")
                .instructionText("Count from 1 to 5")
                .difficulty("Easy")
                .crowns(3)
                .status("PUBLISHED")
                .exercisesCount(1)
                .wordsCount(5)
                .createdBy(admin.getId())
                .build());

        Vocabulary nihao = vocabularyRepository.save(Vocabulary.builder()
                .id(UUID.randomUUID().toString())
                .hanzi("你好")
                .pinyin("nǐ hǎo")
                .tone(3)
                .urduTranslation("ہیلو / سلام")
                .romanUrdu("Assalam-o-Alaikum")
                .literalGloss("تم اچھے (Literal: You good)")
                .partOfSpeech("interjection")
                .hskLevel(1)
                .frequency(5)
                .createdBy(admin.getId())
                .topics(List.of("Daily", "Greetings"))
                .build());

        ExampleSentence example = ExampleSentence.builder()
                .id(UUID.randomUUID().toString())
                .vocabulary(nihao)
                .hanzi("老师，你好！")
                .pinyin("Lǎoshī, nǐ hǎo!")
                .urdu("استاد، ہیلو!")
                .build();
        nihao.getExampleSentences().add(example);
        vocabularyRepository.save(nihao);

        vocabularyRepository.save(Vocabulary.builder()
                .id(UUID.randomUUID().toString())
                .hanzi("谢谢")
                .pinyin("xiè xiè")
                .tone(4)
                .urduTranslation("شکریہ")
                .romanUrdu("shukriya")
                .partOfSpeech("verb")
                .hskLevel(1)
                .frequency(5)
                .createdBy(admin.getId())
                .topics(List.of("Greetings", "Daily"))
                .build());

        vocabularyRepository.save(Vocabulary.builder()
                .id(UUID.randomUUID().toString())
                .hanzi("再见")
                .pinyin("zài jiàn")
                .tone(4)
                .urduTranslation("الوداع")
                .romanUrdu("alwida")
                .partOfSpeech("verb")
                .hskLevel(1)
                .frequency(4)
                .createdBy(admin.getId())
                .topics(List.of("Greetings"))
                .build());

        exerciseRepository.save(Exercise.builder()
                .id(UUID.randomUUID().toString())
                .lesson(lesson1)
                .exerciseType("teach")
                .exerciseOrder(1)
                .exerciseData("""
                        {
                          "promptUr": "اس لفظ کو سیکھیں",
                          "promptEn": "Learn this word",
                          "items": ["你好"],
                          "answer": "你好",
                          "gloss": [{"ur": "ہیلو", "en": "Hello"}],
                          "word": "你好",
                          "pinyin": "nǐ hǎo",
                          "urdu": "ہیلو"
                        }
                        """)
                .createdBy(admin.getId())
                .build());

        exerciseRepository.save(Exercise.builder()
                .id(UUID.randomUUID().toString())
                .lesson(lesson1)
                .exerciseType("pic")
                .exerciseOrder(2)
                .exerciseData("""
                        {
                          "promptUr": "ان میں سے 'ہیلو' منتخب کریں",
                          "promptEn": "Select 'Hello'",
                          "options": ["你好", "谢谢", "再见", "对不起"],
                          "answer": "你好",
                          "audioId": "audio_nihao"
                        }
                        """)
                .createdBy(admin.getId())
                .build());

        exerciseRepository.save(Exercise.builder()
                .id(UUID.randomUUID().toString())
                .lesson(lesson2)
                .exerciseType("teach")
                .exerciseOrder(1)
                .exerciseData("""
                        {
                          "promptUr": "نام پوچھنا",
                          "promptEn": "Ask a name",
                          "word": "你叫什么名字？",
                          "pinyin": "Nǐ jiào shénme míngzi?",
                          "urdu": "آپ کا نام کیا ہے؟"
                        }
                        """)
                .createdBy(admin.getId())
                .build());

        log.info("Sample mobile API data seeded for course {}", course.getId());
    }
}
