# Git 备份和回退指南

## 📌 当前备份信息

### 备份标签
- **标签名**: `v1.0-one-click-pickup`
- **提交 ID**: `9dc2dbf`
- **时间**: 2025-11-19
- **描述**: 一键取件功能实现完成 - 编译成功版本

### 备份内容
```
feat: 一键取件功能实现完成 - 编译成功版本备份

- 实现一键取件功能，替代编辑模式
- 移除编辑模式相关代码（isEditMode、复选框、底部操作栏）
- 实现日期过滤（未取7天、已取30天）
- 修复编译错误（isPicked -> status）
- Release APK 和 Debug APK 编译成功
- 所有功能测试计划已准备
```

## 🔍 查看备份信息

### 查看所有标签
```bash
git tag -l
```

### 查看标签详细信息
```bash
git show v1.0-one-click-pickup
```

### 查看提交历史
```bash
git log --oneline -10
```

### 查看当前分支和状态
```bash
git status
git branch -a
```

## 🔄 回退到备份版本

### 方式 1：创建新分支回退（推荐）

如果需要回退到备份版本，但保留当前工作，使用这种方式：

```bash
# 1. 创建一个新分支，基于备份标签
git checkout -b rollback-v1.0 v1.0-one-click-pickup

# 2. 查看当前分支
git branch

# 3. 如果满意，可以合并回主分支
git checkout main
git merge rollback-v1.0

# 4. 或者删除新分支
git branch -d rollback-v1.0
```

### 方式 2：直接回退（谨慎使用）

如果要直接回退到备份版本，覆盖当前所有更改：

```bash
# 1. 查看当前状态
git status

# 2. 保存当前工作（可选）
git stash

# 3. 回退到备份版本
git reset --hard v1.0-one-click-pickup

# 4. 查看回退结果
git log --oneline -5
```

### 方式 3：比较版本差异

查看当前版本和备份版本的差异：

```bash
# 1. 查看差异文件列表
git diff v1.0-one-click-pickup --name-only

# 2. 查看具体差异
git diff v1.0-one-click-pickup -- <file-path>

# 3. 查看特定文件的历史
git log -p v1.0-one-click-pickup -- <file-path>
```

## 📊 备份版本内容

### 关键文件
- `android/app/src/main/java/com/sms/tagger/ui/screens/ExpressScreen.kt` - 一键取件和日期过滤实现
- `android/app/src/main/java/com/sms/tagger/util/ExpressExtractor.kt` - 快递信息提取
- `express_pickup_pending_with_time_filter.html` - HTML 一键取件页面

### 相关文档
- `ANDROID_ONE_CLICK_PICKUP_SUMMARY.md` - 一键取件功能总结
- `COMPILATION_AND_TEST_SUMMARY.md` - 编译和测试总结
- `DATE_FILTER_FEATURE_SUMMARY.md` - 日期过滤功能总结
- `HTML_TO_ANDROID_MIGRATION_GUIDE.md` - HTML 到 Android 迁移指南

### 编译产物
- `android/app/build/outputs/apk/release/app-release.apk` - Release APK
- `android/app/build/outputs/apk/debug/app-debug.apk` - Debug APK

## 🔧 常用 Git 命令

### 查看历史
```bash
# 查看最近 10 条提交
git log --oneline -10

# 查看详细提交信息
git log -1 --stat

# 查看特定文件的历史
git log -p -- <file-path>
```

### 比较版本
```bash
# 比较两个版本的差异
git diff v1.0-one-click-pickup HEAD

# 比较特定文件
git diff v1.0-one-click-pickup HEAD -- <file-path>

# 查看某个版本的特定文件
git show v1.0-one-click-pickup:<file-path>
```

### 创建新标签
```bash
# 为当前提交创建标签
git tag -a v1.1-new-feature -m "新功能描述"

# 为特定提交创建标签
git tag -a v1.1-new-feature <commit-id> -m "新功能描述"

# 列出所有标签
git tag -l

# 删除标签
git tag -d v1.1-new-feature
```

### 恢复文件
```bash
# 恢复单个文件到备份版本
git checkout v1.0-one-click-pickup -- <file-path>

# 恢复所有文件到备份版本
git checkout v1.0-one-click-pickup -- .

# 撤销最后一次提交（保留更改）
git reset --soft HEAD~1

# 撤销最后一次提交（丢弃更改）
git reset --hard HEAD~1
```

## ⚠️ 注意事项

### 回退前检查
1. **保存工作**: 如果有未提交的更改，先运行 `git stash` 保存
2. **确认分支**: 确保在正确的分支上，运行 `git branch` 查看
3. **查看差异**: 运行 `git diff v1.0-one-click-pickup HEAD` 查看差异

### 回退后操作
1. **验证代码**: 回退后检查代码是否正确
2. **编译测试**: 运行 `./gradlew.bat clean build` 验证编译
3. **功能测试**: 安装 APK 并测试功能

### 恢复回退
如果回退后想恢复，可以使用：
```bash
# 查看所有操作历史
git reflog

# 恢复到特定操作
git reset --hard <commit-id>
```

## 📝 备份策略

### 定期备份
- 每次完成重要功能后创建标签
- 标签命名规范：`v<版本号>-<功能描述>`
- 示例：`v1.0-one-click-pickup`, `v1.1-bug-fix`, `v1.2-optimization`

### 标签命名规范
```
v<主版本>.<次版本>-<功能/修复描述>

示例：
- v1.0-one-click-pickup       # 一键取件功能
- v1.1-date-filter            # 日期过滤功能
- v1.2-bug-fix-ui             # UI 错误修复
- v2.0-major-redesign         # 主要重新设计
```

### 提交信息规范
```
<类型>: <简短描述>

<详细描述>

<相关文件>
- 文件1
- 文件2

<相关链接>
- 文档1
- 文档2
```

## 🚀 快速参考

### 查看备份
```bash
git show v1.0-one-click-pickup
```

### 回退到备份
```bash
git reset --hard v1.0-one-click-pickup
```

### 创建回退分支
```bash
git checkout -b rollback v1.0-one-click-pickup
```

### 比较当前和备份
```bash
git diff v1.0-one-click-pickup HEAD
```

### 恢复单个文件
```bash
git checkout v1.0-one-click-pickup -- <file-path>
```

## 📞 需要帮助？

### 常见问题

**Q: 如何查看备份版本包含了哪些文件？**
```bash
git show v1.0-one-click-pickup --name-only
```

**Q: 如何查看备份版本和当前版本的差异？**
```bash
git diff v1.0-one-click-pickup HEAD --stat
```

**Q: 如何只恢复某个文件到备份版本？**
```bash
git checkout v1.0-one-click-pickup -- <file-path>
```

**Q: 如何撤销回退操作？**
```bash
git reflog  # 查看历史
git reset --hard <commit-id>  # 恢复到特定提交
```

**Q: 如何创建新的备份标签？**
```bash
git tag -a v1.1-new-feature -m "新功能描述"
```

---

**创建时间**: 2025-11-19  
**备份版本**: v1.0-one-click-pickup  
**提交 ID**: 9dc2dbf  
**状态**: ✅ 备份完成
