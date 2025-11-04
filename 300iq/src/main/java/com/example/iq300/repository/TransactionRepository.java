package com.example.iq300.repository; // (중요) 기존 리포지토리 패키지 사용

import com.example.iq300.domain.RealEstateTransaction; // (7번 파일 - domain 패키지)
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<RealEstateTransaction, Long> {

    // 4번 BoardController가 메인 페이지에서 사용할 메서드입니다.
    // (예시) 최근 20개의 거래를 ID 역순(최신순)으로 찾기
    List<RealEstateTransaction> findTop20ByOrderByIdDesc();
}