import pandas as pd
import joblib
from features import add_technical_features, transform_features
from sqlalchemy import create_engine
from sqlalchemy.exc import SQLAlchemyError
from dotenv import load_dotenv, find_dotenv
from datetime import datetime, timedelta
import os
import time
from config import TARGET_DAYS_LIST, MODEL_PATHS, PREPROCESSOR_PATH, NEW_DATA_PATH

df_new = pd.read_parquet(NEW_DATA_PATH)
df_new = add_technical_features(df_new)

if not os.path.exists(NEW_DATA_PATH):
    raise FileNotFoundError(f"{NEW_DATA_PATH}가 존재하지 않습니다.")

df_new = pd.read_parquet(NEW_DATA_PATH)

if df_new.empty:
    raise ValueError("불러온 데이터가 비어 있습니다. 예측할 데이터가 없습니다.")

df_new = add_technical_features(df_new)

if not os.path.exists(PREPROCESSOR_PATH):
    raise FileNotFoundError("전처리기 파일이 존재하지 않습니다.")

load_dotenv(find_dotenv())

DB_USER = os.getenv("DB_USER")
DB_PASSWORD = os.getenv("DB_PASSWORD")
DB_HOST = os.getenv("DB_HOST")
DB_PORT = os.getenv("DB_PORT")
DB_NAME = os.getenv("DB_NAME")

DB_URL = f'mysql+pymysql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}'
try:
    engine = create_engine(DB_URL)
    conn = engine.connect()
    print("DB 연결 성공")
except SQLAlchemyError as e:
    print("DB 연결 실패:", e)
    raise

preprocessor = joblib.load(PREPROCESSOR_PATH)
X_new = transform_features(df_new, preprocessor)

for model_name, model_path in MODEL_PATHS.items():
    if not os.path.exists(model_path):
        print(f"[경고] {model_path} 파일이 존재하지 않아 건너뜁니다.")
        continue

    model = joblib.load(model_path)

    for days in TARGET_DAYS_LIST:
        df_new['target_date'] = df_new['prediction_date'] + timedelta(days=3)
        y_proba = model.predict_proba(X_new)[:,1]

        df_pred = df_new[['stock_id','prediction_date']].copy()
        df_pred['target_date'] = df_pred['prediction_date'] + pd.Timedelta(days=1)
        df_pred['model_name'] = model_name
        df_pred['upProb'] = y_proba
        df_pred['downProb'] = 1 - y_proba
        df_pred['createdAt'] = datetime.now()

        success = False
        retries = 3
        for attempt in range(retries):
            try:
                df_pred.to_sql('predictions', con=engine, if_exists='append', index=False)
                print(f"{model_name} 예측 결과 DB 저장 완료")
                success = True
                break
            except SQLAlchemyError as e:
                print(f"[경고] DB 저장 실패 (재시도 {attempt+1}/{retries}): {e}")
                time.sleep(2)
        if not success:
            print(f"[오류] {model_name} 결과를 DB에 저장하지 못했습니다.")