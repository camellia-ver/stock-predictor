from pykrx import stock
import yfinance as yf
import pandas as pd
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor

def get_korea_stock():
    data = []

    kospi_tickers = stock.get_market_ticker_list(market="KOSPI")
    kosdaq_tickers = stock.get_market_ticker_list(market="KOSDAQ")
    all_tickers = kospi_tickers + kosdaq_tickers

    for ticker in all_tickers:
        name = stock.get_market_ticker_name(ticker)
        market = "KOSPI" if ticker in stock.get_market_ticker_list("KOSPI") else "KOSDAQ"
        data.append([ticker,name,market,None,datetime.now()])

    return pd.DataFrame(data,columns=['ticker','name','market','sector','createAt'])

krx_df = get_korea_stock()
krx_df.to_csv('korea_stocks.csv',index=False,encoding='utf-8-sig')
print(f'총 {len(krx_df)}개 종목 csv 저장완료!')