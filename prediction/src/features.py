import pandas as pd
import numpy as np
from sklearn.preprocessing import OneHotEncoder, StandardScaler
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from config import TARGET_DAYS_LIST

num_cols = ['open_price','close_price','high_price','low_price','volume','change_rate',
            'per','pbr','roe','eps','bps','dps','dividend_yield']
cat_cols = ['market','sector']

def add_technical_features(df):
    df = df.sort_values(['stock_id', 'prediction_date'])

    # 수익률
    df['return_1d'] = df.groupby('stock_id')['close_price'].transform(lambda x: x.pct_change(1))
    df['return_5d'] = df.groupby('stock_id')['close_price'].transform(lambda x: x.pct_change(5))
    df['return_20d'] = df.groupby('stock_id')['close_price'].transform(lambda x: x.pct_change(20))

    # 변동성: 초기 구간은 누적 std 사용, min_periods 적용
    def rolling_std_safe(x, window):
        rstd = x.rolling(window=window, min_periods=2).std()
        # 초기 구간 NaN은 누적 std로 대체
        rstd.iloc[:window] = x.expanding(min_periods=2).std().iloc[:window]
        return rstd

    df['vol_5d'] = df.groupby('stock_id')['return_1d'].transform(lambda x: rolling_std_safe(x, 5))
    df['vol_20d'] = df.groupby('stock_id')['return_1d'].transform(lambda x: rolling_std_safe(x, 20))

    # 이동평균: 초기 구간은 누적 평균 사용
    def rolling_mean_safe(x, window):
        rmean = x.rolling(window=window, min_periods=2).mean()
        rmean.iloc[:window] = x.expanding(min_periods=2).mean().iloc[:window]
        return rmean

    df['ma_5d'] = df.groupby('stock_id')['close_price'].transform(lambda x: rolling_mean_safe(x, 5))
    df['ma_20d'] = df.groupby('stock_id')['close_price'].transform(lambda x: rolling_mean_safe(x, 20))

    # 모멘텀
    df['momentum_5d'] = df['close_price'] - df.groupby('stock_id')['close_price'].transform(lambda x: x.shift(5))

    # 가격 범위, 이동평균 비율, 변동성 비율
    df['price_range_ratio'] = (df['high_price'] - df['low_price']) / df['close_price']
    df['ma_ratio_5d'] = df['close_price'] / df['ma_5d'].replace(0, np.nan)
    df['vol_ratio_20d'] = df['vol_5d'] / df['vol_20d'].replace(0, np.nan)

    tech_cols = ['return_1d','return_5d','return_20d','vol_5d','vol_20d',
                 'ma_5d','ma_20d','momentum_5d','price_range_ratio','ma_ratio_5d','vol_ratio_20d']

    # NaN 처리: ffill → bfill → median
    df[tech_cols] = df.groupby('stock_id')[tech_cols].ffill().bfill()
    df[tech_cols] = df.groupby('stock_id')[tech_cols].transform(lambda x: x.fillna(x.median()))

    # 결측 indicator 생성
    for col in tech_cols:
        df[f'{col}_missing'] = df[col].isna().astype(int)

    # 타겟 생성
    for day in TARGET_DAYS_LIST:
        df[f'y_{day}d'] = (df.groupby('stock_id')['close_price'].transform(lambda x: x.shift(-day)) > df['close_price']).astype(int)

    return df

def add_missing_indicators(df, num_cols):
    df = df.copy()
    for col in num_cols:
        df[f'{col}_missing'] = df[col].isna().astype(int)
    return df

def build_preprocessor(train_df, valid_df=None):
    train_df = add_missing_indicators(train_df, num_cols)
    num_cols_extended = num_cols + [f'{col}_missing' for col in num_cols]

    num_transformer = Pipeline([('scaler', StandardScaler())])
    cat_transformer = Pipeline([('onehot', OneHotEncoder(handle_unknown='ignore', sparse_output=False))])

    preprocessor = ColumnTransformer([
        ('num', num_transformer, num_cols_extended),
        ('cat', cat_transformer, cat_cols)
    ])

    X_train = preprocessor.fit_transform(train_df)

    X_valid = None
    if valid_df is not None and not valid_df.empty:
        for col in [f'{c}_missing' for c in num_cols]:
            if col not in valid_df.columns:
                valid_df[col] = 0
        X_valid = preprocessor.transform(valid_df)

    return preprocessor, X_train, X_valid

def transform_features(df, preprocessor):
    df = add_missing_indicators(df, num_cols)
    return preprocessor.transform(df)
