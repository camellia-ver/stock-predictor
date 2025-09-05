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

for model_name, base_model_path in MODEL_PATHS.items():
    for target_day in TARGET_DAYS_LIST:
        model_path = base_model_path.replace('.pkl',f'_{target_day}d.pkl')

        if not os.path.exists(model_path):
            print(f"[경고] {model_path} 없음 -> 건너뜀")
            continue

        model = joblib.load(model_path)
        y_proba = model.predict_proba(X_new)[:,1]

        df_pred = df_new[['stock_id','prediction_date']].copy()
        df_pred['target_date'] = df_pred['prediction_date'] + pd.Timedelta(days=target_day)
        df_pred['model_name'] = f"{model_name}_{target_day}d"
        df_pred['up_prob'] = y_proba
        df_pred['down_prob'] = 1 - y_proba
        df_pred['created_at'] = datetime.now().strftime('%Y-%m-%d %H:%M:%S')

        df_all_preds.append(df_pred)

if df_all_preds:
    df_all_preds = pd.concat(df_all_preds, ignore_index=True)
    df_all_preds = df_all_preds.drop_duplicates(subset=['stock_id', 'target_date', 'model_name'])

    df_all_preds.to_csv(CSV_PATH, index=False, encoding='utf-8-sig')
    print(f"✅ 모든 예측 결과 CSV 저장 완료: {CSV_PATH}")
else:
    print("⚠️ 예측 결과가 생성되지 않았습니다.")