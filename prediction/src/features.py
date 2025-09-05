import pandas as pd
from sklearn.preprocessing import OneHotEncoder, StandardScaler
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline

def add_technical_features(df):
    df = df.sort_values(['stock_id', 'prediction_date'])

    # 수익률
    df['return_1d'] = df.groupby('stock_id')['close_price'].pct_change(1)
    df['return_5d']  = df.groupby('stock_id')['close_price'].pct_change(5)
    df['return_20d'] = df.groupby('stock_id')['close_price'].pct_change(20)

    # 변동성
    df['vol_5d']  = df.groupby('stock_id')['return_1d'].rolling(5).std().reset_index(0, drop=True)
    df['vol_20d'] = df.groupby('stock_id')['return_1d'].rolling(20).std().reset_index(0, drop=True)

    # 이동평균
    df['ma_5d'] = df.groupby('stock_id')['close_price'].rolling(5).mean().reset_index(0, drop=True)
    df['ma_20d'] = df.groupby('stock_id')['close_price'].rolling(20).mean().reset_index(0, drop=True)

    # 모멘텀
    df['momentum_5d'] = df['close_price'] - df.groupby('stock_id')['close_price'].shift(5)  

    # 가격 범위, 이동평균 비율, 변동성 비율
    df['price_range_ratio'] = (df['high_price'] - df['low_price']) / df['close_price']
    df['ma_ratio_5d'] = df['close_price'] / df['ma_5d']
    df['vol_ratio_20d'] = df['vol_5d'] / df['vol_20d']

    tech_cols = ['return_1d','return_5d','return_20d','vol_5d','vol_20d',
                 'ma_5d','ma_20d','momentum_5d','price_range_ratio','ma_ratio_5d','vol_ratio_20d']
    
    df[tech_cols] = df.groupby('stock_id')[tech_cols].ffill().bfill()
    df[tech_cols] = df.groupby('stock_id')[tech_cols].transform(lambda x: x.fillna(x.median()))
    
    for col in tech_cols:
        df[f'{col}_missing'] = df[col].isna().astype(int)

    return df

def build_preprocessor(df):
    num_cols = ['open_price','close_price','high_price','low_price','volume','change_rate',
                'per','pbr','roe','eps','bps','dps','dividend_yield',
                'index_close','index_volume','index_value','index_marketcap']
    cat_cols = ['market','sector']
    
    for col in num_cols:
        df[f'{col}_missing'] = df[col].isna().astype(int)
    num_cols_extended = num_cols + [f'{col}_missing' for col in num_cols]
    
    num_transformer = Pipeline([('scaler', StandardScaler())])
    cat_transformer = Pipeline([('onehot', OneHotEncoder(handle_unknown='ignore'))])
    
    preprocessor = ColumnTransformer([
        ('num', num_transformer, num_cols_extended),
        ('cat', cat_transformer, cat_cols)
    ])
    
    X = preprocessor.fit_transform(df)

    return preprocessor, X

def transform_features(df, preprocessor):
    return preprocessor.transform(df)