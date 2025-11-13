package com.example.iq300.service;

import com.example.iq300.model.MessageModel;
import com.example.iq300.repository.GrowthRateRepository;
import com.example.iq300.domain.GrowthRate;
import com.example.iq300.repository.TotalDataRepository;
import com.example.iq300.repository.RealEstateAgentRepository;
import com.example.iq300.repository.PopulationRepository;
import com.example.iq300.domain.TotalData;
import com.google.gson.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GptService {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model; // 예: gpt-4o-mini 또는 gpt-4o

    private static final String GPT_API_URL = "https://api.openai.com/v1/chat/completions";
    private final List<MessageModel> chatHistory = new ArrayList<>();

    private final TotalDataRepository totalDataRepository;
    private final RealEstateAgentRepository realEstateAgentRepository;
    private final PopulationRepository populationRepository;
    private final GrowthRateRepository growthRateRepository;

    private static final List<String> ADDRESS_KEYWORDS = Arrays.asList("서원구", "흥덕구", "상당구", "청원구", "청주시 전체");
    private static final List<String> RESIDENTIAL_VALUE_KEYWORDS = Arrays.asList(
            "거주", "살기 좋은", "거주 가치", "전월세", "월세", "전세", "실거주"
    );

    public GptService(TotalDataRepository totalDataRepository,
                      RealEstateAgentRepository realEstateAgentRepository,
                      PopulationRepository populationRepository,
                      GrowthRateRepository growthRateRepository) {
        this.totalDataRepository = totalDataRepository;
        this.realEstateAgentRepository = realEstateAgentRepository;
        this.populationRepository = populationRepository;
        this.growthRateRepository = growthRateRepository;
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
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", "당신은 청주시 부동산 전문 AI 분석가입니다. 데이터 기반의 정확한 설명을 제공합니다.");
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

    private Map<String, Object> parseAndAnalyzeContext(String userMessage) {
        Map<String, Object> context = new HashMap<>();

        String region = ADDRESS_KEYWORDS.stream()
                .filter(userMessage::contains)
                .findFirst()
                .orElse("청주시 전체");

        boolean isResidentialAnalysis = RESIDENTIAL_VALUE_KEYWORDS.stream()
                .anyMatch(userMessage::contains);

        if (isResidentialAnalysis) {
            context.put("analysisType", "RESIDENTIAL_VALUE");

            String period = userMessage.contains("3개월") ? "3개월" : "9개월";

            GrowthRate targetPrice = growthRateRepository.findFirstByAreaNameAndPeriodAndTxpriceType(region, period, "가격");
            GrowthRate benchPrice = growthRateRepository.findFirstByAreaNameAndPeriodAndTxpriceType("청주시 전체", period, "가격");
            GrowthRate targetVolume = growthRateRepository.findFirstByAreaNameAndPeriodAndTxpriceType(region, period, "거래량");
            GrowthRate benchVolume = growthRateRepository.findFirstByAreaNameAndPeriodAndTxpriceType("청주시 전체", period, "거래량");

            Map<String, Double> rates = new HashMap<>();
            rates.put("A_prime", targetPrice != null ? targetPrice.getGrowthRate() : 0.0);
            rates.put("B_prime", benchPrice != null ? benchPrice.getGrowthRate() : 0.0);
            rates.put("C_prime", targetVolume != null ? targetVolume.getGrowthRate() : 0.0);
            rates.put("D_prime", benchVolume != null ? benchVolume.getGrowthRate() : 0.0);

            String diagnosis = analyzeResidentialScenario(rates);

            context.put("region", region);
            context.put("period", period);
            context.put("rates", rates);
            context.put("diagnosis", diagnosis);
        } else {
            context.put("analysisType", "GENERAL_QUERY");
            String generalData = searchGeneralData(userMessage, region);
            context.put("generalData", generalData);
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
        else if (userMessage.contains("전세") || userMessage.contains("월세")) txType = "전월세";

        String transactionKeyword = null;
        if (buildingType != null && txType != null)
            transactionKeyword = buildingType + "(" + txType + ")";
        else if (buildingType != null)
            transactionKeyword = buildingType;
        else if (txType != null)
            transactionKeyword = txType;

        Pageable pageable = PageRequest.of(0, 10);
        List<TotalData> transactions = totalDataRepository.findByDynamicQuery(
                "청주시 전체".equals(region) ? null : region,
                transactionKeyword,
                pageable
        );

        if (!transactions.isEmpty())
            return "[부동산 거래내역 (최대 10건)] :\n" + formatDataForPrompt(transactions);
        else
            return "일치하는 부동산 거래내역을 찾지 못했습니다.";
    }

    private String analyzeResidentialScenario(Map<String, Double> rates) {
        double A = rates.get("A_prime");
        double B = rates.get("B_prime");
        double C = rates.get("C_prime");
        double D = rates.get("D_prime");

        boolean priceLeads = A > B;
        boolean priceLags = A < B;
        boolean volumeLeads = C > D;
        boolean volumeLags = C < D;

        if (priceLeads && volumeLeads)
            return "실거주 수요 폭발 (👍👍)";
        else if (priceLeads && volumeLags)
            return "매물 잠김형 상승 (👍)";
        else if (priceLeads)
            return "꾸준한 수요 강세 (👍)";
        else if (priceLags && volumeLeads)
            return "입주 물량 증가 (⚠️)";
        else if (priceLags && volumeLags)
            return "실거주 수요 붕괴 (👎)";
        else if (priceLags)
            return "거주 매력 정체 (😐)";
        else if (volumeLeads)
            return "안정적 인구 유입 (👍)";
        else if (volumeLags)
            return "거주 이동 정체 (😐)";
        else
            return "평균적 거주 흐름 (😐)";
    }

    private String buildFinalPrompt(String userMessage, Map<String, Object> contextData) {
        String analysisType = (String) contextData.get("analysisType");

        if ("RESIDENTIAL_VALUE".equals(analysisType)) {
            String region = (String) contextData.get("region");
            String period = (String) contextData.get("period");
            Map<String, Double> rates = (Map<String, Double>) contextData.get("rates");
            String diagnosis = (String) contextData.get("diagnosis");

            return String.format("""
                    당신은 청주시 부동산 전문 애널리스트입니다.
                    아래 데이터를 기반으로 '%s' 지역의 '%s'간 거주 가치(전월세 시장)를 분석해주세요.

                    [데이터]
                    - %s 가격 상승률(A'): %.2f%%
                    - 청주시 전체 가격 상승률(B'): %.2f%%
                    - %s 거래량 변화율(C'): %.2f%%
                    - 청주시 전체 거래량 변화율(D'): %.2f%%

                    [진단]
                    %s

                    위 내용을 토대로 부드럽고 자연스러운 분석 보고서를 작성해주세요.
                    사용자 질문: "%s"
                    """, region, period, region, rates.get("A_prime"), rates.get("B_prime"),
                    region, rates.get("C_prime"), rates.get("D_prime"), diagnosis, userMessage);
        } else {
            String generalData = (String) contextData.get("generalData");
            return """
                    당신은 청주시 부동산 데이터 전문가입니다.
                    반드시 아래 데이터만을 근거로 답변하세요.

                    --- [데이터베이스 근거] ---
                    %s
                    --- [끝] ---

                    사용자 질문: %s
                    """.formatted(generalData, userMessage);
        }
    }

    private <T> String formatDataForPrompt(List<T> dataList) {
        if (dataList == null || dataList.isEmpty())
            return "관련 데이터 없음";

        Object firstItem = dataList.get(0);
        if (firstItem instanceof TotalData) {
            return dataList.stream().map(item -> {
                TotalData td = (TotalData) item;
                return String.format("- 주소: %s, 유형: %s, 계약일: %s, 가격(만원): %s, 월세(만원): %d",
                        td.getAddress(), td.getTransactionType(), td.getContractDate(),
                        td.getPrice(), td.getRent());
            }).collect(Collectors.joining("\n"));
        }
        return dataList.stream().map(Object::toString).collect(Collectors.joining("\n"));
    }

    private String extractTextFromResponse(String jsonResponse) {
        try {
            JsonObject obj = JsonParser.parseString(jsonResponse).getAsJsonObject();
            return obj.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
        } catch (Exception e) {
            return "응답 파싱 오류: " + e.getMessage();
        }
    }
}
