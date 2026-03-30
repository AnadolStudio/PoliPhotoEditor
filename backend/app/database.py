import os
from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker, AsyncSession
from sqlalchemy.orm import DeclarativeBase

DATABASE_URL = os.getenv("DATABASE_URL", "sqlite+aiosqlite:///./poliphoto.db")


class Base(DeclarativeBase):
    pass


def get_engine(database_url: str = DATABASE_URL):
    return create_async_engine(
        database_url,
        echo=False,
        connect_args={"check_same_thread": False},
    )


engine = get_engine()

AsyncSessionLocal = async_sessionmaker(
    bind=engine,
    class_=AsyncSession,
    expire_on_commit=False,
    autocommit=False,
    autoflush=False,
)


async def init_db(bind_engine=None):
    target = bind_engine if bind_engine is not None else engine
    async with target.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)


async def get_db() -> AsyncSession:
    async with AsyncSessionLocal() as session:
        try:
            yield session
            await session.commit()
        except Exception:
            await session.rollback()
            raise
        finally:
            await session.close()
