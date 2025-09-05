# stock_list_collector.py
from pykrx import stock
from .base_collector import BaseCollector
import pandas as pd
from datetime import datetime

class StockListCollector(BaseCollector):
    def get_korea_stock(self):
        today = datetime.now().strftime('%Y-%m-%d')

        kospi_tickers = stock.get_market_ticker_list("KOSPI")
        kosdaq_tickers = stock.get_market_ticker_list("KOSDAQ")
        kospi_set = set(kospi_tickers)
        all_tickers = kospi_tickers + kosdaq_tickers

        try:
            kospi_sector_df = stock.get_market_sector_classifications(today, 'KOSPI')
        except:
            kospi_sector_df = pd.DataFrame(columns=['종목명','업종명'])
        try:
            kosdaq_sector_df = stock.get_market_sector_classifications(today, 'KOSDAQ')
        except:
            kosdaq_sector_df = pd.DataFrame(columns=['종목명','업종명'])

        sector_df = pd.concat([kospi_sector_df, kosdaq_sector_df])
        sector_map = dict(zip(sector_df['종목명'], sector_df['업종명']))

        data = []
        for ticker in all_tickers:
            name = stock.get_market_ticker_name(ticker)
            
            market_type = "KOSPI" if ticker in kospi_set else "KOSDAQ"
            sector = sector_map.get(name, 'Unknown')

            data.append([ticker, name, market_type, sector, today])

        df = pd.DataFrame(data, columns=['ticker','name','market','sector','date'])

        return df
