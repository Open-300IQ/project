package com.example.iq300.repository; 

import com.example.iq300.domain.RealEstateTransaction; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TransactionRepository extends JpaRepository<RealEstateTransaction, Long> {
	@Query("SELECT t FROM RealEstateTransaction t " +
           "WHERE t.address LIKE CONCAT(:sigungu, '%') " + 
           "AND SUBSTRING(t.contractDate, 1, 6) = :contractMonth " +
           "AND t.transactionType LIKE CONCAT(:buildingType, '(%') " +
           "AND (" +
           " (:specificType = '매매' AND t.transactionType LIKE '%(매매)') OR " +
           " (:specificType = '전세' AND t.transactionType LIKE '%(전월세)' AND t.rent =0) OR " +
           " (:specificType = '월세' AND t.transactionType LIKE '%(전월세)' AND t.rent >0)" + 
           ")"+
           "ORDER BY t.contractDate DESC")
    List<RealEstateTransaction> findMatchingTransactions(
            @Param("sigungu") String sigungu, 
            @Param("buildingType") String buildingType,
            @Param("contractMonth") String contractMonth,
            @Param("specificType") String specificTransactionType
    );
	
    // 4번 BoardController가 메인 페이지에서 사용할 메서드입니다.
    // (예시) 최근 20개의 거래를 ID 역순(최신순)으로 찾기
    List<RealEstateTransaction> findTop20ByOrderByIdDesc();
}