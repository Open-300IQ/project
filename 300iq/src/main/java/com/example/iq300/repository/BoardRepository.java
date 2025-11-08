package com.example.iq300.repository;

import com.example.iq300.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

// JpaSpecificationExecutor<Board> 상속만 있으면 됩니다.
// 안의 내용은 비워두세요.
public interface BoardRepository extends JpaRepository<Board, Long>, JpaSpecificationExecutor<Board> {
}