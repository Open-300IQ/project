package com.example.iq300.controller;

import com.example.iq300.domain.Answer;
import com.example.iq300.domain.Question;
import com.example.iq300.domain.User;
import com.example.iq300.service.AnswerService;
import com.example.iq300.service.QuestionService;
import com.example.iq300.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;


@RequiredArgsConstructor
@Controller
@RequestMapping("/answer")
public class AnswerController {

    private final QuestionService questionService;
    private final AnswerService answerService;
    private final UserService userService;

  
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{id}")
    public String createAnswer(Model model, @PathVariable("id") Long id, 
                               @Valid AnswerCreateForm answerCreateForm, BindingResult bindingResult, Principal principal) {
        Question question = this.questionService.getQuestion(id);
        User user = this.userService.getUser(principal.getName());
        if (bindingResult.hasErrors()) {
            model.addAttribute("question", question);
            return "question_detail";
        }
        this.answerService.create(question, answerCreateForm.getContent(), user);
        return String.format("redirect:/question/detail/%s", id);
    }
    
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String answerModify(AnswerCreateForm answerCreateForm, @PathVariable("id") Long id, Principal principal) {
        Answer answer = this.answerService.getAnswer(id);
        if (!answer.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정권한이 없습니다.");
        }
        answerCreateForm.setContent(answer.getContent());
        return "answer_form"; // 답변 수정 전용 폼으로 이동
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String answerModify(@Valid AnswerCreateForm answerCreateForm, BindingResult bindingResult,
                                @PathVariable("id") Long id, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "answer_form";
        }
        Answer answer = this.answerService.getAnswer(id);
        if (!answer.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정권한이 없습니다.");
        }
        this.answerService.modify(answer, answerCreateForm.getContent());
        // 수정 후 해당 질문 상세 페이지로 리다이렉트
        return String.format("redirect:/question/detail/%s", answer.getQuestion().getId());
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String answerDelete(@PathVariable("id") Long id, Principal principal) {
        Answer answer = this.answerService.getAnswer(id);
        if (!answer.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "삭제권한이 없습니다.");
        }
        this.answerService.delete(answer);
        return String.format("redirect:/question/detail/%s", answer.getQuestion().getId());
    }
}