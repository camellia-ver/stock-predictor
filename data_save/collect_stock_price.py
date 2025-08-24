from pykrx import stock
import pandas as pd
from datetime import datetime
from sqlalchemy import create_engine
from dotenv import load_dotenv
import os

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

def get_korea_stock_ohlcv(start_date,end_date):
    tickers_df = pd.read_sql("SELECT ticker FROM stocks", engine)
    tickers = tickers_df['ticker'].tolist()
    
    all_data = []

    for ticker in tickers:
        df = stock.get_market_ohlcv_by_date(start_date,end_date,ticker)
        if df.empty:
            continue

        df = df.reset_index()
        df['ticker'] = ticker

        df = df.rename(columns={
            '날짜':'date',
            '시가':'openPrice',
            '종가':'closePrice',
            '고가':'highPrice',
            '저가':'lowPrice',
            '거래량':'volume',
            '등락률':'changeRate'
        })
        df['date'] = pd.to_datetime(df['date']).dt.date
        all_data.append(df)

    return pd.concat(all_data, ignore_index=True)

df_all = get_korea_stock_ohlcv(start_date,end_date)

file_name = f'stock_price_data/{file_prefix}_korea_stock_price_{datetime.now().strftime('%Y_%m_%d')}.csv'
df_all.to_csv(file_name,index=False)

print(f"CSV 파일이 저장되었습니다: {file_name}")