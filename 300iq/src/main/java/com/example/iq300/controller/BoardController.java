package com.example.iq300.controller;

import com.example.iq300.domain.Board;
import com.example.iq300.domain.User;
// import com.example.iq300.exception.DataNotFoundException; // (getPostById가 예외를 던지므로 필요 X)
import com.example.iq300.service.BoardService;
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

// 1. [추가] 댓글 폼 임포트
import com.example.iq300.controller.CommentCreateForm;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final UserService userService;

    // (list 메서드는 그대로)
    @GetMapping("/list")
    public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page) {
        Page<Board> paging = this.boardService.getAllPosts(page);
        model.addAttribute("paging", paging);
        return "index"; 
    }

    // 2. [수정] detail 메서드에 CommentCreateForm 추가
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model, 
                         CommentCreateForm commentCreateForm) { 
        
        Board board = boardService.getPostById(id);
        model.addAttribute("board", board);
        // commentCreateForm은 파라미터로 받으면 자동으로 모델에 추가됩니다.
        
        return "board/detail";
    }

    // (vote 메서드는 그대로)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String boardVote(Principal principal, @PathVariable("id") Long id) {
        Board board = this.boardService.getPostById(id);
        User user = this.userService.getUser(principal.getName());
        this.boardService.vote(board, user);
        return String.format("redirect:/board/detail/%s", id);
    }

    // (newPost 메서드는 그대로)
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String newPost(BoardPostForm boardPostForm) {
        return "board/post_form";
    }

    // (createPost 메서드는 그대로)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String createPost(@Valid BoardPostForm boardPostForm, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "board/post_form";
        }
        User user = this.userService.getUser(principal.getName());
        this.boardService.createPost(boardPostForm.getTitle(),
                boardPostForm.getContent(), user);
        return "redirect:/"; // 루트 페이지로 리다이렉트
    }
}