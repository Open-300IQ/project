package com.example.iq300.controller;

import com.example.iq300.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    /**
     * 회원가입 페이지(signup_form.html)를 보여주는 메서드
     * (이것이 404 오류를 해결합니다)
     */
    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "signup_form";
    }

    /**
     * 회원가입 폼에서 '회원가입' 버튼을 눌렀을 때 POST 요청을 처리하는 메서드
     */
    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // 폼 유효성 검사 실패 시, 다시 회원가입 폼을 보여줌
            return "signup_form";
        }

        if (!userCreateForm.getPassword().equals(userCreateForm.getPassword2())) {
            // 2개의 비밀번호가 일치하지 않을 때
            bindingResult.rejectValue("password2", "passwordInCorrect", 
                    "2개의 패스워드가 일치하지 않습니다.");
            return "signup_form";
        }

        try {
            // 회원가입 서비스 실행
            userService.create(userCreateForm.getUsername(), 
                               userCreateForm.getEmail(), 
                               userCreateForm.getPassword());
        } catch(DataIntegrityViolationException e) {
            // ID 또는 이메일이 중복될 경우
            e.printStackTrace();
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "signup_form";
        } catch(Exception e) {
            e.printStackTrace();
            bindingResult.reject("signupFailed", e.getMessage());
            return "signup_form";
        }

        // 회원가입 성공 시, 루트(메인) 페이지로 보냄
        return "redirect:/";
    }

    /**
     * 로그인 페이지(login_form.html)를 보여주는 메서드
     */
    @GetMapping("/login")
    public String login() {
        return "login_form";
    }
}