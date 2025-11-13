"""
认证相关的依赖注入
"""
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from sqlalchemy.orm import Session
from ..database import get_db
from ..models.user import User
from ..utils.security import decode_access_token
from ..schemas.auth import TokenData

# OAuth2密码流
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/api/auth/login")


async def get_current_user(
    token: str = Depends(oauth2_scheme),
    db: Session = Depends(get_db)
) -> User:
    """
    获取当前登录用户
    
    Args:
        token: JWT token
        db: 数据库会话
    
    Returns:
        User: 当前用户对象
    
    Raises:
        HTTPException: 如果token无效或用户不存在
    """
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="无法验证凭据",
        headers={"WWW-Authenticate": "Bearer"},
    )
    
    # 解码token
    payload = decode_access_token(token)
    if payload is None:
        raise credentials_exception
    
    # 获取用户ID（确保类型转换）
    user_id_str = payload.get("sub")
    if user_id_str is None:
        raise credentials_exception
    
    try:
        user_id = int(user_id_str)
    except (ValueError, TypeError):
        raise credentials_exception
    
    # 从数据库获取用户
    user = db.query(User).filter(User.id == user_id).first()
    if user is None:
        raise credentials_exception
    
    return user


async def get_current_active_user(
    current_user: User = Depends(get_current_user)
) -> User:
    """
    获取当前活跃用户（预留，用于后续添加用户状态检查）
    
    Args:
        current_user: 当前用户
    
    Returns:
        User: 当前活跃用户
    """
    # 这里可以添加用户状态检查，如是否被禁用等
    # if current_user.is_disabled:
    #     raise HTTPException(status_code=400, detail="用户已被禁用")
    
    return current_user
