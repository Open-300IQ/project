package com.example.iq300.domain; // (중요) 기존 도메인 패키지 사용

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor // JPA는 기본 생성자가 필요합니다.
public class PriceTimeSeries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String region;      // 지역 (예: "충북 청주시 서원구")
    private String metricType;  // 통계 항목 (예: "아파트 매매가격지수", "지가지수")
    private String propertyType; // 주택 유형 (예: "아파트", "지가", "거래현황")
    private String yearMonth;   // 데이터 년월 (예: "202509")
    private double value;       // 통계 값

    // CsvDataService에서 사용할 생성자
    public PriceTimeSeries(String region, String metricType, String propertyType, String yearMonth, double value) {
        this.region = region;
        this.metricType = metricType;
        this.propertyType = propertyType;
        this.yearMonth = yearMonth;
        this.value = value;
    }
}