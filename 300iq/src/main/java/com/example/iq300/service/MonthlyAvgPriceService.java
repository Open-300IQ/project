package com.example.iq300.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * 전체 RealEstateTransaction 데이터를 불러와 월별 평당 가격 평균을 계산하고 저장합니다.
     * @return 저장된 집계 데이터 리스트
     */
    public List<MonthlyAvgPrice> aggregateAndSaveData() {
        System.out.println("[MonthlyAvgPriceService] 데이터 집계 및 저장 시작...");

        List<RealEstateTransaction> allTransactions = transactionRepository.findAll();
        List<MonthlyAvgPrice> aggregatedList = calculateAggregates(allTransactions);

        List<MonthlyAvgPrice> savedAggregates = monthlyAvgPriceRepository.saveAll(aggregatedList);

        System.out.println("[MonthlyAvgPriceService] 총 " + savedAggregates.size() + "건의 월별 평균 가격 데이터 저장 완료.");
        return savedAggregates;
    }

    /**
     * Map/Reduce 패턴을 사용하여 집계 로직을 수행합니다.
     */
    private List<MonthlyAvgPrice> calculateAggregates(List<RealEstateTransaction> transactions) {
        // 집계를 위한 그룹화 키 생성 (구, 동, 건물 종류, 세분화된 거래 유형, 계약 월)
        Map<String, List<RealEstateTransaction>> groupedMap = transactions.stream()
                .filter(tx -> tx.getArea() > 0)
                .collect(Collectors.groupingBy(this::generateGroupingKey));
        // 

        // 그룹별로 평균값 계산
        return groupedMap.entrySet().stream()
                .map(entry -> {
                    List<RealEstateTransaction> list = entry.getValue();
                    String[] keyParts = entry.getKey().split("::");
                    
                    // Grouping Key에서 추출된 정보를 사용하여 MonthlyAvgPrice 객체 생성
                    String sigunguFromAddress = list.get(0).getAddress(); // 시군구 전체 주소는 리스트의 첫 번째 항목에서 가져옴
                    String gu = keyParts[0];             // 구
                    String dong = keyParts[1];           // 동
                    String buildingType = keyParts[2];   // 건물 종류
                    String transactionType = keyParts[3];// **세분화된 거래 유형 (매매, 전세, 월세)**
                    String contractMonth = keyParts[4];  // 계약 월

                    // 1. 평당 가격의 합계와 전체 거래 건수 계산
                    double totalPyeongPrice = list.stream()
                            .mapToDouble(tx -> {
                                double effectivePrice = 0; // 유효 가격 (만원)
                                double areaPyeong = tx.getArea() * SQM_TO_PYEONG; // 평 면적
                                
                                // ⭐ 세분화된 거래 유형에 따른 유효 가격 결정 로직
                                if (transactionType.equals("매매") || transactionType.equals("전세")) {
                                    // 매매 또는 순수 전세 (전세금/보증금 사용)
                                    effectivePrice = tx.getPrice(); 
                                } else if (transactionType.equals("월세")) {
                                    // 월세: 보증금 + (월세의 전세 환산액)
                                    double convertedRent = tx.getRent() *12;
                                    effectivePrice = tx.getPrice() + convertedRent;
                                }

                                if (effectivePrice > 0 && areaPyeong > 0) {
                                    return effectivePrice / areaPyeong;
                                }
                                return 0;
                            })
                            .sum();

                    long count = list.size();
                    double avgPricePerPyeong = (count > 0) ? totalPyeongPrice / count : 0; 

                    MonthlyAvgPriceId id = new MonthlyAvgPriceId();
                    id.setGu(gu);
                    id.setDong(dong);
                    id.setBuildingType(buildingType);
                    id.setTransactionType(transactionType); // **세분화된 유형 사용**
                    id.setContractMonth(contractMonth);
                    
                    // 2. MonthlyAvgPrice 객체 생성 및 값 설정
                    MonthlyAvgPrice aggregate = new MonthlyAvgPrice();
                    aggregate.setId(id);
                    aggregate.setSigungu(sigunguFromAddress); // 전체 주소는 대표값으로 사용
                    aggregate.setAvgPricePerPyeong(avgPricePerPyeong);
                    aggregate.setTransactionCount(count);

                    return aggregate;
                })
                .collect(Collectors.toList());
    }

    /**
     * RealEstateTransaction 객체에서 구, 동/면, 건물 종류, **세분화된 거래 유형(매매, 전세, 월세)**, 계약 월을 추출하여 그룹화 키를 생성합니다.
     * 키 형식: Gu::Dong::BuildingType::SpecificTransactionType::ContractMonth
     */
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

        // ⭐ 전월세 거래를 전세와 월세로 분리하여 그룹화 키를 생성
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
}