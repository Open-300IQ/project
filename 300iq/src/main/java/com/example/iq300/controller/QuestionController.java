package com.example.iq300.controller;

import com.example.iq300.controller.AnswerCreateForm;
import com.example.iq300.controller.QuestionCreateForm;
import com.example.iq300.domain.Notice; // 1. [추가] Notice 임포트
import com.example.iq300.domain.Question;
import com.example.iq300.domain.User;
import com.example.iq300.service.NoticeService; // 2. [추가] NoticeService 임포트
import com.example.iq300.service.QuestionService;
import com.example.iq300.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List; // 3. [추가] List 임포트

@RequestMapping("/question")
@RequiredArgsConstructor
@Controller
public class QuestionController {

    private final QuestionService questionService;
    private final UserService userService;
    private final NoticeService noticeService; // 4. [추가] 서비스 주입

    @GetMapping("/list")
    public String list(Model model,
                       @RequestParam(value="page", defaultValue="0") int page,
                       @RequestParam(value="kw", defaultValue="") String kw,
                       @RequestParam(value="searchType", defaultValue="subject") String searchType,
                       @RequestParam(value="sort", defaultValue="latest") String sortType) {
        
        Page<Question> paging = this.questionService.getPage(page, kw, searchType, sortType);
        
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("searchType", searchType);
        model.addAttribute("sortType", sortType);
        model.addAttribute("activeMenu", "qna");

        // ▼▼▼ [추가] 상단 고정용 최신 공지 2개 가져오기 ▼▼▼
        List<Notice> notices = this.noticeService.getTop2Notices();
        model.addAttribute("notices", notices);
        // ▲▲▲

        return "question_list";
    }

    @GetMapping(value = "/detail/{id}")
    public String detail(Model model, @PathVariable("id") Long id, AnswerCreateForm answerCreateForm) {
        Question question = this.questionService.getQuestion(id);
        model.addAttribute("question", question);
        model.addAttribute("activeMenu", "qna");
        return "question_detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String questionCreate(Model model, QuestionCreateForm questionCreateForm) { 
        model.addAttribute("activeMenu", "qna"); 
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String questionCreate(@Valid QuestionCreateForm questionCreateForm, BindingResult bindingResult, Principal principal, Model model) { 
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "qna"); 
            return "question_form"; 
        }
        
        User user = this.userService.getUser(principal.getName());
        this.questionService.create(questionCreateForm.getSubject(), questionCreateForm.getContent(), user);
        
        return "redirect:/question/list"; 
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questionVote(Principal principal, @PathVariable("id") Long id) {
        Question question = this.questionService.getQuestion(id);
        User user = this.userService.getUser(principal.getName());
        this.questionService.vote(question, user);
        return String.format("redirect:/question/detail/%s", id);
    }
}