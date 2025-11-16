package com.example.iq300;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.iq300.domain.User;
import com.example.iq300.service.BoardService; // 1. BoardService 임포트
import com.example.iq300.service.CsvDataService;
import com.example.iq300.service.MonthlyAvgPriceService;
import com.example.iq300.service.QuestionService; // 2. QuestionService 임포트
import com.example.iq300.service.UserService;

import lombok.RequiredArgsConstructor;



@SpringBootApplication
@RequiredArgsConstructor
public class Iq300Application {

    private final CsvDataService csvDataService;
    private final UserService userService;
    private final BoardService boardService; // 3. BoardService 주입
    private final QuestionService questionService; // 4. QuestionService 주입
    private final MonthlyAvgPriceService monthlyAvgPriceService;
    
    public static void main(String[] args) {
        SpringApplication.run(Iq300Application.class, args);
    }

    @Bean
    public CommandLineRunner initData() {
        return (args) -> {
            

            // 2. 관리자(admin) 및 일반 사용자(user1) 생성
            try {
                if (userService.findUser("admin").isEmpty()) {
                    userService.create("admin", "admin@test.com", "1234");
                    System.out.println("사용자(admin) 생성 완료.");
                }
                
                if (userService.findUser("user1").isEmpty()) {
                    userService.create("user1", "user1@test.com", "1234");
                    System.out.println("사용자(user1) 생성 완료.");
                }
                
                // 3. Q&A 및 자유게시판 테스트 게시글 생성 (findUser 사용)
                if (userService.findUser("admin").isPresent()) {
                    User admin = userService.findUser("admin").get();
                    questionService.create("Q&A 테스트 제목 1", "Q&A 테스트 내용 1입니다.", admin);
                }
                if (userService.findUser("user1").isPresent()) {
                    User user1 = userService.findUser("user1").get();
                    boardService.createPost("자유게시판 테스트 1", "자유게시판 내용 1입니다.", user1);
                }

            } catch (Exception e) {
                System.out.println("사용자 또는 게시글 생성 중 오류: " + e.getMessage());
            }

            System.out.println("====== (완료) DB 데이터 적재 완료 ======");
        };

    }
    @Bean
    public CommandLineRunner initCsvData(CsvDataService csvDataService) {
        return args -> {
            System.out.println("====== [CsvDataService] 데이터 로드 시작 ======");

            csvDataService.loadTransactions();
            monthlyAvgPriceService.aggregateAndSaveData(); 
            csvDataService.loadAgents();
            csvDataService.loadPopulation();
            csvDataService.loadTotal();
            csvDataService.calculateAndSaveMonthlyVolumes();
            csvDataService.calculateAndSaveALLGrowthRates();
            
            
            // (중요!) 부동산 용어사전 CSV 로드 실행

            csvDataService.loadRealEstateTerms(); 


            
            System.out.println("====== [CsvDataService] 모든 데이터 로드 완료 ======");
        };
    }
}