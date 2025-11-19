# 快速备份和回退参考

## 📌 当前备份

| 项目 | 值 |
|------|-----|
| 标签名 | `v1.0-one-click-pickup` |
| 提交 ID | `9dc2dbf` |
| 时间 | 2025-11-19 |
| 状态 | ✅ 编译成功 |

## 🚀 快速命令

### 查看备份信息
```bash
# 查看备份标签
git tag -l

# 查看备份详情
git show v1.0-one-click-pickup

# 查看提交历史
git log --oneline -5
```

### 回退到备份（3 种方式）

#### 方式 1：创建新分支（推荐，安全）
```bash
git checkout -b rollback-v1.0 v1.0-one-click-pickup
```

#### 方式 2：直接回退（谨慎）
```bash
git reset --hard v1.0-one-click-pickup
```

#### 方式 3：恢复单个文件
```bash
git checkout v1.0-one-click-pickup -- <file-path>
```

### 比较版本
```bash
# 查看差异文件
git diff v1.0-one-click-pickup HEAD --name-only

# 查看详细差异
git diff v1.0-one-click-pickup HEAD
```

## 📊 备份包含内容

### 核心功能
- ✅ 一键取件功能
- ✅ 日期过滤（未取7天、已取30天）
- ✅ 单个快递状态切换
- ✅ 编辑模式移除

### 编译状态
- ✅ Release APK 编译成功
- ✅ Debug APK 编译成功
- ✅ 所有编译错误已修复

### 关键文件
- `ExpressScreen.kt` - 一键取件实现
- `express_pickup_pending_with_time_filter.html` - HTML 页面
- `ANDROID_ONE_CLICK_PICKUP_SUMMARY.md` - 功能总结

## ⚠️ 回退前检查清单

- [ ] 运行 `git status` 查看当前状态
- [ ] 运行 `git diff v1.0-one-click-pickup HEAD` 查看差异
- [ ] 如有未提交更改，运行 `git stash` 保存
- [ ] 确认在正确分支：`git branch`

## 🔄 回退后验证

```bash
# 1. 验证代码
git log --oneline -5

# 2. 编译验证
./gradlew.bat clean build

# 3. 功能验证
./gradlew.bat installDebug
```

## 📝 常用场景

### 场景 1：发现新版本有 bug，需要回到备份
```bash
# 1. 保存当前工作
git stash

# 2. 创建回退分支
git checkout -b bugfix v1.0-one-click-pickup

# 3. 修复 bug
# ... 修改代码 ...

# 4. 提交修复
git add -A
git commit -m "fix: 修复 bug"

# 5. 合并回主分支
git checkout main
git merge bugfix
```

### 场景 2：只恢复某个文件
```bash
# 恢复 ExpressScreen.kt 到备份版本
git checkout v1.0-one-click-pickup -- android/app/src/main/java/com/sms/tagger/ui/screens/ExpressScreen.kt
```

### 场景 3：查看备份版本的特定文件
```bash
# 查看备份版本的 ExpressScreen.kt
git show v1.0-one-click-pickup:android/app/src/main/java/com/sms/tagger/ui/screens/ExpressScreen.kt
```

### 场景 4：完全回退
```bash
# 1. 查看当前状态
git status

# 2. 保存工作（可选）
git stash

# 3. 回退
git reset --hard v1.0-one-click-pickup

# 4. 验证
git log --oneline -5
```

## 🆘 紧急恢复

如果不小心删除了提交或分支，可以恢复：

```bash
# 查看所有操作历史
git reflog

# 恢复到特定操作
git reset --hard <commit-id>
```

## 📞 帮助命令

```bash
# Git 帮助
git help

# 查看特定命令帮助
git help reset
git help checkout
git help tag

# 查看配置
git config --list
```

---

**快速参考卡片**  
**版本**: v1.0-one-click-pickup  
**更新**: 2025-11-19
