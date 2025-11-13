package com.example.iq300.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class RealEstateTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200)
    private String term; // 용어

    // ======== [ 1. '초성' 컬럼 추가 ] ========
    @Column(length = 10)
    private String initial; // 초성 (ㄱ, ㄴ, ... A-Z)
    // =====================================

    @Column(columnDefinition = "TEXT")
    private String definition; // 설명
}