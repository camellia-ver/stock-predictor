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

tickers_df = pd.read_sql("SELECT ticker FROM stocks", engine)
tickers = tickers_df['ticker'].tolist()

all_data = []

start_date = '2025-01-01'
end_date = '2025-08-21'

for ticker in tickers:
    df = stock.get_market_ohlcv_by_date(start_date,end_date,ticker)
    df = df.reset_index()
    df['source'] = 'KRX'
    df['ticker'] = ticker

    df = df.rename(columns={
        '날짜':'date',
        '시가':'openPrice',
        '종가':'closePrice',
        '고가':'highPrice',
        '저가':'lowPrice',
        '거래량':'volume'
    })
    df['date'] = pd.to_datetime(df['date']).dt.date
    all_data.append(df)

result_df = pd.concat(all_data, ignore_index=True)

print(result_df)    