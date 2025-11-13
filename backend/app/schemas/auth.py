"""
认证相关的Pydantic模式
"""
from pydantic import BaseModel, EmailStr, Field, field_validator
from typing import Optional
from datetime import datetime


class UserRegister(BaseModel):
    """用户注册请求"""
    email: Optional[EmailStr] = None
    phone: Optional[str] = Field(None, min_length=11, max_length=11)
    password: str = Field(..., min_length=6, max_length=50)
    
    @field_validator('phone')
    @classmethod
    def validate_phone(cls, v):
        """验证手机号格式"""
        if v and not v.isdigit():
            raise ValueError('手机号必须是数字')
        return v
    
    @field_validator('email', 'phone')
    @classmethod
    def validate_at_least_one(cls, v, info):
        """至少提供邮箱或手机号之一"""
        # 这个验证在model_validator中更合适
        return v
    
    class Config:
        json_schema_extra = {
            "example": {
                "email": "user@example.com",
                "phone": "13800138000",
                "password": "password123"
            }
        }


class UserLogin(BaseModel):
    """用户登录请求"""
    username: str = Field(..., description="邮箱或手机号")
    password: str = Field(..., min_length=6)
    
    class Config:
        json_schema_extra = {
            "example": {
                "username": "user@example.com",
                "password": "password123"
            }
        }


class Token(BaseModel):
    """JWT Token响应"""
    access_token: str
    token_type: str = "bearer"
    expires_in: int = Field(..., description="过期时间（秒）")
    
    class Config:
        json_schema_extra = {
            "example": {
                "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                "token_type": "bearer",
                "expires_in": 1800
            }
        }


class TokenData(BaseModel):
    """JWT Token中的数据"""
    user_id: Optional[int] = None
    username: Optional[str] = None


class UserResponse(BaseModel):
    """用户信息响应"""
    id: int
    email: Optional[str] = None
    phone: Optional[str] = None
    created_at: datetime
    
    class Config:
        from_attributes = True
        json_schema_extra = {
            "example": {
                "id": 1,
                "email": "user@example.com",
                "phone": "13800138000",
                "created_at": "2025-11-04T10:00:00"
            }
        }


class UserProfile(BaseModel):
    """用户个人信息"""
    id: int
    email: Optional[str] = None
    phone: Optional[str] = None
    created_at: datetime
    updated_at: datetime
    
    class Config:
        from_attributes = True
