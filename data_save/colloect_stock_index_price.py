from pykrx import stock
import pandas as pd
from utills import save_to_csv
from batch_process import process_in_batches

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

def fetch_index_ohlcv_in_korea(index_name, start_date, end_date):
    """
    단일 지수 OHLCV 조회 후 DataFrame 반환
    """
    df = stock.get_index_ohlcv_by_date(start_date, end_date, ticker)

    if df.empty:
        return None

    df = df.reset_index()
    df['indexName'] = index_name
    df = df.rename(columns={
        "날짜": "date",
        "시가": "openPrice",
        "고가": "highPrice",
        "저가": "lowPrice",
        "종가": "closePrice",
        "거래량": "volume",
        "거래대금": "value",
        "상장시가총액":"marketCap"
    })
    df['date'] = pd.to_datetime(df['date']).dt.date

    return df

def process_index_wrapper(index_name):
    return fetch_index_ohlcv_in_korea(index_name, start_date, end_date)

df_indices = process_in_batches(
    items=list(main_index.keys()),
    batch_size=2,
    process_func=process_index_wrapper,
    sleep_sec=1
)

save_to_csv(df_indices,file_prefix, file_name="korea_stock_index")