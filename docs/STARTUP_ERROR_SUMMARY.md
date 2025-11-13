# 启动错误问题总结

## 📋 问题描述

**错误**: `sqlalchemy.exc.OperationalError: (psycopg2.OperationalError)`

**现象**:
- 运行 `start_simple.bat` 或 `start_all.bat` 后
- 后端服务启动失败
- 前端页面 http://localhost:3000/ 空白
- 控制台显示数据库连接错误

## 🔍 根本原因

**缺少 `.env` 配置文件**

后端应用需要 `.env` 文件来读取数据库连接信息和其他配置，但该文件不存在。

## ✅ 解决方案

### 方案1: 快速手动创建（推荐）

#### 1. 创建 .env 文件

```bash
cd d:\tools\python\mypro\sms_agent\backend
notepad .env
```

#### 2. 粘贴配置内容

```env
DATABASE_URL=postgresql://postgres:YOUR_PASSWORD@localhost:5432/sms_agent
SECRET_KEY=09d25e094faa6ca2556c818166b7a9563b93f7099f6f0f4caa6cf63b88e8d3e7
DEBUG=True
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
LOG_LEVEL=INFO
```

**重要**: 将 `YOUR_PASSWORD` 替换为你的 PostgreSQL 密码

#### 3. 保存文件

按 `Ctrl+S` 保存，关闭记事本

#### 4. 创建数据库

```bash
# 连接到 PostgreSQL
psql -U postgres

# 创建数据库
CREATE DATABASE sms_agent;

# 退出
\q
```

#### 5. 测试连接

```bash
cd backend
python test_db_connection.py
```

应该看到：
```
✅ 数据库连接成功!
```

#### 6. 重启服务

```bash
start_all.bat
```

### 方案2: 使用配置向导

运行自动配置脚本：

```bash
setup_env.bat
```

该脚本会：
- 检查 PostgreSQL 服务
- 引导你输入数据库密码
- 自动创建数据库
- 生成 .env 文件
- 测试连接

### 方案3: 复制示例文件

```bash
cd backend
copy .env.example .env
notepad .env
```

修改以下内容：
- `DATABASE_URL` 中的密码
- `sms_tagger` 改为 `sms_agent`（如果需要）

## 🔧 前置条件检查

### 1. PostgreSQL 已安装

```bash
psql --version
```

应该显示版本号，如：`psql (PostgreSQL) 14.x`

### 2. PostgreSQL 服务运行中

```bash
sc query postgresql-x64-14
```

应该显示 `STATE: RUNNING`

如果未运行：
```bash
net start postgresql-x64-14
```

### 3. 知道 PostgreSQL 密码

默认用户名是 `postgres`，密码是安装时设置的。

如果忘记密码，需要重置：
1. 找到 `pg_hba.conf` 文件
2. 临时改为 `trust` 认证
3. 重启 PostgreSQL
4. 修改密码
5. 改回 `md5` 认证

## 📊 配置文件说明

### .env 文件结构

```env
# 数据库连接字符串
# 格式: postgresql://用户名:密码@主机:端口/数据库名
DATABASE_URL=postgresql://postgres:password@localhost:5432/sms_agent

# JWT 密钥（用于生成访问令牌）
SECRET_KEY=随机字符串

# 调试模式（开发环境为 True，生产环境为 False）
DEBUG=True

# CORS 允许的源（前端地址）
ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# 日志级别
LOG_LEVEL=INFO
```

### 各字段说明

| 字段 | 说明 | 示例 |
|------|------|------|
| DATABASE_URL | 数据库连接字符串 | `postgresql://postgres:password@localhost:5432/sms_agent` |
| SECRET_KEY | JWT 密钥 | 随机生成的长字符串 |
| DEBUG | 调试模式 | `True` 或 `False` |
| ALLOWED_ORIGINS | CORS 配置 | `http://localhost:3000` |
| LOG_LEVEL | 日志级别 | `INFO`, `DEBUG`, `WARNING` |

## 🧪 测试步骤

### 1. 测试数据库连接

```bash
cd backend
python test_db_connection.py
```

