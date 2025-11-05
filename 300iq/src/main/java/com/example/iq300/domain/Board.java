package com.example.iq300.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne; // (추가)
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime; // (추가)

@Getter
@Setter
@Entity
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    // (수정 전) private String author;

    // (수정 후)
    @ManyToOne // User 1명이 여러 Board 작성 가능
    private User author; // 타입을 User 객체로 변경

    @Column
    private LocalDateTime createDate; // 생성일자 필드 추가

    @Column
    private LocalDateTime modifyDate; // 수정일자 필드 추가
}