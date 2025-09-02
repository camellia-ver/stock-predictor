from pykrx import stock
import pandas as pd
from datetime import datetime
from sqlalchemy import create_engine
from dotenv import load_dotenv
import os
import math
import time

load_dotenv()

DB_USER = os.getenv("DB_USER")
DB_PASSWORD = os.getenv("DB_PASSWORD")
DB_HOST = os.getenv("DB_HOST")
DB_PORT = os.getenv("DB_PORT")
DB_NAME = os.getenv("DB_NAME")

db_url = f'mysql+pymysql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}'
engine = create_engine(db_url, echo=False)

start_date = input("시작 날짜를 입력하세요 (YYYY-MM-DD): ")
end_date = input("종료 날짜를 입력하세요 (YYYY-MM-DD): ")
file_prefix = input("파일명 접두사 선택 (new / all): ")

def aggregate_investor_flow(df):
    """기관, 외국인, 개인별 매수/매도/순매수 집계"""
    
    institution_types = ["금융투자", "보험", "투신", "사모", "은행", "기타금융", "연기금 등"]
    foreign_types = ["외국인", "기타외국인"]
    
    institution_buy = df[df["투자자구분"].isin(institution_types)]["매수"].sum()
    institution_sell = df[df["투자자구분"].isin(institution_types)]["매도"].sum()
    institution_net = df[df["투자자구분"].isin(institution_types)]["순매수"].sum()
    
    foreign_buy = df[df["투자자구분"].isin(foreign_types)]["매수"].sum()
    foreign_sell = df[df["투자자구분"].isin(foreign_types)]["매도"].sum()
    foreign_net = df[df["투자자구분"].isin(foreign_types)]["순매수"].sum()
    
    individual = df[df["투자자구분"]=="개인"].iloc[0]
    individual_buy = individual["매수"]
    individual_sell = individual["매도"]
    individual_net = individual["순매수"]
    
    return {
        "institutionBuy": institution_buy,
        "institutionSell": institution_sell,
        "institutionNetBuy": institution_net,
        "foreignBuy": foreign_buy,
        "foreignSell": foreign_sell,
        "foreignNetBuy": foreign_net,
        "individualBuy": individual_buy,
        "individualSell": individual_sell,
        "individualNetBuy": individual_net
    }

def get_korea_institutional_flow(start_date, end_date, batch_size=50, sleep_sec=1):
    tickers_df = pd.read_sql("SELECT ticker FROM stocks", engine)
    tickers = tickers_df['ticker'].tolist()
    
    all_data = []
    total_batches = math.ceil(len(tickers) / batch_size)

    for i in range(total_batches):   
        batch = tickers[i * batch_size : (i+1) * batch_size]
        batch_data = []

        for ticker in batch:
            df = stock.get_market_trading_value_by_investor(start_date, end_date, ticker, detail=True)

            if df.empty:
                continue

            df = df.reset_index()
            df['ticker'] = ticker
            df['date'] = pd.to_datetime(df['date']).dt.date

            aggregated = aggregate_investor_flow(df)
            aggregated['ticker'] = ticker

            batch_data.append(df)

        if batch_data:
            all_data.extend(batch_data)

        print(f"✅ Batch {i+1}/{total_batches} 완료")
        time.sleep(sleep_sec)

    return pd.concat(all_data, ignore_index=True)

df_all = get_korea_institutional_flow(start_date, end_date)

os.makedirs("stock_price_data", exist_ok=True)

file_name = f"stock_price_data/{file_prefix}_institutional_flow_{datetime.now().strftime('%Y_%m_%d')}.csv"
df_all.to_csv(file_name, index=False, encoding="utf-8-sig")  # 한글깨짐 방지

print(f"CSV 파일이 저장되었습니다: {file_name}")
