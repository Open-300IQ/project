package com.example.iq300.controller;

import com.example.iq300.domain.Board;
import com.example.iq300.domain.User;
import com.example.iq300.service.BoardService;
import com.example.iq300.service.CommentService;
import com.example.iq300.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@RequestMapping("/comment")
@RequiredArgsConstructor
@Controller
public class CommentController {

    private final BoardService boardService;
    private final CommentService commentService;
    private final UserService userService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{id}")
    public String createComment(Model model, @PathVariable("id") Long id,
                                @Valid CommentCreateForm commentCreateForm, 
                                BindingResult bindingResult, Principal principal) {
        
        Board board = this.boardService.getPostById(id);
        User user = this.userService.getUser(principal.getName());

        if (bindingResult.hasErrors()) {
            model.addAttribute("board", board);
            // 폼 유효성 검사 실패 시, 상세 페이지로 다시 렌더링
            return "board/detail"; 
        }

        this.commentService.create(board, commentCreateForm.getContent(), user);
        
        // 성공 시, 상세 페이지로 리다이렉트
        return String.format("redirect:/board/detail/%s", id);
    }
}