package com.example.iq300.repository;

import com.example.iq300.domain.GrowthRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrowthRateRepository extends JpaRepository<GrowthRate, Long>{
	
	GrowthRate findFirstByAreaNameAndPeriodAndTxpriceType(String areaName, String period, String txpriceType);
}
