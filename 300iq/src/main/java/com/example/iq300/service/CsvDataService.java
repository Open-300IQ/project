package com.example.iq300.service;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets; // 1. (추가) UTF-8 임포트
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

// 2. (추가) 새 도메인 임포트
import com.example.iq300.domain.RealEstateTerm; 
import com.example.iq300.domain.Population;
import com.example.iq300.domain.RealEstateAgent;
import com.example.iq300.domain.RealEstateTransaction;
import com.example.iq300.domain.TotalData;
import com.example.iq300.domain.MonthlyVolume;
import com.example.iq300.domain.GrowthRate;

// 3. (추가) 새 리포지토리 임포트
import com.example.iq300.repository.RealEstateTermRepository; 
import com.example.iq300.repository.PopulationRepository;
import com.example.iq300.repository.RealEstateAgentRepository;
import com.example.iq300.repository.TransactionRepository;
import com.example.iq300.repository.TotalDataRepository;
import com.example.iq300.repository.MonthlyVolumeRepository;
import com.example.iq300.repository.GrowthRateRepository;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

@Service
public class CsvDataService {

    private final ResourceLoader resourceLoader;
    private final JdbcTemplate jdbcTemplate;

    // JPA Repository 주입
    private final TransactionRepository transactionRepository;
    private final RealEstateAgentRepository realEstateAgentRepository;
    private final PopulationRepository populationRepository;
    private final TotalDataRepository totalDataRepository;
    private final MonthlyVolumeRepository monthlyVolumeRepository;
    private final GrowthRateRepository growthRateRepository;
    
    // 4. (추가) 새 리포지토리 필드
    private final RealEstateTermRepository realEstateTermRepository;
    
    // CSV 파일 인코딩 설정.
    private static final String ENC_MS949 = "MS949";
    // 5. (추가) 크롤링한 CSV는 UTF-8이므로 정의
    private static final String ENC_UTF8 = StandardCharsets.UTF_8.name(); 

    /**
     * 생성자를 통한 Dependency Injection (DI)
     */
    public CsvDataService(
            ResourceLoader resourceLoader,
            JdbcTemplate jdbcTemplate,
            TransactionRepository transactionRepository,
            RealEstateAgentRepository realEstateAgentRepository,
            PopulationRepository populationRepository,
            TotalDataRepository totalDataRepository,
            MonthlyVolumeRepository monthlyVolumeRepository,
            GrowthRateRepository growthRateRepository,
            // 6. (추가) 생성자에 새 리포지토리 주입
            RealEstateTermRepository realEstateTermRepository) { 
        this.resourceLoader = resourceLoader;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionRepository = transactionRepository;
        this.realEstateAgentRepository = realEstateAgentRepository;
        this.populationRepository = populationRepository;
        this.totalDataRepository = totalDataRepository;
        this.monthlyVolumeRepository = monthlyVolumeRepository;
        this.growthRateRepository = growthRateRepository;
        // 7. (추가) 리포지토리 초기화
        this.realEstateTermRepository = realEstateTermRepository;
    }

