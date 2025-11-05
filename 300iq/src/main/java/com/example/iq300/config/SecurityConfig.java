package com.example.iq300.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

// ======== [ 이 import 구문이 누락되었습니다 ] ========
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
// ===============================================

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                        // (수정) 로그인 없이 접근 가능한 페이지 설정
                        .requestMatchers(
                                new AntPathRequestMatcher("/"),          // 1. 메인 페이지 (/)
                                new AntPathRequestMatcher("/user/signup"),  // 2. 회원가입 페이지
                                new AntPathRequestMatcher("/user/login")    // 3. 로그인 페이지
                        ).permitAll()
                        
                        // (수정) 모든 정적 리소스(CSS, JS 등) 접근 허용
                        .requestMatchers(
                                new AntPathRequestMatcher("/bootstrap.min.css"),
                                new AntPathRequestMatcher("/style.css"),
                                new AntPathRequestMatcher("/css/**"),
                                new AntPathRequestMatcher("/js/**"),
                                new AntPathRequestMatcher("/images/**")
                        ).permitAll()
                        
                        // (수정) 나머지 모든 요청은 인증(로그인)이 필요함
                        .anyRequest().authenticated()
                )
                
                .csrf((csrf) -> csrf
                        .ignoringRequestMatchers(new AntPathRequestMatcher("/**")))
                
                // (수정) import 구문을 추가했기 때문에 이 부분이 이제 정상 작동합니다.
                .headers((headers) -> headers
                        .addHeaderWriter(new XFrameOptionsHeaderWriter(
                                XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
                
                .formLogin((formLogin) -> formLogin
                        .loginPage("/user/login")
                        .defaultSuccessUrl("/"))
                
                .logout((logout) -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true))
        ;
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}