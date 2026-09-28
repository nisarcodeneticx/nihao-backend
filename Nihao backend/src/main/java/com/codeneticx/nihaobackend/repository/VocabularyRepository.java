package com.codeneticx.nihaobackend.repository;

import com.codeneticx.nihaobackend.model.Vocabulary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, String> {
    List<Vocabulary> findByHskLevelOrderByHanziAsc(Integer hskLevel);

    @Query("SELECT v FROM Vocabulary v WHERE " +
            "(:hskLevel IS NULL OR v.hskLevel = :hskLevel) AND " +
            "(:search IS NULL OR :search = '' OR LOWER(v.hanzi) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.pinyin) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.urduTranslation) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY v.hanzi ASC")
    List<Vocabulary> searchVocabulary(@Param("hskLevel") Integer hskLevel, @Param("search") String search);

    @Query("SELECT v FROM Vocabulary v WHERE " +
            "(:hskLevel IS NULL OR v.hskLevel = :hskLevel) AND " +
            "(:search IS NULL OR :search = '' OR LOWER(v.hanzi) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.pinyin) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.urduTranslation) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Vocabulary> searchVocabularyPage(@Param("hskLevel") Integer hskLevel,
                                          @Param("search") String search,
                                          Pageable pageable);
}
