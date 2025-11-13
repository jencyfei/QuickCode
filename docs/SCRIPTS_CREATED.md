# 启动脚本创建完成

## 📦 已创建的文件

### 1. 启动脚本（项目根目录）

| 文件名 | 功能 | 位置 |
|--------|------|------|
| `start_all.bat` | 一键启动前后端服务 | `/start_all.bat` |
| `check_services.bat` | 检查服务运行状态 | `/check_services.bat` |
| `start_backend.bat` | 启动后端服务 | `/start_backend.bat` |
| `start_frontend.bat` | 启动前端服务 | `/start_frontend.bat` |
| `stop_all.bat` | 停止所有服务 | `/stop_all.bat` |

### 2. 文档文件

| 文件名 | 说明 | 位置 |
|--------|------|------|
| `README_SCRIPTS.md` | 详细使用说明 | `/README_SCRIPTS.md` |
| `QUICK_START.md` | 快速启动指南 | `/QUICK_START.md` |

## 🎯 核心功能

### start_all.bat - 一键启动

**特点**:
- ✅ 自动检查端口占用（10043, 3000）
- ✅ 自动释放被占用的端口
- ✅ 按顺序启动后端和前端
- ✅ 在独立窗口中运行服务
- ✅ 显示访问地址和使用提示

**使用**:
```bash
双击 start_all.bat
```

**输出**:
```
========================================
  SMS Agent 一键启动脚本
========================================

[1/4] 检查端口占用...
✅ 端口检查完成

[2/4] 启动后端服务...
✅ 后端服务启动命令已执行
   访问地址: http://localhost:10043/
   API文档: http://localhost:10043/docs

[3/4] 等待后端服务启动...
✅ 等待完成

[4/4] 启动前端服务...
✅ 前端服务启动命令已执行
   访问地址: http://localhost:3000/

========================================
  🎉 所有服务启动完成！
========================================

📱 前端地址: http://localhost:3000/
🔧 后端地址: http://localhost:10043/
📚 API文档: http://localhost:10043/docs
```

### check_services.bat - 服务检查

**检查项目**:
1. **端口检查**
   - 后端端口 10043
   - 前端端口 3000
   - 显示PID

2. **进程检查**
   - Python 进程
   - Node.js 进程

3. **URL访问测试**
   - 后端API可访问性
   - 前端页面可访问性
   - HTTP状态码

4. **数据库检查**
   - PostgreSQL 服务状态

**使用**:
```bash
双击 check_services.bat
```

**示例输出**:
```
========================================
  SMS Agent 服务检查脚本
========================================

[检查时间] 2025-11-07 17:00:00

========================================
  1. 端口检查
========================================

[后端服务 - 端口 10043]
  状态: ✅ 运行中
  PID: 18960

[前端服务 - 端口 3000]
  状态: ✅ 运行中
  PID: 12345

========================================
  2. 进程检查
========================================

[Python 进程]
  ✅ 发现 Python 进程
  python.exe    18960    ...

[Node.js 进程]
  ✅ 发现 Node.js 进程
  node.exe      12345    ...

========================================
  3. URL 访问测试
========================================

[后端 API]
  测试地址: http://localhost:10043/docs
  状态: ✅ 可访问 (HTTP 200)

[前端页面]
  测试地址: http://localhost:3000/
  状态: ✅ 可访问 (HTTP 200)

========================================
  4. 数据库连接检查
========================================

[PostgreSQL 服务]
  状态: ✅ 运行中

========================================
  📊 总结报告
========================================

  后端服务: ✅ 运行中
  前端服务: ✅ 运行中
  后端URL: ✅ 可访问
  前端URL: ✅ 可访问

  🎉 所有服务运行正常！

  📱 访问地址:
     前端: http://localhost:3000/
     后端: http://localhost:10043/
     文档: http://localhost:10043/docs
```

### start_backend.bat - 启动后端

**功能**:
- 检查端口 10043
- 提示是否终止占用进程
- 激活虚拟环境
- 启动uvicorn服务器

**使用**:
```bash
双击 start_backend.bat
```

### start_frontend.bat - 启动前端

**功能**:
- 检查端口 3000
- 提示是否终止占用进程
- 检查依赖是否安装
- 启动Vite开发服务器

