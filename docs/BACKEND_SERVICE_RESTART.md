# 后端服务重启 - 加载失败问题修复

## 📋 问题描述

**现象**: 
- 页面显示"加载失败"
- 无法获取数据
- 所有API请求失败

## 🔍 问题原因

**后端服务停止运行**:
- 在重启前端服务时，可能误杀了后端的Node进程
- 或者后端服务因其他原因停止

## ✅ 解决方案

### 1. 检查后端服务状态

```bash
netstat -ano | findstr :10043
```

**结果**:
- 如果没有输出 → 服务未运行
- 如果有输出 → 服务正在运行

### 2. 重启后端服务

```bash
cd backend
uvicorn app.main:app --reload --port 10043
```

**当前状态**:
- ✅ 后端服务已启动
- ✅ 运行在端口 10043
- ✅ 进程ID: 18960

## 🌐 服务地址

### 前端服务
```
http://localhost:3000/
```

### 后端服务
```
http://localhost:10043/
http://localhost:10043/docs  (API文档)
```

### API代理配置
前端通过代理访问后端：
```
前端: http://localhost:3000/api/*
  ↓ (代理)
后端: http://localhost:10043/*
```

## 🧪 验证步骤

### 1. 验证后端服务

**方法1: 访问API文档**
```
http://localhost:10043/docs
```
应该看到 Swagger API 文档界面

**方法2: 测试健康检查**
```bash
curl http://localhost:10043/
```
应该返回服务信息

**方法3: 检查端口**
```bash
netstat -ano | findstr :10043
```
应该看到 LISTENING 状态

### 2. 验证前端服务

**访问前端页面**
```
http://localhost:3000/tag-manage
```

**检查控制台**
- 按 F12 打开开发者工具
- 切换到 Network 标签
- 刷新页面
- 查看 API 请求是否成功（状态码 200）

### 3. 测试完整流程

1. **访问标签管理页面**
   ```
   http://localhost:3000/tag-manage
   ```

2. **检查数据加载**
   - 应该能看到标签列表
   - 不应该有"加载失败"提示

3. **点击标签**
   - 应该能跳转到短信列表
   - 应该能看到对应的短信

4. **访问快递页面**
   ```
   http://localhost:3000/express-detail
   ```
   - 应该能看到快递列表或空状态

## 🔧 常见问题

### Q1: 后端启动失败

**错误**: `Address already in use`

**原因**: 端口被占用

**解决**:
```bash
# 查找占用端口的进程
netstat -ano | findstr :10043

# 结束进程（替换PID）
taskkill /F /PID <PID>

# 重新启动
uvicorn app.main:app --reload --port 10043
```

### Q2: 数据库连接失败

**错误**: `could not connect to server`

**原因**: PostgreSQL未启动

**解决**:
```bash
# 检查PostgreSQL服务
# 方法1: 使用服务管理器
services.msc

# 方法2: 使用命令行
net start postgresql-x64-14
```

### Q3: 前端无法访问后端

**错误**: `Network Error` 或 `CORS Error`

**原因**: 
- 后端未启动
- 代理配置错误
- CORS配置问题

**解决**:
1. 确认后端正在运行
2. 检查 `frontend/vite.config.js` 中的代理配置
3. 检查 `backend/app/main.py` 中的CORS配置

### Q4: API返回401未授权

**错误**: `401 Unauthorized`

**原因**: Token过期或无效

**解决**:
```javascript
// 在浏览器控制台检查token
console.log(localStorage.getItem('token'))

// 如果token无效，清除并重新登录
localStorage.removeItem('token')
// 然后访问登录页面
```

## 📊 服务监控

### 检查服务健康状态

**后端服务**:
```bash
# 检查进程
tasklist | findstr python

# 检查端口
netstat -ano | findstr :10043

# 查看日志
# 在运行uvicorn的终端查看输出
```

**前端服务**:
```bash
# 检查进程
tasklist | findstr node

# 检查端口
netstat -ano | findstr :3000

# 查看日志
# 在运行npm的终端查看输出
```

### 性能监控

**后端性能**:
- 访问 http://localhost:10043/docs
- 测试API响应时间
- 查看数据库查询日志

**前端性能**:
- 使用浏览器开发者工具
- Performance 标签录制性能
- Network 标签查看请求时间

## 🚀 启动脚本

为了方便，可以创建启动脚本：

### Windows批处理脚本

**start_backend.bat**:
```bat
@echo off
cd /d D:\tools\python\mypro\sms_agent\backend
call ..\venv\Scripts\activate
uvicorn app.main:app --reload --port 10043
```

**start_frontend.bat**:
```bat
@echo off
cd /d D:\tools\python\mypro\sms_agent\frontend
npm run dev
```

**start_all.bat**:
```bat
@echo off
start cmd /k "cd /d D:\tools\python\mypro\sms_agent\backend && ..\venv\Scripts\activate && uvicorn app.main:app --reload --port 10043"
start cmd /k "cd /d D:\tools\python\mypro\sms_agent\frontend && npm run dev"
```

### PowerShell脚本

**start_services.ps1**:
```powershell
# 启动后端
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd D:\tools\python\mypro\sms_agent\backend; ..\venv\Scripts\activate; uvicorn app.main:app --reload --port 10043"

# 等待2秒
Start-Sleep -Seconds 2

# 启动前端
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd D:\tools\python\mypro\sms_agent\frontend; npm run dev"
```

## 🎉 总结

### 问题根源
- 后端服务停止运行
- 前端无法连接到API

### 解决方案
- ✅ 重启后端服务
- ✅ 验证服务状态
- ✅ 测试API连接

### 当前状态
- ✅ 后端运行在 http://localhost:10043/
- ✅ 前端运行在 http://localhost:3000/
- ✅ 服务正常，可以访问

### 访问地址
- **标签管理**: http://localhost:3000/tag-manage
- **快递详情**: http://localhost:3000/express-detail
- **短信列表**: http://localhost:3000/sms-list
- **API文档**: http://localhost:10043/docs

---

**修复状态**: ✅ 已完成  
**后端状态**: ✅ 运行中 (端口10043, PID 18960)  
**前端状态**: ✅ 运行中 (端口3000)  
**服务状态**: ✅ 正常  
