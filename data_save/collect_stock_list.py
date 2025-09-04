import pandas as pd
from pykrx import stock
from datetime import datetime
import time
from utills import save_to_csv

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

            kospi_set = set(kospi_tickers)
            kosdaq_set = set(kosdaq_tickers)

            today = datetime.now().strftime('%Y-%m-%d')
            
            for ticker in all_tickers:
                for retry in range(3):
                    try:
                        name = stock.get_market_ticker_name(ticker)
                        market_type = "KOSPI" if ticker in kospi_set else "KOSDAQ"
                        data.append([ticker, name, market_type, 'Unknown', today])
                        break
                    except Exception as e:
                        print(f"⚠️ {ticker} 조회 실패: {e}. 재시도 {retry+1}/3")
                        time.sleep(1)
                else:
                    print(f"❌ {ticker} 최종 실패. 데이터 생략")

            df = pd.DataFrame(data, columns=['ticker','name','market','sector','date'])
            return df

        except Exception as e:
            attempt += 1
            print(f"[{attempt}/{max_retries}] 오류 발생: {e}. {delay}초 후 재시도...")
            time.sleep(delay)

    print("모든 재시도 실패. 빈 DataFrame 반환")
    return pd.DataFrame(columns=['ticker','name','market','sector','date'])

df_stock = get_korea_stock()
save_to_csv(df_stock, file_name="stock_list.csv")