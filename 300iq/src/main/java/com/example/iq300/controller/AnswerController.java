package com.example.iq300.controller;

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
    public String createAnswer(Model model, @PathVariable("id") Long id, // Integer -> Long
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
}