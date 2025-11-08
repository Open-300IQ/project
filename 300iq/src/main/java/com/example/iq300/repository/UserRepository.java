package com.example.iq300.repository;

import com.example.iq300.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    // Spring Security가 사용자 이름을 기반으로 유저를 찾을 때 이 메서드를 사용합니다.
    Optional<User> findByUsername(String username);
}