from pykrx import stock
import yfinance as yf
import pandas as pd
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor

def get_korea_stock():
    data = []
    tickers = stock.get_market_ticker_list(market='ALL')

    for ticker in tickers:
        name = stock.get_market_ticker_name(ticker)
        market = "KOSPI" if ticker in stock.get_market_ticker_list("KOSPI") else "KOSDAQ"
        data.append([ticker,name,market,None,datetime.now()])

    return pd.DataFrame(data,columns=['ticker','name','market','sector','createAt'])

# 1. NASDAQ 전체 티커 목록
def get_us_tickers():
    url = "ftp://ftp.nasdaqtrader.com/SymbolDirectory/nasdaqtraded.txt"
    df = pd.read_csv(url,sep="|")
    tickers = df[df["Test Issue"] == 'N']['Symbol'].tolist() # 정상종목만
    return tickers

print(get_us_tickers())