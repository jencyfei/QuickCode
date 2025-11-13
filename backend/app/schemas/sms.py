"""
短信相关的Pydantic模型
"""
from pydantic import BaseModel, Field
from typing import Optional, List
from datetime import datetime, date
from .tag import TagResponse


class SmsBase(BaseModel):
    """短信基础模型"""
    sender: str = Field(..., min_length=1, max_length=100, description="发件人")
    content: str = Field(..., min_length=1, description="短信内容")
    received_at: datetime = Field(..., description="接收时间")
    phone_number: Optional[str] = Field(None, max_length=20, description="手机号码")


class SmsCreate(SmsBase):
    """创建短信请求模型"""
    pass


class SmsBatchCreate(BaseModel):
    """批量创建短信请求模型"""
    messages: List[SmsCreate]


class SmsUpdate(BaseModel):
    """更新短信请求模型"""
    sender: Optional[str] = Field(None, min_length=1, max_length=100)
    content: Optional[str] = Field(None, min_length=1)
    received_at: Optional[datetime] = None
    phone_number: Optional[str] = Field(None, max_length=20)


class SmsResponse(SmsBase):
    """短信响应模型"""
    id: int
    user_id: int
    tags: List[TagResponse] = []
    created_at: datetime
    updated_at: Optional[datetime] = None

    class Config:
        from_attributes = True


class SmsListResponse(BaseModel):
    """短信列表响应模型"""
    total: int
    page: int
    page_size: int
    items: List[SmsResponse]


class SmsFilter(BaseModel):
    """短信筛选参数模型"""
    keyword: Optional[str] = Field(None, description="搜索关键词（发件人或内容）")
    tag_ids: Optional[List[int]] = Field(None, description="标签ID列表（OR逻辑）")
    start_date: Optional[date] = Field(None, description="开始日期")
    end_date: Optional[date] = Field(None, description="结束日期")
    page: int = Field(1, ge=1, description="页码")
    page_size: int = Field(20, ge=1, le=100, description="每页数量")


class SmsBatchDelete(BaseModel):
    """批量删除短信请求模型"""
    ids: List[int] = Field(..., min_items=1, description="要删除的短信ID列表")


class SmsAddTags(BaseModel):
    """为短信添加标签请求模型"""
    tag_ids: List[int] = Field(..., min_items=1, description="标签ID列表")


class SmsBatchAddTags(BaseModel):
    """批量为短信添加标签请求模型"""
    sms_ids: List[int] = Field(..., min_items=1, description="短信ID列表")
    tag_ids: List[int] = Field(..., min_items=1, description="标签ID列表")


class SmsImportRequest(BaseModel):
    """短信导入请求模型（用于手动粘贴导入）"""
    raw_text: str = Field(..., min_length=1, description="原始短信文本")


class SmsImportResponse(BaseModel):
    """短信导入响应模型"""
    success: int = Field(0, description="成功导入数量")
    failed: int = Field(0, description="失败数量")
    duplicated: int = Field(0, description="重复数量")
    total: int = Field(0, description="总数量")
    messages: List[SmsResponse] = Field([], description="导入的短信列表")
