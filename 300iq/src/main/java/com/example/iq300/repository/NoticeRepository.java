package com.example.iq300.repository;

import java.util.List; // List 임포트 확인
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.iq300.domain.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Page<Notice> findAll(Pageable pageable);

    // ▼▼▼ [추가] 작성일(CreateDate) 역순(Desc)으로 상위 2개(Top2)만 가져오기 ▼▼▼
    List<Notice> findTop2ByOrderByCreateDateDesc();
}