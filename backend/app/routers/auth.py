"""
认证相关API路由
"""
from datetime import timedelta
from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.orm import Session
from ..database import get_db
from ..models.user import User
from ..schemas.auth import UserRegister, UserLogin, Token, UserResponse, UserProfile
from ..utils.security import verify_password, get_password_hash, create_access_token
from ..dependencies.auth import get_current_active_user
from ..config import settings

router = APIRouter()


@router.post("/register", response_model=UserResponse, status_code=status.HTTP_201_CREATED)
async def register(user_data: UserRegister, db: Session = Depends(get_db)):
    """
    用户注册
    
    - **email**: 邮箱（可选，与phone二选一）
    - **phone**: 手机号（可选，与email二选一）
    - **password**: 密码（至少6位）
    """
    # 验证至少提供一个登录方式
    if not user_data.email and not user_data.phone:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="必须提供邮箱或手机号"
        )
    
    # 检查邮箱是否已存在
    if user_data.email:
        existing_user = db.query(User).filter(User.email == user_data.email).first()
        if existing_user:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="该邮箱已被注册"
            )
    
    # 检查手机号是否已存在
    if user_data.phone:
        existing_user = db.query(User).filter(User.phone == user_data.phone).first()
        if existing_user:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="该手机号已被注册"
            )
    
    # 创建新用户
    hashed_password = get_password_hash(user_data.password)
    new_user = User(
        email=user_data.email,
        phone=user_data.phone,
        password_hash=hashed_password
    )
    
    db.add(new_user)
    db.commit()
    db.refresh(new_user)
    
    return new_user


@router.post("/login", response_model=Token)
async def login(
    form_data: OAuth2PasswordRequestForm = Depends(),
    db: Session = Depends(get_db)
):
    """
    用户登录
    
    使用邮箱或手机号登录，返回JWT token
    
    - **username**: 邮箱或手机号
    - **password**: 密码
    """
    # 尝试通过邮箱查找用户
    user = db.query(User).filter(User.email == form_data.username).first()
    
    # 如果邮箱未找到，尝试通过手机号查找
    if not user:
        user = db.query(User).filter(User.phone == form_data.username).first()
    
    # 验证用户和密码
    if not user or not verify_password(form_data.password, user.password_hash):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="用户名或密码错误",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    # 创建访问令牌
    access_token_expires = timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = create_access_token(
        data={"sub": str(user.id)},  # JWT标准要求sub必须是字符串
        expires_delta=access_token_expires
    )
    
    return {
        "access_token": access_token,
        "token_type": "bearer",
        "expires_in": settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60
    }


@router.get("/me", response_model=UserProfile)
async def get_current_user_info(
    current_user: User = Depends(get_current_active_user)
):
    """
    获取当前登录用户信息
    
    需要在请求头中携带有效的JWT token
    """
    return current_user


@router.post("/logout")
async def logout(current_user: User = Depends(get_current_active_user)):
    """
    用户登出
    
    注意：JWT是无状态的，实际的登出需要在客户端删除token
    这个端点主要用于记录登出日志或执行其他清理操作
    """
    return {"message": "登出成功"}
