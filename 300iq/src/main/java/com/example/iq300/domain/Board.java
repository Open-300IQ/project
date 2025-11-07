package com.example.iq300.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List; // 1. [추가] java.util.List 임포트
import java.util.Set; 

@Entity
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdDate;

    @ManyToOne
    private User author;
    
    @ManyToMany
    Set<User> voter;

    // 2. [추가] 이 게시글에 달린 댓글 목록 (Question의 answerList와 동일)
    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE)
    private List<Comment> commentList;
    @Column(columnDefinition = "integer default 0")
    private int viewCount = 0;
    

    // --- (기존 Getter/Setter는 그대로) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public Set<User> getVoter() {
        return voter;
    }
    public void setVoter(Set<User> voter) {
        this.voter = voter;
    }
    
    // 3. [추가] commentList의 Getter/Setter
    public List<Comment> getCommentList() {
        return commentList;
    }
    public void setCommentList(List<Comment> commentList) {
        this.commentList = commentList;
    }
    public int getViewCount() {
        return viewCount;
    }
    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }
}