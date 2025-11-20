package com.example.iq300.service;

import com.example.iq300.domain.HousingPolicy;
import com.example.iq300.repository.HousingPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HousingPolicyService {

    private final HousingPolicyRepository housingPolicyRepository;

    // 정책 목록 조회 (페이징 + 검색)
    public Page<HousingPolicy> getList(int page, String kw) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("id")); // 최신순 정렬
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        
        if (kw == null || kw.isEmpty()) {
            return housingPolicyRepository.findAll(pageable);
        } else {
            return housingPolicyRepository.findAllByKeyword(kw, pageable);
        }
    }

    // 청주/충북 관련 정책만 별도로 가져오기 (메인 화면용 등)
    public List<HousingPolicy> getCheongjuPolicies() {
        return housingPolicyRepository.findByRegionContaining("청주");
    }
    
    // 정책 상세 조회
    public HousingPolicy getPolicy(Long id) {
        return housingPolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("정책을 찾을 수 없습니다."));
    }
}