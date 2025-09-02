from pykrx import stock
import pandas as pd
from datetime import datetime, timedelta
from sqlalchemy import create_engine
from dotenv import load_dotenv
import os
import math
import time

# -------------------------
# 환경변수 로드
# -------------------------
load_dotenv()

DB_USER = os.getenv("DB_USER")
DB_PASSWORD = os.getenv("DB_PASSWORD")
DB_HOST = os.getenv("DB_HOST")
DB_PORT = os.getenv("DB_PORT")
DB_NAME = os.getenv("DB_NAME")

db_url = f'mysql+pymysql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}'
engine = create_engine(db_url, echo=False)

# -------------------------
# 사용자 입력
# -------------------------
start_date = input("시작 날짜를 입력하세요 (YYYY-MM-DD): ")
end_date = input("종료 날짜를 입력하세요 (YYYY-MM-DD): ")
file_prefix = input("파일명 접두사 선택 (new / all): ")

# -------------------------
# 안전한 합계 함수
# -------------------------
def safe_sum(df, col):
    return df[col].sum() if col in df.columns else 0

# -------------------------
# 투자자별 집계
# -------------------------
def aggregate_investor_flow(df):
    institution_types = ["금융투자", "보험", "투신", "사모", "은행", "기타금융", "연기금 등"]
    foreign_types = ["외국인", "기타외국인"]

    inst_df = df[df["투자자구분"].isin(institution_types)]
    foreign_df = df[df["투자자구분"].isin(foreign_types)]
    individual_df = df[df["투자자구분"]=="개인"]

    return {
        "institutionBuy": safe_sum(inst_df, "매수"),
        "institutionSell": safe_sum(inst_df, "매도"),
        "institutionNetBuy": safe_sum(inst_df, "순매수"),
        "foreignBuy": safe_sum(foreign_df, "매수"),
        "foreignSell": safe_sum(foreign_df, "매도"),
        "foreignNetBuy": safe_sum(foreign_df, "순매수"),
        "individualBuy": safe_sum(individual_df, "매수"),
        "individualSell": safe_sum(individual_df, "매도"),
        "individualNetBuy": safe_sum(individual_df, "순매수")
    }

# -------------------------
# 일별 기관/외국인/개인 거래 데이터 수집
# -------------------------
def get_korea_institutional_flow_daily(start_date, end_date, batch_size=50, sleep_sec=1):
    tickers_df = pd.read_sql("SELECT ticker FROM stocks", engine)
    tickers = tickers_df['ticker'].tolist()
    
    all_data = []
    current_date = pd.to_datetime(start_date)
    end_date_dt = pd.to_datetime(end_date)

    while current_date <= end_date_dt:
        date_str = current_date.strftime("%Y-%m-%d")
        print(f"📅 날짜: {date_str}")

        total_batches = math.ceil(len(tickers) / batch_size)

        for i in range(total_batches):
            batch = tickers[i * batch_size : (i+1) * batch_size]
            batch_data = []

            for ticker in batch:
                try:
                    df = stock.get_market_trading_volume_by_investor(date_str, date_str, ticker)
                except KeyError as e:
                    print(f"⚠️ {ticker} 조회 중 KeyError: {e}")
                    continue
                except Exception as e:
                    print(f"⚠️ {ticker} 조회 중 기타 오류: {e}")
                    continue

                if df.empty:
                    continue

                df = df.reset_index()
                # 누락 컬럼 자동 채움
                required_cols = ['매수', '매도', '순매수', '거래대금']
                for col in required_cols:
                    if col not in df.columns:
                        df[col] = 0

                aggregated = aggregate_investor_flow(df)
                aggregated['ticker'] = ticker
                aggregated['date'] = current_date.date()
                batch_data.append(aggregated)

            if batch_data:
                all_data.extend(batch_data)

            print(f"✅ Batch {i+1}/{total_batches} 완료")
            time.sleep(sleep_sec)

        current_date += timedelta(days=1)

    return pd.DataFrame(all_data)

# -------------------------
# 데이터 수집 실행
# -------------------------
df_all = get_korea_institutional_flow_daily(start_date, end_date)

# -------------------------
# CSV 저장
# -------------------------
os.makedirs("stock_price_data", exist_ok=True)
file_name = f"stock_price_data/{file_prefix}_institutional_flow_{datetime.now().strftime('%Y_%m_%d')}.csv"
df_all.to_csv(file_name, index=False, encoding="utf-8-sig")

print(f"✅ CSV 파일이 저장되었습니다: {file_name}")