package com.example.iq300.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    /**
     * 루트 URL ("/") 접속 시 index.html 템플릿을 반환합니다.
     */
    @GetMapping("/")
    public String root() {
        return "index"; // templates/index.html 을 렌더링
    }
}