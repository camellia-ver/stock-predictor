import os
import datetime

def save_to_csv(df, prefix, folder="stock_price_data", file_name=None):
    """
    DataFrame을 지정 폴더에 CSV로 저장
    :param df: 저장할 DataFrame
    :param folder: 저장할 폴더
    :param file_name: 파일명 지정, None이면 timestamp 기반 자동 생성
    :return: 저장된 파일 경로
    """
        
    os.makedirs("stock_price_data", exist_ok=True)

    if file_name is None:
        file_name = f"data_{datetime.now().strftime('%Y_%m_%d_%H%M%S')}.csv"
    else:
        file_name = f"{prefix}_file_name_{datetime.now().strftime('%Y_%m_%d_%H%M%S')}.csv"

    file_path = os.path.join(folder, file_name)
    df.to_csv(file_path, index=False, encoding="utf-8-sig")
    print(f"✅ CSV 파일이 저장되었습니다: {file_path}")
    return file_path