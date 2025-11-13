package com.example.iq300.repository;

import com.example.iq300.domain.RealEstateTerm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RealEstateTermRepository extends JpaRepository<RealEstateTerm, Long> {

    // ======== [ 4. 페이징 및 검색 메서드 3개 추가 ] ========
    
    // 1. (초성 필터링용) initial(초성) 컬럼으로 페이징 조회
    Page<RealEstateTerm> findByInitial(String initial, Pageable pageable);

    // 2. (검색용) term(용어)에서 kw를 포함하는 데이터 페이징 조회
    Page<RealEstateTerm> findByTermContaining(String kw, Pageable pageable);

    // 3. (검색용) definition(설명)에서 kw를 포함하는 데이터 페이징 조회
    Page<RealEstateTerm> findByDefinitionContaining(String kw, Pageable pageable);
    
    // JpaRepository에 findAll(Pageable pageable)은 이미 포함되어 있음
}