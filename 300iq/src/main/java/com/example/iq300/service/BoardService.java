package com.example.iq300.service;

import com.example.iq300.domain.Board;
import com.example.iq300.domain.User;
import com.example.iq300.exception.DataNotFoundException;
import com.example.iq300.repository.BoardRepository;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional; 

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    // [수정] sortType 파라미터 추가
    private Specification<Board> search(String kw, String searchType, String sortType) {
        return new Specification<>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<Board> b, CriteriaQuery<?> query, CriteriaBuilder cb) {
                
                // [수정] "popularity" 정렬 로직을 Specification으로 이동
                if ("popularity".equals(sortType)) {
                    // voter(추천인) 컬렉션의 size(개수)로 정렬
                    query.orderBy(cb.desc(cb.size(b.get("voter"))));
                }
                
                if (kw == null || kw.trim().isEmpty()) {
                    return cb.conjunction();
                }
                
                String likePattern = "%" + kw.toLowerCase() + "%";

                switch (searchType) {
                    case "subject": 
                        return cb.like(cb.lower(b.get("title")), likePattern);
                    case "content": 
                        return cb.like(cb.lower(b.get("content")), likePattern);
                    case "author": 
                        query.distinct(true); // JOIN 시에만 distinct 적용
                        Join<Board, User> u = b.join("author", JoinType.INNER);
                        return cb.like(cb.lower(u.get("username")), likePattern);
                    default: 
                        Predicate titleLike = cb.like(cb.lower(b.get("title")), likePattern);
                        Predicate contentLike = cb.like(cb.lower(b.get("content")), likePattern);
                        return cb.or(titleLike, contentLike);
                }
            }
        };
    }

    public Page<Board> getPage(int page, String kw, String searchType, String sortType) {
        
        List<Sort.Order> sorts = new ArrayList<>();
        
        // [수정] 인기순 정렬은 Specification이 처리하므로, 여기서는 최신순만 처리
        if ("latest".equals(sortType) || !"popularity".equals(sortType)) {
            sorts.add(Sort.Order.desc("createdDate"));
        }

        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        // [수정] search 메서드에 sortType 전달
        Specification<Board> spec = search(kw, searchType, sortType); 
        
        return this.boardRepository.findAll(spec, pageable);
    }

    // --- (이하 나머지 메서드는 동일) ---

    public Page<Board> getAllPosts(int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Order.desc("createdDate")));
        return boardRepository.findAll(pageable);
    }

    public Board createPost(String title, String content, User user) {
        Board post = new Board();
        post.setTitle(title);
        post.setContent(content);
        post.setCreatedDate(LocalDateTime.now());
        post.setAuthor(user);
        this.boardRepository.save(post);
        return post; 
    }

    @Transactional 
    public Board getPostById(Long id) {
        Optional<Board> boardOpt = this.boardRepository.findById(id);
        if (boardOpt.isPresent()) {
            Board board = boardOpt.get();
            board.setViewCount(board.getViewCount() + 1);
            return board;
        } else {
            throw new DataNotFoundException("board not found");
        }
    }

    public void vote(Board board, User user) {
        board.getVoter().add(user);
        this.boardRepository.save(board);
    }
}