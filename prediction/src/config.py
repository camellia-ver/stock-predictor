TARGET_DAYS_LIST = [1, 3, 7]  

MODEL_PATHS = {
    'RandomForest_v1': 'models/rf_stock_model.pkl',
    'XGBoost_v1': 'models/xgb_stock_model.pkl',
    'LightGBM_v1': 'models/lgbm_stock_model_v1.pkl'
}

PREPROCESSOR_PATH = '/models/preprocessor.pkl'
DATA_PATH = '/data/dataset_base.parquet'
NEW_DATA_PATH = '/data/new_data.parquet'

SQL_PATH = 'sql/build_dataset.sql'
NEW_SQL_PATH = 'sql/build_new_data.sql'