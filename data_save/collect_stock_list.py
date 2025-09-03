import pandas as pd
from pykrx import stock
from datetime import datetime
import time
import os

def get_korea_stock(max_retries=3, delay=5):
    """
    KOSPI/KOSDAQ 주식 정보를 수집하는 함수.
    오류 발생 시 max_retries만큼 재시도하고, 실패하면 빈 DataFrame 반환.
    
    :param max_retries: 최대 재시도 횟수
    :param delay: 재시도 전 대기 시간(초)
    """
    attempt = 0
    while attempt < max_retries:
        try:
            data = []

            kospi_tickers = stock.get_market_ticker_list(market="KOSPI")
            kosdaq_tickers = stock.get_market_ticker_list(market="KOSDAQ")
            all_tickers = kospi_tickers + kosdaq_tickers
            
            for ticker in all_tickers:
                name = stock.get_market_ticker_name(ticker)
                market_type = "KOSPI" if ticker in kospi_tickers else "KOSDAQ"
                data.append([ticker, name, market_type, None, datetime.now().strftime('%Y-%m-%d')])

            df = pd.DataFrame(data, columns=['ticker','name','market','sector','date'])
            return df

        except Exception as e:
            attempt += 1
            print(f"[{attempt}/{max_retries}] 오류 발생: {e}. {delay}초 후 재시도...")
            time.sleep(delay)

    print("모든 재시도 실패. 빈 DataFrame 반환")
    return pd.DataFrame(columns=['ticker','name','market','sector','date'])

os.makedirs("stock_price_data", exist_ok=True)

file_name = f"stock_price_data/stock_list.csv"
get_korea_stock().to_csv(file_name, index=False, encoding="utf-8-sig")  # 한글깨짐 방지

print(f"CSV 파일이 저장되었습니다: {file_name}")
