package com.example.iq300.service;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets; // 1. (추가) UTF-8 임포트
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

// 2. (추가) 새 도메인 임포트
import com.example.iq300.domain.RealEstateTerm; 
import com.example.iq300.domain.Population;
import com.example.iq300.domain.RealEstateAgent;
import com.example.iq300.domain.RealEstateTransaction;

// 3. (추가) 새 리포지토리 임포트
import com.example.iq300.repository.RealEstateTermRepository; 
import com.example.iq300.repository.PopulationRepository;
import com.example.iq300.repository.RealEstateAgentRepository;
import com.example.iq300.repository.TransactionRepository;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

@Service
public class CsvDataService {

    private final ResourceLoader resourceLoader;

    // JPA Repository 주입
    private final TransactionRepository transactionRepository;
    private final RealEstateAgentRepository realEstateAgentRepository;
    private final PopulationRepository populationRepository;
    
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
            TransactionRepository transactionRepository,
            RealEstateAgentRepository realEstateAgentRepository,
            PopulationRepository populationRepository,
            // 6. (추가) 생성자에 새 리포지토리 주입
            RealEstateTermRepository realEstateTermRepository) { 
        this.resourceLoader = resourceLoader;
        this.transactionRepository = transactionRepository;
        this.realEstateAgentRepository = realEstateAgentRepository;
        this.populationRepository = populationRepository;
        // 7. (추가) 리포지토리 초기화
        this.realEstateTermRepository = realEstateTermRepository;
    }

    // --- 1. 실거래가 CSV 데이터 로드 및 DB 적재 ---
    // (기존 코드와 동일)
    public List<RealEstateTransaction> loadTransactions() {
        System.out.println("[CsvService] 실거래가 CSV 파일 로드 시작...");

        int skipLines = 20;
        final String ENC_FOR_TRANSACTION = ENC_MS949;

        List<RealEstateTransaction> allTransactions = new ArrayList<>();
        
        // 서원구 (Seowon)
        allTransactions.addAll(parseTransactionFile("단독다가구_매매_실거래가_2025년_서원구.csv", "단독다가구(매매)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("단독다가구_전월세_실거래가_2025년_서원구.csv", "단독다가구(전월세)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("아파트_매매_실거래가_2025년_서원구.csv", "아파트(매매)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("아파트_전월세_실거래가_2025년_서원구.csv", "아파트(전월세)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("연립다세대_매매_실거래가_2025년_서원구.csv", "연립다세대(매매)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("연립다세대_전월세_실거래가_2025년_서원구.csv", "연립다세대(전월세)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("오피스텔_매매_실거래가_2025년_서원구.csv", "오피스텔(매매)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("오피스텔_전월세_실거래가_2025년_서원구.csv", "오피스텔(전월세)", ENC_FOR_TRANSACTION, skipLines));


        // 청원구 (Cheongwon)
        allTransactions.addAll(parseTransactionFile("단독다가구_매매_실거래가_2025년_청원구.csv", "단독다가구(매매)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("단독다가구_전월세_실거래가_2025년_청원구.csv", "단독다가구(전월세)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("아파트_매매_실거래가_2025년_청원구.csv", "아파트(매매)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("아파트_전월세_실거래가_2025년_청원구.csv", "아파트(전월세)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("연립다세대_매매_실거래가_2025년_청원구.csv", "연립다세대(매매)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("연립다세대_전월세_실거래가_2025년_청원구.csv", "연립다세대(전월세)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("오피스텔_매매_실거래가_2025년_청원구.csv", "오피스텔(매매)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("오피스텔_전월세_실거래가_2025년_청원구.csv", "오피스텔(전월세)", ENC_FOR_TRANSACTION, skipLines));
        
        // 흥덕구 (Heungdeok)
        allTransactions.addAll(parseTransactionFile("단독다가구_매매_실거래가_2025년_흥덕구.csv", "단독다가구(매매)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("단독다가구_전월세_실거래가_2025년_흥덕구.csv", "단독다가구(전월세)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("아파트_매매_실거래가_2025년_흥덕구.csv", "아파트(매매)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("아파트_전월세_실거래가_2025년_흥덕구.csv", "아파트(전월세)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("연립다세대_매매_실거래가_2025년_흥덕구.csv", "연립다세대(매매)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("연립다세대_전월세_실거래가_2025년_흥덕구.csv", "연립다세대(전월세)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("오피스텔_매매_실거래가_2025년_흥덕구.csv", "오피스텔(매매)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("오피스텔_전월세_실거래가_2025년_흥덕구.csv", "오피스텔(전월세)", ENC_FOR_TRANSACTION, skipLines));
        
        // 상당구 (Sangdang)
        allTransactions.addAll(parseTransactionFile("단독다가구_매매_실거래가_2025년_상당구.csv", "단독다가구(매매)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("단독다가구_전월세_실거래가_2025년_상당구.csv", "단독다가구(전월세)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("아파트_매매_실거래가_2025년_상당구.csv", "아파트(매매)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("아파트_전월세_실거래가_2025년_상당구.csv", "아파트(전월세)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("연립다세대_매매_실거래가_2025년_상당구.csv", "연립다세대(매매)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("연립다세대_전월세_실거래가_2025년_상당구.csv", "연립다세대(전월세)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("오피스텔_매매_실거래가_2025년_상당구.csv", "오피스텔(매매)", ENC_FOR_TRANSACTION, skipLines));
        allTransactions.addAll(parseTransactionFile("오피스텔_전월세_실거래가_2025년_상당구.csv", "오피스텔(전월세)", ENC_FOR_TRANSACTION, skipLines));
        
        
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
    private List<RealEstateTransaction> parseTransactionFile(String filePath, String txType, String encoding, int skipLines) {
        List<RealEstateTransaction> list = new ArrayList<>();
        String fullPath = "classpath:csv/" + filePath;

        try (InputStreamReader reader = new InputStreamReader(resourceLoader.getResource(fullPath).getInputStream(), encoding);
             CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(skipLines).build()) {

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length < 10) continue; 

                RealEstateTransaction tx = new RealEstateTransaction();
                tx.setTransactionType(txType);

                try {
                    if (txType.equals("단독다가구(매매)")) {
                        tx.setAddress(line[1]);      
                        tx.setBuildingName("-"); 
                        tx.setArea(Double.parseDouble(line[5])); 
                        tx.setContractDate(line[8] + String.format("%02d", Integer.parseInt(line[9]))); 
                        tx.setPrice(Integer.parseInt(line[10].replace(",", ""))); 
                        tx.setRent(0); 
                    } else if (txType.equals("단독다가구(전월세)")) {
                        tx.setAddress(line[1]);      
                        tx.setBuildingName("-"); 
                        tx.setArea(Double.parseDouble(line[4])); 
                        tx.setContractDate(line[6] + String.format("%02d", Integer.parseInt(line[7]))); 
                        tx.setPrice(Integer.parseInt(line[8].replace(",", ""))); 
                        tx.setRent(Integer.parseInt(line[9].replace(",", "")));  
                    } else if (txType.equals("아파트(매매)")) {
                        tx.setAddress(line[1]);      
                        tx.setBuildingName(line[5]); 
                        tx.setArea(Double.parseDouble(line[6])); 
                        tx.setContractDate(line[7] + String.format("%02d", Integer.parseInt(line[8]))); 
                        tx.setPrice(Integer.parseInt(line[9].replace(",", ""))); 
                        tx.setRent(0); 
                    } else if (txType.equals("아파트(전월세)")) {
                        tx.setAddress(line[1]);      
                        tx.setBuildingName(line[5]); 
                        tx.setArea(Double.parseDouble(line[7])); 
                        tx.setContractDate(line[8] + String.format("%02d", Integer.parseInt(line[9]))); 
                        tx.setPrice(Integer.parseInt(line[10].replace(",", ""))); 
                        tx.setRent(Integer.parseInt(line[11].replace(",", "")));  
                    } else if (txType.equals("연립다세대(매매)")) {
                        tx.setAddress(line[1]);      
                        tx.setBuildingName(line[5]); 
                        tx.setArea(Double.parseDouble(line[6])); 
                        tx.setContractDate(line[8] + String.format("%02d", Integer.parseInt(line[9]))); 
                        tx.setPrice(Integer.parseInt(line[10].replace(",", ""))); 
                        tx.setRent(0); 
                    } else if (txType.equals("연립다세대(전월세)")) {
                        tx.setAddress(line[1]);      
                        tx.setBuildingName(line[5]); 
                        tx.setArea(Double.parseDouble(line[7])); 
                        tx.setContractDate(line[8] + String.format("%02d", Integer.parseInt(line[9]))); 
                        tx.setPrice(Integer.parseInt(line[10].replace(",", ""))); 
                        tx.setRent(Integer.parseInt(line[11].replace(",", "")));  
                    } else if (txType.equals("오피스텔(매매)")) {
                        tx.setAddress(line[1]);      
                        tx.setBuildingName(line[5]); 
                        tx.setArea(Double.parseDouble(line[6])); 
                        tx.setContractDate(line[7] + String.format("%02d", Integer.parseInt(line[8]))); 
                        tx.setPrice(Integer.parseInt(line[9].replace(",", ""))); 
                        tx.setRent(0); 
                    } else if (txType.equals("오피스텔(전월세)")) {
                        tx.setAddress(line[1]);      
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
        }
        return list;
    }
    
    // --- 2. 중개인 CSV 데이터 로드 및 DB 적재 ---
    // (기존 코드와 동일)
    public List<RealEstateAgent> loadAgents(){
    	System.out.println("[CsvService] 중개인 CSV 파일 로드 시작...");
    	
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