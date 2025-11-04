package com.example.iq300;


import com.example.iq300.repository.PriceTimeSeriesRepository;
import com.example.iq300.repository.TransactionRepository;
import com.example.iq300.service.CsvDataService;
import org.springframework.boot.CommandLineRunner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
@SpringBootApplication
public class Iq300Application {

    public static void main(String[] args) {
        SpringApplication.run(Iq300Application.class, args);
    }

    // (2. 핵심 추가) Spring Boot가 시작될 때 "딱 한 번" 이 코드를 실행합니다.
    @Bean
    public CommandLineRunner loadDataOnStartup(
            CsvDataService csvService, // 새로 만들 CsvDataService
            TransactionRepository txRepo,      // 새로 만들 TransactionRepository
            PriceTimeSeriesRepository priceRepo // 새로 만들 PriceTimeSeriesRepository
    ) {
        return args -> {
            System.out.println("====== (시작) CSV 데이터 DB 적재 ======");

            // (주의) DB에 데이터가 이미 있다면 중복 저장을 방지합니다.
            
            // 1. 시계열(지수) CSV 파일들을 읽어와서 DB에 저장
            if (priceRepo.count() == 0) {
                 priceRepo.saveAll(csvService.loadAllPriceTimeSeriesData());
                 System.out.println("시계열(지수) 데이터 저장 완료.");
            } else {
                 System.out.println("시계열(지수) 데이터는 이미 DB에 존재합니다. (Skipped)");
            }
           
            // 2. 실거래가 CSV 파일들을 읽어와서 DB에 저장
            if (txRepo.count() == 0) {
                txRepo.saveAll(csvService.loadAllRealEstateTransactions());
                System.out.println("실거래가 데이터 저장 완료.");
            } else {
                System.out.println("실거래가 데이터는 이미 DB에 존재합니다. (Skipped)");
            }

            System.out.println("====== (완료) DB 데이터 적재 완료 ======");
        };
    }
}