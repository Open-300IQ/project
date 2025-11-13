package com.example.iq300.service;

import com.example.iq300.model.MessageModel;
import com.google.gson.*;

import com.example.iq300.repository.TotalDataRepository;
import com.example.iq300.repository.RealEstateAgentRepository;
import com.example.iq300.repository.PopulationRepository;
import com.example.iq300.domain.TotalData;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

@Service
public class GeminiService {

	@Value("${gemini.api.key}")
	private String api_key;
	
	private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
	private final List<MessageModel> chatHistory = new ArrayList<>();
	
	private final TotalDataRepository totalDataRepository;
	private final RealEstateAgentRepository realEstateAgentRepository;
	private final PopulationRepository populationRepository;

	
	public GeminiService(TotalDataRepository totalDataRepository,
			RealEstateAgentRepository realEstateAgentRepository,
			PopulationRepository populationRepository) {
		this.totalDataRepository = totalDataRepository;
		this.realEstateAgentRepository = realEstateAgentRepository;
		this.populationRepository = populationRepository;
	}
	
	public List<MessageModel> getChatHistory() {
		return chatHistory;
	}
	
	public String sendMessage(String userMessage) {
		chatHistory.add(new MessageModel("user", userMessage));
	
		try {
			
			String contextData = searchDatabaseForContext(userMessage);
			String finalPrompt = buildFinalPrompt(userMessage, contextData);
			
			RestTemplate restTemplate = new RestTemplate();
			
			JsonObject textPart = new JsonObject();
			textPart.addProperty("text", finalPrompt);
			
			JsonArray parts = new JsonArray();
			parts.add(textPart);
			
			JsonObject content = new JsonObject();
			content.addProperty("role", "user");
			content.add("parts", parts);
			
			JsonArray contents = new JsonArray();
			contents.add(content);
			
			JsonObject requestBody = new JsonObject();
			requestBody.add("contents", contents);
			
			HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);
	        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
	
	        String requestUrl = GEMINI_API_URL + "?key=" + api_key;
	        ResponseEntity<String> response =
	                restTemplate.postForEntity(requestUrl, entity, String.class);
	        
	        String reply = extractTextFromResponse(response.getBody());
	        chatHistory.add(new MessageModel("model", reply));
	        return reply;

		} catch (Exception e) {
			String errorMsg = "오류 발생 : " + e.getMessage();
			chatHistory.add(new MessageModel("model", errorMsg));
			return errorMsg;
		}		
	}
	
	private Map<String, String> parseUserIntent(String userMessage) {
		Map<String, String> criteria = new HashMap<>();
		
		final List<String> ADDRESS_KEYWORDS = Arrays.asList(
				"서원구", "흥덕구", "상당구", "청원구");
		
		for (String addr : ADDRESS_KEYWORDS) {
			if(userMessage.contains(addr)) {
				criteria.put("address", addr);
				break;
			}
		}
		
		String buildingType = null;
		if(userMessage.contains("아파트")) buildingType = "아파트";
		else if (userMessage.contains("오피스텔")) buildingType = "오피스텔";
        else if (userMessage.contains("단독") || userMessage.contains("다가구")) buildingType = "단독다가구";
        else if (userMessage.contains("연립") || userMessage.contains("다세대")) buildingType = "연립다세대";
		
		String txType = null;
		if (userMessage.contains("매매")) txType = "매매";
        else if (userMessage.contains("전세") || userMessage.contains("월세")) txType = "전월세";
		
		String transactionKeyword = null;
		if(buildingType != null && txType != null) {
			transactionKeyword = buildingType + "(" + txType + ")";
		}
		else if (buildingType != null) {
			transactionKeyword = buildingType;
		}
		else if (txType != null) {
			transactionKeyword = txType;
		}
		
		if (transactionKeyword != null) {
			criteria.put("txType", transactionKeyword);
		}
		
		return criteria;
	}
	
	private String searchDatabaseForContext(String userMessage) {
		StringBuilder context = new StringBuilder();
		Map<String, String> criteria = parseUserIntent(userMessage);
		
		String addressKeyword = criteria.get("address");
		String txTypeKeyword = criteria.get("txType");
		
		if (addressKeyword != null || txTypeKeyword != null) {
			Pageable pageable = PageRequest.of(0, 10);
			
			List<TotalData> transactions = totalDataRepository.findByDynamicQuery(addressKeyword, txTypeKeyword, pageable);
		
			if (!transactions.isEmpty()) {
				context.append("[부동산 거래내역 (최대 10건)] :\n");
				context.append(formatDataForPrompt(transactions));
				context.append("\n");
			}
			else {
				if(addressKeyword != null || txTypeKeyword != null) {
					context.append("[부동산 거래내역] : \n 일치하는 데이터를 찾지 못했습니다.");
				}
			}
		}
		
		return context.toString();
	}
	
	private String buildFinalPrompt(String userMessage, String contextData) {
		return "당신은 청주시 부동산 데이터를 기반으로 답변하는 전문 AI 챗봇입니다.\n" +
               "반드시 아래 제공되는 [데이터베이스 근거]만을 바탕으로 사용자의 질문에 대답해야 합니다.\n" +
               "근거 자료에 답변에 필요한 정보가 없다면, '데이터베이스에 관련 정보가 없습니다.'라고 정확하게 대답하세요.\n" +
               "절대로 [데이터베이스 근거]에 없는 내용을 지어내서 대답하면 안 됩니다.\n\n" +
               "--- [데이터베이스 근거] ---\n" +
               (contextData.isEmpty() ? "검색된 데이터 없음." : contextData) + "\n" +
               "--- [데이터베이스 근거 끝] ---\n\n" +
               "[사용자 질문]\n" +
               userMessage;
	}
	
	private <T> String formatDataForPrompt(List<T> dataList) {
		if (dataList == null || dataList.isEmpty()) {
			return "관련 데이터 없음";
		}
		
		Object firstItem = dataList.get(0);
		
		if (firstItem instanceof TotalData) {
			return dataList.stream().map(item -> {TotalData td = (TotalData) item; 
			return String.format("- 주소: %s, 유형: %s, 계약일: %s, 가격(만원): %s, 월세(만원): %d",
					td.getAddress(), td.getTransactionType(), td.getContractDate(), td.getPrice(), td.getRent());
			}).collect(Collectors.joining("\n"));
		}
		return dataList.stream().map(Object::toString).collect(Collectors.joining("\n"));
	}
	
	private String extractTextFromResponse(String jsonResponse) {
		try {
			JsonObject obj = JsonParser.parseString(jsonResponse).getAsJsonObject();
            return obj.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
		} catch (Exception e) {
			return "응답 파싱 오류 : " + e.getMessage();
		}
	}
}
