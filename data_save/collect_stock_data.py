from pykrx import stock
import pandas as pd
from datetime import datetime

def get_korea_stock():
    data = []
    tickers = stock.get_market_ticker_list(market='ALL')

    for ticker in tickers:
        name = stock.get_market_ticker_name(ticker)
        market = "KOSPI" if ticker in stock.get_market_ticker_list("KOSPI") else "KOSDAQ"
        data.append([ticker,name,market,None,datetime.now()])

    return pd.DataFrame(data,columns=['ticker','name','market','sector','createAt'])

print(get_korea_stock())