    // --- 1. 실거래가 CSV 데이터 로드 및 DB 적재 ---
    // (기존 코드와 동일)
    public List<RealEstateTransaction> loadTransactions() {
        System.out.println("[CsvService] 실거래가 CSV 파일 로드 시작...");

        jdbcTemplate.execute("TRUNCATE TABLE realestatetransaction");
        
//        int skipLines = 20;
        final String ENC_FOR_TRANSACTION = ENC_MS949;
        
        List<RealEstateTransaction> allTransactions = new ArrayList<>();
        
        // 서원구 (Seowon)
        allTransactions.addAll(parseTransactionFile("단독다가구_매매_실거래가_2025년_서원구.csv", "단독다가구(매매)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("단독다가구_전월세_실거래가_2025년_서원구.csv", "단독다가구(전월세)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("아파트_매매_실거래가_2025년_서원구.csv", "아파트(매매)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("아파트_전월세_실거래가_2025년_서원구.csv", "아파트(전월세)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("연립다세대_매매_실거래가_2025년_서원구.csv", "연립다세대(매매)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("연립다세대_전월세_실거래가_2025년_서원구.csv", "연립다세대(전월세)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("오피스텔_매매_실거래가_2025년_서원구.csv", "오피스텔(매매)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("오피스텔_전월세_실거래가_2025년_서원구.csv", "오피스텔(전월세)", ENC_FOR_TRANSACTION));


        // 청원구 (Cheongwon)
        allTransactions.addAll(parseTransactionFile("단독다가구_매매_실거래가_2025년_청원구.csv", "단독다가구(매매)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("단독다가구_전월세_실거래가_2025년_청원구.csv", "단독다가구(전월세)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("아파트_매매_실거래가_2025년_청원구.csv", "아파트(매매)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("아파트_전월세_실거래가_2025년_청원구.csv", "아파트(전월세)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("연립다세대_매매_실거래가_2025년_청원구.csv", "연립다세대(매매)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("연립다세대_전월세_실거래가_2025년_청원구.csv", "연립다세대(전월세)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("오피스텔_매매_실거래가_2025년_청원구.csv", "오피스텔(매매)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("오피스텔_전월세_실거래가_2025년_청원구.csv", "오피스텔(전월세)", ENC_FOR_TRANSACTION));
        
        // 흥덕구 (Heungdeok)
        allTransactions.addAll(parseTransactionFile("단독다가구_매매_실거래가_2025년_흥덕구.csv", "단독다가구(매매)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("단독다가구_전월세_실거래가_2025년_흥덕구.csv", "단독다가구(전월세)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("아파트_매매_실거래가_2025년_흥덕구.csv", "아파트(매매)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("아파트_전월세_실거래가_2025년_흥덕구.csv", "아파트(전월세)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("연립다세대_매매_실거래가_2025년_흥덕구.csv", "연립다세대(매매)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("연립다세대_전월세_실거래가_2025년_흥덕구.csv", "연립다세대(전월세)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("오피스텔_매매_실거래가_2025년_흥덕구.csv", "오피스텔(매매)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("오피스텔_전월세_실거래가_2025년_흥덕구.csv", "오피스텔(전월세)", ENC_FOR_TRANSACTION));
        
        // 상당구 (Sangdang)
        allTransactions.addAll(parseTransactionFile("단독다가구_매매_실거래가_2025년_상당구.csv", "단독다가구(매매)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("단독다가구_전월세_실거래가_2025년_상당구.csv", "단독다가구(전월세)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("아파트_매매_실거래가_2025년_상당구.csv", "아파트(매매)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("아파트_전월세_실거래가_2025년_상당구.csv", "아파트(전월세)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("연립다세대_매매_실거래가_2025년_상당구.csv", "연립다세대(매매)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("연립다세대_전월세_실거래가_2025년_상당구.csv", "연립다세대(전월세)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("오피스텔_매매_실거래가_2025년_상당구.csv", "오피스텔(매매)", ENC_FOR_TRANSACTION));
        allTransactions.addAll(parseTransactionFile("오피스텔_전월세_실거래가_2025년_상당구.csv", "오피스텔(전월세)", ENC_FOR_TRANSACTION));
        
        
        // 단일 Repository를 사용해 모든 데이터를 일괄 저장
        List<RealEstateTransaction> allSavedTransactions = transactionRepository.saveAll(allTransactions); 
        
        int totalLoaded = allTransactions.size();
        System.out.println("[CsvService] 총 " + totalLoaded + "건의 실거래가 데이터 로드 완료.");
        System.out.println("[CsvService] 총 " + allSavedTransactions.size() + "건의 실거래가 데이터 DB 적재 완료.");
        return allSavedTransactions;

    }

    /**
     * 실거래가 CSV 파일을 파싱합니다.
     * (기존 코드와 동일)
     */
    private List<RealEstateTransaction> parseTransactionFile(String filePath, String txType, String encoding) {
        List<RealEstateTransaction> list = new ArrayList<>();
        
        final List<String> EXPECTED_HEADER_KEYWORDS = Arrays.asList("시군구", "전용면적", "계약년월", "계약일", "거래금액", "보증금", "월세금", "도로명");
        
        final int MIN_HEADERS_TO_MATCH = 3;
        
        String fullPath = "classpath:csv/" + filePath;
        
        CSVReader csvReader = null;

        try (InputStreamReader reader = new InputStreamReader(resourceLoader.getResource(fullPath).getInputStream(), encoding);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
        	
        	String currentLine;
        	boolean headerFound = false;
        	int linesRead = 0;
        	
        	while((currentLine = bufferedReader.readLine()) != null) {
        		linesRead++;
        		
        		long matchCount = EXPECTED_HEADER_KEYWORDS.stream().filter(currentLine::contains).count();
        		
        		if(matchCount >= MIN_HEADERS_TO_MATCH) {
        			headerFound = true;
        			
        			break;
        		}
        		
        		if(linesRead > 100) {
        			throw new IOException("100줄을 지나도 헤더가 없습니다. : " + filePath);
        		}
        	}
        	
        	csvReader = new CSVReaderBuilder(bufferedReader).build();
        	
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length < 10) continue; 

                RealEstateTransaction tx = new RealEstateTransaction();
                tx.setTransactionType(txType);

                try {
                    if (txType.equals("단독다가구(매매)")) {
                        tx.setAddress(line[1]);      
                        tx.setBuildingName(line[13]); 
                        tx.setArea(Double.parseDouble(line[5])); 
                        tx.setContractDate(line[8] + String.format("%02d", Integer.parseInt(line[9]))); 
                        tx.setPrice(Integer.parseInt(line[10].replace(",", ""))); 
                        tx.setRent(0); 
                    } else if (txType.equals("단독다가구(전월세)")) {
                        tx.setAddress(line[1]);      
                        tx.setBuildingName(line[11]); 
                        tx.setArea(Double.parseDouble(line[4])); 
                        tx.setContractDate(line[6] + String.format("%02d", Integer.parseInt(line[7]))); 
                        tx.setPrice(Integer.parseInt(line[8].replace(",", ""))); 
                        tx.setRent(Integer.parseInt(line[9].replace(",", "")));  
                    } else if (txType.equals("아파트(매매)")) {
                        tx.setAddress(line[1]);      
                        tx.setBuildingName(line[15]); 
                        tx.setArea(Double.parseDouble(line[6])); 
                        tx.setContractDate(line[7] + String.format("%02d", Integer.parseInt(line[8]))); 
                        tx.setPrice(Integer.parseInt(line[9].replace(",", ""))); 
                        tx.setRent(0); 
                    } else if (txType.equals("아파트(전월세)")) {
                        tx.setAddress(line[1]);      
                        tx.setBuildingName(line[14]); 
                        tx.setArea(Double.parseDouble(line[7])); 
                        tx.setContractDate(line[8] + String.format("%02d", Integer.parseInt(line[9]))); 
                        tx.setPrice(Integer.parseInt(line[10].replace(",", ""))); 
                        tx.setRent(Integer.parseInt(line[11].replace(",", "")));  
                    } else if (txType.equals("연립다세대(매매)")) {
                        tx.setAddress(line[1]);      
                        tx.setBuildingName(line[15]); 
                        tx.setArea(Double.parseDouble(line[6])); 
                        tx.setContractDate(line[8] + String.format("%02d", Integer.parseInt(line[9]))); 
                        tx.setPrice(Integer.parseInt(line[10].replace(",", ""))); 
                        tx.setRent(0); 
                    } else if (txType.equals("연립다세대(전월세)")) {
                        tx.setAddress(line[1]);      
                        tx.setBuildingName(line[14]); 
                        tx.setArea(Double.parseDouble(line[7])); 
                        tx.setContractDate(line[8] + String.format("%02d", Integer.parseInt(line[9]))); 
                        tx.setPrice(Integer.parseInt(line[10].replace(",", ""))); 
                        tx.setRent(Integer.parseInt(line[11].replace(",", "")));  
                    } else if (txType.equals("오피스텔(매매)")) {
                        tx.setAddress(line[1]);
                        tx.setBuildingName(line[14]); 
                        tx.setArea(Double.parseDouble(line[6])); 
                        tx.setContractDate(line[7] + String.format("%02d", Integer.parseInt(line[8]))); 
                        tx.setPrice(Integer.parseInt(line[9].replace(",", ""))); 
                        tx.setRent(0); 
                    } else if (txType.equals("오피스텔(전월세)")) {
                        tx.setAddress(line[1]);      
                        tx.setBuildingName(line[14]); 
                        tx.setArea(Double.parseDouble(line[7])); 
                        tx.setContractDate(line[8] + String.format("%02d", Integer.parseInt(line[9]))); 
                        tx.setPrice(Integer.parseInt(line[10].replace(",", ""))); 
                        tx.setRent(Integer.parseInt(line[11].replace(",", "")));  
                    }
                    if (tx.getAddress() != null && !tx.getAddress().isEmpty()) { 
                        list.add(tx);
                    }
                } catch (Exception e) {
                    // 파싱 중 오류 발생 시 무시
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[CsvService] 실거래가 CSV 파싱 실패: " + filePath);
        } finally {
        	if(csvReader != null) {
        		try {
        			csvReader.close();
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
        	}
        }
        return list;
    }
    
    public List<TotalData> loadTotal() {
        System.out.println("[CsvService] 실거래가 CSV 파일 로드 시작...");

        jdbcTemplate.execute("TRUNCATE TABLE totaldata");
        
//        int skipLines = 20;
        final String ENC_FOR_TRANSACTION = ENC_MS949;
        
        List<TotalData> allTotals = new ArrayList<>();
        
        // 서원구 (Seowon)
        allTotals.addAll(parseTotalFile("단독다가구_매매_실거래가_2025년_서원구.csv", "단독다가구(매매)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("단독다가구_전월세_실거래가_2025년_서원구.csv", "단독다가구(전월세)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("아파트_매매_실거래가_2025년_서원구.csv", "아파트(매매)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("아파트_전월세_실거래가_2025년_서원구.csv", "아파트(전월세)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("연립다세대_매매_실거래가_2025년_서원구.csv", "연립다세대(매매)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("연립다세대_전월세_실거래가_2025년_서원구.csv", "연립다세대(전월세)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("오피스텔_매매_실거래가_2025년_서원구.csv", "오피스텔(매매)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("오피스텔_전월세_실거래가_2025년_서원구.csv", "오피스텔(전월세)", ENC_FOR_TRANSACTION));


        // 청원구 (Cheongwon)
        allTotals.addAll(parseTotalFile("단독다가구_매매_실거래가_2025년_청원구.csv", "단독다가구(매매)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("단독다가구_전월세_실거래가_2025년_청원구.csv", "단독다가구(전월세)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("아파트_매매_실거래가_2025년_청원구.csv", "아파트(매매)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("아파트_전월세_실거래가_2025년_청원구.csv", "아파트(전월세)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("연립다세대_매매_실거래가_2025년_청원구.csv", "연립다세대(매매)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("연립다세대_전월세_실거래가_2025년_청원구.csv", "연립다세대(전월세)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("오피스텔_매매_실거래가_2025년_청원구.csv", "오피스텔(매매)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("오피스텔_전월세_실거래가_2025년_청원구.csv", "오피스텔(전월세)", ENC_FOR_TRANSACTION));
        
        // 흥덕구 (Heungdeok)
        allTotals.addAll(parseTotalFile("단독다가구_매매_실거래가_2025년_흥덕구.csv", "단독다가구(매매)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("단독다가구_전월세_실거래가_2025년_흥덕구.csv", "단독다가구(전월세)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("아파트_매매_실거래가_2025년_흥덕구.csv", "아파트(매매)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("아파트_전월세_실거래가_2025년_흥덕구.csv", "아파트(전월세)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("연립다세대_매매_실거래가_2025년_흥덕구.csv", "연립다세대(매매)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("연립다세대_전월세_실거래가_2025년_흥덕구.csv", "연립다세대(전월세)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("오피스텔_매매_실거래가_2025년_흥덕구.csv", "오피스텔(매매)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("오피스텔_전월세_실거래가_2025년_흥덕구.csv", "오피스텔(전월세)", ENC_FOR_TRANSACTION));
        
        // 상당구 (Sangdang)
        allTotals.addAll(parseTotalFile("단독다가구_매매_실거래가_2025년_상당구.csv", "단독다가구(매매)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("단독다가구_전월세_실거래가_2025년_상당구.csv", "단독다가구(전월세)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("아파트_매매_실거래가_2025년_상당구.csv", "아파트(매매)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("아파트_전월세_실거래가_2025년_상당구.csv", "아파트(전월세)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("연립다세대_매매_실거래가_2025년_상당구.csv", "연립다세대(매매)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("연립다세대_전월세_실거래가_2025년_상당구.csv", "연립다세대(전월세)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("오피스텔_매매_실거래가_2025년_상당구.csv", "오피스텔(매매)", ENC_FOR_TRANSACTION));
        allTotals.addAll(parseTotalFile("오피스텔_전월세_실거래가_2025년_상당구.csv", "오피스텔(전월세)", ENC_FOR_TRANSACTION));
        
        
        // 단일 Repository를 사용해 모든 데이터를 일괄 저장
        List<TotalData> allSavedTotals = totalDataRepository.saveAll(allTotals); 
        
        int totalLoaded = allTotals.size();
        System.out.println("[CsvService] 총 " + totalLoaded + "건의 실거래가 데이터 로드 완료.");
        System.out.println("[CsvService] 총 " + allSavedTotals.size() + "건의 실거래가 데이터 DB 적재 완료.");
        return allSavedTotals;

    }

    /**
     * 실거래가 CSV 파일을 파싱합니다.
     * (기존 코드와 동일)
     */
    private List<TotalData> parseTotalFile(String filePath, String txType, String encoding) {
        List<TotalData> list = new ArrayList<>();
        
        final List<String> EXPECTED_HEADER_KEYWORDS = Arrays.asList("시군구", "전용면적", "계약년월", "계약일", "거래금액", "보증금", "월세금", "도로명");
        
        final int MIN_HEADERS_TO_MATCH = 3;
        
        String fullPath = "classpath:csv/" + filePath;
        
        CSVReader csvReader = null;

        try (InputStreamReader reader = new InputStreamReader(resourceLoader.getResource(fullPath).getInputStream(), encoding);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
        	
        	String currentLine;
        	boolean headerFound = false;
        	int linesRead = 0;
        	
        	while((currentLine = bufferedReader.readLine()) != null) {
        		linesRead++;
        		
        		long matchCount = EXPECTED_HEADER_KEYWORDS.stream().filter(currentLine::contains).count();
        		
        		if(matchCount >= MIN_HEADERS_TO_MATCH) {
        			headerFound = true;
        			
        			break;
        		}
        		
        		if(linesRead > 100) {
        			throw new IOException("100줄을 지나도 헤더가 없습니다. : " + filePath);
        		}
        	}
        	
        	csvReader = new CSVReaderBuilder(bufferedReader).build();
      
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length < 10) continue; 

                TotalData tx = new TotalData();
                tx.setTransactionType(txType);

                try {
                    if (txType.equals("단독다가구(매매)")) {
                        tx.setAddress(line[1] + " " + line[13]);      
                        tx.setBuildingName("-"); 
                        tx.setArea(Double.parseDouble(line[5])); 
                        tx.setContractDate(line[8] + String.format("%02d", Integer.parseInt(line[9]))); 
                        tx.setPrice(Integer.parseInt(line[10].replace(",", ""))); 
                        tx.setRent(0); 
                    } else if (txType.equals("단독다가구(전월세)")) {
                        tx.setAddress(line[1] + " " + line[11]);      
                        tx.setBuildingName("-"); 
                        tx.setArea(Double.parseDouble(line[4])); 
                        tx.setContractDate(line[6] + String.format("%02d", Integer.parseInt(line[7]))); 
                        tx.setPrice(Integer.parseInt(line[8].replace(",", ""))); 
                        tx.setRent(Integer.parseInt(line[9].replace(",", "")));  
                    } else if (txType.equals("아파트(매매)")) {
                        tx.setAddress(line[1] + " " + line[15]);      
                        tx.setBuildingName(line[5]); 
                        tx.setArea(Double.parseDouble(line[6])); 
                        tx.setContractDate(line[7] + String.format("%02d", Integer.parseInt(line[8]))); 
                        tx.setPrice(Integer.parseInt(line[9].replace(",", ""))); 
                        tx.setRent(0); 
                    } else if (txType.equals("아파트(전월세)")) {
                        tx.setAddress(line[1] + " " + line[14]);      
                        tx.setBuildingName(line[5]); 
                        tx.setArea(Double.parseDouble(line[7])); 
                        tx.setContractDate(line[8] + String.format("%02d", Integer.parseInt(line[9]))); 
                        tx.setPrice(Integer.parseInt(line[10].replace(",", ""))); 
                        tx.setRent(Integer.parseInt(line[11].replace(",", "")));  
                    } else if (txType.equals("연립다세대(매매)")) {
                        tx.setAddress(line[1] + " " + line[15]);      
                        tx.setBuildingName(line[5]); 
                        tx.setArea(Double.parseDouble(line[6])); 
                        tx.setContractDate(line[8] + String.format("%02d", Integer.parseInt(line[9]))); 
                        tx.setPrice(Integer.parseInt(line[10].replace(",", ""))); 
                        tx.setRent(0); 
                    } else if (txType.equals("연립다세대(전월세)")) {
                        tx.setAddress(line[1] + " " + line[14]);      
                        tx.setBuildingName(line[5]); 
                        tx.setArea(Double.parseDouble(line[7])); 
                        tx.setContractDate(line[8] + String.format("%02d", Integer.parseInt(line[9]))); 
                        tx.setPrice(Integer.parseInt(line[10].replace(",", ""))); 
                        tx.setRent(Integer.parseInt(line[11].replace(",", "")));  
                    } else if (txType.equals("오피스텔(매매)")) {
                        tx.setAddress(line[1] + " " + line[14]);
                        tx.setBuildingName(line[5]); 
                        tx.setArea(Double.parseDouble(line[6])); 
                        tx.setContractDate(line[7] + String.format("%02d", Integer.parseInt(line[8]))); 
                        tx.setPrice(Integer.parseInt(line[9].replace(",", ""))); 
                        tx.setRent(0); 
                    } else if (txType.equals("오피스텔(전월세)")) {
                        tx.setAddress(line[1] + " " + line[14]);      
                        tx.setBuildingName(line[5]); 
                        tx.setArea(Double.parseDouble(line[7])); 
                        tx.setContractDate(line[8] + String.format("%02d", Integer.parseInt(line[9]))); 
                        tx.setPrice(Integer.parseInt(line[10].replace(",", ""))); 
                        tx.setRent(Integer.parseInt(line[11].replace(",", "")));  
                    }
                    if (tx.getAddress() != null && !tx.getAddress().isEmpty()) { 
                        list.add(tx);
                    }
                } catch (Exception e) {
                    // 파싱 중 오류 발생 시 무시
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[CsvService] 실거래가 CSV 파싱 실패: " + filePath);
        } finally {
        	if(csvReader != null) {
        		try {
        			csvReader.close();
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
        	}
        }
        return list;
    }
    
    public void calculateAndSaveMonthlyVolumes() {
    	System.out.println("[CsvService] '구 + 월별' 거래량 집계 시작");
    	
    	jdbcTemplate.execute("TRUNCATE TABLE monthlyvolume");
    	
    	String sql = "SELECT " +
                "    CASE " +
                "        WHEN address LIKE '%상당구%' THEN '상당구' " +
                "        WHEN address LIKE '%흥덕구%' THEN '흥덕구' " +
                "        WHEN address LIKE '%서원구%' THEN '서원구' " +
                "        WHEN address LIKE '%청원구%' THEN '청원구' " +
                "        ELSE '기타' " +
                "    END AS guName, " +
                "    SUBSTRING(contractDate, 1, 6) AS contractMonth, " + 
                "    COUNT(*) AS transactionCount " + 
                "FROM " +
                "    realestatetransaction " + 
                "WHERE " +
                "    (address LIKE '%상당구%' OR address LIKE '%흥덕구%' OR address LIKE '%서원구%' OR address LIKE '%청원구%') " +
                "    AND contractDate IS NOT NULL AND LENGTH(contractDate) >= 6 " + 
                "	 AND transactionType LIKE '%전월세%'" + 
                "GROUP BY " + 
                "    guName, contractMonth " +
                "ORDER BY " + 
                "    guName, contractMonth";
    	
    	RowMapper<MonthlyVolume> rowMapper = (rs, rowNum) -> new MonthlyVolume(
    			rs.getString("guName"),
    			rs.getString("contractMonth"),
    			rs.getLong("transactionCount")
    	);
    	
    	List<MonthlyVolume> results = jdbcTemplate.query(sql, rowMapper);
    	
    	monthlyVolumeRepository.saveAll(results);
    	
    	System.out.println("[CsvService] '구 + 월별' 거래량 집계 및 저장 완료. (" + results.size() + "개 그룹)");
    }
    
    public void calculateAndSaveALLGrowthRates() {
    	System.out.println("[CsvService] 3개월/1년 가격/거래량 증가율 계산 시작");
    	
    	jdbcTemplate.execute("TRUNCATE TABLE growthrate");
    	
    	String sql =
    			// 1) 구별 월간 통계 (거래량, 평균가격) - (변경 없음)
    			"WITH MonthlyGuStats AS ( " +
    		            "    SELECT  " +
    		            "        CASE  " +
    		            "            WHEN address LIKE '%상당구%' THEN '상당구'  " +
    		            "            WHEN address LIKE '%흥덕구%' THEN '흥덕구'  " +
    		            "            WHEN address LIKE '%서원구%' THEN '서원구'  " +
    		            "            WHEN address LIKE '%청원구%' THEN '청원구'  " +
    		            "        END AS guName,  " +
    		            "        SUBSTRING(contractDate, 1, 6) AS contractMonth,  " +
    		            "        COUNT(*) AS monthVolume,  " +
    		            "        AVG(price) AS monthAvgPrice  " + 
    		            "    FROM realestatetransaction  " +
    		            "    WHERE  " +
    		            "        transactionType LIKE '%전월세%'  " +
    		            "        AND (address LIKE '%상당구%' OR address LIKE '%흥덕구%' OR address LIKE '%서원구%' OR address LIKE '%청원구%')  " +
    		            "        AND price > 100  " + 
    		            "        AND LENGTH(contractDate) >= 6  " +
    		            "    GROUP BY guName, contractMonth  " +
    		            "),  " +
    		    // 2) 청주시 전체 월간 통계 - (변경 없음)
    		    "CityMonthlyStats AS ( " +
    		            "    SELECT  " +
    		            "        '청주시 전체' AS guName,  " +
    		            "        SUBSTRING(contractDate, 1, 6) AS contractMonth,  " +
    		            "        COUNT(*) AS monthVolume,  " +
    		            "        AVG(price) AS monthAvgPrice  " +
    		            "    FROM realestatetransaction  " +
    		            "    WHERE  " +
    		            "        transactionType LIKE '%전월세%'  " +
    		            "        AND (address LIKE '%상당구%' OR address LIKE '%흥덕구%' OR address LIKE '%서원구%' OR address LIKE '%청원구%')  " +
    		            "        AND price > 100  " +
    		            "        AND LENGTH(contractDate) >= 6  " +
    		            "    GROUP BY contractMonth  " +
    		            "),  " +
    		    // 3) 구별 + 시전체 통계를 하나로 합침 - (변경 없음)
    		    "CombinedStats AS ( " +
    		            "    SELECT * FROM MonthlyGuStats " +
    		            "    UNION ALL " +
    		            "    SELECT * FROM CityMonthlyStats " +
    		            "),  " +
    		            
    		    // 4) [핵심 수정] 202501, 202507, 202510 데이터만 피벗(Pivot)하여 한 줄로 만듭니다.
    		    "PivotedStats AS ( " +
    		            "    SELECT " +
    		            "        guName, " +
    		            // '현재' (2025년 10월) 데이터
    		            "        MAX(CASE WHEN contractMonth = '202510' THEN monthVolume END) AS volume_current, " +
    		            "        MAX(CASE WHEN contractMonth = '202510' THEN monthAvgPrice END) AS price_current, " +
    		            
    		            // '3개월' 비교 시작점 (2025년 7월) 데이터
    		            "        MAX(CASE WHEN contractMonth = '202507' THEN monthVolume END) AS volume_start_3m, " +
    		            "        MAX(CASE WHEN contractMonth = '202507' THEN monthAvgPrice END) AS price_start_3m, " +
    		            
    		            // '9개월' 비교 시작점 (2025년 1월) 데이터
    		            "        MAX(CASE WHEN contractMonth = '202501' THEN monthVolume END) AS volume_start_9m, " +
    		            "        MAX(CASE WHEN contractMonth = '202501' THEN monthAvgPrice END) AS price_start_9m " +
    		            
    		            "    FROM CombinedStats " +
    		            "    WHERE contractMonth IN ('202501', '202507', '202510') " + // 필요한 3개 월만 필터링
    		            "    GROUP BY guName " +
    		            ") " +
    		            
    		    // 5) [수정] 피벗된 데이터를 바탕으로 증가율 계산
    		    "SELECT areaName, txpriceType, period, growthRate FROM ( " +
    		            // '3개월' (202510 vs 202507) 거래량
    		            "    SELECT guName AS areaName, '거래량' AS txpriceType, '3개월' AS period, (volume_current - volume_start_3m) * 100.0 / volume_start_3m AS growthRate " +
    		            "    FROM PivotedStats WHERE volume_start_3m > 0 " +
    		            "    UNION ALL " +
    		            // '9개월' (202510 vs 202501) 거래량
    		            "    SELECT guName AS areaName, '거래량' AS txpriceType, '9개월' AS period, (volume_current - volume_start_9m) * 100.0 / volume_start_9m AS growthRate " +
    		            "    FROM PivotedStats WHERE volume_start_9m > 0 " +
    		            "    UNION ALL " +
    		            // '3개월' (202510 vs 202507) 가격
    		            "    SELECT guName AS areaName, '가격' AS txpriceType, '3개월' AS period, (price_current - price_start_3m) * 100.0 / price_start_3m AS growthRate " +
    		            "    FROM PivotedStats WHERE price_start_3m > 0 " +
    		            "    UNION ALL " +
    		            // '9개월' (202510 vs 202501) 가격
    		            "    SELECT guName AS areaName, '가격' AS txpriceType, '9개월' AS period, (price_current - price_start_9m) * 100.0 / price_start_9m AS growthRate " +
    		            "    FROM PivotedStats WHERE price_start_9m > 0 " +
    		            ") AS FinalResults";
    	RowMapper<GrowthRate> rowMapper = (rs, rowNum) -> new GrowthRate(
			rs.getString("areaName"),
			rs.getString("txpriceType"),
			rs.getString("period"),
			rs.getDouble("growthRate")
    	);
    	
    	List<GrowthRate> results = jdbcTemplate.query(sql, rowMapper);
    	
    	growthRateRepository.saveAll(results);
    	
    	System.out.println("[CsvService] 모든 증가율 통계 계산 및 저장 완료. (\" + results.size() + \"개 지표)");
    }
    
    // --- 2. 중개인 CSV 데이터 로드 및 DB 적재 ---
    // (기존 코드와 동일)
    public List<RealEstateAgent> loadAgents(){
    	System.out.println("[CsvService] 중개인 CSV 파일 로드 시작...");
    	
    	jdbcTemplate.execute("TRUNCATE TABLE realestateagent");
    	
    	int skipLines=1;
    	final String ENC_FOR_AGENT = ENC_MS949;
    	final String filePath = "중개업정보.csv";
    	
    	List<RealEstateAgent> allAgents = parseAgentFile(filePath, ENC_FOR_AGENT,skipLines); 
    	List<RealEstateAgent> allSavedAgents = realEstateAgentRepository.saveAll(allAgents);
    	System.out.println("[CsvService] 총 " + allAgents.size() + "건의 중개인 데이터 로드 완료.");
    	System.out.println("[CsvService] 총 " + allSavedAgents.size() + "건의 중개인 데이터 DB 적재 완료.");
    	return allSavedAgents;
    }
    
    /**
     * 중개인 CSV 파일을 파싱합니다.
     * (기존 코드와 동일)
     */
    private List<RealEstateAgent> parseAgentFile(String filePath, String encoding, int skipLines){
    	List<RealEstateAgent> list=new ArrayList<>();
    	String fullPath = "classpath:csv/"+filePath;
    	
    	try (InputStreamReader reader=new InputStreamReader(resourceLoader.getResource(fullPath).getInputStream(),encoding);
    		 CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(skipLines).build()){
    		String[] line;
    		
    		while ((line=csvReader.readNext())!=null) {
    			if(line.length <6)continue;
    			
    			RealEstateAgent agent = new RealEstateAgent();
    			try {
    				agent.setAgentName(line[4].trim());
    				agent.setOfficeName(line[5].trim());
    				agent.setAddress(line[6].trim());
    				agent.setIdenty_num(line[2].trim());
    				if(!agent.getIdenty_num().isEmpty()) {
    					list.add(agent);
    				}
    			}catch(Exception e) {
    				
    			}
    		}
    	}catch (Exception e) {
        	e.printStackTrace();
        	System.err.println("[CsvService] 중개인 CSV 파싱 실패: " + filePath);
    	}
    	return list;
    }
    
    // --- 3. 인구 CSV 데이터 로드 및 DB 적재 ---
    // (기존 코드와 동일)
    public List<Population> loadPopulation(){
    	System.out.println("[CsvService] 인구 CSV 파일 로드 시작...");
    	
    	jdbcTemplate.execute("TRUNCATE TABLE population");
    	
    	int skipLines=1;
    	final String ENC_FOR_AGENT = ENC_MS949; // (변수명은 agent지만 MS949)
    	final String filePath = "인구현황.csv";
    	
    	List<Population> populations = parsePopulationFile(filePath, ENC_FOR_AGENT,skipLines);
    	
    	List<Population> allSavedpopulations = populationRepository.saveAll(populations); 
    	
    	System.out.println("[CsvService] 총 " + populations.size() + "건의 인구 데이터 로드 완료.");
    	System.out.println("[CsvService] 총 " + allSavedpopulations.size() + "건의 인구 데이터 DB 적재 완료.");
    	return allSavedpopulations;
    }
    
    /**
     * 인구 CSV 파일을 파싱합니다.
     * (기존 코드와 동일)
     */
    private List<Population> parsePopulationFile(String filePath, String encoding, int skipLines){
    	List<Population> list=new ArrayList<>();
    	String fullPath = "classpath:csv/"+filePath;
    	
    	try (InputStreamReader reader=new InputStreamReader(resourceLoader.getResource(fullPath).getInputStream(),encoding);
    		 CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(skipLines).build()){
    		String[] line;
    		
    		while ((line=csvReader.readNext())!=null) {
    			if(line.length <6)continue; // (기존 코드에 6으로 되어있어 유지)
    			
    			Population pop = new Population();
    			try {
    				pop.setAddress(line[0].trim());
    				pop.setPopulation_num(line[1].trim());
    				if(!pop.getPopulation_num().isEmpty()) {
    					list.add(pop);
    				}
    			}catch(Exception e) {
    				
    			}
    		}
    	}catch (Exception e) {
        	e.printStackTrace();
        	System.err.println("[CsvService] 인구 CSV 파싱 실패: " + filePath);
    	}
    	return list;
    }


    // ======== [ 8. (신규) 부동산 용어사전 CSV 로드 메서드 추가 ] ========
    
    /**
     * 4. 부동산 용어사전 CSV 데이터 로드 및 DB 적재
     */
    public List<RealEstateTerm> loadRealEstateTerms() {
        System.out.println("[CsvService] 부동산 용어사전 CSV 파일 로드 시작...");
        
        jdbcTemplate.execute("TRUNCATE TABLE realestateterm");
        
        int skipLines = 1; // 헤더(첫 줄) 스킵
        
        // ======== [ 2. 파일명 변경 ] ========
        final String filePath = "부동산용어_신조어_전체_초성추가.csv"; 
        
        // 파이썬 크롤링 파일이 UTF-8이므로 ENC_UTF8 사용
        List<RealEstateTerm> terms = parseTermsFile(filePath, ENC_UTF8, skipLines);
        
        // (중요!) DB에 저장하기 전 기존 데이터 모두 삭제 (중복 방지)
        realEstateTermRepository.deleteAll(); 
        
        List<RealEstateTerm> savedTerms = realEstateTermRepository.saveAll(terms);
        
        System.out.println("[CsvService] 총 " + terms.size() + "건의 용어 데이터 로드 완료.");
        System.out.println("[CsvService] 총 " + savedTerms.size() + "건의 용어 데이터 DB 적재 완료.");
        return savedTerms;
    }

    /**
     * 부동산 용어사전 CSV 파일을 파싱합니다.
     */
    private List<RealEstateTerm> parseTermsFile(String filePath, String encoding, int skipLines) {
        List<RealEstateTerm> list = new ArrayList<>();
        String fullPath = "classpath:csv/" + filePath;

        try (InputStreamReader reader = new InputStreamReader(resourceLoader.getResource(fullPath).getInputStream(), encoding);
             CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(skipLines).build()) {

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                // ======== [ 3. 컬럼 개수 3개로 변경 ] ========
                if (line.length < 3) continue; // 용어, 초성, 설명

                RealEstateTerm term = new RealEstateTerm();
                try {
                    term.setTerm(line[0].trim());       // 첫 번째 컬럼 (용어)
                    term.setInitial(line[1].trim());    // 두 번째 컬럼 (초성)
                    term.setDefinition(line[2].trim()); // 세 번째 컬럼 (설명)
                    
                    if (!term.getTerm().isEmpty()) {
                        list.add(term);
                    }
                } catch (Exception e) {
                    System.err.println("[CsvService] 용어 파싱 중 오류 발생: " + String.join(", ", line));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[CsvService] 용어사전 CSV 파싱 실패: " + filePath);
        }
        return list;
    }
}