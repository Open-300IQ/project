package com.example.iq300.service;

import com.example.iq300.domain.Answer;
import com.example.iq300.domain.Question;
import com.example.iq300.domain.User;
import com.example.iq300.repository.AnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class AnswerService {

    private final AnswerRepository answerRepository;

    /**
     * 답변 생성
     * @param question 이 답변이 달릴 질문 객체
     * @param content 답변 내용
     * @param author 답변자
     */
    public void create(Question question, String content, User author) {
        Answer answer = new Answer();
        answer.setContent(content);
        answer.setCreateDate(LocalDateTime.now());
        answer.setQuestion(question);
        answer.setAuthor(author);
        this.answerRepository.save(answer);
    }
}