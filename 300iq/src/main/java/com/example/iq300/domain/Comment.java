package com.example.iq300.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createDate;
    private LocalDateTime modifyDate;
    // 댓글은 Board(게시글)에 달려있음
    @ManyToOne
    private Board board; 

    // 댓글은 작성자(User)가 있음
    @ManyToOne
    private User author;
}