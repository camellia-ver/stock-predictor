import pandas as pd
import glob
import os

def merge_parquet_files(input_dir, output_path, pattern='*.parquet'):
    """
    지정된 폴더 안의 parquet 파일들을 하나로 통합합니다.

    Args:
        input_dir (str): parquet 파일들이 있는 디렉토리 경로
        output_path (str): 통합 parquet 파일을 저장할 경로
        pattern (str): 읽을 파일 패턴 (기본값: "*.parquet")
    """
    files = glob.glob(os.path.join(input_dir, pattern))
    if not files:
        raise FileNotFoundError(f'No parquet files found in {input_dir} with pattern {pattern}')
    
    df_list = [pd.read_parquet(file) for file in files]
    merged_df = pd.concat(df_list, ignore_index=True)

    merged_df.to_parquet(output_path, index=False)
    print(f'✅ {len(files)}개의 parquet 파일을 통합하여 {output_path}에 저장했습니다.')