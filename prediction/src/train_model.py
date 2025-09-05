import pandas as pd
import joblib
from sklearn.metrics import accuracy_score, roc_auc_score
from features import add_technical_features, build_preprocessor, add_missing_indicators
from sklearn.ensemble import RandomForestClassifier
from xgboost import XGBClassifier
from lightgbm import LGBMClassifier
import os
from config import DATA_PATH, PREPROCESSOR_PATH, TARGET_DAYS_LIST, MODEL_PATHS

df = pd.read_parquet(DATA_PATH)
df = add_technical_features(df)

split_date = df['prediction_date'].max() - pd.Timedelta(days=60)
train_df_base = df[df['prediction_date'] < split_date].copy()
valid_df_base = df[df['prediction_date'] >= split_date].copy()

if train_df_base.empty:
    raise ValueError('학습 데이터가 없습니다. 날짜 기준 확인 필요')

if valid_df_base.empty:
    print('경고: 검증 데이터가 없습니다. train만 수행됩니다.')

models_to_train = {
    'RandomForest_v1': RandomForestClassifier(n_estimators=200, max_depth=10, random_state=42),
    'XGBoost_v1': XGBClassifier(n_estimators=200, max_depth=6, random_state=42),
    'LightGBM_v1': LGBMClassifier(n_estimators=500, max_depth=10, random_state=42)
}

preprocessor, _, _ = build_preprocessor(train_df_base)
os.makedirs(os.path.dirname(PREPROCESSOR_PATH), exist_ok=True)
joblib.dump(preprocessor, PREPROCESSOR_PATH)
print(f"전처리기 저장 완료 -> {PREPROCESSOR_PATH}")

for target_day in TARGET_DAYS_LIST:
    y_col = f'y_{target_day}d'
    print(f"--- TARGET DAY: {target_day} ---")

    train_df = train_df_base.dropna(subset=[y_col])
    valid_df = valid_df_base.dropna(subset=[y_col]) if not valid_df_base.empty else None

    y_train = train_df[y_col]
    y_valid = valid_df[y_col] if valid_df is not None else None

    _, X_train_np, X_valid_np = build_preprocessor(train_df, valid_df)

    feature_cols = train_df.drop(columns=[y_col]).columns

    X_train = pd.DataFrame(X_train_np, columns=feature_cols)
    X_valid = pd.DataFrame(X_valid_np, columns=feature_cols) if X_valid_np is not None else None

    for model_name, model in models_to_train.items():
        print(f"=== {model_name} 학습 시작 ({target_day}d) ===")
        model.fit(X_train, y_train)
        
        if X_valid is not None:
            y_pred = model.predict(X_valid)
            
            if hasattr(model, "predict_proba"):
                y_proba = model.predict_proba(X_valid)[:, 1]
            else:
                # 확률 지원 안하는 모델 대비 fallback
                y_proba = y_pred 

            print(f"{model_name} ({target_day}d) Accuracy:", accuracy_score(y_valid, y_pred))
            print(f"{model_name} ({target_day}d) AUC:", roc_auc_score(y_valid, y_proba))

        model_path = MODEL_PATHS[model_name]
        joblib.dump(model, model_path)
        print(f"{model_name} ({target_day}d) 모델 저장 완료 -> {model_path}")