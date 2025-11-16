package com.example.iq300.controller;
import com.example.iq300.domain.RealEstateTransaction;
import com.example.iq300.repository.TransactionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

@Controller
@RequestMapping("/analysis")
public class MonthlyAvgPriceController {
	
	private final TransactionRepository transactionRepository;

    public MonthlyAvgPriceController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }
    
    @GetMapping("/getDetailTransactions")
    @ResponseBody
    public List<RealEstateTransaction> getDetailTransactions(
	    		@RequestParam("sigungu") String sigungu, 
	        @RequestParam("buildingType") String buildingType,
	        @RequestParam("contractMonth") String contractMonth,
	        @RequestParam("transactionType") String transactionType){
	    	List<RealEstateTransaction> transactions = transactionRepository.findMatchingTransactions(
	    			sigungu, 
                buildingType, 
                contractMonth,
                transactionType);
	    	return transactions;
    }
	
}