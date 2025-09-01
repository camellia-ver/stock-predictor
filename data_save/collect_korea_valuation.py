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

def get_korea_valuation(start_date, end_date, batch_size=50, sleep_sec=1):
    tickers_df = pd.read_sql("SELECT ticker FROM stocks", engine)
    tickers = tickers_df['ticker'].tolist()
    
    all_data = []
    total_batches = math.ceil(len(tickers) / batch_size)

    for i in range(total_batches):   
        batch = tickers[i * batch_size : (i+1) * batch_size]
        batch_data = []

        for ticker in batch:
            df = stock.get_market_fundamental(start_date, end_date, ticker)
            if df.empty:
                continue

            df = df.reset_index()
            df['ticker'] = ticker
            df['ROE'] = df['EPS'] / df['BPS'] * 100

            df = df.rename(columns={
                'PER': 'per',
                'PBR': 'pbr',
                'EPS': 'eps',
                'BPS': 'bps',
                'DIV': 'dividendYield',
                '날짜': 'date'
            })

            df['date'] = pd.to_datetime(df['date']).dt.date
            batch_data.append(df)

        if batch_data:
            all_data.extend(batch_data)

        print(f"✅ Batch {i+1}/{total_batches} 완료")
        time.sleep(sleep_sec)

    return pd.concat(all_data, ignore_index=True)

df_all = get_korea_valuation(start_date, end_date)

os.makedirs("stock_price_data", exist_ok=True)

file_name = f"stock_price_data/{file_prefix}_korea_valuation_{datetime.now().strftime('%Y_%m_%d')}.csv"
df_all.to_csv(file_name, index=False, encoding="utf-8-sig")  # 한글깨짐 방지

print(f"CSV 파일이 저장되었습니다: {file_name}")

