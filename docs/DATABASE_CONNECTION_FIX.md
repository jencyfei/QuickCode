# 数据库连接失败问题修复

## 📋 问题描述

**错误信息**:
```
sqlalchemy.exc.OperationalError: (psycopg2.OperationalError)
```

**现象**:
- 启动后端服务失败
- 前端页面空白
- 无法连接到 PostgreSQL 数据库

## 🔍 问题原因

**缺少 `.env` 配置文件**

后端需要 `.env` 文件来配置数据库连接信息，但该文件不存在。

## ✅ 解决方案

### 方案1: 创建 .env 文件（推荐）

在 `backend` 目录下创建 `.env` 文件：

**文件位置**: `d:\tools\python\mypro\sms_agent\backend\.env`

**文件内容**:
```env
# 数据库配置
DATABASE_URL=postgresql://postgres:your_password@localhost:5432/sms_agent

# JWT密钥（随机生成）
SECRET_KEY=your-secret-key-here-change-in-production

# 调试模式
DEBUG=True

# CORS配置
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

**重要**: 
- 将 `your_password` 替换为你的 PostgreSQL 密码
- 将 `your-secret-key-here-change-in-production` 替换为随机字符串

### 方案2: 使用环境变量

如果不想创建 `.env` 文件，可以设置环境变量：

**Windows PowerShell**:
```powershell
$env:DATABASE_URL="postgresql://postgres:your_password@localhost:5432/sms_agent"
$env:SECRET_KEY="your-secret-key-here"
```

**Windows CMD**:
```cmd
set DATABASE_URL=postgresql://postgres:your_password@localhost:5432/sms_agent
set SECRET_KEY=your-secret-key-here
```

## 🔧 详细步骤

### 步骤1: 确认 PostgreSQL 运行

```bash
# 检查 PostgreSQL 服务状态
sc query postgresql-x64-14

# 如果未运行，启动服务
net start postgresql-x64-14
```

### 步骤2: 创建数据库

```bash
# 使用 psql 连接
psql -U postgres

# 创建数据库
CREATE DATABASE sms_agent;

# 退出
\q
```

### 步骤3: 创建 .env 文件

创建 `backend\.env` 文件并填入配置：

```env
DATABASE_URL=postgresql://postgres:your_password@localhost:5432/sms_agent
SECRET_KEY=09d25e094faa6ca2556c818166b7a9563b93f7099f6f0f4caa6cf63b88e8d3e7
DEBUG=True
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

### 步骤4: 生成安全的 SECRET_KEY

**Python 方式**:
```python
import secrets
print(secrets.token_hex(32))
```

**在线生成**:
访问 https://generate-secret.vercel.app/32

### 步骤5: 重启后端服务

```bash
# 停止当前服务（如果在运行）
Ctrl+C

# 重新启动
cd backend
uvicorn app.main:app --reload --port 10043
```

或使用启动脚本：
```bash
start_backend.bat
```

## 📝 .env 文件模板

创建 `backend\.env.example` 作为模板：

```env
# 数据库配置
# 格式: postgresql://用户名:密码@主机:端口/数据库名
DATABASE_URL=postgresql://postgres:password@localhost:5432/sms_agent

# JWT密钥（用于生成访问令牌）
# 生成方式: python -c "import secrets; print(secrets.token_hex(32))"
SECRET_KEY=your-secret-key-here-change-in-production

# 调试模式（生产环境设为 False）
DEBUG=True

# CORS允许的源（多个用逗号分隔）
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# 日志级别
LOG_LEVEL=INFO
```

## 🔍 验证配置

### 检查 .env 文件是否存在

```bash
cd backend
dir .env
```

应该看到文件列表中有 `.env`

### 测试数据库连接

创建测试脚本 `backend/test_db.py`:

```python
from app.config import settings
from sqlalchemy import create_engine, text

print("数据库URL:", settings.DATABASE_URL)

try:
    engine = create_engine(settings.DATABASE_URL)
    with engine.connect() as conn:
        result = conn.execute(text("SELECT version()"))
        print("✅ 数据库连接成功!")
        print("PostgreSQL 版本:", result.fetchone()[0])
except Exception as e:
    print("❌ 数据库连接失败:", e)
```

运行测试:
```bash
cd backend
python test_db.py
```

## 🐛 常见问题

### Q1: 找不到 PostgreSQL 服务

**错误**: `sc query postgresql-x64-14` 返回错误

**解决**:
1. 检查 PostgreSQL 是否已安装
2. 服务名称可能不同，查看所有服务：
   ```bash
   sc query | findstr postgres
   ```
3. 如果未安装，下载并安装 PostgreSQL

### Q2: 密码错误

**错误**: `password authentication failed`

**解决**:
1. 确认 PostgreSQL 密码
2. 重置密码：
   ```bash
   # 以管理员身份运行 psql
   psql -U postgres
   
   # 修改密码
   ALTER USER postgres PASSWORD 'new_password';
   ```
3. 更新 `.env` 文件中的密码

### Q3: 数据库不存在

**错误**: `database "sms_agent" does not exist`

**解决**:
```bash
# 连接到 PostgreSQL
psql -U postgres

# 创建数据库
CREATE DATABASE sms_agent;

# 验证
\l
```

### Q4: 端口被占用

**错误**: `port 5432 is already in use`

**解决**:
1. 检查是否有其他 PostgreSQL 实例运行
2. 修改 `.env` 中的端口号
3. 或停止其他实例

### Q5: .env 文件不生效

**可能原因**:
1. 文件编码不是 UTF-8
2. 文件位置错误
3. 环境变量格式错误

**解决**:
1. 确保文件保存为 UTF-8 编码
2. 确认文件在 `backend` 目录下
3. 检查没有多余的空格或引号

## 📊 配置检查清单

使用此清单逐项检查：

- [ ] PostgreSQL 服务正在运行
- [ ] 数据库 `sms_agent` 已创建
- [ ] `.env` 文件存在于 `backend` 目录
- [ ] `.env` 文件包含 `DATABASE_URL`
- [ ] `.env` 文件包含 `SECRET_KEY`
- [ ] 数据库密码正确
- [ ] 数据库端口正确（默认 5432）
- [ ] 可以手动连接数据库
- [ ] 后端服务可以启动

## 🎯 快速修复命令

```bash
# 1. 进入 backend 目录
cd d:\tools\python\mypro\sms_agent\backend

# 2. 创建 .env 文件（使用记事本）
notepad .env

# 3. 粘贴以下内容（修改密码）
DATABASE_URL=postgresql://postgres:YOUR_PASSWORD@localhost:5432/sms_agent
SECRET_KEY=09d25e094faa6ca2556c818166b7a9563b93f7099f6f0f4caa6cf63b88e8d3e7
DEBUG=True
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# 4. 保存并关闭

# 5. 测试连接
python test_db.py

# 6. 启动服务
uvicorn app.main:app --reload --port 10043
```

## 🎉 验证成功

如果配置正确，应该看到：

```
INFO:     Uvicorn running on http://127.0.0.1:10043
INFO:     Application startup complete.
```

访问 http://localhost:10043/docs 应该能看到 API 文档。

访问 http://localhost:3000/ 应该能看到前端页面。

---

**问题状态**: ⏳ 待修复  
**优先级**: 🔴 高  
**预计时间**: 5-10分钟  
