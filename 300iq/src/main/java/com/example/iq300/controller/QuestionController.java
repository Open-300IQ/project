package com.example.iq300.controller;

// 1. 필요한 폼 클래스 임포트
import com.example.iq300.controller.AnswerCreateForm;
import com.example.iq300.controller.QuestionCreateForm;
import com.example.iq300.domain.Question;
import com.example.iq300.domain.User;
import com.example.iq300.service.QuestionService;
import com.example.iq300.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
// import org.springframework.http.HttpStatus; // (사용 안 함)
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
// import org.springframework.web.server.ResponseStatusException; // (사용 안 함)

import java.security.Principal;

@RequestMapping("/question")
@RequiredArgsConstructor
@Controller
public class QuestionController {

    private final QuestionService questionService;
    private final UserService userService;

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
    public String questionCreate(Model model, QuestionCreateForm questionCreateForm) { // 1. Model 추가
        model.addAttribute("activeMenu", "qna"); // 2. 이 줄 추가
        return "question_form";
    }

 
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String questionCreate(@Valid QuestionCreateForm questionCreateForm, BindingResult bindingResult, Principal principal, Model model) { // 1. Model 추가
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "qna"); // 2. 이 줄 추가
            return "question_form"; 
        }
        
        User user = this.userService.getUser(principal.getName());
        this.questionService.create(questionCreateForm.getSubject(), questionCreateForm.getContent(), user);
        
        return "redirect:/question/list"; 
    }
    // 3. [추가] 추천 기능 (이전에 빠져있던 것)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questionVote(Principal principal, @PathVariable("id") Long id) {
        Question question = this.questionService.getQuestion(id);
        User user = this.userService.getUser(principal.getName());
        this.questionService.vote(question, user);
        return String.format("redirect:/question/detail/%s", id);
    }
}