package com.example.iq300.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class HousingPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String policyNo;
    private String title;
    private String organizer;
    private String region;
    private String targetAge;
    
    // ▼▼▼ [수정] 긴 글이 들어가는 곳은 모두 TEXT로 변경 ▼▼▼
    @Column(columnDefinition = "TEXT")
    private String description;    // 정책 설명

    @Column(columnDefinition = "TEXT")
    private String benefits;       // 혜택 내용

    @Column(columnDefinition = "TEXT")
    private String applicationMethod; // 신청 방법 (★에러 원인★)
    
    @Column(columnDefinition = "TEXT")
    private String eligibility;       // 자격 요건 (추가 추천)

    @Column(columnDefinition = "TEXT")
    private String aplyUrl;        // 신청 URL (URL도 길 수 있음)
    // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

    private String contactInfo;
    private String status;
    private String startDate;
    private String endDate;
}