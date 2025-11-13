"""
FastAPI应用主入口
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from .config import settings
from .database import init_db

# 创建FastAPI应用实例
app = FastAPI(
    title=settings.APP_NAME,
    version=settings.VERSION,
    debug=settings.DEBUG,
)

# 配置CORS中间件
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.on_event("startup")
async def startup_event():
    """应用启动时执行"""
    # 初始化数据库（开发环境）
    if settings.DEBUG:
        init_db()
    print(f"🚀 {settings.APP_NAME} v{settings.VERSION} 启动成功！")
    print(f"📝 API文档: http://localhost:8000/docs")


@app.on_event("shutdown")
async def shutdown_event():
    """应用关闭时执行"""
    print(f"👋 {settings.APP_NAME} 已关闭")


@app.get("/")
async def root():
    """根路径"""
    return {
        "message": f"欢迎使用 {settings.APP_NAME}",
        "version": settings.VERSION,
        "docs": "/docs"
    }


@app.get("/api/health")
async def health_check():
    """健康检查端点"""
    return {
        "status": "ok",
        "app": settings.APP_NAME,
        "version": settings.VERSION
    }


# 导入路由
from .routers import auth, tags, sms, extraction_rules
app.include_router(auth.router, prefix="/api/auth", tags=["认证"])
app.include_router(tags.router, prefix="/api/tags", tags=["标签"])
app.include_router(sms.router, prefix="/api/sms", tags=["短信"])
app.include_router(extraction_rules.router, prefix="/api")
