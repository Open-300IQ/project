package com.example.iq300.repository;

import com.example.iq300.domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    // JpaRepository의 기본 기능(save, findById 등)만 사용합니다.
}