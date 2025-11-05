package com.example.iq300.service;

import com.example.iq300.domain.User;
import com.example.iq300.repository.UserRepository;
import com.example.iq300.domain.UserRole;
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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // (중요) 로그인 폼에서 'username'으로 넘어온 값은 사실 'email'입니다.
        
        // (수정) 
        // 27라인 오류: findByUsername(username) -> findByEmail(username)
        Optional<User> _user = this.userRepository.findByEmail(username); 
        
        if (_user.isEmpty()) {
            // (수정) 오류 메시지도 email 기준으로 변경
            throw new UsernameNotFoundException("사용자(이메일)를 찾을 수 없습니다.");
        }
        User user = _user.get();
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        if ("admin".equals(user.getNickname())) { // 닉네임이 admin이면
            authorities.add(new SimpleGrantedAuthority(UserRole.ADMIN.getValue()));
        } else {
            authorities.add(new SimpleGrantedAuthority(UserRole.USER.getValue()));
        }
        
        // (중요) principal.getName()이 될 값으로 email을 반환합니다.
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
    }
}