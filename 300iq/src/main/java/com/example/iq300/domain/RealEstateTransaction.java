package com.example.iq300.domain; // (중요) 기존 도메인 패키지 사용

import jakarta.persistence.Column;
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
public class RealEstateTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30) private String transactionType; // "아파트(매매)", "아파트(전월세)" 등
    @Column(length = 255) private String address;         // 시군구 (예: "충청북도 청주시 흥덕구")
    @Column(length = 30) private String buildingName;    // 도로명
    private double area;            // 전용면적(㎡)
    @Column(length = 30) private String contractDate;    // 계약년월일 (예: "20251009")

    private int price;           // 거래금액(만원) 또는 보증금(만원)
    private int rent;            // 월세(만원) (매매의 경우 0)

}