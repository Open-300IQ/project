package com.example.iq300.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.iq300.domain.MapData;

public interface MapDataRepository extends JpaRepository<MapData, Long> {
}