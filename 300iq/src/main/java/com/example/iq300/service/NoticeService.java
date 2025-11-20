package com.example.iq300.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.iq300.domain.Notice;
import com.example.iq300.domain.User;
import com.example.iq300.exception.DataNotFoundException;
import com.example.iq300.repository.NoticeRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;

    // 공지사항 목록 조회 (최신순)
    public Page<Notice> getList(int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return this.noticeRepository.findAll(pageable);
    }

    // 공지사항 상세 조회
    public Notice getNotice(Long id) {
        Optional<Notice> notice = this.noticeRepository.findById(id);
        if (notice.isPresent()) {
            return notice.get();
        } else {
            throw new DataNotFoundException("공지사항을 찾을 수 없습니다.");
        }
    }
    
    // 공지사항 생성
    public void create(String title, String content, User user) {
        Notice n = new Notice();
        n.setTitle(title);
        n.setContent(content);
        n.setCreateDate(LocalDateTime.now());
        n.setAuthor(user);
        this.noticeRepository.save(n);
    }

    // ▼▼▼ [추가] 상단 고정용 최신 공지 2개 조회 ▼▼▼
    public List<Notice> getTop2Notices() {
        return this.noticeRepository.findTop2ByOrderByCreateDateDesc();
    }
}