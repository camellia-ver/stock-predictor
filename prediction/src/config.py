import os

TARGET_DAYS_LIST =  [1, 3, 7] 

current_dir = os.path.dirname(os.path.abspath(__file__))

MODEL_PATHS = {
    'RandomForest_v1': os.path.join(current_dir, "..", "models", 'rf_stock_model.pkl'),
    'XGBoost_v1': os.path.join(current_dir, "..", "models",'xgb_stock_model.pkl'),
    'LightGBM_v1': os.path.join(current_dir, "..", "models",'lgbm_stock_model_v1.pkl')
}

PREPROCESSOR_PATH = os.path.join(current_dir, "..", "models", "preprocessor.pkl")
DATA_PATH =  os.path.join(current_dir, "..", "data", "dataset_base.parquet")
NEW_DATA_PATH = os.path.join(current_dir, "..", "data", "new_data.parquet")

SQL_PATH = 'sql/build_dataset.sql'
NEW_SQL_PATH = 'sql/build_new_data.sql'