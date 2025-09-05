import pandas as pd
import joblib
from datetime import datetime
from sklearn.metrics import accuracy_score, roc_auc_score
from features import add_technical_features, build_preprocessor
from sklearn.ensemble import RandomForestClassifier
from xgboost import XGBClassifier
from lightgbm import LGBMClassifier
import os
from src import DATA_PATH, PREPROCESSOR_PATH, MODEL_PATHS

df = pd.read_parquet(DATA_PATH)
df = add_technical_features(df)

split_date = df['prediction_date'].max() - pd.Timedelta(days=60)
train_df = df[df['prediction_date'] < split_date]
valid_df = df[df['prediction_date'] >= split_date]

if train_df.empty:
    raise ValueError('학습 데이터가 없습니다. 날짜 기준 확인 필요')

if valid_df.empty:
    print('경고: 검증 데이터가 없습니다. train만 수행됩니다.')

y_train = train_df['y']
y_valid = valid_df['y'] if not valid_df.empty else None

X_train, preprocessor = build_preprocessor(train_df)
X_valid = preprocessor.transform(valid_df) if not valid_df.empty else None

os.makedirs('models', exist_ok=True)
joblib.dump(preprocessor, PREPROCESSOR_PATH)
print(f"전처리기 저장 완료 -> {PREPROCESSOR_PATH}")

models_to_train = {
    'RandomForest_v1': RandomForestClassifier(n_estimators=200, max_depth=10, random_state=42),
    'XGBoost_v1': XGBClassifier(n_estimators=200, max_depth=6, random_state=42),
    'LightGBM_v1': LGBMClassifier(n_estimators=500, max_depth=10, random_state=42)
}

for name, model in models_to_train.items():
    print(f"=== {name} 학습 시작 ===")
    model.fit(X_train, y_train)

    # 검증
    if X_valid is not None:
        y_pred = model.predict(X_valid)
        y_proba = model.predict_proba(X_valid)[:,1]
        
        print(f"{name} Accuracy:", accuracy_score(y_valid, y_pred))
        print(f"{name} AUC:", roc_auc_score(y_valid, y_proba))

    joblib.dump(model, MODEL_PATHS[name])
    print(f"{name} 모델 저장 완료")