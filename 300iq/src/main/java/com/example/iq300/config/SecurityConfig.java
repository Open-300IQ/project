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

@Configuration
@EnableWebSecurity 
public class SecurityConfig {

    @Bean 
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                // (수정) "/board/dashboard" (부동산 현황)도 허용 목록에 추가
                .requestMatchers("/", "/board/", "/board/list", "/board/dashboard", "/css/**", "/js/**", "/img/**", "/vendor/**").permitAll() 
                .requestMatchers("/user/signup").permitAll()
                .anyRequest().authenticated() 
            )
            .csrf((csrf) -> csrf
                .ignoringRequestMatchers(
                    new AntPathRequestMatcher("/user/signup")
                )
            )
            .formLogin((formLogin) -> formLogin
                .loginPage("/user/login") 
                // (수정) 로그인 성공 시 /board/list 로 이동
                .defaultSuccessUrl("/board/list") 
                .permitAll()
            )
            .logout((logout) -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout")) 
                // (수정) 로그아웃 성공 시 /board/list 로 이동
                .logoutSuccessUrl("/board/list") 
                .invalidateHttpSession(true) 
            );
            
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}