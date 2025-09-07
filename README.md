# 📊 Stock Predictor — 주식 데이터 분석 & 예측 서비스

[![Java](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql)](https://www.mysql.com/)
[![Python](https://img.shields.io/badge/Python-3.13-yellow?logo=python)](https://www.python.org/)

**Stock Predictor**는 주식 초보자도 쉽고 직관적으로 과거 주식 데이터를 분석하고, 간단한 예측 정보를 확인할 수 있는 웹 애플리케이션입니다.  
Python 라이브러리를 통해 과거 주식 데이터를 자동 수집·가공하고, 시각화를 통해 보기 쉽게 제공합니다.  
또한 **관심 있는 종목을 즐겨찾기**하여 빠르게 확인하고, **각 주식별로 메모**를 남겨 개인적인 분석 및 기록이 가능합니다.

---

## 🎯 주요 기능
- 📈 **과거 데이터 기반 분석**
- 🤖 **자동 데이터 수집**: `pykrx`를 활용한 주식 데이터 수집
- 📊 **시각적 정보 제공**: `Chart.js`를 활용한 차트와 그래프
- 🔮 **간단한 예측 모델**: `RandomForestClassifier`, `XGBClassifier`, `LGBMClassifier`를 사용하여 `scikit-learn` 기반 머신러닝으로 주가 상승/하락 가능성을 예측
- ⭐ **즐겨찾기 기능**: 관심 종목을 저장해 한 번의 클릭으로 빠르게 조회
- 📝 **주식별 메모 기능**: 각 종목에 개인 메모와 분석 기록 작성/수정 가능
---

## 🛠 기술 스택
| 구분 | 기술 |
|------|------|
| **Frontend** | HTML, CSS, JavaScript, Bootstrap |
| **Backend** | Java 21, Spring Boot 3.5.4 |
| **Database** | MySQL 8.0.25 |
| **Data Processing & AI Model** | Python (`Pandas`, `scikit-learn`, `pykrx`) |
| **ORM** | Spring Data JPA (Hibernate) |
| **Visualization**  | Chart.js |
