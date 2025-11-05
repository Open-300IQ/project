package com.example.iq300.controller;

// (1. 추가) 부동산 Repository import
import com.example.iq300.repository.PriceTimeSeriesRepository;
import com.example.iq300.repository.TransactionRepository;
// ---

import com.example.iq300.domain.Board;
import com.example.iq300.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // (import 추가)
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping; 
import com.example.iq300.domain.User; 
import com.example.iq300.service.UserService; 

import java.security.Principal; 
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/board")
@Controller
public class BoardController {

    private final BoardService boardService;
    private final UserService userService; 

    // (2. 추가) 새로 만든 Repository 2개를 주입받습니다.
    private final TransactionRepository transactionRepo;
    private final PriceTimeSeriesRepository priceRepo;


    // (수정 1) 
    // 기존의 '/' (시작 페이지) 매핑을 -> '/board/list' (Q&A 게시판)로 리다이렉트시킵니다.
    @GetMapping("/")
    public String root() {
        return "redirect:/board/list"; 
    }

    // (수정 2)
    // 기존 부동산 현황 페이지는 '/dashboard'라는 새 주소로 매핑합니다.
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        
        model.addAttribute("transactions", transactionRepo.findTop20ByOrderByIdDesc());
        
        model.addAttribute("priceIndices", priceRepo.findByRegionAndMetricTypeOrderByYearMonthAsc(
                "충북 청주시 서원구", 
                "매매가격지수"
        ));
        
         model.addAttribute("landIndices", priceRepo.findByRegionAndMetricTypeOrderByYearMonthAsc(
                "충북 청주시 흥덕구", 
                "지가지수"
        ));

        return "index"; // index.html (부동산 현황)을 반환
    }


    // --- (이하 기존 BoardController 코드는 그대로 유지) ---
    
    @GetMapping("/list")
    public String list(Model model) {
        List<Board> boardList = this.boardService.getList();
        model.addAttribute("boardList", boardList);
        return "board/list";
    }

    // ( ... detail, create, edit, delete 등 나머지 메서드 모두 그대로 둠 ... )
    
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model) { // (Long 타입 확인)
        Board board = this.boardService.getBoard(id);
        model.addAttribute("board", board);
        return "board/detail";
    }

    @GetMapping("/create")
    public String postCreate() {
        return "board/post_form";
    }

    @PostMapping("/create")
    public String postCreate(Board board, Principal principal) {
        User user = this.userService.getUser(principal.getName()); // email로 User 조회
        this.boardService.create(board.getTitle(), board.getContent(), user);
        return "redirect:/board/list";
    }

    @GetMapping("/edit/{id}")
    public String postEdit(@PathVariable("id") Long id, Model model) { // (Long 타입 확인)
        Board board = this.boardService.getBoard(id);
        model.addAttribute("board", board);
        return "board/edit_form";
    }

    @PostMapping("/edit/{id}")
    public String postEdit(Board board, @PathVariable("id") Long id) { // (Long 타입 확인)
        this.boardService.update(id, board.getTitle(), board.getContent());
        return "redirect:/board/detail/" + id;
    }

    @GetMapping("/delete/{id}")
    public String postDelete(@PathVariable("id") Long id) { // (Long 타입 확인)
        this.boardService.delete(id);
        return "redirect:/board/list";
    }
}