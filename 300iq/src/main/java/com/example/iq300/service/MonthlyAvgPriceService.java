package com.example.iq300.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.example.iq300.domain.MonthlyAvgPrice;
import com.example.iq300.domain.MonthlyAvgPriceId;
import com.example.iq300.domain.RealEstateTransaction;
import com.example.iq300.repository.MonthlyAvgPriceRepository;
import com.example.iq300.repository.TransactionRepository;

@Service
public class MonthlyAvgPriceService {

    private final TransactionRepository transactionRepository;
    private final MonthlyAvgPriceRepository monthlyAvgPriceRepository;

    // 평방미터(㎡)를 평으로 환산하기 위한 상수 (1㎡ ≈ 0.3025평)
    private static final double SQM_TO_PYEONG = 0.3025;
    
    // 월세를 보증금으로 환산할 때 사용할 전환율 (연 4.5% 가정)
    // 월세 1만원을 전세 보증금으로 환산하는 계수: (12개월 / 0.045)
    private static final double RENT_TO_DEPOSIT_COEFFICIENT = 12.0 / 0.045; 

    public MonthlyAvgPriceService(
            TransactionRepository transactionRepository,
            MonthlyAvgPriceRepository monthlyAvgPriceRepository) {
        this.transactionRepository = transactionRepository;
        this.monthlyAvgPriceRepository = monthlyAvgPriceRepository;
    }

    
    public List<MonthlyAvgPrice> aggregateAndSaveData() {
        System.out.println("[MonthlyAvgPriceService] 데이터 집계 및 저장 시작...");

        List<RealEstateTransaction> allTransactions = transactionRepository.findAll();
        
        // 1. 기존의 동별 집계 수행
        List<MonthlyAvgPrice> aggregatedList = calculateAggregates(allTransactions); // aggregatedList 이름 유지
        
        // 2. ⭐ [수정됨]: 구 전체 평균 집계 수행
        List<MonthlyAvgPrice> guAggregatedList = calculateGuAggregates(allTransactions);

        // 3. ⭐ [수정됨]: 두 리스트를 합쳐 aggregatedList에 저장 (이름 유지)
        aggregatedList.addAll(guAggregatedList);
        
        List<MonthlyAvgPrice> savedAggregates = monthlyAvgPriceRepository.saveAll(aggregatedList); // aggregatedList 저장

        System.out.println("[MonthlyAvgPriceService] 총 " + savedAggregates.size() + "건의 월별 평균 가격 데이터 저장 완료.");
        return savedAggregates;
    }

    private List<MonthlyAvgPrice> calculateAggregates(List<RealEstateTransaction> transactions) {
        // 집계를 위한 그룹화 키 생성 (구, 동, 건물 종류, 세분화된 거래 유형, 계약 월)
        Map<String, List<RealEstateTransaction>> groupedMap = transactions.stream()
                .filter(tx -> tx.getArea() > 0)
                .collect(Collectors.groupingBy(this::generateGroupingKey)); // 기존 generateGroupingKey 사용
        
        // 그룹별로 평균값 계산
        return groupedMap.entrySet().stream()
                .map(entry -> {
                    List<RealEstateTransaction> list = entry.getValue();
                    String[] keyParts = entry.getKey().split("::");
                    
                    // Grouping Key에서 추출된 정보를 사용하여 MonthlyAvgPrice 객체 생성
                    String sigunguFromAddress = list.get(0).getAddress();
                    String gu = keyParts[0];             
                    String dong = keyParts[1];           
                    String buildingType = keyParts[2];   
                    String transactionType = keyParts[3];
                    String contractMonth = keyParts[4];  

                    // 1. 평당 가격의 합계와 전체 거래 건수 계산
                    double totalPyeongPrice = list.stream()
                            .mapToDouble(tx -> {
                                double effectivePrice = 0; 
                                double areaPyeong = tx.getArea() * SQM_TO_PYEONG;
                                
                                // ⭐ 세분화된 거래 유형에 따른 유효 가격 결정 로직
                                if (transactionType.equals("매매") || transactionType.equals("전세")) {
                                    effectivePrice = tx.getPrice(); 

                                    if (effectivePrice > 0 && areaPyeong > 0) {
                                        return effectivePrice / areaPyeong;
                                    }
                                    
                                } else if (transactionType.equals("월세")) {
                                    double convertedRent = tx.getRent();
                                    effectivePrice = convertedRent;
                                    if (effectivePrice > 0 && areaPyeong > 0) {
                                        return effectivePrice/ areaPyeong;
                                    }
                                }

                                return 0;
                            })
                            .sum();

                    long count = list.size();
                    double avgPricePerPyeong = (count > 0) ? totalPyeongPrice / count : 0; 

                    MonthlyAvgPriceId id = new MonthlyAvgPriceId();
                    id.setGu(gu);
                    id.setDong(dong); // 동별 값 저장
                    id.setBuildingType(buildingType);
                    id.setTransactionType(transactionType);
                    id.setContractMonth(contractMonth);
                    
                    // 2. MonthlyAvgPrice 객체 생성 및 값 설정
                    MonthlyAvgPrice aggregate = new MonthlyAvgPrice();
                    aggregate.setId(id);
                    aggregate.setSigungu(sigunguFromAddress); 
                    aggregate.setAvgPricePerPyeong(avgPricePerPyeong);
                    aggregate.setTransactionCount(count);

                    return aggregate;
                })
                .collect(Collectors.toList());
    }
    
