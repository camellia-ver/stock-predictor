# base_collector.py

import os
import math
import time
import pandas as pd
from datetime import datetime

class BaseCollector:
    def __init__(self, folder='stock_price_data'):
        self.folder = folder
        os.makedirs(self.folder, exist_ok=True)

    def save_to_csv(self, df ,file_name, prefix=None):
        if prefix == 'new':
            file_name = f"{prefix}_{file_name}_{datetime.now().strftime('%Y_%m_%d')}.csv"
        else:
            file_name = f"{prefix}_{file_name}.csv"

        file_path = os.path.abspath(os.path.join(self.folder, file_name))
        df.to_csv(file_path, index=False, encoding="utf-8-sig")

        print(f"✅ CSV 파일이 저장되었습니다: {file_path}")

        return file_path
    
    @staticmethod
    def process_in_batches(items, batch_size, process_func, sleep_sec=1, max_retries=3):
        all_data = []
        total_batches = math.ceil(len(items) / batch_size)

        for i in range(total_batches):
            batch = items[i*batch_size : (i+1)*batch_size]
            batch_data = []

            for item in batch:
                for attempt in range(max_retries):
                    try:
                        df = process_func(item)

                        if df is not None and not df.empty:
                            batch_data.append(df)
                            
                        break
                    except Exception as e:
                        print(f"⚠️ {item} 처리 실패: {e}. 재시도 {attempt+1}/{max_retries}")
                        time.sleep(1)
                else:
                    print(f"❌ {item} 최종 실패, 건너뜀")
            
            if batch_data:
                all_data.extend(batch_data)

            print(f"✅ Batch {i+1}/{total_batches} 완료")
            time.sleep(sleep_sec)

        return pd.concat(all_data, ignore_index=True) if all_data else pd.DataFrame()
