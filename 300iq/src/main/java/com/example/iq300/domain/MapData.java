package com.example.iq300.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
public class MapData {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30) private String transactionType; // "아파트(매매)", "아파트(전월세)" 등
    @Column(length = 255) private String address;         // 시군구 (예: "충청북도 청주시 흥덕구")
    private double area;            // 전용면적(㎡)
    private int price;           // 거래금액(만원) 또는 보증금(만원)
    private int rent;            // 월세(만원) (매매의 경우 0)
    
    private double Latitude; //위도 
    private double longitude; //경도
}
