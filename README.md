# 📊 Stock Insight — 주식 데이터 분석 & 예측 서비스

## 📌 프로젝트 개요
Stock Insight는 주식 초보자도 쉽게 투자 데이터를 분석하고, 간단한 예측 정보를 확인할 수 있도록 제작된 웹 애플리케이션입니다.  
이 서비스는 여러 출처에서 주식 데이터를 자동으로 수집·가공하고, 시각화를 통해 직관적으로 표시합니다.  
또한 Python 기반의 예측 모델을 통해 향후 주가 변동 가능성을 제시하여 투자 판단에 도움을 줍니다.

---

## 🎯 목표 & 특징
- **데이터 자동 수집**: 다양한 주식 데이터 API를 통해 실시간 정보 수집
- **데이터 시각화**: 차트와 그래프를 통한 직관적 정보 전달
- **간단한 예측 기능**: 초보자도 이해하기 쉬운 주가 예측 제공
- **사용자 친화적 UI**: 불필요한 정보 제거, 간단하고 명확한 화면 구성

---

## 🧑‍💻 개발 강점
- 백엔드 아키텍처 설계
- 데이터 수집·가공·시각화
- 실시간 데이터 처리
- 자동화 기능 구현

---

## 👥 타깃 사용자
- **주식 투자 초보자**
  - 복잡한 차트나 지표 대신, 직관적인 시각화와 간단한 분석을 원하는 사람
  - 투자 공부를 시작하며 데이터 분석 경험을 쌓고 싶은 사람
- **투자 정보를 한 번에 확인하고 싶은 사람**
  - 여러 사이트를 오가며 정보를 찾는 것이 번거로운 사용자
- **간단한 예측치를 통해 참고 자료를 얻고 싶은 사람**

---

## 🛠 기술 스택
- **Frontend:** HTML, CSS, JavaScript, Bootstrap
- **Backend:** Java 21, Spring Boot 3.5.4
- **Database:** MySQL 8.0.25
- **Data Processing & AI Model:** Python (Pandas, scikit-learn)
- **API:** yfinance, pykrx
- **ORM:** Spring Data JPA (Hibernate)
- **Build Tool:** Gradle
- **Version Control:** Git, GitHub
- **Visualization:** Chart.js

## 📐 서비스 아키텍처
아래 다이어그램은 Stock Insight 서비스의 주요 구성 요소와 데이터 흐름을 나타냅니다.
```mermaid
flowchart TD
    User[사용자 브라우저] --> Frontend[Frontend<br>HTML/CSS/JS/Bootstrap<br>Chart.js]
    Frontend --> Backend[Backend<br>Spring Boot<br>Controller/Service/Repository]
    Backend --> DB[(MySQL)]
    Backend --> StockAPI[주식 데이터 API<br>(yfinance, pykrx)]
    Backend --> PythonModel[Python 예측 모델 서버<br>Pandas, scikit-learn<br>FastAPI]
