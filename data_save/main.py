if __name__ == "__main__":
    start_date = input("시작 날짜 (YYYY-MM-DD): ")
    end_date = input("종료 날짜 (YYYY-MM-DD): ")

    collector = StockDataCollector()

    # 1️⃣ 주식 리스트
    df_stock = collector.get_korea_stock()
    collector.save_to_csv(df_stock, file_name="stock_list.csv")

    # 2️⃣ 지수 OHLCV
    def wrapper_index(name): return collector.fetch_index_ohlcv(name, start_date, end_date)
    df_indices = collector.process_in_batches(list(collector.main_index.keys()), batch_size=2, process_func=wrapper_index)
    collector.save_to_csv(df_indices, file_name="korea_stock_index.csv")

    # 3️⃣ 주식 밸류에이션
    tickers = collector.get_tickers_from_db()
    def wrapper_val(t): return collector.fetch_stock_valuation(t, start_date, end_date)
    df_val = collector.process_in_batches(tickers, batch_size=50, process_func=wrapper_val)
    collector.save_to_csv(df_val, file_name="korea_stock_valuation.csv")

    # 4️⃣ 주식 OHLCV
    def wrapper_ohlcv(t): return collector.fetch_stock_ohlcv(t, start_date, end_date)
    df_ohlcv = collector.process_in_batches(tickers, batch_size=50, process_func=wrapper_ohlcv)
    collector.save_to_csv(df_ohlcv, file_name="korea_stock_ohlcv.csv")
