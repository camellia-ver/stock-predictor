import pandas as pd
import joblib
from features import add_technical_features, transform_features
from datetime import datetime
import os
from config import MODEL_PATHS, PREPROCESSOR_PATH, NEW_DATA_PATH, TARGET_DAYS_LIST, CSV_PATH

if not os.path.exists(NEW_DATA_PATH):
    raise FileNotFoundError(f"{NEW_DATA_PATH}가 존재하지 않습니다.")

df_new = pd.read_parquet(NEW_DATA_PATH)

if df_new.empty:
    raise ValueError("불러온 데이터가 비어 있습니다. 예측할 데이터가 없습니다.")

df_new = add_technical_features(df_new)

if not os.path.exists(PREPROCESSOR_PATH):
    raise FileNotFoundError("전처리기 파일이 존재하지 않습니다.")

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

try:
    df_all_preds.to_csv(CSV_PATH, index=False, encoding='utf-8-sig')
    print(f"모든 예측 결과 CSV 저장 완료: {CSV_PATH}")
except Exception as e:
    print(f"[오류] CSV 저장 실패: {e}")