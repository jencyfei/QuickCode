"""
短信管理相关API路由
"""
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from sqlalchemy import or_, and_, func
from typing import List, Optional
from datetime import datetime, date, time

from ..database import get_db
from ..models.sms import SMSMessage as SmsMessage
from ..models.tag import Tag, SMSTag as SmsTag
from ..models.user import User
from ..schemas.sms import (
    SmsCreate, SmsBatchCreate, SmsUpdate, SmsResponse, SmsListResponse,
    SmsFilter, SmsBatchDelete, SmsAddTags, SmsBatchAddTags
)
from ..schemas.tag import TagResponse
from ..dependencies.auth import get_current_user
from ..services.sms_classifier import auto_tag_sms

router = APIRouter()


def _build_sms_response(sms: SmsMessage, db: Session) -> SmsResponse:
    """构建短信响应对象，包含标签信息"""
    # 查询短信的所有标签
    tags = db.query(Tag).join(
        SmsTag, Tag.id == SmsTag.tag_id
    ).filter(
        SmsTag.sms_id == sms.id
    ).all()
    
    # 构建标签响应
    tags_response = []
    for tag in tags:
        # 获取标签的短信数量
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
        tags_response.append(TagResponse(**tag_dict))
    
    # 构建短信响应
    sms_dict = {
        "id": sms.id,
        "sender": sms.sender,
        "content": sms.content,
        "received_at": sms.received_at,
        "phone_number": sms.phone_number,
        "user_id": sms.user_id,
        "tags": tags_response,
        "created_at": sms.created_at,
        "updated_at": sms.updated_at
    }
    
    return SmsResponse(**sms_dict)


@router.get("", response_model=SmsListResponse)
async def get_sms_list(
    keyword: Optional[str] = Query(None, description="搜索关键词"),
    tag_ids: Optional[str] = Query(None, description="标签ID列表，逗号分隔"),
    start_date: Optional[date] = Query(None, description="开始日期"),
    end_date: Optional[date] = Query(None, description="结束日期"),
    page: int = Query(1, ge=1, description="页码"),
    page_size: int = Query(20, ge=1, le=100, description="每页数量"),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    获取短信列表
    
    支持以下筛选条件：
    - **keyword**: 搜索发件人或内容
    - **tag_ids**: 按标签筛选（多个标签用逗号分隔，OR逻辑）
    - **start_date**: 开始日期（YYYY-MM-DD）
    - **end_date**: 结束日期（YYYY-MM-DD）
    - **page**: 页码（从1开始）
    - **page_size**: 每页数量（1-100）
    """
    # 基础查询
    query = db.query(SmsMessage).filter(
        SmsMessage.user_id == current_user.id
    )
    
    # 关键词搜索
    if keyword:
        search_pattern = f"%{keyword}%"
        query = query.filter(
            or_(
                SmsMessage.sender.ilike(search_pattern),
                SmsMessage.content.ilike(search_pattern)
            )
        )
    
    # 标签筛选
    if tag_ids:
        try:
            tag_id_list = [int(tid.strip()) for tid in tag_ids.split(",")]
            # 使用子查询筛选包含指定标签的短信
            query = query.join(
                SmsTag, SmsMessage.id == SmsTag.sms_id
            ).filter(
                SmsTag.tag_id.in_(tag_id_list)
            ).distinct()
        except ValueError:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="标签ID格式错误"
            )
    
    # 时间范围筛选
    if start_date:
        start_datetime = datetime.combine(start_date, time.min)
        query = query.filter(SmsMessage.received_at >= start_datetime)
    
    if end_date:
        end_datetime = datetime.combine(end_date, time.max)
        query = query.filter(SmsMessage.received_at <= end_datetime)
    
    # 计算总数
    total = query.count()
    
    # 排序和分页
    query = query.order_by(SmsMessage.received_at.desc())
    offset = (page - 1) * page_size
    sms_list = query.offset(offset).limit(page_size).all()
    
    # 构建响应
    items = [_build_sms_response(sms, db) for sms in sms_list]
    
    return SmsListResponse(
        total=total,
        page=page,
        page_size=page_size,
        items=items
    )


@router.get("/{sms_id}", response_model=SmsResponse)
async def get_sms(
    sms_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    获取单条短信详情
    """
    sms = db.query(SmsMessage).filter(
        SmsMessage.id == sms_id,
        SmsMessage.user_id == current_user.id
    ).first()
    
    if not sms:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="短信不存在"
        )
    
    return _build_sms_response(sms, db)


