package com.example.iq300.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // (추가)
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // (추가) @PreAuthorize 애너테이션을 사용하기 위해 필요
public class SecurityConfig {

	@Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                .requestMatchers(
                        new AntPathRequestMatcher("/"),
                        new AntPathRequestMatcher("/user/login"),
                        new AntPathRequestMatcher("/user/signup"),
                        new AntPathRequestMatcher("/board/detail/**"),
                        new AntPathRequestMatcher("/analysis"),
                        new AntPathRequestMatcher("/ai"),
                        
                        // ======== [ 이 2줄을 추가합니다 ] ========
                        new AntPathRequestMatcher("/question/list"),
                        new AntPathRequestMatcher("/question/detail/**")
                        
                ).permitAll()
                .anyRequest().authenticated()
            )
            .csrf((csrf) -> csrf
                .ignoringRequestMatchers(new AntPathRequestMatcher("/**")))
                
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