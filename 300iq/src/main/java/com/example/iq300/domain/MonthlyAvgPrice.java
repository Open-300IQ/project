package com.example.iq300.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "transaction_aggregate")
public class MonthlyAvgPrice {

    @EmbeddedId
    private MonthlyAvgPriceId id;

    // 집계 기준
    @Column(length = 30)private String sigungu;        // 시군구 (예: "충청북도 청주시 흥덕구")

    // 집계 값
    private double avgPricePerPyeong; // 평당 가격 평균 (단위: 만원/평)
    private long transactionCount;   // 거래 건수
    
    public void setId(MonthlyAvgPriceId id) {
    	this.id= id;
    }
}