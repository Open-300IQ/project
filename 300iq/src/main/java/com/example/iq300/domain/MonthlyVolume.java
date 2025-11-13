package com.example.iq300.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "monthlyvolume")
@NoArgsConstructor
@Getter
@Setter
public class MonthlyVolume {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	
	private Long id;
	private String guName;
	private String contractMonth;
	private Long transactionCount;
	
	public MonthlyVolume(String guName, String contractMonth, Long transactionCount) {
		this.guName = guName;
		this.contractMonth = contractMonth;
		this.transactionCount = transactionCount;
	}
}
