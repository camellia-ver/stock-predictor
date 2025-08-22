from sqlalchemy import create_engine
from sqlalchemy.exc import SQLAlchemyError
from sqlalchemy import text
from dotenv import load_dotenv
import os

from collect_stock_data import get_korea_stock

load_dotenv()

DB_USER = os.getenv("DB_USER")
DB_PASSWORD = os.getenv("DB_PASSWORD")
DB_HOST = os.getenv("DB_HOST")
DB_PORT = os.getenv("DB_PORT")
DB_NAME = os.getenv("DB_NAME")

db_url = f'mysql+pymysql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}'
engine = create_engine(db_url, echo=False)

def upsert_to_mysql(df,table_name='stocks'):
    insert_sql = f"""
    INSERT INTO {table_name} (ticker, name, market, sector, created_at)
    VALUES (:ticker, :name, :market, :sector, :created_at)
    ON DUPLICATE KEY UPDATE
        name = VALUES(name),
        market = VALUES(market),
        sector = VALUES(sector),
        created_at = VALUES(created_at);
    """

    try:
        with engine.begin() as conn:
            for _, row in df.iterrows():
                conn.execute(text(insert_sql), row.to_dict())
        print('데이터 저장/업데이트 완료!')
    except SQLAlchemyError as e:
        print('데이터 저장 실패:',e)

if __name__ == '__main__':
    df_stock = get_korea_stock()
    upsert_to_mysql(df_stock)