

package com.example.iq300.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.example.iq300.domain.FinalData;
import com.example.iq300.domain.HousingPolicy;
import com.example.iq300.domain.MapData;
import com.example.iq300.domain.Population;
import com.example.iq300.domain.RealEstateAgent;
import com.example.iq300.domain.RealEstateTerm;
import com.example.iq300.domain.RealEstateTransaction;
import com.example.iq300.domain.TotalData;
import com.example.iq300.repository.FinalDataRepository;
import com.example.iq300.repository.HousingPolicyRepository;
import com.example.iq300.repository.MapDataRepository;
import com.example.iq300.repository.PopulationRepository;
import com.example.iq300.repository.RealEstateAgentRepository;
import com.example.iq300.repository.RealEstateTermRepository;
import com.example.iq300.repository.TotalDataRepository;
import com.example.iq300.repository.TransactionRepository;
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
    private final FinalDataRepository finalDataRepository;
    private final MapService mapService;
    private final MapDataRepository mapDataRepository;
    private final RealEstateTermRepository realEstateTermRepository;
    private final HousingPolicyRepository housingPolicyRepository;
    // CSV 파일 인코딩 설정.
    private static final String ENC_MS949 = "MS949";
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
            FinalDataRepository finalDataRepository,
            MapDataRepository mapDataRepository,
            MapService mapService,
            RealEstateTermRepository realEstateTermRepository,
            HousingPolicyRepository housingPolicyRepository) { 
        this.resourceLoader = resourceLoader;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionRepository = transactionRepository;
        this.realEstateAgentRepository = realEstateAgentRepository;
        this.populationRepository = populationRepository;
        this.totalDataRepository = totalDataRepository;
        this.finalDataRepository = finalDataRepository;
        this.mapDataRepository = mapDataRepository;
        this.mapService = mapService;
        this.realEstateTermRepository = realEstateTermRepository;
        this.housingPolicyRepository = housingPolicyRepository;
    }

    // --- 1. 실거래가 CSV 데이터 로드 및 DB 적재 ---
    public List<RealEstateTransaction> loadTransactions() {
        System.out.println("[CsvService] 실거래가 CSV 파일 로드 시작...");

        jdbcTemplate.execute("TRUNCATE TABLE realestatetransaction");
        
//      int skipLines = 20;
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
    
    public void buildAndSaveFinalData() {
    	System.out.println("[CsvService] FinalData(가격/거래량 병합) 데이터 생성 시작...");
    	
    	jdbcTemplate.execute("TRUNCATE TABLE finaldata");
    	
    	String sql = "SELECT " +
                "    t_price.gu AS areaName, " +
                "    t_price.contractMonth AS contractMonth, " +
                "    t_price.transactionType AS contractType, " +
                "	 t_price.buildingType AS buildingType, " +
                "    t_price.avgPricePerPyeong AS price, " +
                "    t_price.transactionCount AS count " +
                "FROM " +
                "    transaction_aggregate AS t_price " +
                "WHERE " +
                "    t_price.dong = '전체' " + 
                "ORDER BY " +
                "    areaName, contractMonth, contractType, buildingType";
    	
    	RowMapper<FinalData> rowMapper = (rs, rowNum) -> {
    		FinalData finalData = new FinalData();
    		
    		finalData.setAreaName(rs.getString("areaName"));
    		finalData.setContractMonth(rs.getString("contractMonth"));
    		finalData.setContractType(rs.getString("contractType"));
    		finalData.setBuildingType(rs.getString("buildingType"));
    		finalData.setPrice(rs.getDouble("price"));
    		finalData.setCount(rs.getLong("count"));
    		return finalData;
    	};
    	
    	List<FinalData> results = jdbcTemplate.query(sql, rowMapper);
    	finalDataRepository.saveAll(results);
    	System.out.println("[CsvService] 'FinalData' 집계 완료 및 저장 완료 : (" + results.size() + "건)");
    	
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
    
    
    //======== [ 부동산 거래(Map) CSV 로드 메서드 추가 ] ========
    
    public List<MapData> loadMapTransactions() {
        System.out.println("[CsvService] map CSV 파일 로드 시작...");
        
        // 이전의 TRUNCATE 명령어 제거 (MapService.clearAndSaveAll에서 처리)
        // jdbcTemplate.execute("TRUNCATE TABLE map");
        
        final String filePath = "map_data.csv"; 
        final String encoding = ENC_UTF8; 
        
        // 1. CSV 파일을 파싱하여 MapData 객체 목록을 얻습니다.
        List<MapData> transactions = parseMapFile(filePath, encoding, 1);
        
        
        // 2. MapService를 호출하여 기존 데이터를 비우고 새로운 데이터를 저장합니다.
        //    (DB TRUNCATE 및 saveAll 로직은 MapService로 위임)
        mapService.clearAndSaveAll(transactions); 
        
        // MapService는 저장된 데이터 목록을 반환하지 않으므로, 파싱된 목록을 반환합니다.
        System.out.println("[CsvService] 총 " + transactions.size() + "건의 map 데이터 로드 완료.");
        System.out.println("[CsvService] 총 " + transactions.size() + "건의 map 데이터 DB 적재 완료.");
        return transactions;
    }
    
    private List<MapData> parseMapFile(String filePath, String encoding, int skipLines) {
		// (수정) 반환 타입과 리스트를 Map으로 설정
        List<MapData> list = new ArrayList<>();
        
        // (수정) 실제 CSV 헤더 키워드에 맞춰 "거래유형" 대신 "타입", "거래금액" 대신 "금액"을 사용하도록 수정
        final List<String> EXPECTED_HEADER_KEYWORDS = Arrays.asList("주소", "면적", "금액", "월세", "위도");
        final int MIN_HEADERS_TO_MATCH = 3;
        
        String fullPath = "classpath:csv/" + filePath;
        CSVReader csvReader = null;

        try (InputStreamReader reader = new InputStreamReader(resourceLoader.getResource(fullPath).getInputStream(), encoding);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
        	
        	// 기존 로직과 동일하게 실제 데이터 라인까지 건너뛰는 로직 (헤더 찾기)
        	String currentLine;
        	boolean headerFound = false;
        	int linesRead = 0;
        	
        	while((currentLine = bufferedReader.readLine()) != null) {
        		linesRead++;
        		
        		// 헤더 키워드가 현재 라인에 몇 개 포함되어 있는지 카운트
        		long matchCount = EXPECTED_HEADER_KEYWORDS.stream().filter(currentLine::contains).count();
        		
        		if(matchCount >= MIN_HEADERS_TO_MATCH) {
        			headerFound = true;
        			break;
        		}
        		
        		if(linesRead > 100) {
        			// 헤더를 찾지 못하면 오류 발생
        			throw new IOException("100줄을 지나도 헤더가 없습니다. : " + filePath);
        		}
        	}
        	
        	csvReader = new CSVReaderBuilder(bufferedReader).build();
      
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                // Map 엔티티 필드(7개)에 맞게 최소 컬럼 개수를 7개로 가정
                if (line.length < 7) continue; 

                MapData mapTx = new MapData();
                try {
                    // CSV 파일의 컬럼 순서: 주소(0), 면적(1), 금액(2), 월세(3), 타입(4), 위도(5), 경도(6)
                    mapTx.setAddress(line[0].trim());         // 주소 (인덱스 0)
                    mapTx.setArea(Double.parseDouble(line[1].replace(",", "").trim())); // 면적 (인덱스 1)
                    mapTx.setPrice(Integer.parseInt(line[2].replace(",", "").trim())); // 금액 (인덱스 2)
                    mapTx.setRent(Integer.parseInt(line[3].replace(",", "").trim())); // 월세 (인덱스 3)
                    mapTx.setTransactionType(line[4].trim()); // 타입 (인덱스 4)
                    mapTx.setLatitude(Double.parseDouble(line[5].trim()));    // 위도 (인덱스 5)
                    mapTx.setLongitude(Double.parseDouble(line[6].trim()));   // 경도 (인덱스 6)

                    if (!mapTx.getAddress().isEmpty()) { 
                        list.add(mapTx);
                    }
                } catch (Exception e) {
                    System.err.println("[CsvService] Map 데이터 파싱 중 오류 발생: " + String.join(", ", line) + " - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[CsvService] 부동산 거래(Map) CSV 파싱 실패: " + filePath);
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

    public void loadHousingPolicies() {
        System.out.println("[CsvService] 부동산 정책 CSV 파일 로드 시작...");
        
        housingPolicyRepository.deleteAll(); 
        
        final String filePath = "부동산_정책.csv";
        List<HousingPolicy> policies = parsePolicyFile(filePath, ENC_UTF8, 1);
        
        housingPolicyRepository.saveAll(policies);
        System.out.println("[CsvService] 총 " + policies.size() + "건의 부동산 정책 데이터 로드 완료.");
    }

    private List<HousingPolicy> parsePolicyFile(String filePath, String encoding, int skipLines) {
        List<HousingPolicy> list = new ArrayList<>();
        String fullPath = "classpath:csv/" + filePath;

        try (InputStreamReader reader = new InputStreamReader(resourceLoader.getResource(fullPath).getInputStream(), encoding);
             CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(skipLines).build()) {

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length < 8) continue; 

                HousingPolicy p = new HousingPolicy();
                try {
                    p.setPolicyNo(line[1].trim());
                    p.setTitle(line[2].trim());
                    p.setOrganizer(line[3].trim());
                    p.setRegion(line[4].trim());
                    p.setTargetAge(line[5].trim());
                    p.setBenefits(line[6].trim());
                    p.setDescription(line[6].trim());
                    p.setApplicationMethod(line[7].trim());
                    if (line.length > 8) p.setAplyUrl(line[8].trim());
                    p.setStatus("진행중");

                    list.add(p);
                } catch (Exception e) {
                    System.err.println("[CsvService] 정책 데이터 파싱 오류: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[CsvService] 부동산 정책 CSV 파싱 실패: " + filePath);
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