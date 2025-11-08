package com.example.iq300.service;

import com.example.iq300.domain.Board;
import com.example.iq300.domain.Comment;
import com.example.iq300.domain.User;
import com.example.iq300.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public void create(Board board, String content, User user) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setCreateDate(LocalDateTime.now());
        comment.setBoard(board);
        comment.setAuthor(user);
        this.commentRepository.save(comment);
    }
}