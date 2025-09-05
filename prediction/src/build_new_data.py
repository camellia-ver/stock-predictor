import pandas as pd
from sqlalchemy import create_engine, text
from sqlalchemy.exc import SQLAlchemyError
from dotenv import load_dotenv, find_dotenv
import os
from config import NEW_SQL_PATH

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

SQL = open(NEW_SQL_PATH).read()

try:
    df_new = pd.read_sql(text(SQL), conn)
    if df_new.empty:
        raise ValueError("조회된 데이터가 없습니다.")
    df_new.to_parquet('data/new_data.parquet', index=False)
    print(f"new_data.parquet 생성 완료: {df_new.shape[0]}건")
except Exception as e:
    print("데이터 생성 실패:", e)
finally:
    conn.close()