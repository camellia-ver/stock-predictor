# scripts/update_data.py
# python -m scripts.update_data로 실행

import sys
from collectors.index_collector import IndexCollector
from collectors.stock_collector import StockCollector
from collectors.stock_list_collector import StockListCollector

def collect_and_save(collector, items, batch_size, fetch_func, file_name, prefix):
    df = collector.process_in_batches(items, batch_size, fetch_func)
    
    if not df.empty:
        collector.save_to_csv(df, file_name, prefix)

if __name__ == '__main__':
    start_date = input("시작 날짜 (YYYY-MM-DD): ")
    end_date = input("종료 날짜 (YYYY-MM-DD): ")

    mode = sys.argv[1] if len(sys.argv) > 1 else "daily"
    prefix = "new" if mode == "daily" else "all"

    index_col = IndexCollector()
    stock_col = StockCollector()
    list_col = StockListCollector()

    # 지수
    collect_and_save(index_col, list(index_col.main_index.keys()), 2,
                     lambda name: index_col.fetch_index_ohlcv(name, start_date, end_date),
                     "korea_stock_index_price", prefix)

    # 주식 리스트
    tickers_df = list_col.get_korea_stock()
    tickers = tickers_df['ticker'].tolist()

    # 밸류
    collect_and_save(stock_col, tickers, 50,
                     lambda t: stock_col.fetch_stock_valuation(t, start_date, end_date),
                     "korea_valuation", prefix)

    # OHLCV
    collect_and_save(stock_col, tickers, 50,
                     lambda t: stock_col.fetch_stock_ohlcv(t, start_date, end_date),
                     "korea_stock_price", prefix)

    print(f"✅ {mode} 수집 완료 ({prefix})")
