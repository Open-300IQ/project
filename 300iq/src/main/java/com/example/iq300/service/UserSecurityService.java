package com.example.iq300.service;

import com.example.iq300.domain.User;
import com.example.iq300.domain.UserRole;
import com.example.iq300.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserSecurityService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Spring Security가 로그인을 진행할 때 호출하는 메서드입니다.
     * 사용자 이름(username)으로 User 엔티티를 조회합니다.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> _user = this.userRepository.findByUsername(username);
        if (_user.isEmpty()) {
            throw new UsernameNotFoundException("사용자를 찾을수 없습니다.");
        }
        User user = _user.get();

        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // 사용자 이름이 "admin"인 경우 ADMIN 권한을 부여하고,
        // 그 외에는 USER 권한을 부여합니다.
        if ("admin".equals(username)) {
            authorities.add(new SimpleGrantedAuthority(UserRole.ADMIN.getValue()));
        } else {
            authorities.add(new SimpleGrantedAuthority(UserRole.USER.getValue()));
        }
        
        // Spring Security의 UserDetails 객체를 반환합니다.
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), 
                user.getPassword(), 
                authorities
        );
    }
}