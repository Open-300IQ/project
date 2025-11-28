package com.example.iq300.service;

import com.example.iq300.model.MessageModel;
import com.example.iq300.repository.FinalDataRepository;
import com.example.iq300.repository.TotalDataRepository;
import com.example.iq300.repository.RealEstateTermRepository;
import com.example.iq300.repository.HousingPolicyRepository;
import com.example.iq300.domain.FinalData;
import com.example.iq300.domain.TotalData;
import com.example.iq300.domain.IFinalDataAgg;
import com.example.iq300.domain.RealEstateTerm;
import com.example.iq300.domain.HousingPolicy;

import com.google.gson.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GptService {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    private static final String GPT_API_URL = "https://api.openai.com/v1/chat/completions";
    private final List<MessageModel> chatHistory = new ArrayList<>();

    private final TotalDataRepository totalDataRepository;
    private final FinalDataRepository finalDataRepository;
    private final HousingPolicyRepository housingPolicyRepository;
    private final RealEstateTermRepository realEstateTermRepository;
    
    private static final String DATA_START_MONTH = "202301";
    private static final String DATA_END_MONTH = "202510";

    private static final List<String> ADDRESS_KEYWORDS = Arrays.asList("서원구", "흥덕구", "상당구", "청원구", "청주시 전체");
    private static final List<String> INVESTMENT_KEYWORDS = Arrays.asList("투자", "갭투자", "전망", "거품", "버블", "급매", "고점", "하락세", "회복");
    private static final List<String> RESIDENTIAL_KEYWORDS = Arrays.asList("거주", "살기 좋은", "거주 가치", "실거주", "살기", "이사", "살고");
    private static final List<String> BUILDING_TYPE_KEYWORDS = Arrays.asList("아파트", "오피스텔", "연립다세대", "단독다가구");
    
    public GptService(TotalDataRepository totalDataRepository,
                      FinalDataRepository finalDataRepository,
                      HousingPolicyRepository housingPolicyRepository,
                      RealEstateTermRepository realEstateTermRepository) {
        this.totalDataRepository = totalDataRepository;
        this.finalDataRepository = finalDataRepository;
        this.housingPolicyRepository = housingPolicyRepository;
        this.realEstateTermRepository = realEstateTermRepository;
    }

    public List<MessageModel> getChatHistory() {
        return chatHistory;
    }

    public String sendMessage(String userMessage) {
        chatHistory.add(new MessageModel("user", userMessage));

        try {
            Map<String, Object> contextData = parseAndAnalyzeContext(userMessage);
            String finalPrompt = buildFinalPrompt(userMessage, contextData);

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", model);

            JsonArray messages = new JsonArray();
            JsonObject systemMessage = new JsonObject();

            if ("GENERAL_QUERY".equals(contextData.get("analysisType"))) {
            	systemMessage.addProperty("role", "system");
            	systemMessage.addProperty("content", "당신은 청주시 부동산 데이터 전문가입니다. 제공된 거래 내역 데이터만을 근거로 답변하세요.");
            } else {
            	systemMessage.addProperty("role", "system");
            	systemMessage.addProperty("content", "당신은 청주시 부동산 전문 AI 분석가입니다. 제공된 데이터 범위(2023.01~2025.10) 내에서만 사실에 입각해 분석하세요.");
            }
            messages.add(systemMessage);

            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");
            userMsg.addProperty("content", finalPrompt);
            messages.add(userMsg);

            requestBody.add("messages", messages);
            requestBody.addProperty("temperature", 0.3);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(GPT_API_URL, HttpMethod.POST, entity, String.class);

            String reply = extractTextFromResponse(response.getBody());
            chatHistory.add(new MessageModel("model", reply));
            return reply;

        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = "GPT 처리 중 오류 발생: " + e.getMessage();
            chatHistory.add(new MessageModel("model", errorMsg));
            return errorMsg;
        }
    }
    
    // 정책 검색 프롬프트
    public String searchPolicy(String userTopic) {
        String keyword = extractKeyword(userTopic, "policy");
        
        Pageable limit = PageRequest.of(0, 5);
        List<HousingPolicy> policies = housingPolicyRepository.findByTitleContainingOrDescriptionContaining(keyword, keyword, limit);

        if (policies.isEmpty()) {
            return "죄송합니다. '" + keyword + "'와 관련된 주거 정책을 찾을 수 없습니다. 다른 키워드로 검색해 보세요.";
        }

        StringBuilder policyContext = new StringBuilder();
        for (HousingPolicy p : policies) {
            policyContext.append(String.format("""
                [정책 ID] : %d
                [제목] : %s
                [대상 연령] : %s
                [지역] : %s
                [정책 번호] : %s
                [주관 기관] : %s
                [설명] : %s
                -----------------------------------
                """, 
                p.getId(), p.getTitle(), p.getTargetAge(), p.getRegion(), 
                p.getPolicyNo(), p.getOrganizer(), p.getDescription()));
        }

        String systemContent = """
            당신은 대한민국 주거 정책 상담 전문가입니다.
            사용자의 질문과 관련된 정책을 아래 [제공된 데이터]에서 찾아서 친절하게 안내해 주세요.
            
            [작성 원칙]
            1. [제공된 데이터]에 있는 정책만 언급하세요. 없는 내용을 지어내지 마세요.
            2. 각 정책의 제목, 주요 내용(설명 요약), 대상 연령, 주관 기관을 보기 좋게 정리하세요.
            3. **가장 중요** : 각 정책의 마지막에는 반드시 상세 페이지로 이동하는 링크를 아래 포맷 그대로 출력하세요.
               포맷: [상세보기](/policy/detail/{정책ID})
               예시: [상세보기](/policy/detail/15)
            """;

        String userPrompt = String.format("""
            사용자 검색어 : "%s"
            (검색된 키워드 : %s)
            
            [제공된 데이터] :
            %s
            
            위 데이터를 바탕으로 사용자의 검색 의도에 맞는 정책들을 소개해주세요.
            """,userTopic, keyword, policyContext.toString());

        return callGptApi(systemContent, userPrompt, "search");
    }
    
    // 단어 검색 프롬프트
    public String searchTerm(String userTopic) {
    	String keyword = extractKeyword(userTopic, "term");
    	
    	Pageable limit = PageRequest.of(0, 1);
    	List<RealEstateTerm> terms = realEstateTermRepository.findTop3ByTermContaining(keyword, limit);
    	
    	if (terms.isEmpty()) {
    		return "죄송합니다. '" + keyword + "'와 관련된 단어를 찾을 수 없습니다. 다른 키워드로 검색해 보세요.";
    	}
    	
    	StringBuilder termContext = new StringBuilder();
    	for (RealEstateTerm t : terms) {
    		termContext.append(String.format("""
    			[단어 이름] : %s
    			[단어 설명] : %s
    		""", t.getTerm(), t.getDefinition()));
    	}
    	
        String systemContent = """
                당신은 대한민국 부동산 단어 검색 엔진입니다.
                사용자의 질문과 관련된 단어를 아래 [제공된 데이터]에서 찾아서 친절하게 안내해 주세요.
                
                [작성 원칙]
                1. [제공된 데이터]에 있는 단어만 출력하세요. 없는 단어를 지어내지 마세요.
                2. 각 단어 이름과 해당 단어 이름에 대한 단어 설명을 보기 좋게 정리하세요.
                
        		""";

            String userPrompt = String.format("""
                사용자 검색어 : "%s"
                (검색된 키워드 : %s)
                
                [제공된 데이터] :
                %s
                
                위 데이터를 바탕으로 사용자의 검색 의도에 맞는 정책들을 소개해주세요.
                """,userTopic, keyword, termContext.toString());

            return callGptApi(systemContent, userPrompt, "search");
    }
        
    
    private String extractKeyword(String userQuestion, String option) {
    	if ("policy".equals(option)) {
	    	String systemContent = """
	    			당신은 검색 엔진의 키워드 추출기입니다.
	    			사용자의 질문에서 데이터베이스 검색에 사용할 핵심적인 '단어'를 추출해서 출력하세요.
	    			정책, 혜택과 같이 포괄적인 단어는 포함하지 말고 목적이 확실한 단어만 추출하세요.
	    			조사나 서술어는 모두 제거하고, 핵심적인 명사 위주로 남기세요.
	    			
	    			[예시]
	    			Q : "청년과 관련된 정책에 대해 알려줘" -> 청년
	    			Q : "청년 전세 자금 대출에 대해 알려줘" -> 청년 전세 자금 대출
	    			Q : "혹시 청소년과 관련된 정책에는 어떤게 있어?" -> 청소년
	    			Q : "신혼부부 혜택 궁금해" -> 신혼부부
	    			Q : "경기도 주거 지원" -> 경기도 주거
	    	""";
	    	
	    	String keyword = callGptApi(systemContent, userQuestion, "search").trim();
	    	
	    	return keyword.replace("\"", "").replace("'", "").trim();
    	}
    	else {
    		String systemContent = """
    				당신은 검색 엔진의 키워드 추출기입니다.
    				사용자의 질문에서 데이터베이스 검색에 사용할 핵심적인 '단어 하나'를 추출해서 출력하세요.
    				조사나 서술어는 모두 제거하고, 핵심적인 명사만 남기세요.
    				
    				[예시]
    				Q : "가건물이 무슨 뜻이야?" -> 가건물
    				Q : "가사소송법에 대해서 자세히 알려줘" -> 가사소송법
    				Q : "과세표준이 대체 뭐임?" -> 과세표준
    		""";
    		
    		String keyword = callGptApi(systemContent, userQuestion, "search").trim();
    		
    		return keyword.replace("\"", "").replace("'", "").trim();
    	}
    }
    
    // 게시글 추천 프롬프트
    public String recommendBoardQuestions(String userTopic) {
    	chatHistory.add(new MessageModel("user", "(게시글 추천 요청) " + userTopic));
    	
    	String systemContent = """
            당신은 부동산 커뮤니티의 숙련된 에디터이자 도우미입니다.
            사용자가 특정 주제(예: 부동산 정책, 세금, 특정 지역 전망 등)를 입력하면,
            커뮤니티 게시판에 올렸을 때 사람들의 활발한 토론과 답변을 이끌어낼 수 있는 '매력적인 질문 제목과 본문' 3가지를 추천해주세요.
            
            [작성 원칙]
            1. 3가지 옵션을 번호를 매겨 제안하세요.
            2. 각 옵션은 '제목'과 '본문(간략한 질문 내용)'으로 구성하세요.
            3. 질문은 구체적이고, 커뮤니티 이용자들의 경험이나 의견을 묻는 방식이 좋습니다.
            """;

        String userPrompt = String.format("사용자 입력 주제: \"%s\"\n위 주제와 관련하여 게시판에 올릴만한 좋은 질문 3가지를 추천해줘.", userTopic);

        try {
            String reply = callGptApi(systemContent, userPrompt, "recommend");
            chatHistory.add(new MessageModel("model", reply));
            return reply;
        } catch (Exception e) {
            return "추천 중 오류가 발생했습니다.";
        }
    }
    
    private String callGptApi(String systemContent, String userPrompt, String option) {
    	RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);

        JsonArray messages = new JsonArray();
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", systemContent);
        messages.add(systemMessage);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userPrompt);
        messages.add(userMsg);

        requestBody.add("messages", messages);
        if ("recommend".equals(option)) requestBody.addProperty("temperature", 0.7);
        else if ("search".equals(option)) requestBody.addProperty("temperature", 0.3);
        
        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<String> response = restTemplate.exchange(GPT_API_URL, HttpMethod.POST, entity, String.class);

        return extractTextFromResponse(response.getBody());
    }

    private Map<String, Object> parseAndAnalyzeContext(String userMessage) {
        Map<String, Object> context = new HashMap<>();

        String region = ADDRESS_KEYWORDS.stream()
                .filter(userMessage::contains)
                .findFirst()
                .orElse("청주시 전체");
        context.put("region", region);

        boolean isInvestment = INVESTMENT_KEYWORDS.stream().anyMatch(userMessage::contains);
        boolean isResidential = RESIDENTIAL_KEYWORDS.stream().anyMatch(userMessage::contains);

        String analysisType;
        if (isInvestment) {
        	analysisType = "INVESTMENT_VALUE";
        } else if (isResidential) {
        	analysisType = "RESIDENTIAL_VALUE";
        } else {
        	analysisType = "GENERAL_QUERY";
        }
        context.put("analysisType", analysisType);
        
        if ("GENERAL_QUERY".equals(analysisType)) {
        	String generalData = searchGeneralData(userMessage, region);
        	context.put("generalData", generalData);
        	return context;
        }
        
        List<String> allMonths = generateMonthList(DATA_START_MONTH, DATA_END_MONTH);
        context.put("dataStart", DATA_START_MONTH);
        context.put("dataEnd", DATA_END_MONTH);
        
        if ("RESIDENTIAL_VALUE".equals(analysisType)) {
        	Map<String, Double> rates = calculateResidentialRates(region);
        	String diagnosis = analyzeResidentialScenario(rates);
        	context.put("rates", rates);
        	context.put("diagnosis", diagnosis);
        	
        	context.put("dataJeonse", getFormattedMonthlyData(region, "전세", allMonths));
        	context.put("dataWolse", getFormattedMonthlyData(region, "월세", allMonths));
        } else if ("INVESTMENT_VALUE".equals(analysisType)) {
        	Map<String, Double> invRates = calculateInvestmentRates(region);
        	Map<String, String> invDiagnosis = analyzeInvestmentScenario(invRates);
        	
        	context.put("invRates", invRates);
        	context.put("diagnosisTitle", invDiagnosis.get("title"));
        	context.put("diagnosisEval", invDiagnosis.get("eval"));
        	context.put("diagnosisDesc", invDiagnosis.get("desc"));
        	
        	context.put("dataSale", getFormattedMonthlyData(region, "매매", allMonths));
        	context.put("dataJeonse", getFormattedMonthlyData(region, "전세", allMonths));
        }
        
        return context;
    }
    
    private String searchGeneralData(String userMessage, String region) {
    	String buildingType = null;
    	if (userMessage.contains("아파트")) buildingType = "아파트";
    	else if (userMessage.contains("오피스텔")) buildingType = "오피스텔";
    	else if (userMessage.contains("단독") || userMessage.contains("다가구")) buildingType = "단독다가구";
    	else if (userMessage.contains("연립") || userMessage.contains("다세대")) buildingType = "연립다세대";
    	
    	String txType = null;
    	if (userMessage.contains("매매")) txType = "매매";
    	else if (userMessage.contains("전세")) txType = "전세";
    	else if (userMessage.contains("월세")) txType = "월세";
    	else if (userMessage.contains("전월세")) txType = "전월세";
    	
    	String searchKeyword = userMessage;
        if (!"청주시 전체".equals(region)) searchKeyword = searchKeyword.replace(region, "");
        for (String stopWord : Arrays.asList("거래내역", "알려줘", "좀", "매물", "정보", "해줘", "어때", "보여줘", "에", "의", "아파트", "오피스텔", "매매", "전세", "월세")) {
            searchKeyword = searchKeyword.replace(stopWord, "");
        }
        searchKeyword = searchKeyword.trim().replaceAll("\\s+", " ");
        
        Pageable pageable = PageRequest.of(0, 10);
    	List<TotalData> transactions = totalDataRepository.findByDynamicQuery("청주시 전체".equals(region) ? null : region, buildingType ,searchKeyword ,pageable);
    	
    	if (buildingType != null) {
    		String targetType = buildingType;
    		transactions = transactions.stream().filter(t -> t.getTransactionType().contains(targetType)).collect(Collectors.toList());
    	}
    	
    	if (txType != null && !transactions.isEmpty()) {
    		if (txType.equals("월세")) transactions = transactions.stream().filter(t -> t.getRent() > 0).collect(Collectors.toList());
    		else if (txType.equals("전세")) transactions = transactions.stream().filter(t -> t.getRent() == 0).collect(Collectors.toList());
    	}
    	
    	if (!transactions.isEmpty()) return "[부동산 거래내역 (최대 10건)] :\n" + formatDataForPrompt(transactions);
    	else return "일치하는 " + (txType != null ? txType : "") + " " + (buildingType != null ? buildingType : "") + " 거래내역을 찾지 못했습니다. (검색어: " + searchKeyword + ")";
    }
    
    private <T> String formatDataForPrompt(List<T> dataList) {
    	if (dataList == null || dataList.isEmpty()) return "관련 데이터 없음";
    	
    	Object firstItem = dataList.get(0);
    	if (firstItem instanceof TotalData) {
    		return dataList.stream().map(item -> {
    			TotalData td = (TotalData) item;
    			return String.format("- 주소: %s, 건물명: %s, 유형: %s, 계약일: %s, 가격(만원): %s, 월세(만원): %d", td.getAddress(), td.getBuildingName(), td.getTransactionType(), td.getContractDate(), td.getPrice(), td.getRent());
    		}).collect(Collectors.joining("\n"));
    	}
    	return dataList.stream().map(Object::toString).collect(Collectors.joining("\n"));
    }
    
    private Map<String, Double> calculateResidentialRates(String region){
    	Map<String, Double> rates = new HashMap<>();
    	
    	List<String> startMonths = Arrays.asList("202301", "202302", "202303");
    	List<String> endMonths = Arrays.asList("202508", "202509", "202510");
    	
    	double priceStart = getAvgPrice(region, "전세", startMonths);
    	double priceEnd = getAvgPrice(region, "전세", endMonths);
    	rates.put("A_prime", calculateGrowthRate(priceEnd, priceStart));
    	
    	double cityPriceStart = getAvgPrice("청주시 전체", "전세", startMonths);
    	double cityPriceEnd = getAvgPrice("청주시 전체", "전세", endMonths);
    	rates.put("B_prime", calculateGrowthRate(cityPriceEnd, cityPriceStart));
    	
    	double volStart = getAvgVolume(region, "전세", startMonths);
    	double volEnd = getAvgVolume(region, "전세", endMonths);
    	rates.put("C_prime", calculateGrowthRate(volEnd, volStart));
    	
    	double cityVolStart = getAvgVolume("청주시 전체", "전세", startMonths);
    	double cityVolEnd = getAvgVolume("청주시 전체", "전세", endMonths);
    	rates.put("D_prime", calculateGrowthRate(cityVolEnd, cityVolStart));
    	
    	return rates;
    }
    
    private Map<String, Double> calculateInvestmentRates(String region) {
    	Map<String, Double> rates = new HashMap<>();
    	
    	List<String> startMonths = Arrays.asList("202301", "202302", "202303");
    	List<String> endMonths = Arrays.asList("202508", "202509", "202510");
    	
    	double saleStart = getAvgPrice(region, "매매", startMonths);
    	double saleEnd = getAvgPrice(region, "매매", endMonths);
    	rates.put("A", calculateGrowthRate(saleEnd, saleStart));
    	
    	double jeonseStart = getAvgPrice(region, "전세", startMonths);
    	double jeonseEnd = getAvgPrice(region, "전세", endMonths);
    	rates.put("A_prime", calculateGrowthRate(jeonseEnd, jeonseStart));
    	
    	double currentSale = getSingleMonthPrice(region, "매매", DATA_END_MONTH);
    	double currentJeonse = getSingleMonthPrice(region, "전세", DATA_END_MONTH);
    	double ratioT = (currentSale > 0) ? (currentJeonse / currentSale * 100) : 0.0;
    	rates.put("Ratio_T", ratioT);
    	
    	double citySale = getSingleMonthPrice("청주시 전체", "매매", DATA_END_MONTH);
    	double cityJeonse = getSingleMonthPrice("청주시 전체", "전세", DATA_END_MONTH);
    	double ratioB = (citySale > 0) ? (cityJeonse / citySale * 100) : 0.0;
    	rates.put("Ratio_B", ratioB);
    	
    	return rates;
    }
    
    private double getAvgPrice(String region, String type, List<String> months) {
    	List<IFinalDataAgg> data;
    	if ("청주시 전체".equals(region)) {
    		data = finalDataRepository.findAggregatedTotalCity(type, months);
    	} else {
    		data = finalDataRepository.findAggregatedBy(region, type, months);
    	}
    	
    	if (data.isEmpty()) return 0.0;
    	return data.stream().mapToDouble(d -> d.getWeightedAvgPrice() != null ? d.getWeightedAvgPrice() : 0.0).average().orElse(0.0);
    }
    
    private double getAvgVolume(String region, String type, List<String> months) {
    	Long totalVol;
    	if ("청주시 전체".equals(region)) totalVol = finalDataRepository.sumCountTotalCity(type, months);
    	else totalVol = finalDataRepository.sumCountByArea(region, type, months);
    	
    	if (totalVol == null) return 0.0;
    	return (double) totalVol / months.size();
    }
    
    private double getSingleMonthPrice(String region, String type, String month) {
    	IFinalDataAgg data;
    	if ("청주시 전체".equals(region)) data = finalDataRepository.findStatTotalCityByMonth(type, month);
    	else data = finalDataRepository.findStatByAreaAndMonth(region, type, month);
    	return (data != null && data.getWeightedAvgPrice() != null) ? data.getWeightedAvgPrice() : 0.0;
    }
    
    private double calculateGrowthRate(double current, double past) {
    	if (past == 0) return 0.0;
    	return ((current - past) / past) * 100;
    }
    
    private String analyzeResidentialScenario(Map<String, Double> rates) {
    	double A = rates.get("A_prime");
    	double B = rates.get("B_prime");
    	double C = rates.get("C_prime");
    	double D = rates.get("D_prime");
    	
    	boolean priceLeads = A > B;
    	boolean volumeLeads = C > D;
    	
    	if (priceLeads && volumeLeads) return "실거주 수요 폭발 - 가격과 거래량이 모두 시장 평균을 상회";
    	else if (priceLeads && !volumeLeads) return "매물 잠김형 상승 - 거래는 적으나 가격은 강세";
    	else if (!priceLeads && volumeLeads) return "저가 매수 유입 - 가격은 약세나 거래량이 활발함";
    	else return "거주 선호도 정체 - 시장 평균 대비 약세";
    }
    
    private Map<String, String> analyzeInvestmentScenario(Map<String, Double> rates) {
    	double A = rates.get("A");
    	double A_prime = rates.get("A_prime");
    	double Ratio_T = rates.get("Ratio_T");
    	double Ratio_B = rates.get("Ratio_B");
    	
    	Map<String, String> result = new HashMap<>();
    	
    	if (A_prime > A) {
    		if (Ratio_T > Ratio_B) {
    			result.put("title", "1. 강력한 실수요장");
    			result.put("eval", "매우 좋음");
    			result.put("desc", "실수요가 매우 탄탄하고(고전세가율), 갭이 좁혀지고 있습니다. 매매가 상승 압력이 가장 높습니다.");
    		} else {
    			result.put("title", "2. 갭 메우기 회복");
    			result.put("eval", "좋음");
    			result.put("desc", "과거 고평가 상태였으나, 전세가가 오르며 격차를 좁히고 있습니다. 시장 건강도가 회복 중입니다.");
    		} 
    	} else {
    		if (Ratio_T > Ratio_B) {
    			result.put("title", "3. 과열 진입 초기");
    			result.put("eval", "중립");
    			result.put("desc", "실수요는 탄탄하지만 매매가(투자)가 더 빠르게 오르고 있으며, 갭이 벌어지고 있습니다.");
    		} else {
    			result.put("title", "4. 고평가 심화");
    			result.put("eval", "매우 나쁨");
    			result.put("desc", "이미 갭이 큰 상태에서, 갭이 더욱 벌어지고 있습니다. 하락 위험이 가장 큰 상태입니다.");
    		}
    	}
    	return result;
    }
    
    private String buildFinalPrompt(String userMessage, Map<String, Object> contextData) {
    	String analysisType = (String) contextData.get("analysisType");
    	String region = (String) contextData.get("region");
    	
    	if ("GENERAL_QUERY".equals(analysisType)) {
    		String generalData = (String) contextData.get("generalData");
    		return String.format("""
                    당신은 청주시 부동산 데이터 전문가입니다.
                    반드시 아래 데이터만을 근거로 답변하세요. 외부 지식이나 존재하지 않는 매물 정보를 꾸며내지 마세요.

                    --- [데이터베이스 검색 결과] ---
                    %s
                    --- [끝] ---

                    사용자 질문: %s
                    답변 시 거래일자, 가격, 층수 등 구체적인 정보를 포함하여 친절하게 안내해주세요.
                    """, generalData, userMessage);
    	}
    	
    	String dataStart = (String) contextData.get("dataStart");
    	String dataEnd = (String) contextData.get("dataEnd");
    	
    	if ("RESIDENTIAL_VALUE".equals(analysisType)) {
    		Map<String, Double> rates = (Map<String, Double>) contextData.get("rates");
    		String diagnosis = (String) contextData.get("diagnosis");
    		
    		return String.format("""
                    당신은 청주시 부동산 분석가입니다. 아래 데이터를 바탕으로 '%s' 지역의 **거주 가치(실거주 관점)**를 분석해주세요.
                    
                    [주의사항]
                    1. 데이터 기간은 **%s ~ %s** 입니다. 
                    2. 거주 가치 분석이므로 **매매 가격은 언급하지 말고**, 전세와 월세 시장 위주로 분석하세요.

                    --- [1차 진단] ---
                    - %s 전세가 변동률: %.2f%% (청주 평균 %.2f%%)
                    - %s 전세 거래량 변동률: %.2f%% (청주 평균 %.2f%%)
                    - AI 결론: "%s"

                    --- [월별 상세 데이터] ---
                    [전세] %s
                    [월세] %s
                    
                    [작성 가이드]
                    1차 진단을 바탕으로 거주 선호도를 평가하고, 월별 데이터 패턴을 분석하여 실거주자에게 조언을 제공하세요.
                    """,
                    region, formatMonth(dataStart), formatMonth(dataEnd),
                    region, rates.get("A_prime"), rates.get("B_prime"),
                    region, rates.get("C_prime"), rates.get("D_prime"),
                    diagnosis,
                    contextData.get("dataJeonse"),
                    contextData.get("dataWolse")
            );
    	} else if ("INVESTMENT_VALUE".equals(analysisType)) {
    		Map<String, Double> rates = (Map<String, Double>) contextData.get("invRates");
    		
    		return String.format("""
                    당신은 부동산 투자 컨설턴트입니다. '%s' 지역의 **투자 가치**를 분석해주세요.

                    [주의사항]
                    1. 데이터 기간: **%s ~ %s**
                    2. 매매와 전세의 갭(Gap)과 추세를 중점적으로 분석하세요.

                    --- [투자 진단] ---
                    1. 추세: 매매상승률(%.2f%%) vs 전세상승률(%.2f%%) -> %s
                    2. 갭: 전세가율(%.1f%%) vs 청주평균(%.1f%%) -> %s
                    
                    [최종 평가]
                    - 진단명: %s (%s)
                    - 설명: %s

                    --- [상세 데이터] ---
                    [매매] %s
                    [전세] %s
                    
                    [작성 가이드]
                    위 평가를 근거로 투자 적기인지, 리스크는 무엇인지 설명하세요.
                    """,
                    region, formatMonth(dataStart), formatMonth(dataEnd),
                    rates.get("A"), rates.get("A_prime"),
                    (rates.get("A") > rates.get("A_prime") ? "투자수요 우세" : "실수요 우세"),
                    rates.get("Ratio_T"), rates.get("Ratio_B"),
                    (rates.get("Ratio_T") > rates.get("Ratio_B") ? "갭 작음(전세가율 높음)" : "갭 큼(전세가율 낮음)"),
                    contextData.get("diagnosisTitle"), contextData.get("diagnosisEval"), contextData.get("diagnosisDesc"),
                    contextData.get("dataSale"), contextData.get("dataJeonse")
            );
    	}
    	
    	return "분석할 수 없습니다";
    }
    
    private String getFormattedMonthlyData(String region, String type, List<String> months) {
    	List<IFinalDataAgg> list;
    	
    	if ("청주시 전체".equals(region)) {
    		list = finalDataRepository.findAggregatedTotalCity(type, months);
    	} else {
    		list = finalDataRepository.findAggregatedBy(region, type, months);
    	}
    	if (list.isEmpty()) return "데이터 없음";
    	
    	return list.stream().map(d -> String.format("%s: %.0f만원(%d건)", d.getContractMonth(), d.getWeightedAvgPrice() != null ? d.getWeightedAvgPrice() : 0, d.getTotalCount())).collect(Collectors.joining(", "));
    }
    
    private List<String> generateMonthList(String startYYYYMM, String endYYYYMM) {
    	List<String> months = new ArrayList<>();
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
    	YearMonth start = YearMonth.parse(startYYYYMM, formatter);
    	YearMonth end = YearMonth.parse(endYYYYMM, formatter);
    	
    	YearMonth current = start;
    	while (!current.isAfter(end)) {
    		months.add(current.format(formatter));
    		current = current.plusMonths(1);
    	}
    	return months;
    }
    
    private String formatMonth(String yyyyMM) {
    	return yyyyMM.substring(0, 4) + "년 " + yyyyMM.substring(4) + "월";
    }
    
    private String extractTextFromResponse(String jsonResponse) {
    	try {
    		JsonObject obj = JsonParser.parseString(jsonResponse).getAsJsonObject();
    		return obj.getAsJsonArray("choices").get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString();
    	} catch (Exception e) {
    		return "응답 파싱 오류";
    	}
    }
}