**成功输出**:
```
==================================================
  数据库连接测试
==================================================

数据库URL: postgresql://postgres:****@localhost:5432/sms_agent

正在连接...

✅ 数据库连接成功!

数据库名称: sms_agent
PostgreSQL 版本: PostgreSQL 14.x

已存在的表 (5):
  - sms_messages
  - sms_tags
  - tags
  - users
  - ...

==================================================
  测试完成
==================================================
```

### 2. 测试后端启动

```bash
cd backend
uvicorn app.main:app --reload --port 10043
```

**成功输出**:
```
INFO:     Uvicorn running on http://127.0.0.1:10043
INFO:     Application startup complete.
```

### 3. 测试 API 访问

访问: http://localhost:10043/docs

应该看到 Swagger API 文档界面

### 4. 测试前端访问

访问: http://localhost:3000/

应该看到登录页面或主界面

## 🐛 常见错误

### 错误1: 找不到 .env 文件

**错误信息**:
```
pydantic_core._pydantic_core.ValidationError: 1 validation error for Settings
DATABASE_URL
  Field required
```

**解决**: 创建 `.env` 文件

### 错误2: 数据库密码错误

**错误信息**:
```
psycopg2.OperationalError: FATAL: password authentication failed for user "postgres"
```

**解决**: 
1. 确认 PostgreSQL 密码
2. 更新 `.env` 文件中的密码

### 错误3: 数据库不存在

**错误信息**:
```
psycopg2.OperationalError: FATAL: database "sms_agent" does not exist
```

**解决**:
```bash
psql -U postgres -c "CREATE DATABASE sms_agent;"
```

### 错误4: PostgreSQL 未运行

**错误信息**:
```
psycopg2.OperationalError: could not connect to server
```

**解决**:
```bash
net start postgresql-x64-14
```

### 错误5: 端口被占用

**错误信息**:
```
OSError: [WinError 10048] 通常每个套接字地址只允许使用一次
```

**解决**:
```bash
# 查找占用端口的进程
netstat -ano | findstr :10043

# 终止进程
taskkill /F /PID <PID>
```

## 📝 检查清单

启动前请确认：

- [ ] PostgreSQL 已安装
- [ ] PostgreSQL 服务正在运行
- [ ] 知道 PostgreSQL 密码
- [ ] 数据库 `sms_agent` 已创建
- [ ] `.env` 文件存在于 `backend` 目录
- [ ] `.env` 文件包含正确的数据库密码
- [ ] 可以运行 `test_db_connection.py` 成功
- [ ] 端口 10043 和 3000 未被占用

## 🎯 完整启动流程

```bash
# 1. 确保 PostgreSQL 运行
net start postgresql-x64-14

# 2. 创建数据库（首次）
psql -U postgres -c "CREATE DATABASE sms_agent;"

# 3. 配置环境（首次）
cd d:\tools\python\mypro\sms_agent\backend
copy .env.example .env
notepad .env
# 修改密码后保存

# 4. 测试连接
python test_db_connection.py

# 5. 启动服务
cd ..
start_all.bat

# 6. 访问应用
# 前端: http://localhost:3000/
# 后端: http://localhost:10043/docs
```

## 📚 相关文档

- [快速修复指南](../QUICK_FIX.md)
- [详细修复文档](DATABASE_CONNECTION_FIX.md)
- [启动脚本说明](../README_SCRIPTS.md)

## 🎉 成功标志

如果一切正常，你应该看到：

1. **后端控制台**:
   ```
   INFO:     Uvicorn running on http://127.0.0.1:10043
   INFO:     Application startup complete.
   ```

2. **前端控制台**:
   ```
   VITE v7.x.x  ready in xxx ms
   ➜  Local:   http://localhost:3000/
   ```

3. **浏览器**:
   - http://localhost:3000/ 显示登录页面
   - http://localhost:10043/docs 显示 API 文档

---

**问题**: 数据库连接失败  
**原因**: 缺少 .env 配置文件  
**解决**: 创建 .env 文件并配置数据库连接  
**状态**: ✅ 已提供解决方案  
**预计时间**: 2-5 分钟  