    private List<MonthlyAvgPrice> calculateGuAggregates(List<RealEstateTransaction> transactions) {
        // 집계를 위한 그룹화 키 생성 (구, 건물 종류, 세분화된 거래 유형, 계약 월)
        Map<String, List<RealEstateTransaction>> groupedMap = transactions.stream()
                .filter(tx -> tx.getArea() > 0)
                .collect(Collectors.groupingBy(this::generateGuGroupingKey)); // 새로운 generateGuGroupingKey 사용
        
        // 그룹별로 평균값 계산
        return groupedMap.entrySet().stream()
                .map(entry -> {
                    List<RealEstateTransaction> list = entry.getValue();
                    String[] keyParts = entry.getKey().split("::");
                    
                    // Grouping Key에서 추출된 정보를 사용하여 MonthlyAvgPrice 객체 생성
                    String sigunguFromAddress = list.get(0).getAddress(); 
                    String gu = keyParts[0];             // 구
                    String buildingType = keyParts[1];   // 건물 종류
                    String transactionType = keyParts[2];// **세분화된 거래 유형 (매매, 전세, 월세)**
                    String contractMonth = keyParts[3];  // 계약 월

                    // 1. 평당 가격의 합계와 전체 거래 건수 계산 (로직 동일)
                    double totalPyeongPrice = list.stream()
                            .mapToDouble(tx -> {
                                double effectivePrice = 0; 
                                double areaPyeong = tx.getArea() * SQM_TO_PYEONG;
                                
                                if (transactionType.equals("매매") || transactionType.equals("전세")) {
                                    effectivePrice = tx.getPrice(); 
                                    if (effectivePrice > 0 && areaPyeong > 0) {
                                        return effectivePrice/ areaPyeong;
                                    }
                                } else if (transactionType.equals("월세")) {
                                    double convertedRent = tx.getRent();
                                    effectivePrice = convertedRent;
                                    if (effectivePrice > 0 && areaPyeong > 0) {
                                        return effectivePrice/ areaPyeong;
                                    }
                                }
                                
                                return 0;
                            })
                            .sum();

                    long count = list.size();
                    double avgPricePerPyeong = (count > 0) ? totalPyeongPrice / count : 0; 

                    MonthlyAvgPriceId id = new MonthlyAvgPriceId();
                    id.setGu(gu);
                    id.setDong("전체"); // 동 카테고리를 '전체'로 설정
                    id.setBuildingType(buildingType);
                    id.setTransactionType(transactionType);
                    id.setContractMonth(contractMonth);
                    
                    // 2. MonthlyAvgPrice 객체 생성 및 값 설정
                    MonthlyAvgPrice aggregate = new MonthlyAvgPrice();
                    aggregate.setId(id);
                    aggregate.setSigungu(sigunguFromAddress); 
                    aggregate.setAvgPricePerPyeong(avgPricePerPyeong);
                    aggregate.setTransactionCount(count);

                    return aggregate;
                })
                .collect(Collectors.toList());
    }

