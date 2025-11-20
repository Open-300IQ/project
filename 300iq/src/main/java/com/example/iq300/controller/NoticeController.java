package com.example.iq300.controller;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.iq300.domain.Notice;
import com.example.iq300.domain.User;
import com.example.iq300.service.NoticeService;
import com.example.iq300.service.UserService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/notice")
@RequiredArgsConstructor
@Controller
public class NoticeController {

    private final NoticeService noticeService;
    private final UserService userService;

    @GetMapping("/list")
    public String list(Model model, @RequestParam(value="page", defaultValue="0") int page) {
        Page<Notice> paging = this.noticeService.getList(page);
        model.addAttribute("paging", paging);
        model.addAttribute("activeMenu", "notice");
        return "notice_list";
    }

    @GetMapping("/detail/{id}")
    public String detail(Model model, @PathVariable("id") Long id) {
        Notice notice = this.noticeService.getNotice(id);
        model.addAttribute("notice", notice);
        model.addAttribute("activeMenu", "notice");
        return "notice_detail";
    }

    // ▼▼▼ [추가] 공지사항 등록 페이지 (관리자만 접근 가능) ▼▼▼
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/create")
    public String createNotice() {
        return "notice_form";
    }
    
    // ▼▼▼ [추가] 공지사항 저장 (관리자만 가능) ▼▼▼
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public String createNotice(@RequestParam("title") String title, @RequestParam("content") String content, Principal principal) {
        User siteUser = this.userService.getUser(principal.getName());
        this.noticeService.create(title, content, siteUser);
        return "redirect:/notice/list";
    }
}