# Smart SMS Tagger - Backend

基于 FastAPI 的后端服务

## 技术栈

- Python 3.9+
- FastAPI
- PostgreSQL
- SQLAlchemy
- Pydantic
- JWT认证

## 项目结构

```
backend/
├── app/
│   ├── __init__.py
│   ├── main.py              # FastAPI应用入口
│   ├── config.py            # 配置管理
│   ├── database.py          # 数据库连接
│   ├── models/              # SQLAlchemy模型
│   │   ├── __init__.py
│   │   ├── user.py
│   │   ├── sms.py
│   │   └── tag.py
│   ├── schemas/             # Pydantic模式
│   │   ├── __init__.py
│   │   ├── auth.py
│   │   ├── sms.py
│   │   └── tag.py
│   ├── routers/             # API路由
│   │   ├── __init__.py
│   │   ├── auth.py
│   │   ├── sms.py
│   │   └── tags.py
│   ├── services/            # 业务逻辑
│   │   ├── __init__.py
│   │   └── sms_parser.py
│   ├── utils/               # 工具函数
│   │   ├── __init__.py
│   │   └── security.py
│   └── dependencies/        # 依赖注入
│       ├── __init__.py
│       └── auth.py
├── tests/                   # 测试文件
├── requirements.txt         # 依赖列表
├── .env.example            # 环境变量示例
└── README.md
```

## 安装依赖

```bash
cd backend
python -m venv venv
venv\Scripts\activate  # Windows
pip install -r requirements.txt
```

## 运行开发服务器

```bash
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

## 环境变量

复制 `.env.example` 为 `.env` 并配置：

```
DATABASE_URL=postgresql://user:password@localhost:5432/sms_tagger
SECRET_KEY=your-secret-key-here
ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=30
```