    private String generateGroupingKey(RealEstateTransaction tx) {
        // 주소에서 구, 동/면 추출 (주소 체계를 기반으로 인덱스 접근)
        String[] addressParts = tx.getAddress().split(" ");
        String gu = addressParts.length > 2 ? addressParts[2] : "미상"; // 2번째 인덱스가 구
        String dong = addressParts.length > 3 ? addressParts[3] : "미상"; // 3번째 인덱스가 동/면 (읍/면)

        // transactionType에서 건물 종류와 거래 유형 분리
        String fullType = tx.getTransactionType();
        int openParen = fullType.indexOf('(');
        int closeParen = fullType.indexOf(')');
        
        String buildingType = (openParen != -1) ? fullType.substring(0, openParen) : fullType;
        String baseTransactionType = (openParen != -1 && closeParen != -1) ? 
                                 fullType.substring(openParen + 1, closeParen) : "미상";

        // 전월세 거래를 전세와 월세로 분리하여 그룹화 키를 생성
        String specificTransactionType;
        if (baseTransactionType.equals("전월세")) {
            if (tx.getRent() > 0) {
                // 월세 거래: 월세가 있는 경우
                specificTransactionType = "월세"; 
            } else if (tx.getPrice() > 0) {
                // 전세 거래: 월세가 없고 보증금/전세금이 있는 경우
                specificTransactionType = "전세"; 
            } else {
                specificTransactionType = "전월세(기타)"; // 가격/월세 정보가 없는 예외 케이스
            }
        } else {
            specificTransactionType = baseTransactionType; // 매매 등
        }

        // contractDate에서 년월만 추출 (YYYYMM)
        String contractMonth = tx.getContractDate().length() >= 6 ? tx.getContractDate().substring(0, 6) : "미상";

        // 그룹화 키를 Gu::Dong::BuildingType::SpecificTransactionType::ContractMonth 형식으로 생성
        return gu + "::" + dong + "::" + buildingType + "::" + specificTransactionType + "::" + contractMonth;
    }
    
    private String generateGuGroupingKey(RealEstateTransaction tx) {
        // 주소에서 구 추출
        String[] addressParts = tx.getAddress().split(" ");
        String gu = addressParts.length > 2 ? addressParts[2] : "미상";

        // transactionType에서 건물 종류와 거래 유형 분리
        String fullType = tx.getTransactionType();
        int openParen = fullType.indexOf('(');
        int closeParen = fullType.indexOf(')');
        
        String buildingType = (openParen != -1) ? fullType.substring(0, openParen) : fullType;
        String baseTransactionType = (openParen != -1 && closeParen != -1) ? 
                                 fullType.substring(openParen + 1, closeParen) : "미상";

        // 전월세 거래를 전세와 월세로 분리하여 그룹화 키를 생성
        String specificTransactionType;
        if (baseTransactionType.equals("전월세")) {
            if (tx.getRent() > 0) {
                specificTransactionType = "월세"; 
            } else if (tx.getPrice() > 0) {
                specificTransactionType = "전세"; 
            } else {
                specificTransactionType = "전월세(기타)"; 
            }
        } else {
            specificTransactionType = baseTransactionType;
        }

        String contractMonth = tx.getContractDate().length() >= 6 ? tx.getContractDate().substring(0, 6) : "미상";

        // 키 형식: Gu::BuildingType::SpecificTransactionType::ContractMonth
        return gu + "::" + buildingType + "::" + specificTransactionType + "::" + contractMonth;
    }


    /**
     * Controller에서 4개 구의 차트 데이터를 조회할 때 사용됩니다.
     */
    public List<MonthlyAvgPrice> getDistrictAvgPriceData(){
    	List<String> targetDistricts = Arrays.asList("상당구","서원구", "청원구", "흥덕구");
    	
        List<MonthlyAvgPrice> monthlyAvgPriceList = monthlyAvgPriceRepository.findById_GuIn(targetDistricts).stream()
            .collect(Collectors.toList());
            
    	return monthlyAvgPriceList;  
    }
}