"""
短信模型
"""
from sqlalchemy import Column, Integer, String, Text, DateTime, ForeignKey
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from ..database import Base


class SMSMessage(Base):
    """短信消息表"""
    __tablename__ = "sms_messages"
    
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    sender = Column(String(100), index=True)
    content = Column(Text, nullable=False)
    received_at = Column(DateTime(timezone=True), index=True)
    phone_number = Column(String(20), nullable=True)  # 手机号码
    imported_at = Column(DateTime(timezone=True), server_default=func.now())
    raw_text = Column(Text)  # 原始导入文本
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    # 关系
    user = relationship("User", back_populates="sms_messages")
    tags = relationship("Tag", secondary="sms_tags", back_populates="sms_messages")
    
    def __repr__(self):
        return f"<SMSMessage(id={self.id}, sender={self.sender})>"