@router.post("", response_model=SmsResponse, status_code=status.HTTP_201_CREATED)
async def create_sms(
    sms_data: SmsCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    创建单条短信
    
    用于Android端上传短信
    """
    # 创建短信
    new_sms = SmsMessage(
        sender=sms_data.sender,
        content=sms_data.content,
        received_at=sms_data.received_at,
        phone_number=sms_data.phone_number,
        user_id=current_user.id
    )
    
    db.add(new_sms)
    db.commit()
    db.refresh(new_sms)
    
    return _build_sms_response(new_sms, db)


@router.post("/batch", response_model=List[SmsResponse], status_code=status.HTTP_201_CREATED)
async def create_sms_batch(
    batch_data: SmsBatchCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    批量创建短信
    
    用于Android端批量上传短信
    支持自动标签识别
    """
    created_sms_list = []
    
    for sms_data in batch_data.messages:
        # 检查是否重复（基于内容和时间）
        existing = db.query(SmsMessage).filter(
            SmsMessage.user_id == current_user.id,
            SmsMessage.content == sms_data.content,
            SmsMessage.received_at == sms_data.received_at
        ).first()
        
        if existing:
            continue  # 跳过重复的短信
        
        # 创建短信
        new_sms = SmsMessage(
            sender=sms_data.sender,
            content=sms_data.content,
            received_at=sms_data.received_at,
            phone_number=sms_data.phone_number,
            user_id=current_user.id
        )
        
        db.add(new_sms)
        db.flush()  # 刷新以获取ID，但不提交
        
        # 自动识别并添加标签
        suggested_tags = auto_tag_sms(sms_data.content, sms_data.sender)
        if suggested_tags:
            _auto_add_tags(db, new_sms.id, suggested_tags, current_user.id)
        
        created_sms_list.append(new_sms)
    
    db.commit()
    
    # 刷新所有创建的短信
    for sms in created_sms_list:
        db.refresh(sms)
    
    return [_build_sms_response(sms, db) for sms in created_sms_list]


def _auto_add_tags(db: Session, sms_id: int, tag_names: List[str], user_id: int):
    """
    自动添加标签到短信
    如果标签不存在则自动创建
    
    Args:
        db: 数据库会话
        sms_id: 短信ID
        tag_names: 标签名称列表
        user_id: 用户ID
    """
    # 默认标签配置
    DEFAULT_TAG_CONFIG = {
        '快递': {'color': '#10b981', 'icon': '📦'},
        '验证码': {'color': '#3b82f6', 'icon': '🔐'},
        '银行': {'color': '#f59e0b', 'icon': '🏦'},
        '通知': {'color': '#6366f1', 'icon': '🔔'},
        '营销': {'color': '#ec4899', 'icon': '🎁'},
    }
    
    for tag_name in tag_names:
        # 查找或创建标签
        tag = db.query(Tag).filter(
            Tag.name == tag_name,
            Tag.user_id == user_id
        ).first()
        
        if not tag:
            # 自动创建标签
            config = DEFAULT_TAG_CONFIG.get(tag_name, {'color': '#6b7280', 'icon': '🏷️'})
            tag = Tag(
                name=tag_name,
                color=config['color'],
                icon=config['icon'],
                user_id=user_id
            )
            db.add(tag)
            db.flush()  # 刷新以获取ID
        
        # 添加短信-标签关联
        existing_relation = db.query(SmsTag).filter(
            SmsTag.sms_id == sms_id,
            SmsTag.tag_id == tag.id
        ).first()
        
        if not existing_relation:
            sms_tag = SmsTag(sms_id=sms_id, tag_id=tag.id)
            db.add(sms_tag)


@router.put("/{sms_id}", response_model=SmsResponse)
async def update_sms(
    sms_id: int,
    sms_data: SmsUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    更新短信信息
    """
    sms = db.query(SmsMessage).filter(
        SmsMessage.id == sms_id,
        SmsMessage.user_id == current_user.id
    ).first()
    
    if not sms:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="短信不存在"
        )
    
    # 更新字段
    update_data = sms_data.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(sms, field, value)
    
    db.commit()
    db.refresh(sms)
    
    return _build_sms_response(sms, db)


@router.delete("/{sms_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_sms(
    sms_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    删除单条短信
    """
    sms = db.query(SmsMessage).filter(
        SmsMessage.id == sms_id,
        SmsMessage.user_id == current_user.id
    ).first()
    
    if not sms:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="短信不存在"
        )
    
    db.delete(sms)
    db.commit()
    
    return None


@router.post("/batch-delete", status_code=status.HTTP_204_NO_CONTENT)
async def batch_delete_sms(
    delete_data: SmsBatchDelete,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    批量删除短信
    """
    # 删除指定ID的短信（只删除属于当前用户的）
    deleted_count = db.query(SmsMessage).filter(
        SmsMessage.id.in_(delete_data.ids),
        SmsMessage.user_id == current_user.id
    ).delete(synchronize_session=False)
    
    db.commit()
    
    if deleted_count == 0:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="未找到要删除的短信"
        )
    
    return None


@router.post("/{sms_id}/tags", response_model=SmsResponse)
async def add_tags_to_sms(
    sms_id: int,
    tag_data: SmsAddTags,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    为单条短信添加标签
    """
    # 检查短信是否存在
    sms = db.query(SmsMessage).filter(
        SmsMessage.id == sms_id,
        SmsMessage.user_id == current_user.id
    ).first()
    
    if not sms:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="短信不存在"
        )
    
    # 检查标签是否都属于当前用户
    tags = db.query(Tag).filter(
        Tag.id.in_(tag_data.tag_ids),
        Tag.user_id == current_user.id
    ).all()
    
    if len(tags) != len(tag_data.tag_ids):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="部分标签不存在或不属于当前用户"
        )
    
    # 添加标签（避免重复）
    for tag_id in tag_data.tag_ids:
        existing = db.query(SmsTag).filter(
            SmsTag.sms_id == sms_id,
            SmsTag.tag_id == tag_id
        ).first()
        
        if not existing:
            sms_tag = SmsTag(sms_id=sms_id, tag_id=tag_id)
            db.add(sms_tag)
    
    db.commit()
    db.refresh(sms)
    
    return _build_sms_response(sms, db)


