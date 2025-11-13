# 故障排查指南

## 当前问题：页面一直转圈

### ✅ 已确认正常的部分

1. **后端服务**: ✅ 运行正常
   - 端口: 10043
   - API文档可访问: http://localhost:10043/docs

2. **前端服务**: ✅ 运行正常
   - 端口: 3000
   - Vite开发服务器启动成功

3. **API代理**: ✅ 工作正常
   - 前端可以通过 `/api` 访问后端

### 🔍 可能的原因

#### 1. 浏览器缓存问题

**最常见原因**

**解决方法**:
```
1. 按 Ctrl + Shift + Delete
2. 选择"缓存的图片和文件"
3. 选择"全部时间"
4. 点击"清除数据"
5. 关闭浏览器
6. 重新打开浏览器
7. 访问 http://localhost:3000/
```

#### 2. 浏览器控制台错误

**检查方法**:
```
1. 打开 http://localhost:3000/
2. 按 F12 打开开发者工具
3. 查看 Console 标签是否有红色错误
4. 查看 Network 标签，看哪个请求失败了
```

**常见错误**:
- `401 Unauthorized` - 需要登录
- `CORS error` - 跨域问题
- `net::ERR_CONNECTION_REFUSED` - 后端未运行

#### 3. 路由问题

**症状**: 页面空白或一直加载

**解决方法**:
```
直接访问特定页面:
- http://localhost:3000/#/login
- http://localhost:3000/#/tag-manage
- http://localhost:3000/#/sms-list
```

#### 4. Token过期

**症状**: 页面转圈，控制台显示401错误

**解决方法**:
```javascript
// 在浏览器控制台执行
localStorage.clear()
// 然后刷新页面
```

## 🛠️ 完整重启流程

### 方法1: 使用脚本（推荐）

```bash
# 1. 停止所有服务
stop_services.bat

# 2. 等待3秒

# 3. 重启服务
restart_services.bat

# 4. 等待10秒让服务完全启动

# 5. 清除浏览器缓存

# 6. 访问 http://localhost:3000/
```

### 方法2: 手动重启

```bash
# 1. 停止所有进程
taskkill /F /IM node.exe
taskkill /F /IM python.exe

# 2. 等待3秒

# 3. 启动后端
cd backend
python -m uvicorn app.main:app --reload --port 10043

# 4. 新开一个命令行窗口，启动前端
cd frontend
npm run dev

# 5. 等待服务启动完成

# 6. 清除浏览器缓存并访问
```

## 📊 诊断工具

### 运行诊断脚本

```bash
diagnose.bat
```

这会检查:
- 后端服务状态
- 前端服务状态
- 进程列表
- 端口占用情况

### 手动检查

#### 检查后端

```bash
# 访问API文档
http://localhost:10043/docs

# 或使用curl
curl http://localhost:10043/docs
```

#### 检查前端

```bash
# 访问首页
http://localhost:3000/

# 检查代理
curl http://localhost:3000/api/tags
```

#### 检查进程

```bash
# Node进程
tasklist | findstr node

# Python进程
tasklist | findstr python
```

#### 检查端口

```bash
# 后端端口
netstat -ano | findstr :10043

# 前端端口
netstat -ano | findstr :3000
```

## 🐛 常见问题解决

### Q1: 页面完全空白

**原因**: 前端服务未启动或路由错误

**解决**:
1. 确认前端服务在运行
2. 查看浏览器控制台错误
3. 尝试访问 http://localhost:3000/#/login

### Q2: 页面显示"加载失败"

**原因**: 后端API无法访问

**解决**:
1. 确认后端服务在运行
2. 访问 http://localhost:10043/docs 测试
3. 检查 `.env` 文件配置

### Q3: 页面一直转圈

**原因**: 
- 浏览器缓存
- Token过期
- API请求卡住

**解决**:
1. 清除浏览器缓存
2. 清除localStorage: `localStorage.clear()`
3. 查看Network标签，找出卡住的请求
4. 重启服务

### Q4: 登录后立即退出

**原因**: Token验证失败

**解决**:
1. 清除localStorage
2. 检查后端日志
3. 确认数据库连接正常

### Q5: 批处理脚本乱码

**原因**: 文件编码问题

**解决**:
- 使用更新后的脚本（已移除中文字符）
- 或直接使用命令行手动操作

## 🎯 推荐的调试步骤

### 步骤1: 确认服务状态

```bash
# 运行诊断
diagnose.bat
```

### 步骤2: 检查浏览器

```
1. 打开 http://localhost:3000/
2. 按 F12
3. 查看 Console 和 Network 标签
4. 截图或记录错误信息
```

### 步骤3: 清除缓存

```
1. Ctrl + Shift + Delete
2. 清除所有缓存
3. 关闭浏览器
4. 重新打开
```

### 步骤4: 重启服务

```bash
restart_services.bat
```

### 步骤5: 测试访问

```
1. 等待10秒
2. 访问 http://localhost:3000/
3. 如果还是转圈，查看控制台
```

## 📝 收集诊断信息

如果问题仍未解决，请收集以下信息：

### 1. 服务状态

```bash
diagnose.bat
```

### 2. 浏览器控制台

```
F12 -> Console 标签
截图所有红色错误
```

### 3. Network标签

```
F12 -> Network 标签
找出状态不是200的请求
查看请求详情
```

### 4. 后端日志

```
查看后端命令行窗口
复制最近的日志输出
```

### 5. 前端日志

```
查看前端命令行窗口
复制最近的日志输出
```

## 🚀 快速修复清单

遇到问题时，按顺序尝试：

- [ ] 清除浏览器缓存（Ctrl + Shift + Delete）
- [ ] 清除localStorage（控制台执行 `localStorage.clear()`）
- [ ] 运行 `stop_services.bat`
- [ ] 等待3秒
- [ ] 运行 `restart_services.bat`
- [ ] 等待10秒
- [ ] 关闭浏览器
- [ ] 重新打开浏览器
- [ ] 访问 http://localhost:3000/
- [ ] 如果还有问题，查看浏览器控制台（F12）

## 💡 预防措施

1. **定期重启**: 每天开始工作时重启服务
2. **清除缓存**: 遇到奇怪问题先清除缓存
3. **单一实例**: 不要重复启动服务
4. **使用脚本**: 用 `restart_services.bat` 而不是手动启动
5. **检查日志**: 定期查看命令行窗口的输出

---

**当前状态**: 服务运行正常，但页面转圈  
**最可能原因**: 浏览器缓存或Token问题  
**建议操作**: 清除浏览器缓存，清除localStorage，重启浏览器
