package com.example.iq300.service;

import com.example.iq300.domain.RealEstateTerm;
import com.example.iq300.repository.RealEstateTermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DictionaryService {

    private final RealEstateTermRepository realEstateTermRepository;

    // ======== [ 5. 로직 전체 수정 ] ========
    public Page<RealEstateTerm> getList(String part, String searchType, String kw, int page) {
        
        // 1. 페이징 설정: 10개씩, "term"(용어) 기준으로 오름차순 정렬
        // (page는 0부터 시작)
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.ASC, "term"));

        // 2. [요구사항 2: 검색] 검색어가 있는 경우 (kw != null)
        if (kw != null && !kw.isEmpty()) {
            
            // 'part' (초성)을 무시하고 전체 DB에서 검색
            if ("content".equals(searchType)) {
                // (2-1) '설명'으로 검색
                return realEstateTermRepository.findByDefinitionContaining(kw, pageable);
            } else {
                // (2-2) '용어'로 검색 (기본값)
                return realEstateTermRepository.findByTermContaining(kw, pageable);
            }
        }

        // 3. [요구사항 1: 페이징] 검색어가 없는 경우 (kw == null)
        // 'part' (초성)에 따라 DB에서 필터링
        
        if (part == null || part.isEmpty() || "전체".equals(part)) {
            // (3-1) '전체' 또는 기본값
            return realEstateTermRepository.findAll(pageable);
        } else {
            // (3-2) 'ㄱ', 'ㄴ', 'A-Z' 등 선택된 초성
            return realEstateTermRepository.findByInitial(part, pageable);
        }
    }
}