package com.example.iq300.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Answer { // <-- 이 파일이 없어서 오류가 난 겁니다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content; // 답변 내용

    private LocalDateTime createDate; // 생성일시

    @ManyToOne // 답변자
    private User author;

    @ManyToOne // 이 답변이 달린 질문
    private Question question;
}