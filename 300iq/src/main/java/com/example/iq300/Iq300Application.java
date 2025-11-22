package com.example.iq300;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
        		
            try {
                if (userService.findUser("admin").isEmpty()) {
                    userService.create("admin", "admin@test.com", "1234");
                    System.out.println("사용자(admin) 생성 완료.");
                }
                
                if (userService.findUser("park").isEmpty()) {
                    userService.create("park", "user11@test.com", "1234");
                    System.out.println("사용자(user1) 생성 완료.");
                }
                if (userService.findUser("jun").isEmpty()) {
                    userService.create("jun", "user22@test.com", "1234");
                    System.out.println("사용자(user2) 생성 완료.");
                }
                if (userService.findUser("hichan").isEmpty()) {
                    userService.create("hichan", "user32@test.com", "1234");
                    System.out.println("사용자(user2) 생성 완료.");
                }
                if (userService.findUser("junghwan").isEmpty()) {
                    userService.create("junghwan", "user43@test.com", "1234");
                    System.out.println("사용자(user2) 생성 완료.");
                }
                if (userService.findUser("sungwong").isEmpty()) {
                    userService.create("sungwong", "user53@test.com", "1234");
                    System.out.println("사용자(user2) 생성 완료.");
                }
                
                if (userService.findUser("lim").isEmpty()) {
                    userService.create("lim", "user36@test.com", "1234");
                    System.out.println("사용자(user2) 생성 완료.");
                }
                if (userService.findUser("hong").isEmpty()) {
                    userService.create("hong", "user73@test.com", "1234");
                    System.out.println("사용자(user2) 생성 완료.");
                }
                if (userService.findUser("han").isEmpty()) {
                    userService.create("han", "user83@test.com", "1234");
                    System.out.println("사용자(user2) 생성 완료.");
                }
//                // 3. Q&A 및 자유게시판 테스트 게시글 생성 (findUser 사용)
//                if (userService.findUser("admin").isPresent()) {
//                    User admin = userService.findUser("admin").get();
//                    questionService.create("Q&A 테스트 제목 1", "Q&A 테스트 내용 1입니다.", admin);
//                }
//                if (userService.findUser("user1").isPresent()) {
//                    User user1 = userService.findUser("user1").get();
//                    boardService.createPost("자유게시판 테스트 1", "자유게시판 내용 1입니다.", user1);
//                }
                
                

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

//            csvDataService.loadTransactions();
//            monthlyAvgPriceService.aggregateAndSaveData(); 
//            csvDataService.loadAgents();
//            csvDataService.loadPopulation();
//            csvDataService.loadTotal();
//            csvDataService.buildAndSaveFinalData();
//            
//            
//            // (중요!) 부동산 용어사전 CSV 로드 실행
//
 //           csvDataService.loadRealEstateTerms(); 
 //           csvDataService.loadMapTransactions();
//            csvDataService.loadHousingPolicies();
            csvDataService.loadBoardPosts();
            csvDataService.loadQuestions();
            csvDataService.loadAnswers();
            csvDataService.loadComments();
            System.out.println("====== [CsvDataService] 모든 데이터 로드 완료 ======");
        };
    }
    
}