@router.post("/batch-add-tags", status_code=status.HTTP_200_OK)
async def batch_add_tags(
    batch_data: SmsBatchAddTags,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    批量为短信添加标签
    """
    # 检查短信是否都属于当前用户
    sms_list = db.query(SmsMessage).filter(
        SmsMessage.id.in_(batch_data.sms_ids),
        SmsMessage.user_id == current_user.id
    ).all()
    
    if len(sms_list) != len(batch_data.sms_ids):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="部分短信不存在或不属于当前用户"
        )
    
    # 检查标签是否都属于当前用户
    tags = db.query(Tag).filter(
        Tag.id.in_(batch_data.tag_ids),
        Tag.user_id == current_user.id
    ).all()
    
    if len(tags) != len(batch_data.tag_ids):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="部分标签不存在或不属于当前用户"
        )
    
    # 批量添加标签
    added_count = 0
    for sms_id in batch_data.sms_ids:
        for tag_id in batch_data.tag_ids:
            existing = db.query(SmsTag).filter(
                SmsTag.sms_id == sms_id,
                SmsTag.tag_id == tag_id
            ).first()
            
            if not existing:
                sms_tag = SmsTag(sms_id=sms_id, tag_id=tag_id)
                db.add(sms_tag)
                added_count += 1
    
    db.commit()
    
    return {
        "message": f"成功为 {len(batch_data.sms_ids)} 条短信添加了标签",
        "added_relations": added_count
    }


@router.delete("/{sms_id}/tags/{tag_id}", status_code=status.HTTP_204_NO_CONTENT)
async def remove_tag_from_sms(
    sms_id: int,
    tag_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    移除短信的标签
    """
    # 检查短信是否存在
    sms = db.query(SmsMessage).filter(
        SmsMessage.id == sms_id,
        SmsMessage.user_id == current_user.id
    ).first()
    
    if not sms:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="短信不存在"
        )
    
    # 检查标签是否存在
    tag = db.query(Tag).filter(
        Tag.id == tag_id,
        Tag.user_id == current_user.id
    ).first()
    
    if not tag:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="标签不存在"
        )
    
    # 删除关联
    sms_tag = db.query(SmsTag).filter(
        SmsTag.sms_id == sms_id,
        SmsTag.tag_id == tag_id
    ).first()
    
    if sms_tag:
        db.delete(sms_tag)
        db.commit()
    
    return None
