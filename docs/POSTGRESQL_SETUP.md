# PostgreSQL 设置指南

## 📋 前置要求

- Windows 操作系统
- 管理员权限

## 🚀 安装步骤

### 方法1: 使用官方安装包（推荐）

#### 1. 下载 PostgreSQL

访问官方网站下载：
- 官网: https://www.postgresql.org/download/windows/
- 推荐版本: PostgreSQL 15 或 16

或使用 EDB 安装器：
- https://www.enterprisedb.com/downloads/postgres-postgresql-downloads

#### 2. 安装 PostgreSQL

运行安装程序，按照以下步骤：

1. **选择组件**（全部勾选）：
   - PostgreSQL Server
   - pgAdmin 4（图形化管理工具）
   - Stack Builder
   - Command Line Tools

2. **设置数据目录**：
   - 默认即可：`C:\Program Files\PostgreSQL\15\data`

3. **设置超级用户密码**：
   - ⚠️ **重要**：记住这个密码！
   - 建议使用简单密码用于开发：`postgres` 或 `123456`

4. **设置端口**：
   - 默认：`5432`

5. **选择区域设置**：
   - 默认：`Chinese (Simplified), China`

6. 完成安装

#### 3. 验证安装

打开命令行（以管理员身份）：

```bash
# 检查 PostgreSQL 服务状态
sc query postgresql-x64-15

# 或使用 PowerShell
Get-Service -Name postgresql-x64-15
```

应该看到服务状态为 `RUNNING`

---

### 方法2: 使用 Chocolatey（命令行安装）

如果你已安装 Chocolatey：

```bash
choco install postgresql
```

---

## 🔧 配置数据库

### 1. 启动 PostgreSQL 服务

如果服务未运行：

```bash
# 启动服务
net start postgresql-x64-15

# 或使用 PowerShell
Start-Service postgresql-x64-15
```

### 2. 连接到 PostgreSQL

#### 方法A: 使用 psql 命令行

```bash
# 切换到 PostgreSQL bin 目录
cd "C:\Program Files\PostgreSQL\15\bin"

# 连接到数据库
psql -U postgres

# 输入之前设置的密码
```

#### 方法B: 使用 pgAdmin 4

1. 打开 pgAdmin 4（开始菜单搜索）
2. 输入主密码（首次使用需要设置）
3. 展开 Servers -> PostgreSQL 15
4. 输入密码连接

### 3. 创建项目数据库

在 psql 或 pgAdmin 的 SQL 查询窗口中执行：

```sql
-- 创建数据库
CREATE DATABASE sms_tagger;

-- 验证创建成功
\l
-- 或在 pgAdmin 中刷新数据库列表
```

你应该看到 `sms_tagger` 数据库已创建。

### 4. 创建专用用户（可选，推荐）

```sql
-- 创建用户
CREATE USER sms_user WITH PASSWORD 'sms_password_123';

-- 授予权限
GRANT ALL PRIVILEGES ON DATABASE sms_tagger TO sms_user;

-- 切换到 sms_tagger 数据库
\c sms_tagger

-- 授予 schema 权限
GRANT ALL ON SCHEMA public TO sms_user;
```

---

## ⚙️ 配置项目环境变量

编辑 `backend/.env` 文件：

### 使用 postgres 超级用户（简单）

```env
DATABASE_URL=postgresql://postgres:你的密码@localhost:5432/sms_tagger
```

### 使用专用用户（推荐）

```env
DATABASE_URL=postgresql://sms_user:sms_password_123@localhost:5432/sms_tagger
```

### 完整配置示例

```env
# 数据库配置
DATABASE_URL=postgresql://postgres:postgres@localhost:5432/sms_tagger

# JWT配置
SECRET_KEY=your-super-secret-key-change-this-in-production-abc123xyz
ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=30

# 应用配置
APP_NAME=Smart SMS Tagger
DEBUG=True
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# 日志配置
LOG_LEVEL=INFO
```

---

## 🧪 测试数据库连接

### 方法1: 使用 Python 脚本

创建测试文件 `test_db_connection.py`：

```python
import psycopg2

try:
    conn = psycopg2.connect(
        host="localhost",
        port=5432,
        database="sms_tagger",
        user="postgres",
        password="你的密码"
    )
    print("✅ 数据库连接成功！")
    conn.close()
except Exception as e:
    print(f"❌ 数据库连接失败: {e}")
```

运行：
```bash
python test_db_connection.py
```

### 方法2: 使用 psql

```bash
psql -U postgres -d sms_tagger -c "SELECT version();"
```

---

## 🔄 初始化数据库表

配置好 `.env` 后，运行初始化脚本：

```bash
cd d:\tools\python\mypro\sms_agent
python scripts\init_db.py
```

你应该看到：
```
🔧 开始初始化数据库...
✅ 数据库表创建成功！

已创建的表:
  - users (用户表)
  - sms_messages (短信表)
  - tags (标签表)
  - sms_tags (短信-标签关联表)
```

---

## 🚀 启动后端服务

```bash
cd backend
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

成功启动后访问：
- API文档: http://localhost:8000/docs
- 健康检查: http://localhost:8000/api/health

---

## 🔧 常见问题

### 问题1: 服务未启动

**错误**: `connection refused`

**解决**:
```bash
# 检查服务状态
Get-Service postgresql-x64-15

# 启动服务
Start-Service postgresql-x64-15
```

### 问题2: 密码认证失败

**错误**: `password authentication failed`

**解决**:
1. 确认密码正确
2. 检查 `.env` 文件中的连接字符串
3. 重置 postgres 密码（如果忘记）

### 问题3: 端口被占用

**错误**: `port 5432 already in use`

**解决**:
1. 检查是否有其他 PostgreSQL 实例运行
2. 修改 PostgreSQL 端口（不推荐）

### 问题4: 权限不足

**错误**: `permission denied`

**解决**:
```sql
-- 授予用户权限
GRANT ALL PRIVILEGES ON DATABASE sms_tagger TO postgres;
```

---

## 📚 有用的 PostgreSQL 命令

```sql
-- 列出所有数据库
\l

-- 连接到数据库
\c sms_tagger

-- 列出所有表
\dt

-- 查看表结构
\d users

-- 查看表数据
SELECT * FROM users;

-- 退出 psql
\q
```

---

## 🆘 获取帮助

- PostgreSQL 官方文档: https://www.postgresql.org/docs/
- pgAdmin 文档: https://www.pgadmin.org/docs/
- 中文教程: https://www.runoob.com/postgresql/postgresql-tutorial.html
