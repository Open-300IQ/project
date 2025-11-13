package com.example.iq300.controller;

import com.example.iq300.domain.RealEstateTerm;
import com.example.iq300.service.DictionaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page; // [추가]
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
public class DictionaryController {

    private final DictionaryService dictionaryService;

    @GetMapping("/dictionary/list")
    public String list(Model model,
                       // ======== [ 6. 'page' 파라미터 추가 ] ========
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "part", defaultValue = "전체") String part, // 기본값 '전체'로
                       @RequestParam(value = "kw", required = false) String kw,
                       @RequestParam(value = "searchType", defaultValue = "term") String searchType) {

        // [수정] Service 호출 시 page 파라미터 전달, 반환값을 Page로 받기
        Page<RealEstateTerm> paging = this.dictionaryService.getList(part, searchType, kw, page);
        
        // [수정] Model에 'list' 대신 'paging' 객체를 전달
        model.addAttribute("paging", paging);
        
        // 검색어와 타입을 유지하기 위해 model에 추가
        model.addAttribute("part", part); 
        model.addAttribute("kw", kw);
        model.addAttribute("searchType", searchType);
        model.addAttribute("activeMenu", "dictionary");

        return "dictionary_list";
    }
}