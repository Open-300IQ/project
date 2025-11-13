package com.example.iq300.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "growthrate")
@NoArgsConstructor
@Getter
@Setter
public class GrowthRate {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String areaName;
	private String txpriceType;
	private String period;
	private Double growthRate;
	
	public GrowthRate(String areaName, String txpriceType, String period, Double growthRate) {
		this.areaName = areaName;
		this.txpriceType = txpriceType;
		this.period = period;
		this.growthRate = growthRate;
	}
}
