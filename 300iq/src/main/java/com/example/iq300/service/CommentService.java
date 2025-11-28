package com.example.iq300.service;

import com.example.iq300.domain.Board;
import com.example.iq300.domain.Comment;
import com.example.iq300.domain.User;
import com.example.iq300.exception.DataNotFoundException;
import com.example.iq300.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

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
    public Comment getComment(Long id) {
        Optional<Comment> comment = this.commentRepository.findById(id);
        if (comment.isPresent()) {
            return comment.get();
        } else {
            throw new DataNotFoundException("comment not found");
        }
    }
    public void modify(Comment comment, String content) {
        comment.setContent(content);
        comment.setModifyDate(LocalDateTime.now());
        this.commentRepository.save(comment);
    }
    public void delete(Comment comment) {
        this.commentRepository.delete(comment);
    }
}