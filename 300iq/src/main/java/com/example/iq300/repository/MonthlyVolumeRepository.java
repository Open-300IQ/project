package com.example.iq300.repository;

import com.example.iq300.domain.MonthlyVolume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthlyVolumeRepository extends JpaRepository<MonthlyVolume, Long> {

}
