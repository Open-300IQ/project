package com.example.iq300.repository;

import com.example.iq300.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 기본 CRUD 기능만 사용
}