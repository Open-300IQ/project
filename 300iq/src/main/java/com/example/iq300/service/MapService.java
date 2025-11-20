package com.example.iq300.service;

import com.example.iq300.domain.MapData; // MapData로 변경
import com.example.iq300.repository.MapDataRepository; // MapDataRepository로 변경
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MapService {

    private final MapDataRepository mapDataRepository;

    private String[] extractGuAndDong(String address) {
        if (address == null || address.isEmpty()) {
            return new String[]{"", ""};
        }
        String[] parts = address.trim().split("\\s+");

        String gu = "";
        String dong = "";
        
        // CSV 주소: 충청북도 청주시 [서원구] [모충동] ... -> parts[2] = 구, parts[3] = 동
        // parts.length >= 4 인지 확인하고, parts[2]를 '구', parts[3]을 '동'으로 설정합니다.
        if (parts.length >= 4) {
            if (parts[2].endsWith("구")) {
                gu = parts[2];
            }
            dong = parts[3]; 
        }
        
        return new String[]{gu, dong};
    }

    public Map<String, List<String>> getUniqueDistrictsAndNeighborhoods() {
        List<MapData> allMaps = mapDataRepository.findAll();
        
        // Map<Gu, Set<Dong>> 구조로 데이터 집계 (Set으로 중복 제거)
        Map<String, Set<String>> districtMap = new HashMap<>();

        for (MapData mapItem : allMaps) {
            String address = mapItem.getAddress();
            String[] location = extractGuAndDong(address);
            String gu = location[0];
            String dong = location[1];

            if (!gu.isEmpty() && !dong.isEmpty()) {
                // 특정 구에 동을 추가
                districtMap.computeIfAbsent(gu, k -> new HashSet<>()).add(dong);
            }
        }
        
        // Map<Gu, List<Dong>>으로 변환하고, 동 목록을 알파벳순으로 정렬
        Map<String, List<String>> finalMap = districtMap.entrySet().stream()
                .collect(Collectors.toMap(
                        java.util.Map.Entry::getKey,
                        e -> e.getValue().stream().sorted().collect(Collectors.toList())
                ));
        
        return finalMap;
    }
    
    public void clearAndSaveAll(List<MapData> dataToSave) {
        // 1. 기존 데이터 모두 삭제
        mapDataRepository.deleteAll();
        
        // 2. 새로운 데이터 저장
        mapDataRepository.saveAll(dataToSave);
        
        System.out.println("[MapService] DB 초기화 및 MapData " + dataToSave.size() + "건 저장 완료.");
    }
    
    public List<MapData> getAllMapData() {
        return mapDataRepository.findAll();
    }
}