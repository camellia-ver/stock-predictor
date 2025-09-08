import pandas as pd
import glob
import os

def merge_parquet_files(input_dir, output_path, pattern='*.parquet', delete_original=True):
    """
    지정된 폴더 안의 parquet 파일들을 하나로 통합하고 필요 시 원본 삭제.

    Args:
        input_dir (str): parquet 파일들이 있는 디렉토리 경로
        output_path (str): 통합 parquet 파일을 저장할 경로
        pattern (str): 읽을 파일 패턴 (기본값: "*.parquet")
        delete_original (bool): 통합 후 원본 parquet 파일 삭제 여부 (기본값: True)
    """
    files = glob.glob(os.path.join(input_dir, pattern))

    if not files:
        raise FileNotFoundError(f'No parquet files found in {input_dir} with pattern {pattern}')
    
    output_path = os.path.abspath(output_path)
    files_abs = [os.path.abspath(f) for f in files]

    df_list = [pd.read_parquet(file) for file in files_abs]
    merged_df = pd.concat(df_list, ignore_index=True)

    merged_df.to_parquet(output_path, index=False)
    print(f'✅ {len(files_abs)}개의 parquet 파일을 통합하여 {output_path}에 저장했습니다.')

    if delete_original:
        for file in files_abs:
            if file != output_path:
                os.remove(file)
        print(f'🗑️ 원본 parquet 파일 {len(files_abs) - 1}개를 삭제했습니다.')

