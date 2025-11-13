"""
应用配置管理
"""
from pydantic_settings import BaseSettings
from typing import List


class Settings(BaseSettings):
    """应用配置"""
    
    # 应用信息
    APP_NAME: str = "Smart SMS Tagger"
    DEBUG: bool = True
    VERSION: str = "0.1.0"
    
    # 数据库配置
    DATABASE_URL: str
    
    # JWT配置
    SECRET_KEY: str
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30
    
    # CORS配置
    ALLOWED_ORIGINS: str = "http://localhost:3000,http://localhost:5173"
    
    # 日志配置
    LOG_LEVEL: str = "INFO"
    
    class Config:
        import os
        from pathlib import Path
        # 获取backend目录的绝对路径
        backend_dir = Path(__file__).parent.parent
        env_file = str(backend_dir / ".env")
        case_sensitive = True
        env_file_encoding = 'utf-8'
    
    @property
    def cors_origins(self) -> List[str]:
        """解析CORS允许的源"""
        return [origin.strip() for origin in self.ALLOWED_ORIGINS.split(",")]


# 全局配置实例
settings = Settings()
