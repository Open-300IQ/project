package com.example.demo.controller; // 본인의 패키지 이름에 맞게 수정하세요.

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    // 웹 브라우저에서 http://localhost:8080/ 주소로 접속했을 때
    @GetMapping("/")
    public String mainPage() {
        // templates 폴더에 있는 index.html 파일을 찾아서 보여준다.
        return "index";
    }
}