from sqlalchemy import create_engine, text
import pandas as pd
from dotenv import load_dotenv, find_dotenv
import os

load_dotenv(find_dotenv())

DB_USER = os.getenv("DB_USER")
DB_PASSWORD = os.getenv("DB_PASSWORD")
DB_HOST = os.getenv("DB_HOST")
DB_PORT = os.getenv("DB_PORT")
DB_NAME = os.getenv("DB_NAME")

DB_URL = f'mysql+pymysql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}'
engine = create_engine(DB_URL)

SQL = open('sql/build_dataset.sql').read()

with engine.begin() as conn:
    df = pd.read_sql(text(SQL), conn)

if df.empty:
    raise ValueError("DB에서 데이터를 가져오지 못했습니다.")

print(f"데이터 shape: {df.shape}")
print(df.head())

print(df.isna().sum())

os.makedirs('data', exist_ok=True)

df.to_parquet('data/dataset_base.parquet', index=False)
print('saved -> dataset_base.parquet')