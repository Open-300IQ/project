package com.example.iq300.service;

import com.example.iq300.domain.Board;
import com.example.iq300.domain.User;
import com.example.iq300.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.example.iq300.exception.DataNotFoundException; // (이것도 필요합니다)


@RequiredArgsConstructor
@Service
public class BoardService {

    private final BoardRepository boardRepository;

    // BoardController (line 60)가 호출하는 메서드
    public List<Board> getList() {
        return this.boardRepository.findAll();
    }

    // BoardController (line 67, 88)가 호출하는 메서드
    public Board getBoard(Long id) {
        Optional<Board> board = this.boardRepository.findById(id);
        if (board.isPresent()) {
            return board.get();
        } else {
            throw new DataNotFoundException("board not found");
        }
    }

    // BoardController (line 82)가 호출하는 메서드
    public void create(String title, String content, User user) {
        Board b = new Board();
        b.setTitle(title);
        b.setContent(content);
        b.setAuthor(user); // (GitHub 코드에 따라 author 설정)
        b.setCreateDate(LocalDateTime.now());
        this.boardRepository.save(b);
    }

    // BoardController (line 95)가 호출하는 메서드
    public void update(Long id , String title, String content) {
        Board board = getBoard(id); // (getBoard 메서드 재사용)
        board.setTitle(title);
        board.setContent(content);
        board.setModifyDate(LocalDateTime.now()); // (수정 날짜 추가 - 선택 사항)
        this.boardRepository.save(board);
    }
    
    // BoardController (line 101)가 호출하는 메서드
    public void delete(Long id) {
        Board board = getBoard(id); // (getBoard 메서드 재사용)
        this.boardRepository.delete(board);
    }
}