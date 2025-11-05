package com.example.iq300.service;

import com.example.iq300.domain.User;
import com.example.iq300.repository.UserRepository;
import com.example.iq300.exception.DataNotFoundException; // (필요)
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * UserController (signup)에서 호출
     */
    public User createUser(String email, String nickname, String password) {
        // 닉네임 중복 검사
        if (userRepository.findByNickname(nickname).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }

        User user = new User();
        user.setEmail(email);
        user.setNickname(nickname);
        user.setPassword(passwordEncoder.encode(password));
        user.setVerified(false); // 인증 상태 초기값
        this.userRepository.save(user);
        return user;
    }

    /**
     * UserController (applyVerification)에서 호출
     */
    public void applyForVerification(String email) {
        User user = getUser(email); // 아래 getUser 메서드 재사용
        user.setVerified(true); // (임시) 인증 신청 시 상태 변경
        this.userRepository.save(user);
        
        System.out.println(email + " (닉네임: " + user.getNickname() + ")님이 인증을 신청했습니다.");
    }

    /**
     * ID로 email을 사용하는 getUser 메서드
     */
    public User getUser(String email) {
        Optional<User> user = this.userRepository.findByEmail(email); 
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new DataNotFoundException("user not found");
        }
    }
}