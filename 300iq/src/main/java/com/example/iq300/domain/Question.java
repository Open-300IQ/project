package com.example.iq300.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set; // 1. Set 임포트 추가

@Getter
@Setter
@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createDate;

    @OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE)
    private List<Answer> answerList;

    @ManyToOne
    private User author;
    
    // 2. 추천인(voter) 필드를 @ManyToMany로 추가
    @ManyToMany
    Set<User> voter;
 // 1. [추가] 조회수 필드 (기본값 0)
    @Column(columnDefinition = "integer default 0")
    private int viewCount = 0;
}