# stock_collector.py
from pykrx import stock
from .base_collector import BaseCollector
import pandas as pd

class StockCollector(BaseCollector):
    def fetch_stock_ohlcv(self, ticker, start_date, end_date):
        df = stock.get_market_ohlcv_by_date(start_date, end_date, ticker)

        if df.empty:
            return None
        
        df = df.reset_index().rename(columns={
            '날짜':'date','시가':'openPrice','종가':'closePrice','고가':'highPrice',
            '저가':'lowPrice','거래량':'volume','등락률':'changeRate'
        })

        df['ticker'] = ticker
        df['date'] = pd.to_datetime(df['date']).dt.date

        return df

    def fetch_stock_valuation(self, ticker, start_date, end_date):
        df = stock.get_market_fundamental(start_date, end_date, ticker)

        if df.empty:
            return None
        
        df = df.reset_index()

        df['roe'] = (df['EPS'] / df['BPS'] * 100).where(df['BPS'] != 0)

        df = df.rename(columns={
            'PER':'per','PBR':'pbr','EPS':'eps','BPS':'bps','DPS':'dps','DIV':'dividendYield','날짜':'date'
        })

        df['ticker'] = ticker
        df['date'] = pd.to_datetime(df['date']).dt.date

        return df
