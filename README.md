#  300IQ (Cheongju Real Estate Insight)

> **2025년 청주 부동산 실거래가 데이터 기반 분석 및 AI 어드바이저 플랫폼**

---

## 프로젝트 소개 (Project Overview)
**300IQ**의 프로젝트는 청주시(상당구, 서원구, 흥덕구, 청원구)의 최근 3년간의 부동산 실거래가를 시각화하여 제공하며, 사용자가 부동산 정책 및 매물에 대해 빠른 정보를 접하고 자유롭게 소통할 수 있는 커뮤니티 플랫폼으로 설계되었으며, OpenAI의 Gpt 모델을 활용하여 부동산 관련 질의응답을 제공합니다.

###  기획 의도
- 복잡한 부동산 실거래가 데이터를 지도와 그래프로 쉽게 파악
- 청주 지역 맞춤형 부동산 정보 제공
- 초보자도 쉽게 접근할 수 있는 부동산 용어 사전 및 AI 상담 기능 구현

---

##  주요 기능 (Key Features)

### 1. 부동산 데이터 시각화 (Data Visualization)
- **지도 기반 검색:** 청주시 각 구별(상당/서원/청원/흥덕) 매물 위치 및 가격 정보 확인 (`MapController`)
- **시세 분석:** 아파트, 오피스텔, 단독다가구 등 주거 유형별 월별 평균 가격 추이 그래프 제공
- **실거래가 조회:** 2023년부터 2025년 까지의 CSV 데이터를 기반으로 한 정확한 실거래가 정보

### 2. AI 부동산 어드바이저 (AI Advisor)
- **Gemini / GPT 연동:** 사용자의 부동산 관련 질문에 대해 AI가 실시간으로 답변 (`GeminiController`, `GptService`)
- **맞춤형 추천:** 사용자의 관심 지역 및 예산에 따른 정책 추천

### 3.  커뮤니티 & 정보 공유 (Community)
- **질문/답변 (Q&A):** 사용자 간 부동산 지식 공유 (`QuestionController`, `AnswerController`)
- **자유 게시판:** 지역 주민들의 생생한 정보 교류
- **부동산 정책 아카이브:** 최신 주거 정책 PDF 뷰어 및 다운로드 제공

### 4.  부가 기능
- **부동산 용어 사전:** 어려운 부동산 신조어 및 용어 검색 기능 (`DictionaryController`)
- **캘린더:** 청약 일정 및 주요 부동산 이벤트 관리 (`CalendarController`)

---

##  기술 스택 (Tech Stack)

| 분류 | 기술 |
| :-- | :-- |
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.x |
| **Build Tool** | Gradle |
| **Frontend** | Thymeleaf, HTML/CSS/JS, Bootstrap |
| **Database** | MySQL |
| **AI Model** | Google Gemini API, OpenAI GPT |
| **Data** | Public Data Portal (CSV Parsing),    |

---

##  프로젝트 구조 (Project Structure)

```bash
300iq
├── src/main/java/com/example/iq300
│   ├── controller  # 웹 요청 처리 (Map, Board, AI, User 등)
│   ├── service     # 비즈니스 로직 (CSV 파싱, 데이터 가공)
│   ├── domain      # 엔티티 (RealEstateTransaction, Policy 등)
│   └── repository  # DB 접근
├── src/main/resources
│   ├── templates   # Thymeleaf 화면 (map, board, analysis)
│   ├── static      # CSS, JS, PDF, CSV 파일
│   └── application.properties
└── build.gradle****
