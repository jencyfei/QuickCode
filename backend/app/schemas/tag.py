"""
标签相关的Pydantic模型
"""
from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime


class TagBase(BaseModel):
    """标签基础模型"""
    name: str = Field(..., min_length=1, max_length=50, description="标签名称")
    color: str = Field(..., pattern="^#[0-9A-Fa-f]{6}$", description="标签颜色（十六进制）")
    icon: Optional[str] = Field(None, max_length=10, description="标签图标（Emoji）")


class TagCreate(TagBase):
    """创建标签请求模型"""
    pass


class TagUpdate(BaseModel):
    """更新标签请求模型"""
    name: Optional[str] = Field(None, min_length=1, max_length=50, description="标签名称")
    color: Optional[str] = Field(None, pattern="^#[0-9A-Fa-f]{6}$", description="标签颜色")
    icon: Optional[str] = Field(None, max_length=10, description="标签图标")


class TagResponse(TagBase):
    """标签响应模型"""
    id: int
    user_id: int
    sms_count: int = Field(0, description="关联的短信数量")
    created_at: datetime
    updated_at: Optional[datetime] = None

    class Config:
        from_attributes = True


class TagListResponse(BaseModel):
    """标签列表响应模型"""
    total: int
    tags: list[TagResponse]
