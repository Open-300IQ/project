package com.example.iq300.controller;

import com.example.iq300.dto.CalendarEventDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
@Controller
public class CalendarController {

    @GetMapping("/calendar")
    public String showCalendar(Model model) {
        
        List<CalendarEventDTO> events = new ArrayList<>();

        // [수정됨] DTO 생성자에 5번째 인자로 PDF 경로(url)를 추가합니다.
        // (Thymeleaf의 @{...} 경로는 컨트롤러에서 "/..." 경로로 바꿔줘야 합니다)
        
        // --- 1월 ---
        events.add(new CalendarEventDTO("청주테크노폴리스 하트리움(S4)", "2025-01-02", "1순위", "rank-1", "/pdf/1_2.pdf"));
        events.add(new CalendarEventDTO("청주테크노폴리스 하트리움(S4)", "2025-01-03", "2순위", "rank-2", "/pdf/1_3.pdf"));
        
        // --- 4월 ---
        events.add(new CalendarEventDTO("청주테크노폴리스 아테라 2차(A7)", "2025-04-14", "특별공급", "rank-special", "/pdf/4_14.pdf"));
        events.add(new CalendarEventDTO("청주테크노폴리스 아테라 2차(A7)", "2025-04-15", "특별공급", "rank-special", "/pdf/4_15.pdf"));
        events.add(new CalendarEventDTO("청주테크노폴리스 아테라 2차(A7)", "2025-04-16", "1순위", "rank-1", "/pdf/4_16.pdf"));
        events.add(new CalendarEventDTO("청주테크노폴리스 아테라 2차(A7)", "2025-04-17", "2순위", "rank-2", "/pdf/4_17.pdf"));

        // --- 6월 ---
        events.add(new CalendarEventDTO("동남 하늘채 에디크", "2025-06-23", "특별공급", "rank-special", "/pdf/6_23.pdf"));
        events.add(new CalendarEventDTO("동남 하늘채 에디크", "2025-06-24", "1순위", "rank-1", "/pdf/6_24.pdf")); // 원본 HTML에 6_24.pdf가 없었지만, 추정하여 추가
        events.add(new CalendarEventDTO("동남 하늘채 에디크", "2025-06-25", "2순위", "rank-2", "/pdf/6_25.pdf")); // 원본 HTML에 6_25.pdf가 없었지만, 추정하여 추가

        // --- 7월 ---
        events.add(new CalendarEventDTO("신분평 더웨이시티 제일풍경채", "2025-07-21", "특별공급", "rank-special", "/pdf/7_21.pdf"));
        events.add(new CalendarEventDTO("신분평 더웨이시티 제일풍경채", "2025-07-22", "1순위", "rank-1", "/pdf/7_22.pdf"));
        events.add(new CalendarEventDTO("신분평 더웨이시티 제일풍경채", "2025-07-23", "2순위", "rank-2", "/pdf/7_23.pdf"));

        // --- 8월 ---
        events.add(new CalendarEventDTO("청주 센텀 푸르지오 자이", "2025-08-04", "특별공급", "rank-special", "/pdf/8_4.pdf"));
        events.add(new CalendarEventDTO("청주 센텀 푸르지오 자이", "2025-08-05", "1순위", "rank-1", "/pdf/8_5.pdf"));
        events.add(new CalendarEventDTO("청주 센텀 푸르지오 자이", "2025-08-06", "2순위", "rank-2", "/pdf/8_6.pdf"));
        events.add(new CalendarEventDTO("동남 하늘채 에디크", "2025-08-18", "무순위", "rank-unranked", "/pdf/8_18.pdf"));
        events.add(new CalendarEventDTO("신분평 더웨이시티 제일풍경채", "2025-08-25", "무순위", "rank-unranked", "/pdf/8_25.pdf"));

        // --- 9월 ---
        events.add(new CalendarEventDTO("청주 센텀 푸르지오 자이", "2025-09-08", "무순위", "rank-unranked", "/pdf/9_8.pdf"));

        // --- 10월 ---
        events.add(new CalendarEventDTO("청주 롯데캐슬 시그니처", "2025-10-14", "특별공급", "rank-special", "/pdf/10_14.pdf"));
        events.add(new CalendarEventDTO("청주 롯데캐슬 시그니처", "2025-10-15", "1순위", "rank-1", "/pdf/10_15.pdf"));
        events.add(new CalendarEventDTO("청주 롯데캐슬 시그니처", "2025-10-16", "2순위", "rank-2", "/pdf/10_16.pdf"));
        events.add(new CalendarEventDTO("두산위브더제니스 센트럴파크", "2025-10-27", "특별공급", "rank-special", "/pdf/10_27.pdf"));
        events.add(new CalendarEventDTO("두산위브더제니스 센트럴파크", "2025-10-28", "1순위", "rank-1", "/pdf/10_28.pdf"));
        events.add(new CalendarEventDTO("두산위브더제니스 센트럴파크", "2025-10-29", "2순위", "rank-2", "/pdf/10_29.pdf"));
        

        // ... (JSON 변환 로직, 하단은 동일) ...
        ObjectMapper objectMapper = new ObjectMapper();
        String eventsJson = "[]"; 
        try {
            eventsJson = objectMapper.writeValueAsString(events);
        } catch (Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("eventsJson", eventsJson);
        model.addAttribute("activeMenu", "calendar");
        return "calendar"; 
    }
}