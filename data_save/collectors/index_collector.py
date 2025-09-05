# index_collector.py

from pykrx import stock
from .base_collector import BaseCollector
import pandas as pd

class IndexCollector(BaseCollector):
    def __init__(self):
        super().__init__()
        self.main_index = {
            'KOSPI':'1001',
            'KOSDAQ':'2001',
            'KOSPI200':'1028',
            'KOSPI100':'1026',
            'KOSPI50':'1027',
            'KRX100':'1003',
            'KRX300':'1004'
        }

    def fetch_index_ohlcv(self, index_name, start_date, end_date):
        df = stock.get_index_ohlcv_by_date(start_date, end_date, self.main_index[index_name])
        
        if df.empty:
            return None
        
        df = df.reset_index().rename(columns={
            "날짜": "date", "시가": "openPrice", "고가": "highPrice",
            "저가": "lowPrice", "종가": "closePrice", "거래량": "volume",
            "거래대금": "value", "상장시가총액": "marketCap"
        })
        
        df['indexName'] = index_name
        df['date'] = pd.to_datetime(df['date']).dt.date

        return df