**使用**:
```bash
双击 start_frontend.bat
```

### stop_all.bat - 停止服务

**功能**:
- 提示确认操作
- 停止后端服务（端口 10043）
- 停止前端服务（端口 3000）
- 清理残留进程

**使用**:
```bash
双击 stop_all.bat
```

## 🔧 技术实现

### 端口检查
```batch
netstat -ano | findstr ":10043" | findstr "LISTENING"
```

### 进程终止
```batch
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":10043" ^| findstr "LISTENING"') do (
    taskkill /F /PID %%a
)
```

### URL测试
```batch
curl -s -o nul -w "%%{http_code}" http://localhost:10043/docs --connect-timeout 3
```

### 独立窗口启动
```batch
start "SMS Agent - Backend" cmd /k "cd backend && uvicorn app.main:app --reload --port 10043"
```

## 📝 使用场景

### 场景1: 日常开发

```
1. 开始工作
   → 双击 start_all.bat

2. 开发过程
   → 代码自动热更新
   → 无需重启服务

3. 结束工作
   → 双击 stop_all.bat
   或关闭命令行窗口
```

### 场景2: 问题诊断

```
1. 页面无法访问
   → 双击 check_services.bat

2. 查看检查结果
   → 确定哪个服务有问题

3. 重启服务
   → 双击对应的启动脚本
   或 start_all.bat 重启所有
```

### 场景3: 单独调试

```
后端调试:
  → start_backend.bat
  → 修改代码
  → 自动重载

前端调试:
  → start_frontend.bat
  → 修改代码
  → 热更新
```

## 💡 最佳实践

### 推荐工作流

```
启动: start_all.bat
  ↓
开发: 修改代码（自动更新）
  ↓
遇到问题: check_services.bat
  ↓
结束: stop_all.bat
```

### 性能优化

1. **避免频繁重启**
   - 前端使用Vite热更新
   - 后端使用--reload自动重载
   - 只在必要时完全重启

2. **资源管理**
   - 不使用时停止服务
   - 避免多实例运行
   - 定期检查进程状态

3. **快速诊断**
   - 使用check_services.bat
   - 查看命令行窗口日志
   - 检查浏览器控制台

## 🆘 故障排除

### 问题1: 脚本无法运行

**原因**: 执行策略限制

**解决**:
```powershell
# 以管理员身份运行PowerShell
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### 问题2: 端口被占用

**解决**:
```
方法1: start_all.bat 会自动处理
方法2: 手动运行 stop_all.bat
方法3: 手动终止进程
```

### 问题3: 虚拟环境未激活

**解决**:
```bash
# 创建虚拟环境
python -m venv venv

# 安装依赖
venv\Scripts\activate
pip install -r requirements.txt
```

### 问题4: 依赖缺失

**前端**:
```bash
cd frontend
npm install
```

**后端**:
```bash
cd backend
pip install -r requirements.txt
```

## 📚 相关文档

- [详细使用说明](../README_SCRIPTS.md)
- [快速启动指南](../QUICK_START.md)
- [项目主文档](../README.md)

## 🎉 总结

### 创建的脚本

| 脚本 | 功能 | 推荐度 |
|------|------|--------|
| start_all.bat | 一键启动 | ⭐⭐⭐⭐⭐ |
| check_services.bat | 状态检查 | ⭐⭐⭐⭐⭐ |
| stop_all.bat | 停止服务 | ⭐⭐⭐⭐ |
| start_backend.bat | 启动后端 | ⭐⭐⭐ |
| start_frontend.bat | 启动前端 | ⭐⭐⭐ |

### 核心优势

- ✅ 一键启动，无需手动操作
- ✅ 自动处理端口冲突
- ✅ 完整的状态检查
- ✅ 清晰的错误提示
- ✅ 独立窗口运行
- ✅ 详细的使用文档

### 使用建议

**日常使用**:
```
start_all.bat → 开发 → stop_all.bat
```

**遇到问题**:
```
check_services.bat → 诊断 → 重启对应服务
```

**单独调试**:
```
start_backend.bat 或 start_frontend.bat
```

---

**创建日期**: 2025-11-07  
**版本**: 1.0  
**状态**: ✅ 完成并可用
