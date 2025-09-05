# fetch_stock_list.py
from collectors.stock_list_collector import StockListCollector

if __name__ == '__main__':
    # 1️⃣ Collector 생성
    list_col = StockListCollector()

    # 2️⃣ 주식 리스트 수집
    tickers_df = list_col.get_korea_stock()

    # 3️⃣ CSV 저장
    list_col.save_to_csv(tickers_df, "stock_list", prefix="all")

    print("✅ 주식 리스트 수집 완료")
