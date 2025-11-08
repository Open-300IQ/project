package com.example.iq300.service;

import com.example.iq300.domain.User;
import com.example.iq300.exception.DataNotFoundException;
import com.example.iq300.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime; // isVerified를 위해 추가
import java.util.Optional; 

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User create(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        this.userRepository.save(user);
        return user;
    }

    /**
     * User 객체를 Optional로 반환 (Iq300Application.java에서 사용)
     */
    public Optional<User> findUser(String username) {
        return this.userRepository.findByUsername(username);
    }

    /**
     * User 객체를 바로 반환 (Controller에서 사용)
     * (없으면 DataNotFoundException 발생)
     */
    public User getUser(String username) {
        Optional<User> user = this.userRepository.findByUsername(username);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new DataNotFoundException("user not found");
        }
    }
}