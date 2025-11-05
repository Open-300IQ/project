package com.example.iq300.repository;

import com.example.iq300.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // (추가) UserController와 UserSecurityService가 사용할 메서드
    Optional<User> findByEmail(String email);

    // (추가) UserService가 닉네임 중복 검사에 사용할 메서드
    Optional<User> findByNickname(String nickname);
}