package com.example.iq300.repository;

import com.example.iq300.domain.HousingPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HousingPolicyRepository extends JpaRepository<HousingPolicy, Long> {
    
    // 지역으로 검색 (예: "청주"가 포함된 정책)
    List<HousingPolicy> findByRegionContaining(String region);

    // 페이징 처리가 된 전체 목록 조회 (최신순)
    Page<HousingPolicy> findAll(Pageable pageable);
    
    // 검색 기능 (정책명 또는 내용에 검색어 포함)
    @Query("select p from HousingPolicy p where p.title like %:kw% or p.description like %:kw%")
    Page<HousingPolicy> findAllByKeyword(@Param("kw") String kw, Pageable pageable);
}