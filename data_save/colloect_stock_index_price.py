from pykrx import stock
import pandas as pd
from datetime import datetime
import os

main_index = {
    'KOSPI':'1001',
    'KOSDAQ':'2001',
    'KOSPI200':'1028',
    'KOSPI100':'1026',
    'KOSPI50':'1027',
    'KRX100':'1003',
    'KRX300':'1004'
}

start_date = input("시작 날짜를 입력하세요 (YYYY-MM-DD): ")
end_date = input("종료 날짜를 입력하세요 (YYYY-MM-DD): ")
file_prefix = input("파일명 접두사 선택 (new / all): ")

def get_korea_index_ohlcv(start_date, end_date):
    all_data = []
    for index_name,ticker in main_index.items():
        df = stock.get_index_ohlcv_by_date(start_date, end_date, ticker)
        if df.empty:
            continue
        
        df.reset_index(inplace=True)
        df["indexName"] = index_name

        df = df.rename(columns={
            "시가": "openPrice",
            "고가": "highPrice",
            "저가": "lowPrice",
            "종가": "closePrice",
            "거래량": "volume",
            "거래대금": "value",
            "상장시가총액":"marketCap"
        })
        df['date'] = pd.to_datetime(df['날짜']).dt.date
        df.drop(columns=["날짜"], inplace=True)  # 원래 한글 컬럼 제거
        all_data.append(df)

    return pd.concat(all_data, ignore_index=True)

df_all = get_korea_index_ohlcv(start_date, end_date)

# 폴더 없으면 생성
os.makedirs("stock_price_data", exist_ok=True)

file_name = f"stock_price_data/{file_prefix}_korea_stock_index_price_{datetime.now().strftime('%Y_%m_%d')}.csv"
df_all.to_csv(file_name, index=False, encoding="utf-8-sig")

print(f"CSV 파일이 저장되었습니다: {file_name}")
