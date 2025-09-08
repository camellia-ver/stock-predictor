# 프로젝트 루트에서 python -m scripts.run_merge_csv 실행
import os
from utils.merge_csv import update_all_with_new_general

if __name__ == '__main__':
    current_file_dir = os.path.dirname(os.path.abspath(__file__))
    parent_dir = os.path.dirname(current_file_dir)
    data_dir = os.path.join(parent_dir, 'stock_price_data')
    os.makedirs(data_dir, exist_ok=True)

    df = update_all_with_new_general(data_dir,'korea_stock_index')
    df = update_all_with_new_general(data_dir,'korea_stock_price')
    df = update_all_with_new_general(data_dir,'korea_valuation')