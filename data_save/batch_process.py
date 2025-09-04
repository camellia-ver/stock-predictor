import math
import time
import pandas as pd

def process_in_batches(items, batch_size, process_func, sleep_sec=1, max_retries=3):
    """
    items 리스트를 batch로 나누어 process_func 적용
    :param items: 처리할 리스트
    :param batch_size: batch 단위
    :param process_func: 각 item 처리 함수, DataFrame 반환
    :param sleep_sec: batch 간 대기
    :param max_retries: item별 최대 재시도
    :return: concat된 DataFrame
    """
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

    if all_data:
        return pd.concat(all_data, ignore_index=True)
    else:
        return pd.DataFrame()