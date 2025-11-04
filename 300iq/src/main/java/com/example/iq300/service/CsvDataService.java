package com.example.iq300.service; // (중요) 기존 서비스 패키지 사용

import com.example.iq300.domain.PriceTimeSeries;
import com.example.iq300.domain.RealEstateTransaction;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets; // UTF-8
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvDataService {

    private final ResourceLoader resourceLoader;
    // (중요) CSV 파일 인코딩 설정.
    // R-ONE 지수/현황 파일 = UTF-8
    // 국토부 실거래가 파일 = EUC-KR
    private static final String ENC_EUC_KR = "EUC-KR";
    private static final String ENC_UTF_8 = "UTF-8";

    public CsvDataService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    // --- 1. 시계열(지수/현황) CSV 데이터 로드 (10개) ---
    // (참고) 이 부분은 지수/현황 10개 파일이므로 "변경되지 않았습니다."
    public List<PriceTimeSeries> loadAllPriceTimeSeriesData() {
        System.out.println("[CsvService] 시계열(지수/현황) CSV 파일 로드 시작...");
        List<PriceTimeSeries> allTimeSeries = new ArrayList<>();

        // (주의) 파일명, 통계명, 인코딩, 헤더 줄 수(skipLines)를 정확히 확인하세요.
        // (참고) "Type1" = 날짜별 2개 컬럼(지수, 변동률), "Type2" = 날짜별 1개 컬럼(값)
        int skipLines = 0; // CSV의 첫 줄이 헤더이므로, 0줄을 스킵하고 바로 헤더를 읽습니다.

        // Type 1 (날짜별 2개 컬럼)
        allTimeSeries.addAll(parseTimeSeries_Type1("(월) 매매가격지수_아파트.csv", "매매가격지수", "아파트", ENC_UTF_8, skipLines));
        allTimeSeries.addAll(parseTimeSeries_Type1("(월) 전세가격지수_아파트.csv", "전세가격지수", "아파트", ENC_UTF_8, skipLines));
        allTimeSeries.addAll(parseTimeSeries_Type1("(월) 월세가격지수_아파트.csv", "월세가격지수", "아파트", ENC_UTF_8, skipLines));
        allTimeSeries.addAll(parseTimeSeries_Type1("(월) 지역별 지가변동률.csv", "지가변동률", "지가", ENC_UTF_8, skipLines));
        allTimeSeries.addAll(parseTimeSeries_Type1("(월) 행정구역별 아파트거래현황.csv", "아파트거래현황", "아파트", ENC_UTF_8, skipLines));
        allTimeSeries.addAll(parseTimeSeries_Type1("(월) 행정구역별 아파트매매거래현황.csv", "아파트매매거래현황", "아파트", ENC_UTF_8, skipLines));
        
        // Type 2 (날짜별 1개 컬럼)
        allTimeSeries.addAll(parseTimeSeries_Type2("(월) 지역별 지가지수.csv", "지가지수", "지가", ENC_UTF_8, skipLines));
        allTimeSeries.addAll(parseTimeSeries_Type2("(월) 평균매매가격_아파트.csv", "평균매매가격", "아파트", ENC_UTF_8, skipLines));
        allTimeSeries.addAll(parseTimeSeries_Type2("(월) 평균전세가격_아파트.csv", "평균전세가격", "아파트", ENC_UTF_8, skipLines));
        allTimeSeries.addAll(parseTimeSeries_Type2("(월) 평균월세가격_아파트.csv", "평균월세가격", "아파트", ENC_UTF_8, skipLines));
        
        System.out.println("[CsvService] 총 " + allTimeSeries.size() + "건의 시계열(지수/현황) 데이터 로드 완료.");
        return allTimeSeries;
    }

    /**
     * '넓은' 시계열 CSV (Type1: 날짜당 2컬럼)를 '긴(long)' DB 데이터로 변환
     */
    private List<PriceTimeSeries> parseTimeSeries_Type1(String filePath, String metricType, String propertyType, String encoding, int skipLines) {
        List<PriceTimeSeries> list = new ArrayList<>();
        String fullPath = "classpath:csv/" + filePath; // 'src/main/resources/csv/' 기준 경로

        try (InputStreamReader reader = new InputStreamReader(resourceLoader.getResource(fullPath).getInputStream(), encoding);
             CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(skipLines).build()) {

            String[] dateHeader = csvReader.readNext(); // [No, 지역, 지역, 지역, 2003년 11월, 2003년 11월, ...]
            String[] line;
            
            while ((line = csvReader.readNext()) != null) {
                // 1. 지역명 조합 (예: "충북 청주시 서원구")
                String region = line[1] + (line[2].isEmpty() ? "" : " " + line[2]) + (line[3].isEmpty() ? "" : " " + line[3]);
                region = region.trim(); // "충북 청주시 " 같은 공백 제거

                // 2. 날짜 컬럼(4번 인덱스부터)을 2칸씩 순회 (지수/값만 사용)
                for (int i = 4; i < line.length && i < dateHeader.length; i += 2) {
                    String yearMonthStr = dateHeader[i]; // 예: "2025년 9월"
                    String valueStr = line[i]; // (변동률 컬럼(i+1)은 무시하고 값(i)만 사용)

                    if (valueStr == null || valueStr.isEmpty() || valueStr.equals("-") || yearMonthStr == null || yearMonthStr.isEmpty()) {
                        continue;
                    }

                    try {
                        double value = Double.parseDouble(valueStr.replace(",", "")); // "1,234" 같은 콤마 제거
                        String yearMonth = formatYearMonth(yearMonthStr); // "2025년 9월" -> "202509"
                        
                        list.add(new PriceTimeSeries(region, metricType, propertyType, yearMonth, value));
                    } catch (NumberFormatException e) { 
                        // 숫자 변환 실패 (e.g., "...")
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[CsvService] Type1 파싱 실패: " + filePath);
        }
        return list;
    }
    
    /**
     * '넓은' 시계열 CSV (Type2: 날짜당 1컬럼)를 '긴(long)' DB 데이터로 변환
     */
    private List<PriceTimeSeries> parseTimeSeries_Type2(String filePath, String metricType, String propertyType, String encoding, int skipLines) {
        List<PriceTimeSeries> list = new ArrayList<>();
        String fullPath = "classpath:csv/" + filePath;

        try (InputStreamReader reader = new InputStreamReader(resourceLoader.getResource(fullPath).getInputStream(), encoding);
             CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(skipLines).build()) {

            String[] dateHeader = csvReader.readNext(); // [No, 지역, 지역, 지역, 2005년 1월, 2005년 2월, ...]
            String[] line;

            while ((line = csvReader.readNext()) != null) {
                // 1. 지역명 조합
                String region = line[1] + (line[2].isEmpty() ? "" : " " + line[2]) + (line[3].isEmpty() ? "" : " " + line[3]);
                region = region.trim();

                // 2. 날짜 컬럼(4번 인덱스부터)을 1칸씩 순회
                for (int i = 4; i < line.length && i < dateHeader.length; i++) {
                    String yearMonthStr = dateHeader[i]; // 예: "2025년 9월"
                    String valueStr = line[i];

                    if (valueStr == null || valueStr.isEmpty() || valueStr.equals("-") || yearMonthStr == null || yearMonthStr.isEmpty()) {
                        continue;
                    }

                    try {
                        double value = Double.parseDouble(valueStr.replace("\"", "").replace(",", "")); // ""1,234"" 같은 따옴표/콤마 제거
                        String yearMonth = formatYearMonth(yearMonthStr); // "2025년 9월" -> "202509"
                        
                        list.add(new PriceTimeSeries(region, metricType, propertyType, yearMonth, value));
                    } catch (NumberFormatException e) {
                         // 숫자 변환 실패 (e.g., "...")
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[CsvService] Type2 파싱 실패: " + filePath);
        }
        return list;
    }

    // --- 2. 실거래가 CSV 데이터 로드 (8개) ---
    // (변경) 파일 이름들을 새 형식으로 모두 수정했습니다.
    public List<RealEstateTransaction> loadAllRealEstateTransactions() {
        System.out.println("[CsvService] 실거래가 CSV 파일 로드 시작...");
        List<RealEstateTransaction> allTransactions = new ArrayList<>();
        
        int skipLines = 12; // 실거래가 파일은 헤더 12줄
        
        allTransactions.addAll(parseTransactionFile("아파트_매매__실거래가_2025년_서원구.csv", "아파트(매매)", ENC_EUC_KR, skipLines));
        allTransactions.addAll(parseTransactionFile("아파트_매매__실거래가_2025년_청원구.csv", "아파트(매매)", ENC_EUC_KR, skipLines));
        allTransactions.addAll(parseTransactionFile("아파트_매매__실거래가_2025년_흥덕구.csv", "아파트(매매)", ENC_EUC_KR, skipLines));
        allTransactions.addAll(parseTransactionFile("아파트_매매__실거래가_2025년도_상당구.csv", "아파트(매매)", ENC_EUC_KR, skipLines)); // "년도"
        
        allTransactions.addAll(parseTransactionFile("아파트_전월세__실거래가_2025년_서원구.csv", "아파트(전월세)", ENC_EUC_KR, skipLines));
        allTransactions.addAll(parseTransactionFile("아파트_전월세__실거래가_2025년_청원구.csv", "아파트(전월세)", ENC_EUC_KR, skipLines));
        allTransactions.addAll(parseTransactionFile("아파트_전월세__실거래가_2025년_흥덕구.csv", "아파트(전월세)", ENC_EUC_KR, skipLines));
        allTransactions.addAll(parseTransactionFile("아파트_전월세__실거래가_2025년도_상당구.csv", "아파트(전월세)", ENC_EUC_KR, skipLines)); // "년도"
        
        System.out.println("[CsvService] 총 " + allTransactions.size() + "건의 실거래가 데이터 로드 완료.");
        return allTransactions;
    }

    /**
     * 실거래가 CSV 파일을 파싱합니다.
     * (내부 파싱 로직은 변경되지 않았습니다.)
     */
    private List<RealEstateTransaction> parseTransactionFile(String filePath, String txType, String encoding, int skipLines) {
        List<RealEstateTransaction> list = new ArrayList<>();
        String fullPath = "classpath:csv/" + filePath;

        try (InputStreamReader reader = new InputStreamReader(resourceLoader.getResource(fullPath).getInputStream(), encoding);
             CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(skipLines).build()) {

            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length < 10) continue; // 데이터가 부족한 줄은 건너뜁니다.

                RealEstateTransaction tx = new RealEstateTransaction();
                tx.setTransactionType(txType);
                
                // (주의) CSV 파일을 열어보고 인덱스를 정확히 확인해야 합니다.
                try {
                    if (txType.equals("아파트(매매)")) {
                        tx.setAddress(line[1]);       // 시군구 (인덱스 1)
                        tx.setBuildingName(line[5]);  // 단지명 (인덱스 5)
                        tx.setArea(Double.parseDouble(line[6])); // 전용면적(㎡) (인덱스 6)
                        tx.setContractDate(line[7] + String.format("%02d", Integer.parseInt(line[8]))); // 계약년월 + 계약일 (인덱스 7, 8)
                        tx.setPrice(Integer.parseInt(line[9].replace(",", ""))); // 거래금액 (인덱스 9)
                        tx.setRent(0); // 매매
                    } else if (txType.equals("아파트(전월세)")) {
                         tx.setAddress(line[1]);       // 시군구 (인덱스 1)
                         tx.setBuildingName(line[5]);  // 단지명 (인덱스 5)
                         tx.setArea(Double.parseDouble(line[7])); // 전용면적(㎡) (인덱스 7)
                         tx.setContractDate(line[8] + String.format("%02d", Integer.parseInt(line[9]))); // 계약년월 + 계약일 (인덱스 8, 9)
                         tx.setPrice(Integer.parseInt(line[10].replace(",", ""))); // 보증금 (인덱스 10)
                         tx.setRent(Integer.parseInt(line[11].replace(",", "")));  // 월세 (인덱스 11)
                    }
                    
                    if (tx.getAddress() != null && !tx.getAddress().isEmpty()) { // 파싱이 성공한 경우(주소가 있는 경우)에만 추가
                        list.add(tx);
                    }
                } catch (Exception e) {
                     // System.err.println("[CsvService] 파싱 오류: " + e.getMessage() + " | " + String.join(",", line));
                     // 파싱 중 오류(e.g., 빈 줄)가 발생해도 무시하고 다음 줄로 넘어갑니다.
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("[CsvService] 실거래가 CSV 파싱 실패: " + filePath);
        }
        return list;
    }
    
    // "2025년 9월" -> "202509"로 변환하는 헬퍼 메서드
    private String formatYearMonth(String yearMonthStr) {
        String[] parts = yearMonthStr.replace("월", "").split("년 ");
        if (parts.length == 2) {
            try {
                return parts[0] + String.format("%02d", Integer.parseInt(parts[1]));
            } catch (NumberFormatException e) {
                return yearMonthStr; // 숫자 변환 실패시 원본 반환
            }
        }
        return yearMonthStr; // "년 " 형식이 아닐 경우 원본 반환
    }
}