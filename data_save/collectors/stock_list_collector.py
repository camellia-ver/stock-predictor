# stock_list_collector.py
from pykrx import stock
from .base_collector import BaseCollector
import pandas as pd
from datetime import datetime

class StockListCollector(BaseCollector):
    def get_korea_stock(self):
        today = datetime.now().strftime('%Y%m%d') 

        kospi_tickers = stock.get_market_ticker_list(market="KOSPI")
        kosdaq_tickers = stock.get_market_ticker_list(market="KOSDAQ")
        kospi_set = set(kospi_tickers)
        all_tickers = kospi_tickers + kosdaq_tickers

        ticker_name_map = {t: stock.get_market_ticker_name(t) for t in all_tickers}
        name_ticker_map = {v: k for k, v in ticker_name_map.items()}

        try:
            kospi_sector_df = stock.get_market_sector_classifications(today, "KOSPI")
        except:
            kospi_sector_df = pd.DataFrame(columns=["종목명", "업종명"])
        try:
            kosdaq_sector_df = stock.get_market_sector_classifications(today, "KOSDAQ")
        except:
            kosdaq_sector_df = pd.DataFrame(columns=["종목명", "업종명"])

        sector_df = pd.concat([kospi_sector_df, kosdaq_sector_df], ignore_index=True)
        sector_df["ticker"] = sector_df["종목명"].map(name_ticker_map)

        # ✅ key = ticker
        sector_map = dict(zip(sector_df["ticker"], sector_df["업종명"]))

        data = []
        for ticker in all_tickers:
            name = ticker_name_map[ticker]
            market_type = "KOSPI" if ticker in kospi_set else "KOSDAQ"
            sector = sector_map.get(ticker, "Unknown")   # ✅ 여기 고침
            data.append([ticker, name, market_type, sector, today])

        df = pd.DataFrame(data, columns=["ticker", "name", "market", "sector", "date"])
        return df

