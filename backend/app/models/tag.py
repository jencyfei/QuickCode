"""
标签模型
"""
from sqlalchemy import Column, Integer, String, DateTime, ForeignKey, UniqueConstraint
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from ..database import Base


class Tag(Base):
    """标签表"""
    __tablename__ = "tags"
    
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    name = Column(String(50), nullable=False)
    color = Column(String(20), default="#FF6B9D")
    icon = Column(String(10), nullable=True)  # Emoji图标
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
    
    # 关系
    user = relationship("User", back_populates="tags")
    sms_messages = relationship("SMSMessage", secondary="sms_tags", back_populates="tags")
    
    # 唯一约束：同一用户不能有重名标签
    __table_args__ = (
        UniqueConstraint('user_id', 'name', name='uq_user_tag_name'),
    )
    
    def __repr__(self):
        return f"<Tag(id={self.id}, name={self.name})>"


class SMSTag(Base):
    """短信-标签关联表"""
    __tablename__ = "sms_tags"
    
    sms_id = Column(Integer, ForeignKey("sms_messages.id", ondelete="CASCADE"), primary_key=True)
    tag_id = Column(Integer, ForeignKey("tags.id", ondelete="CASCADE"), primary_key=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    
    def __repr__(self):
        return f"<SMSTag(sms_id={self.sms_id}, tag_id={self.tag_id})>"
