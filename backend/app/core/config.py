from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    APP_NAME: str = "AIFintes API"
    DEBUG: bool = False
    DATABASE_URL: str = "postgresql+asyncpg://postgres:postgres@localhost:5433/aifintes"
    SECRET_KEY: str = "change-me-in-production"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30
    REFRESH_TOKEN_EXPIRE_DAYS: int = 7

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"


settings = Settings()
