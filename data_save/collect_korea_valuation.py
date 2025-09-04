from pykrx import stock
import pandas as pd
from sqlalchemy import create_engine
from dotenv import load_dotenv
import math
import time
import os
from utills import save_to_csv
from batch_process import process_in_batches

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

tickers_df = pd.read_sql("SELECT ticker FROM stocks", engine)
tickers = tickers_df['ticker'].tolist()

def fetch_stock_valuation_in_korea(ticker, start_date, end_date):
    """
    단일 주식 밸류에이션 조회 후 DataFrame 반환
    """
    df = stock.get_market_fundamental(start_date, end_date, ticker)
    
    if df.empty:
        return None
    
    df = df.reset_index()
    df['ticker'] = ticker

    df['roe'] = df.apply(lambda row: (row['EPS']/row['BPS']*100) if row['BPS'] else None, axis=1)

    df = df.rename(columns={
        'PER': 'per',
        'PBR': 'pbr',
        'EPS': 'eps',
        'BPS': 'bps',
        'DPS':  'dps',
        'DIV': 'dividendYield',
        '날짜': 'date'
    })
    df['date'] = pd.to_datetime(df['date']).dt.date

    return df

def process_valuation_wrapper(ticker):
    return fetch_stock_valuation_in_korea(ticker, start_date, end_date)

df_valuation = process_in_batches(
    items=tickers,
    batch_size=50,
    process_func=process_valuation_wrapper,
    sleep_sec=1
)

save_to_csv(df_valuation, file_prefix,'korea_valuation')