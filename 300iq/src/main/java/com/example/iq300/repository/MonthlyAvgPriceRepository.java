package com.example.iq300.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.iq300.domain.MonthlyAvgPrice;
import com.example.iq300.domain.MonthlyAvgPriceId;

@Repository
public interface MonthlyAvgPriceRepository extends JpaRepository<MonthlyAvgPrice, MonthlyAvgPriceId> {
	List<MonthlyAvgPrice> findById_GuIn(List<String> districts);	 
}