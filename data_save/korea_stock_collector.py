# ========================================
# KOREA STOCK DATA COLLECTOR CLASS
# ========================================

import os
import math
import time
import pandas as pd
from datetime import datetime
from pykrx import stock
from sqlalchemy import create_engine
from dotenv import load_dotenv

class StockDataCollector:
    def __init__(self, db_env=True, folder="stock_price_data"):
        self.folder = folder
        if db_env:
            load_dotenv()
            DB_USER = os.getenv("DB_USER")
            DB_PASSWORD = os.getenv("DB_PASSWORD")
            DB_HOST = os.getenv("DB_HOST")
            DB_PORT = os.getenv("DB_PORT")
            DB_NAME = os.getenv("DB_NAME")
            db_url = f'mysql+pymysql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}'
            self.engine = create_engine(db_url, echo=False)
        else:
            self.engine = None

        self.main_index = {
            'KOSPI':'1001',
            'KOSDAQ':'2001',
            'KOSPI200':'1028',
            'KOSPI100':'1026',
            'KOSPI50':'1027',
            'KRX100':'1003',
            'KRX300':'1004'
        }

    # ----------------------------------------
    # CSV 저장
    # ----------------------------------------
    def save_to_csv(self, df ,file_name, prefix=None):
        os.makedirs(self.folder, exist_ok=True)
        if prefix is not None:
            file_name = f"{prefix}_{file_name}_{datetime.now().strftime('%Y_%m_%d_%H%M%S')}.csv"

        file_path = os.path.abspath(os.path.join(self.folder, file_name))
        df.to_csv(file_path, index=False, encoding="utf-8-sig")
        print(f"✅ CSV 파일이 저장되었습니다: {file_path}")
        return file_path

    # ----------------------------------------
    # Batch 처리 공용
    # ----------------------------------------
    @staticmethod
    def process_in_batches(items, batch_size, process_func, sleep_sec=1, max_retries=3):
        all_data = []
        total_batches = math.ceil(len(items) / batch_size)

        for i in range(total_batches):
            batch = items[i*batch_size : (i+1)*batch_size]
            batch_data = []

            for item in batch:
                for attempt in range(max_retries):
                    try:
                        df = process_func(item)
                        if df is not None and not df.empty:
                            batch_data.append(df)
                        break
                    except Exception as e:
                        print(f"⚠️ {item} 처리 실패: {e}. 재시도 {attempt+1}/{max_retries}")
                        time.sleep(1)
                else:
                    print(f"❌ {item} 최종 실패, 건너뜀")
            
            if batch_data:
                all_data.extend(batch_data)
            print(f"✅ Batch {i+1}/{total_batches} 완료")
            time.sleep(sleep_sec)

        return pd.concat(all_data, ignore_index=True) if all_data else pd.DataFrame()

    # ========================================
    # 1️⃣ 주식 리스트
    # ========================================
    def get_korea_stock(self, max_retries=3, delay=5):
        attempt = 0
        today = datetime.now().strftime('%Y-%m-%d')
        
        while attempt < max_retries:
            try:
                data = []
                kospi_tickers = stock.get_market_ticker_list(market="KOSPI")
                kosdaq_tickers = stock.get_market_ticker_list(market="KOSDAQ")
                kospi_set = set(kospi_tickers)
                all_tickers = kospi_tickers + kosdaq_tickers

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

    # ========================================
    # 2️⃣ 지수 OHLCV
    # ========================================
    def fetch_index_ohlcv(self, index_name, start_date, end_date):
        df = stock.get_index_ohlcv_by_date(start_date, end_date, self.main_index[index_name])
        if df.empty:
            return None
        df = df.reset_index()
        df["indexName"] = index_name
        df = df.rename(columns={
            "날짜": "date", "시가": "openPrice", "고가": "highPrice", "저가": "lowPrice",
            "종가": "closePrice", "거래량": "volume", "거래대금": "value", "상장시가총액":"marketCap"
        })
        df['date'] = pd.to_datetime(df['date']).dt.date
        return df

    # ========================================
    # 3️⃣ 개별 주식 밸류에이션
    # ========================================
    def fetch_stock_valuation(self, ticker, start_date, end_date):
        df = stock.get_market_fundamental(start_date, end_date, ticker)
        if df.empty:
            return None
        df = df.reset_index()
        df['ticker'] = ticker
        df['roe'] = df.apply(lambda row: (row['EPS']/row['BPS']*100) if row['BPS'] else None, axis=1)
        df = df.rename(columns={
            'PER':'per','PBR':'pbr','EPS':'eps','BPS':'bps','DPS':'dps','DIV':'dividendYield','날짜':'date'
        })
        df['date'] = pd.to_datetime(df['date']).dt.date
        return df

    # ========================================
    # 4️⃣ 개별 주식 OHLCV
    # ========================================
    def fetch_stock_ohlcv(self, ticker, start_date, end_date):
        df = stock.get_market_ohlcv_by_date(start_date, end_date, ticker)
        if df.empty:
            return None
        df = df.reset_index()
        df['ticker'] = ticker
        df = df.rename(columns={
            '날짜':'date','시가':'openPrice','종가':'closePrice','고가':'highPrice',
            '저가':'lowPrice','거래량':'volume','등락률':'changeRate'
        })
        df['date'] = pd.to_datetime(df['date']).dt.date
        return df

    # ========================================
    # 5️⃣ DB에서 ticker 리스트 가져오기
    # ========================================
    def get_tickers_from_db(self):
        if self.engine is None:
            raise ValueError("DB 엔진이 초기화되지 않았습니다.")
        tickers_df = pd.read_sql("SELECT ticker FROM stocks", self.engine)
        return tickers_df['ticker'].tolist()

if __name__ == "__main__":
    # start_date = input("시작 날짜 (YYYY-MM-DD): ")
    # end_date = input("종료 날짜 (YYYY-MM-DD): ")
    # file_prefix = input("파일명 접두사 선택 (new / all): ")

    collector = StockDataCollector()

    # 1️⃣ 주식 리스트
    df_stock = collector.get_korea_stock()
    collector.save_to_csv(df_stock, file_name="stock_list.csv")

    # # 2️⃣ 지수 OHLCV
    # def wrapper_index(name): return collector.fetch_index_ohlcv(name, start_date, end_date)
    # df_indices = collector.process_in_batches(list(collector.main_index.keys()), batch_size=2, process_func=wrapper_index)
    # collector.save_to_csv(df_indices,file_name="korea_stock_index",file_prefix)

    # # 3️⃣ 주식 밸류에이션
    # tickers = collector.get_tickers_from_db()
    # def wrapper_val(t): return collector.fetch_stock_valuation(t, start_date, end_date)
    # df_val = collector.process_in_batches(tickers, batch_size=50, process_func=wrapper_val)
    # collector.save_to_csv(df_val, file_name="korea_stock_valuation",file_prefix)

    # # 4️⃣ 주식 OHLCV
    # def wrapper_ohlcv(t): return collector.fetch_stock_ohlcv(t, start_date, end_date)
    # df_ohlcv = collector.process_in_batches(tickers, batch_size=50, process_func=wrapper_ohlcv)
    # collector.save_to_csv(df_ohlcv, file_name="korea_stock_ohlcv",file_prefix)
