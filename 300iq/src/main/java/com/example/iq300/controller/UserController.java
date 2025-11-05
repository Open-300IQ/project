package com.example.iq300.controller;

import com.example.iq300.domain.User;
import com.example.iq300.repository.UserRepository; // 👈 1. UserRepository 임포트
import com.example.iq300.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor; // 👈 2. RequiredArgsConstructor 임포트
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.dao.DataIntegrityViolationException;
import java.security.Principal; // 👈 3. Principal 임포트

@Controller
@RequiredArgsConstructor // 👈 4. @RequiredArgsConstructor 어노테이션 추가
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository; // 👈 5. UserRepository 필드 선언

    /**
     * 회원가입 폼 페이지
     */
    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "signup_form"; // templates/signup_form.html 을 반환
    }

    /**
     * 회원가입 처리 (FR-001)
     */
    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            return "signup_form";
        }

        if (!userCreateForm.getPassword_1().equals(userCreateForm.getPassword_2())) {
            bindingResult.rejectValue("password_2", "passwordInCorrect", 
                    "2개의 패스워드가 일치하지 않습니다.");
            return "signup_form";
        }

        try {
            userService.createUser(
                userCreateForm.getEmail(), 
                userCreateForm.getNickname(), 
                userCreateForm.getPassword_1()
            );
        } catch(DataIntegrityViolationException e) {
            bindingResult.reject("signupFailed", "이미 등록된 사용자(이메일 또는 닉네임)입니다.");
            return "signup_form";
        } catch(Exception e) {
            bindingResult.reject("signupFailed", e.getMessage());
            return "signup_form";
        }

        return "redirect:/board/list"; 
    }

    /**
     * 로그인 폼 페이지
     */
    @GetMapping("/login")
    public String login() {
        return "login_form";
    }

    /**
     * 마이페이지 (GET)

     */
    @GetMapping("/mypage")
    public String myPage(Model model, Principal principal) {
        String email = principal.getName();
        
        // 6. 이제 userRepository가 무엇인지 알기 때문에 정상 작동합니다.
        User user = userRepository.findByEmail(email) 
                .orElseThrow(() -> new RuntimeException("로그인한 사용자를 찾을 수 없습니다."));
        
        model.addAttribute("user", user);
        return "mypage";
    }


    /**
     * 인증 신청 처리 (POST) (FR-018)
     */
    @PostMapping("/apply-verification")
    public String applyVerification(Principal principal) {
        userService.applyForVerification(principal.getName());
        return "redirect:/user/mypage";
    }
}