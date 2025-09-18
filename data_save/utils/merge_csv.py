import pandas as pd
import glob
import os

def update_all_with_new_general(data_dir, file_keyword=""):
    """
    new_*_*.csv 파일을 기존 all_*_*.csv 파일에 추가하고, 중복 제거 후 저장.
    new_ 파일이 없으면 로그만 출력.
    
    Parameters
    ----------
    data_dir : str
        CSV 파일들이 저장된 폴더 경로
    file_keyword : str, optional
        파일 이름에 포함된 키워드로 필터링 (예: 'korea_stock_index'). 
        기본값은 "" (모든 파일 대상)
    
    Returns
    -------
    pd.DataFrame
        업데이트된 all_* DataFrame (new_ 파일 없으면 기존 all_ 반환)
    """
    all_pattern = os.path.join(data_dir, f'all_{file_keyword}.csv') if file_keyword else os.path.join(data_dir, 'all_*.csv')
    new_pattern = os.path.join(data_dir, f"new_{file_keyword}_*.csv") if file_keyword else os.path.join(data_dir, "new_*_*.csv")
    
    all_files = glob.glob(all_pattern)
    if not all_files:
        raise FileNotFoundError('기존 all_ 파일이 없습니다.')
    latest_all_file = max(all_files, key=os.path.getmtime)

    all_df = pd.read_csv(latest_all_file)

    new_files = glob.glob(new_pattern)
    if not new_files:
        print("ℹ️ 새로운 new_ 파일이 없습니다. 업데이트 필요 없음.")
        return all_df
    
    new_df = pd.concat((pd.read_csv(f) for f in new_files), ignore_index=True)
    merged_df = pd.concat([all_df, new_df], ignore_index=True)

    merged_df = merged_df.drop_duplicates()

    merged_df.to_csv(latest_all_file, index=False, encoding='utf-8-sig')
    print(f"✅ all_ 파일 업데이트 완료: {latest_all_file}")

    for f in new_files:
        try:
            os.remove(f)
            print(f"🗑 삭제 완료: {f}")
        except Exception as e:
            print(f"⚠️ 삭제 실패: {f} ({e})")

    return merged_df