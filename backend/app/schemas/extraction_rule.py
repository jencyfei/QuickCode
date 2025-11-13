"""
提取规则相关的Pydantic模型
"""
from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime


class ExtractionRuleBase(BaseModel):
    """提取规则基础模型"""
    name: str = Field(..., min_length=1, max_length=100, description="规则名称")
    rule_type: str = Field(..., description="规则类型: pickup_code, address, sender")
    pattern: str = Field(..., min_length=1, description="正则表达式或模式")
    extract_group: int = Field(default=1, ge=0, description="提取第几个捕获组")
    priority: int = Field(default=0, description="优先级（数字越大越优先）")
    is_active: bool = Field(default=True, description="是否启用")
    description: Optional[str] = Field(None, description="规则描述")


class ExtractionRuleCreate(ExtractionRuleBase):
    """创建提取规则"""
    pass


class ExtractionRuleUpdate(BaseModel):
    """更新提取规则"""
    name: Optional[str] = Field(None, min_length=1, max_length=100)
    pattern: Optional[str] = Field(None, min_length=1)
    extract_group: Optional[int] = Field(None, ge=0)
    priority: Optional[int] = None
    is_active: Optional[bool] = None
    description: Optional[str] = None


class ExtractionRuleResponse(ExtractionRuleBase):
    """提取规则响应"""
    id: int
    user_id: int
    created_at: datetime
    updated_at: Optional[datetime] = None
    
    class Config:
        from_attributes = True


class RuleTemplateResponse(BaseModel):
    """规则模板响应"""
    id: int
    name: str
    category: Optional[str] = None
    rule_type: str
    pattern: str
    extract_group: int
    priority: int
    description: Optional[str] = None
    created_at: datetime
    
    class Config:
        from_attributes = True


class RuleTestRequest(BaseModel):
    """规则测试请求"""
    pattern: str = Field(..., description="正则表达式")
    extract_group: int = Field(default=1, ge=0, description="提取组")
    test_text: str = Field(..., description="测试文本")


class RuleTestResponse(BaseModel):
    """规则测试响应"""
    success: bool
    matched: bool
    extracted: Optional[str] = None
    error: Optional[str] = None
