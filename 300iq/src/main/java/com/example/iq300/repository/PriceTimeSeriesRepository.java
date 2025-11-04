package com.example.iq300.repository; // (중요) 기존 리포지토리 패키지 사용

import com.example.iq300.domain.PriceTimeSeries; // (6번 파일 - domain 패키지)
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PriceTimeSeriesRepository extends JpaRepository<PriceTimeSeries, Long> {

    // 4번 BoardController에서 메인 페이지 데이터를 조회할 때 사용할 메서드입니다.
    // (예시) 특정 지역의 특정 통계 항목을 시간순으로 조회
    List<PriceTimeSeries> findByRegionAndMetricTypeOrderByYearMonthAsc(String region, String metricType);
}