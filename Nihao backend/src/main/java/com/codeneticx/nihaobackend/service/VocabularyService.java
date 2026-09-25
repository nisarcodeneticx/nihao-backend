package com.codeneticx.nihaobackend.service;

import com.codeneticx.nihaobackend.dto.request.ExampleRequest;
import com.codeneticx.nihaobackend.dto.request.VocabularyRequest;
import com.codeneticx.nihaobackend.dto.response.PaginatedResponse;
import com.codeneticx.nihaobackend.dto.response.VocabularyResponse;
import com.codeneticx.nihaobackend.exception.ResourceNotFoundException;
import com.codeneticx.nihaobackend.mapper.AdminMapper;
import com.codeneticx.nihaobackend.model.ExampleSentence;
import com.codeneticx.nihaobackend.model.Vocabulary;
import com.codeneticx.nihaobackend.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final AdminMapper adminMapper;

    @Transactional(readOnly = true)
    public PaginatedResponse<VocabularyResponse> getAll(String search, Integer hskLevel, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Vocabulary> results = vocabularyRepository.searchVocabularyPage(
                hskLevel,
                emptyToNull(search),
                pageable
        );
        Page<VocabularyResponse> mapped = results.map(adminMapper::toVocabularyResponse);
        return PaginatedResponse.from(mapped);
    }

    @Transactional(readOnly = true)
    public VocabularyResponse getById(String id) {
        return adminMapper.toVocabularyResponse(findVocabulary(id));
    }

    @Transactional
    public VocabularyResponse create(VocabularyRequest request, String userId) {
        Vocabulary vocabulary = buildVocabulary(UUID.randomUUID().toString(), request, userId);
        vocabularyRepository.save(vocabulary);
        return adminMapper.toVocabularyResponse(vocabulary);
    }

    @Transactional
    public VocabularyResponse update(String id, VocabularyRequest request) {
        Vocabulary vocabulary = findVocabulary(id);
        applyRequest(vocabulary, request);
        vocabularyRepository.save(vocabulary);
        return adminMapper.toVocabularyResponse(vocabulary);
    }

    @Transactional
    public void delete(String id) {
        Vocabulary vocabulary = vocabularyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary not found: " + id));
        if (vocabulary.getTopics() != null) {
            vocabulary.getTopics().clear();
        }
        if (vocabulary.getExampleSentences() != null) {
            vocabulary.getExampleSentences().clear();
        }
        vocabularyRepository.save(vocabulary);
        vocabularyRepository.delete(vocabulary);
    }

    @Transactional
    public List<VocabularyResponse> bulkImport(List<VocabularyRequest> requests, String userId) {
        List<VocabularyResponse> created = new ArrayList<>();
        for (VocabularyRequest request : requests) {
            Vocabulary vocabulary = buildVocabulary(UUID.randomUUID().toString(), request, userId);
            vocabularyRepository.save(vocabulary);
            created.add(adminMapper.toVocabularyResponse(vocabulary));
        }
        return created;
    }

    private Vocabulary buildVocabulary(String id, VocabularyRequest request, String userId) {
        Vocabulary vocabulary = Vocabulary.builder()
                .id(id)
                .hanzi(request.getHanzi())
                .pinyin(request.getPinyin())
                .tone(defaultInt(request.getTone(), 0))
                .urduTranslation(request.getUrduTranslation())
                .romanUrdu(request.getRomanUrdu())
                .literalGloss(request.getLiteralGloss())
                .partOfSpeech(request.getPartOfSpeech())
                .hskLevel(defaultInt(request.getHskLevel(), 1))
                .frequency(defaultInt(request.getFrequency(), 0))
                .strokeCount(request.getStrokeCount())
                .radical(request.getRadical())
                .audioMaleUrl(request.getAudioMaleUrl())
                .audioFemaleUrl(request.getAudioFemaleUrl())
                .illustrationUrl(request.getIllustrationUrl())
                .topics(request.getTopics() != null ? new ArrayList<>(request.getTopics()) : new ArrayList<>())
                .createdBy(userId)
                .build();
        applyExamples(vocabulary, request.getExamples());
        return vocabulary;
    }

    private void applyRequest(Vocabulary vocabulary, VocabularyRequest request) {
        vocabulary.setHanzi(request.getHanzi());
        vocabulary.setPinyin(request.getPinyin());
        if (request.getTone() != null) {
            vocabulary.setTone(request.getTone());
        }
        vocabulary.setUrduTranslation(request.getUrduTranslation());
        if (request.getRomanUrdu() != null) {
            vocabulary.setRomanUrdu(request.getRomanUrdu());
        }
        if (request.getLiteralGloss() != null) {
            vocabulary.setLiteralGloss(request.getLiteralGloss());
        }
        if (request.getPartOfSpeech() != null) {
            vocabulary.setPartOfSpeech(request.getPartOfSpeech());
        }
        if (request.getHskLevel() != null) {
            vocabulary.setHskLevel(request.getHskLevel());
        }
        if (request.getTopics() != null) {
            vocabulary.setTopics(new ArrayList<>(request.getTopics()));
        }
        if (request.getFrequency() != null) {
            vocabulary.setFrequency(request.getFrequency());
        }
        if (request.getStrokeCount() != null) {
            vocabulary.setStrokeCount(request.getStrokeCount());
        }
        if (request.getRadical() != null) {
            vocabulary.setRadical(request.getRadical());
        }
        if (request.getAudioMaleUrl() != null) {
            vocabulary.setAudioMaleUrl(request.getAudioMaleUrl());
        }
        if (request.getAudioFemaleUrl() != null) {
            vocabulary.setAudioFemaleUrl(request.getAudioFemaleUrl());
        }
        if (request.getIllustrationUrl() != null) {
            vocabulary.setIllustrationUrl(request.getIllustrationUrl());
        }
        if (request.getExamples() != null) {
            vocabulary.getExampleSentences().clear();
            applyExamples(vocabulary, request.getExamples());
        }
    }

    private void applyExamples(Vocabulary vocabulary, List<ExampleRequest> examples) {
        if (examples == null) {
            return;
        }
        for (ExampleRequest exampleRequest : examples) {
            ExampleSentence example = ExampleSentence.builder()
                    .id(UUID.randomUUID().toString())
                    .vocabulary(vocabulary)
                    .hanzi(exampleRequest.getHanzi())
                    .pinyin(exampleRequest.getPinyin())
                    .urdu(exampleRequest.getUrdu())
                    .build();
            vocabulary.getExampleSentences().add(example);
        }
    }

    private Vocabulary findVocabulary(String id) {
        return vocabularyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary not found: " + id));
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private int defaultInt(Integer value, int fallback) {
        return value == null ? fallback : value;
    }
}
