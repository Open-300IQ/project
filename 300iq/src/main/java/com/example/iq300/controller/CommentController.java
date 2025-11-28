package com.example.iq300.controller;

import com.example.iq300.domain.Board;
import com.example.iq300.domain.Comment;
import com.example.iq300.domain.User;
import com.example.iq300.service.BoardService;
import com.example.iq300.service.CommentService;
import com.example.iq300.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

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
            return "board/detail";
        }
        this.commentService.create(board, commentCreateForm.getContent(), user);
        return String.format("redirect:/board/detail/%s", id);
    }

    // ▼▼▼ [추가] 댓글 수정 화면 보여주기 ▼▼▼
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String commentModify(CommentCreateForm commentCreateForm, @PathVariable("id") Long id, Principal principal) {
        Comment comment = this.commentService.getComment(id);
        if (!comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        commentCreateForm.setContent(comment.getContent());
        return "comment_form"; // 댓글 수정 전용 폼으로 이동
    }

    // ▼▼▼ [추가] 댓글 수정 처리 ▼▼▼
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String commentModify(@Valid CommentCreateForm commentCreateForm, BindingResult bindingResult,
                                @PathVariable("id") Long id, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "comment_form";
        }
        Comment comment = this.commentService.getComment(id);
        if (!comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.commentService.modify(comment, commentCreateForm.getContent());
        // 수정 후 해당 게시글 상세 페이지로 리다이렉트 (앵커 #comment_id 추가 가능)
        return String.format("redirect:/board/detail/%s", comment.getBoard().getId());
    }

    // ▼▼▼ [추가] 댓글 삭제 처리 ▼▼▼
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String commentDelete(@PathVariable("id") Long id, Principal principal) {
        Comment comment = this.commentService.getComment(id);
        if (!comment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        this.commentService.delete(comment);
        return String.format("redirect:/board/detail/%s", comment.getBoard().getId());
    }
}