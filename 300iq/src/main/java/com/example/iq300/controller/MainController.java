package com.example.iq300.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
// (추가) Page 임포트
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam; // (추가)

import com.example.iq300.domain.Board;
import com.example.iq300.domain.MonthlyAvgPrice;
import com.example.iq300.service.BoardService;
import com.example.iq300.service.MonthlyAvgPriceService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class MainController {
	
	@Autowired
	private MonthlyAvgPriceService monthlyAvgPriceService;
	
    private final BoardService boardService;

    /**
     * 메인 페이지 ("/") - 자유게시판
     */
    @GetMapping("/")
    public String root(Model model,
                       @RequestParam(value="page", defaultValue="0") int page,
                       @RequestParam(value="kw", defaultValue="") String kw,
                       @RequestParam(value="searchType", defaultValue="subject") String searchType,
                       @RequestParam(value="sort", defaultValue="latest") String sortType) {
        
        Page<Board> paging = this.boardService.getPage(page, kw, searchType, sortType);
        
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("searchType", searchType);
        model.addAttribute("sortType", sortType);
        
        return "index"; // templates/index.html
    }


    /**
     * 자료 분석하기 페이지
     */
    @GetMapping("/analysis")
    public String analysis(Model model) {
        // 이 라인이 실행될 때 데이터가 조회되어야 합니다.
	    	List<MonthlyAvgPrice> avgPriceList = monthlyAvgPriceService.getDistrictAvgPriceData(); 
        model.addAttribute("avgPriceData", avgPriceList);
        model.addAttribute("activeMenu", "analysis");
        return "analysis";
    }

    /**
     * AI 상담받기 페이지
     */
    @GetMapping("/ai")
    public String ai(Model model) { // 👈 2. Model 파라미터 추가
        model.addAttribute("activeMenu", "ai"); // 👈 3. 이 줄 추가
        return "ai";
    }
   
}