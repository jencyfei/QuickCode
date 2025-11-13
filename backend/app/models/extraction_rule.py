"""
提取规则模型
用于自定义短信内容提取规则（取件码、地址、发件人等）
"""
from sqlalchemy import Column, Integer, String, DateTime, ForeignKey, Boolean, Text
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from ..database import Base


class ExtractionRule(Base):
    """提取规则表"""
    __tablename__ = "extraction_rules"
    
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    name = Column(String(100), nullable=False)  # 规则名称
    rule_type = Column(String(20), nullable=False, index=True)  # 规则类型: pickup_code, address, sender
    pattern = Column(Text, nullable=False)  # 正则表达式或模式
    extract_group = Column(Integer, default=1)  # 提取第几个捕获组
    priority = Column(Integer, default=0)  # 优先级（数字越大越优先）
    is_active = Column(Boolean, default=True)  # 是否启用
    description = Column(Text, nullable=True)  # 规则描述
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    # 关系
    user = relationship("User", back_populates="extraction_rules")
    
    def __repr__(self):
        return f"<ExtractionRule(id={self.id}, name={self.name}, type={self.rule_type})>"


class RuleTemplate(Base):
    """规则模板表（预设规则）"""
    __tablename__ = "rule_templates"
    
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False)  # 模板名称
    category = Column(String(50), nullable=True)  # 分类: 快递, 银行, 验证码
    rule_type = Column(String(20), nullable=False)  # 规则类型
    pattern = Column(Text, nullable=False)  # 正则表达式
    extract_group = Column(Integer, default=1)
    priority = Column(Integer, default=0)
    description = Column(Text, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    
    def __repr__(self):
        return f"<RuleTemplate(id={self.id}, name={self.name})>"
