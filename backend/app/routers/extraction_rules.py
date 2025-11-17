"""
提取规则管理路由
"""
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from ..database import get_db
from ..models.extraction_rule import ExtractionRule, RuleTemplate
from ..schemas.extraction_rule import (
    ExtractionRuleCreate,
    ExtractionRuleUpdate,
    ExtractionRuleResponse,
    RuleTemplateResponse,
    RuleTestRequest,
    RuleTestResponse
)
from ..dependencies.auth import get_current_user
from ..models.user import User
from ..services.rule_engine import get_rule_engine

router = APIRouter(prefix="/extraction-rules", tags=["extraction_rules"])


@router.get("", response_model=List[ExtractionRuleResponse])
def get_extraction_rules(
    rule_type: str = None,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """获取用户的提取规则列表"""
    query = db.query(ExtractionRule).filter(ExtractionRule.user_id == current_user.id)
    
    if rule_type:
        query = query.filter(ExtractionRule.rule_type == rule_type)
    
    rules = query.order_by(ExtractionRule.priority.desc(), ExtractionRule.created_at.desc()).all()
    return rules


@router.post("/test", response_model=RuleTestResponse)
def test_extraction_rule(
    test_data: RuleTestRequest,
    current_user: User = Depends(get_current_user)
):
    """测试提取规则"""
    engine = get_rule_engine()
    success, extracted, error = engine.test_pattern(
        test_data.pattern,
        test_data.test_text,
        test_data.extract_group
    )
    
    return RuleTestResponse(
        success=success,
        matched=extracted is not None,
        extracted=extracted,
        error=error
    )


@router.get("/{rule_id}", response_model=ExtractionRuleResponse)
def get_extraction_rule(
    rule_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """获取单个提取规则"""
    rule = db.query(ExtractionRule).filter(
        ExtractionRule.id == rule_id,
        ExtractionRule.user_id == current_user.id
    ).first()
    
    if not rule:
        raise HTTPException(status_code=404, detail="规则不存在")
    
    return rule


@router.post("", response_model=ExtractionRuleResponse, status_code=status.HTTP_201_CREATED)
def create_extraction_rule(
    rule_data: ExtractionRuleCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """创建提取规则"""
    # 验证规则类型
    valid_types = ['pickup_code', 'address', 'sender']
    if rule_data.rule_type not in valid_types:
        raise HTTPException(
            status_code=400,
            detail=f"无效的规则类型，必须是: {', '.join(valid_types)}"
        )
    
    # 测试正则表达式是否有效
    engine = get_rule_engine()
    success, _, error = engine.test_pattern(rule_data.pattern, "测试文本", rule_data.extract_group)
    if not success:
        raise HTTPException(status_code=400, detail=error)
    
    # 创建规则
    rule = ExtractionRule(
        user_id=current_user.id,
        name=rule_data.name,
        rule_type=rule_data.rule_type,
        pattern=rule_data.pattern,
        extract_group=rule_data.extract_group,
        priority=rule_data.priority,
        is_active=rule_data.is_active,
        description=rule_data.description
    )
    
    db.add(rule)
    db.commit()
    db.refresh(rule)
    
    return rule


@router.put("/{rule_id}", response_model=ExtractionRuleResponse)
def update_extraction_rule(
    rule_id: int,
    rule_data: ExtractionRuleUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """更新提取规则"""
    rule = db.query(ExtractionRule).filter(
        ExtractionRule.id == rule_id,
        ExtractionRule.user_id == current_user.id
    ).first()
    
    if not rule:
        raise HTTPException(status_code=404, detail="规则不存在")
    
    # 如果更新了pattern，验证正则表达式
    if rule_data.pattern:
        extract_group = rule_data.extract_group if rule_data.extract_group is not None else rule.extract_group
        engine = get_rule_engine()
        success, _, error = engine.test_pattern(rule_data.pattern, "测试文本", extract_group)
        if not success:
            raise HTTPException(status_code=400, detail=error)
    
    # 更新字段
    update_data = rule_data.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(rule, field, value)
    
    db.commit()
    db.refresh(rule)
    
    return rule


@router.delete("/{rule_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_extraction_rule(
    rule_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """删除提取规则"""
    rule = db.query(ExtractionRule).filter(
        ExtractionRule.id == rule_id,
        ExtractionRule.user_id == current_user.id
    ).first()
    
    if not rule:
        raise HTTPException(status_code=404, detail="规则不存在")
    
    db.delete(rule)
    db.commit()
    
    return None


@router.get("/templates", response_model=List[RuleTemplateResponse])
def get_rule_templates(
    rule_type: str = None,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """获取规则模板列表"""
    query = db.query(RuleTemplate)
    
    if rule_type:
        query = query.filter(RuleTemplate.rule_type == rule_type)
    
    templates = query.order_by(RuleTemplate.category, RuleTemplate.priority.desc()).all()
    return templates


@router.post("/templates/{template_id}/apply", response_model=ExtractionRuleResponse, status_code=status.HTTP_201_CREATED)
def apply_rule_template(
    template_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """应用规则模板（创建规则）"""
    template = db.query(RuleTemplate).filter(RuleTemplate.id == template_id).first()
    
    if not template:
        raise HTTPException(status_code=404, detail="模板不存在")
    
    # 基于模板创建规则
    rule = ExtractionRule(
        user_id=current_user.id,
        name=template.name,
        rule_type=template.rule_type,
        pattern=template.pattern,
        extract_group=template.extract_group,
        priority=template.priority,
        is_active=True,
        description=template.description
    )
    
    db.add(rule)
    db.commit()
    db.refresh(rule)
    
    return rule
