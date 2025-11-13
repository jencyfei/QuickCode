"""
标签管理相关API路由
"""
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from sqlalchemy import func
from typing import List

from ..database import get_db
from ..models.tag import Tag, SMSTag as SmsTag
from ..models.user import User
from ..schemas.tag import TagCreate, TagUpdate, TagResponse, TagListResponse
from ..dependencies.auth import get_current_user

router = APIRouter()


@router.get("", response_model=TagListResponse)
async def get_tags(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    获取当前用户的所有标签
    
    返回标签列表，包含每个标签关联的短信数量
    """
    # 查询用户的所有标签
    tags_query = db.query(
        Tag,
        func.count(SmsTag.sms_id).label('sms_count')
    ).outerjoin(
        SmsTag, Tag.id == SmsTag.tag_id
    ).filter(
        Tag.user_id == current_user.id
    ).group_by(
        Tag.id
    ).order_by(
        Tag.created_at.desc()
    )
    
    tags_with_count = tags_query.all()
    
    # 构建响应
    tags_response = []
    for tag, sms_count in tags_with_count:
        tag_dict = {
            "id": tag.id,
            "name": tag.name,
            "color": tag.color,
            "icon": tag.icon,
            "user_id": tag.user_id,
            "sms_count": sms_count or 0,
            "created_at": tag.created_at,
            "updated_at": tag.updated_at
        }
        tags_response.append(TagResponse(**tag_dict))
    
    return TagListResponse(
        total=len(tags_response),
        tags=tags_response
    )


@router.post("", response_model=TagResponse, status_code=status.HTTP_201_CREATED)
async def create_tag(
    tag_data: TagCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    创建新标签
    
    - **name**: 标签名称（1-50字符）
    - **color**: 标签颜色（十六进制格式，如 #FF6B9D）
    - **icon**: 标签图标（可选，Emoji）
    """
    # 检查标签名称是否已存在
    existing_tag = db.query(Tag).filter(
        Tag.user_id == current_user.id,
        Tag.name == tag_data.name
    ).first()
    
    if existing_tag:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"标签 '{tag_data.name}' 已存在"
        )
    
    # 创建新标签
    new_tag = Tag(
        name=tag_data.name,
        color=tag_data.color,
        icon=tag_data.icon,
        user_id=current_user.id
    )
    
    db.add(new_tag)
    db.commit()
    db.refresh(new_tag)
    
    # 返回响应（包含sms_count=0）
    tag_dict = {
        "id": new_tag.id,
        "name": new_tag.name,
        "color": new_tag.color,
        "icon": new_tag.icon,
        "user_id": new_tag.user_id,
        "sms_count": 0,
        "created_at": new_tag.created_at,
        "updated_at": new_tag.updated_at
    }
    
    return TagResponse(**tag_dict)


@router.get("/{tag_id}", response_model=TagResponse)
async def get_tag(
    tag_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    获取单个标签详情
    """
    # 查询标签及其短信数量
    tag_query = db.query(
        Tag,
        func.count(SmsTag.sms_id).label('sms_count')
    ).outerjoin(
        SmsTag, Tag.id == SmsTag.tag_id
    ).filter(
        Tag.id == tag_id,
        Tag.user_id == current_user.id
    ).group_by(
        Tag.id
    ).first()
    
    if not tag_query:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="标签不存在"
        )
    
    tag, sms_count = tag_query
    
    tag_dict = {
        "id": tag.id,
        "name": tag.name,
        "color": tag.color,
        "icon": tag.icon,
        "user_id": tag.user_id,
        "sms_count": sms_count or 0,
        "created_at": tag.created_at,
        "updated_at": tag.updated_at
    }
    
    return TagResponse(**tag_dict)


@router.put("/{tag_id}", response_model=TagResponse)
async def update_tag(
    tag_id: int,
    tag_data: TagUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    更新标签信息
    
    可以更新标签的名称、颜色、图标
    """
    # 查询标签
    tag = db.query(Tag).filter(
        Tag.id == tag_id,
        Tag.user_id == current_user.id
    ).first()
    
    if not tag:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="标签不存在"
        )
    
    # 如果更新名称，检查是否与其他标签重名
    if tag_data.name and tag_data.name != tag.name:
        existing_tag = db.query(Tag).filter(
            Tag.user_id == current_user.id,
            Tag.name == tag_data.name,
            Tag.id != tag_id
        ).first()
        
        if existing_tag:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"标签 '{tag_data.name}' 已存在"
            )
    
    # 更新字段
    update_data = tag_data.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(tag, field, value)
    
    db.commit()
    db.refresh(tag)
    
    # 获取短信数量
    sms_count = db.query(func.count(SmsTag.sms_id)).filter(
        SmsTag.tag_id == tag.id
    ).scalar() or 0
    
    tag_dict = {
        "id": tag.id,
        "name": tag.name,
        "color": tag.color,
        "icon": tag.icon,
        "user_id": tag.user_id,
        "sms_count": sms_count,
        "created_at": tag.created_at,
        "updated_at": tag.updated_at
    }
    
    return TagResponse(**tag_dict)


@router.delete("/{tag_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_tag(
    tag_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    删除标签
    
    删除标签时会同时删除所有短信-标签关联，但不会删除短信本身
    """
    # 查询标签
    tag = db.query(Tag).filter(
        Tag.id == tag_id,
        Tag.user_id == current_user.id
    ).first()
    
    if not tag:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="标签不存在"
        )
    
    # 删除所有关联（级联删除会自动处理）
    db.delete(tag)
    db.commit()
    
    return None
