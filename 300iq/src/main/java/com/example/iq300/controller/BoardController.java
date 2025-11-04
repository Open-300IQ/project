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
import org.springframework.web.bind.annotation.PostMapping; // (기존 코드에 필요할 수 있으므로)
import com.example.iq300.domain.User; // (기존 코드에 필요할 수 있으므로)
import com.example.iq300.service.UserService; // (기존 코드에 필요할 수 있으므로)

import java.security.Principal; // (기존 코드에 필요할 수 있으므로)
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/board")
@Controller
public class BoardController {

    private final BoardService boardService;
    private final UserService userService; // (기존 코드에 있었으므로 유지)

    // (2. 추가) 새로 만든 Repository 2개를 주입받습니다.
    private final TransactionRepository transactionRepo;
    private final PriceTimeSeriesRepository priceRepo;


    // (3. 수정) 기존의 '/' 매핑을 수정합니다.
    @GetMapping("/")
    public String root(Model model) {
        
        // (3. 핵심) 이 3줄이 빠져있는지 확인하세요!
        model.addAttribute("transactions", transactionRepo.findTop20ByOrderByIdDesc());
        
        model.addAttribute("priceIndices", priceRepo.findByRegionAndMetricTypeOrderByYearMonthAsc(
                "충북 청주시 서원구", 
                "매매가격지수"
        ));
        
         model.addAttribute("landIndices", priceRepo.findByRegionAndMetricTypeOrderByYearMonthAsc(
                "충북 청주시 흥덕구", 
                "지가지수"
        ));

        return "index"; 
    }

    // --- (이하 기존 BoardController 코드는 그대로 유지) ---
    
    @GetMapping("/list")
    public String list(Model model) {
        List<Board> boardList = this.boardService.getList();
        model.addAttribute("boardList", boardList);
        return "board/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Integer id, Model model) {
        Board board = this.boardService.getBoard(id);
        model.addAttribute("board", board);
        return "board/detail";
    }

    // ( ... postCreate, postEdit, getCreate, getEdit 등 GitHub의 나머지 메서드 모두 그대로 둠 ... )
    
    @GetMapping("/create")
    public String postCreate() {
        return "board/post_form";
    }

    @PostMapping("/create")
    public String postCreate(Board board, Principal principal) {
        User user = this.userService.getUser(principal.getName());
        this.boardService.create(board.getTitle(), board.getContent(), user);
        return "redirect:/board/list";
    }

    @GetMapping("/edit/{id}")
    public String postEdit(@PathVariable("id") Integer id, Model model) {
        Board board = this.boardService.getBoard(id);
        model.addAttribute("board", board);
        return "board/edit_form";
    }

    @PostMapping("/edit/{id}")
    public String postEdit(Board board, @PathVariable("id") Integer id) {
        this.boardService.update(id, board.getTitle(), board.getContent());
        return "redirect:/board/detail/" + id;
    }

    @GetMapping("/delete/{id}")
    public String postDelete(@PathVariable("id") Integer id) {
        this.boardService.delete(id);
        return "redirect:/board/list";
    }
}