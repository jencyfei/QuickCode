# 快速上传 APK 到 GitHub Release

## 最简单的使用方法

### 1. 获取 GitHub Token

访问：https://github.com/settings/tokens
- 点击 "Generate new token (classic)"
- 选择 `repo` 权限
- 复制生成的 token

### 2. 设置 Token（临时）

```powershell
$env:GITHUB_TOKEN = "your_token_here"
```

### 3. 运行脚本

```powershell
.\scripts\upload_apk_to_github.ps1
```

脚本会自动：
- ✅ 使用最新的 Git Tag
- ✅ 查找 APK 文件（`android/app/build/outputs/apk/release/app-release.apk`）
- ✅ 创建或更新 Release
- ✅ 上传 APK 文件

### 完整示例

```powershell
# 1. 设置 Token
$env:GITHUB_TOKEN = "ghp_xxxxxxxxxxxxxxxxxxxx"

# 2. 运行上传脚本
.\scripts\upload_apk_to_github.ps1

# 或者指定 Tag
.\scripts\upload_apk_to_github.ps1 -TagName "v1.5.0"
```

## 如果脚本无法使用

### 方法 1：手动上传（最简单）

1. 访问：https://github.com/jencyfei/sms_agent/releases
2. 点击 "Draft a new release" 或选择已存在的 release
3. 选择 Tag（如 `v1.4-express-card-status-fix`）
4. 拖拽 APK 文件到上传区域
5. 点击 "Publish release"

### 方法 2：使用 GitHub CLI

```powershell
# 安装 GitHub CLI（如果未安装）
# choco install gh

# 登录
gh auth login

# 上传 APK
gh release upload v1.4.0 android/app/build/outputs/apk/release/app-release.apk
```

## 常见问题

**Q: Token 在哪里获取？**  
A: https://github.com/settings/tokens → Generate new token (classic)

**Q: 需要哪些权限？**  
A: 只需要勾选 `repo` 权限

**Q: APK 文件在哪里？**  
A: `android/app/build/outputs/apk/release/app-release.apk`

**Q: Tag 不存在怎么办？**  
A: 先创建 Tag: `git tag v1.5.0 && git push origin v1.5.0`

