import pandas as pd
import joblib
from features import add_technical_features, transform_features
from sqlalchemy import create_engine
from sqlalchemy.exc import SQLAlchemyError
from dotenv import load_dotenv, find_dotenv
from datetime import datetime
import os
import time
from config import MODEL_PATHS, PREPROCESSOR_PATH, NEW_DATA_PATH, TARGET_DAYS_LIST

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
X_new = pd.DataFrame(X_new, columns=preprocessor.get_feature_names_out())

df_all_preds = []

for model_name, model_path in MODEL_PATHS.items():
    if not os.path.exists(model_path):
        print(f"[경고] {model_path} 파일이 존재하지 않아 건너뜁니다.")
        continue

    model = joblib.load(model_path)
    y_proba = model.predict_proba(X_new)[:,1]

    for target_day in TARGET_DAYS_LIST:
        df_pred = df_new[['stock_id','prediction_date']].copy()
        df_pred['target_date'] = df_pred['prediction_date'] + pd.Timedelta(days=target_day)

        df_pred['model_name'] = model_name
        df_pred['upProb'] = y_proba
        df_pred['downProb'] = 1 - y_proba
        df_pred['createdAt'] = datetime.now()

        df_all_preds.append(df_pred)

if df_all_preds:
    df_all_preds = pd.concat(df_all_preds, ignore_index=True)

for col in ['prediction_date','target_date','createdAt']:
    df_all_preds[col] = pd.to_datetime(df_all_preds[col])

for col in ['upProb','downProb']:
    df_all_preds[col] = df_all_preds[col].astype(float)

df_all_preds.rename(columns={
    'upProb': 'up_prob',
    'downProb': 'down_prob',
    'createdAt': 'created_at'
}, inplace=True)

success = False
retries = 3
for attempt in range(retries):
    try:
        with engine.begin() as conn:
            df_all_preds.to_sql(
                'predictions',
                con=conn,
                if_exists='append',
                index=False,
                method='multi',
                chunksize=5000
            )
        print("모든 예측 결과 DB 저장 완료")
        success = True
        break
    except SQLAlchemyError as e:
        print(f"[경고] DB 저장 실패 (재시도 {attempt+1}/{retries}): {e}")
        time.sleep(2)

if not success:
    print("[오류] 결과를 DB에 저장하지 못했습니다.")