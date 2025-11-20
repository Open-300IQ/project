package com.example.iq300.controller;

import com.example.iq300.domain.Board;
import com.example.iq300.domain.Notice; 
import com.example.iq300.domain.User;
import com.example.iq300.service.BoardService;
import com.example.iq300.service.NoticeService; 
import com.example.iq300.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

import com.example.iq300.controller.CommentCreateForm;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final UserService userService;
    private final NoticeService noticeService; 

    // ▼▼▼ [수정] 파라미터 추가 (kw, searchType, sort) ▼▼▼
    @GetMapping("/list")
    public String list(Model model, 
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "kw", defaultValue = "") String kw,
                       @RequestParam(value = "searchType", defaultValue = "subject") String searchType,
                       @RequestParam(value = "sort", defaultValue = "latest") String sort) { // HTML name='sort'
        
        // 1. 서비스 호출 변경: getAllPosts(page) -> getPage(...)
        Page<Board> paging = this.boardService.getPage(page, kw, searchType, sort);
        
        // 2. 모델에 데이터 담기 (HTML에서 쓸 이름으로 정확히 매핑)
        model.addAttribute("paging", paging);      // ${paging}
        model.addAttribute("kw", kw);              // ${kw}
        model.addAttribute("searchType", searchType); // ${searchType}
        model.addAttribute("sortType", sort);      // ${sortType} (HTML 변수명과 일치시킴)
        model.addAttribute("activeMenu", "board");

        // 3. 공지사항 데이터 전달 (null 방지)
        List<Notice> notices = this.noticeService.getTop2Notices();
        model.addAttribute("notices", notices);    // ${notices}
        
        return "index";
    }
    // ▲▲▲

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model, 
                         CommentCreateForm commentCreateForm) { 
        Board board = boardService.getPostById(id);
        model.addAttribute("board", board);
        model.addAttribute("activeMenu", "board");
        return "board/detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String boardVote(Principal principal, @PathVariable("id") Long id) {
        Board board = this.boardService.getPostById(id);
        User user = this.userService.getUser(principal.getName());
        this.boardService.vote(board, user);
        return String.format("redirect:/board/detail/%s", id);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String newPost(Model model, BoardPostForm boardPostForm) { 
        model.addAttribute("activeMenu", "board");
        return "board/post_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String createPost(@Valid BoardPostForm boardPostForm, BindingResult bindingResult, Principal principal, Model model) { 
        if (bindingResult.hasErrors()) {
            model.addAttribute("activeMenu", "board"); 
            return "board/post_form";
        }
        User user = this.userService.getUser(principal.getName());
        this.boardService.createPost(boardPostForm.getTitle(),
                boardPostForm.getContent(), user);
                
        return "redirect:/board/list"; 
    }
}