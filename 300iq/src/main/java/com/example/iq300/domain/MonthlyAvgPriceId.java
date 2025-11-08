package com.example.iq300.domain;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class MonthlyAvgPriceId implements Serializable {
	@Column(length = 50) private String gu;
	@Column(length = 50) private String dong;
	@Column(length = 50) private String buildingType;
	@Column(length = 10) private String transactionType; 
	@Column(length = 10) private String contractMonth;

}
