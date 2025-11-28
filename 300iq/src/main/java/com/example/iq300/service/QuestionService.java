package com.example.iq300.service;

import com.example.iq300.domain.Answer; 
import com.example.iq300.domain.Question;
import com.example.iq300.domain.User;
import com.example.iq300.exception.DataNotFoundException;
import com.example.iq300.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    // [수정] sortType 파라미터 추가
    private Specification<Question> search(String kw, String searchType, String sortType) {
        return new Specification<>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                
                // [수정] "popularity" 정렬 로직을 Specification으로 이동
                if ("popularity".equals(sortType)) {
                    query.orderBy(cb.desc(cb.size(q.get("voter"))));
                }
                
                if (kw == null || kw.trim().isEmpty()) {
                    return cb.conjunction();
                }
                
                String likePattern = "%" + kw.toLowerCase() + "%";

                switch (searchType) {
                    case "subject": 
                        return cb.like(cb.lower(q.get("subject")), likePattern);
                    case "content": 
                        return cb.like(cb.lower(q.get("content")), likePattern);
                    case "author": 
                        query.distinct(true); // JOIN 시에만 distinct 적용
                        Join<Question, User> u = q.join("author", JoinType.INNER);
                        return cb.like(cb.lower(u.get("username")), likePattern);
                    case "answer": 
                        query.distinct(true); // JOIN 시에만 distinct 적용
                        Join<Question, Answer> a = q.join("answerList", JoinType.LEFT);
                        return cb.like(cb.lower(a.get("content")), likePattern);
                    default: 
                        Predicate subjectLike = cb.like(cb.lower(q.get("subject")), likePattern);
                        Predicate contentLike = cb.like(cb.lower(q.get("content")), likePattern);
                        return cb.or(subjectLike, contentLike);
                }
            }
        };
    }

    public Page<Question> getPage(int page, String kw, String searchType, String sortType) {
        
        List<Sort.Order> sorts = new ArrayList<>();
        
        // [수정] 인기순 정렬은 Specification이 처리하므로, 여기서는 최신순만 처리
        if ("latest".equals(sortType) || !"popularity".equals(sortType)) {
            sorts.add(Sort.Order.desc("createDate")); 
        }

        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        // [수정] search 메서드에 sortType 전달
        Specification<Question> spec = search(kw, searchType, sortType); 
        
        return this.questionRepository.findAll(spec, pageable);
    }
    
    @Transactional 
    public Question getQuestion(Long id) {
        Optional<Question> questionOpt = this.questionRepository.findById(id);
        if (questionOpt.isPresent()) {
            Question question = questionOpt.get();
            question.setViewCount(question.getViewCount() + 1); 
            return question;
        } else {
            throw new DataNotFoundException("question not found");
        }
    }

    public void create(String subject, String content, User author) {
        Question q = new Question();
        q.setSubject(subject);
        q.setContent(content);
        q.setCreateDate(LocalDateTime.now());
        q.setAuthor(author); 
        this.questionRepository.save(q);
    }

    public void vote(Question question, User user) {
        question.getVoter().add(user); 
        this.questionRepository.save(question);
    }
}