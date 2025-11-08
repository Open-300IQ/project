package com.example.iq300.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "RealEstateAgent", indexes = {
    @Index(name = "idx_office_search", columnList = "agentName, officeName, address, identy_num")
})
public class RealEstateAgent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 집계 기준
    @Column(length = 30) private String agentName;        
    @Column(length = 30) private String officeName;            
    @Column(length = 30) private String address; 
    @Column(length = 30) private String identy_num;
    
    
}