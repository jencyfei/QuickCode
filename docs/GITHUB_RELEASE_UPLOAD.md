# 将 APK 上传到 GitHub Release

本文档介绍如何将打包好的 APK 文件上传到 GitHub Release。

## 方法一：使用 PowerShell 脚本（推荐）

### 1. 获取 GitHub Token

1. 访问 https://github.com/settings/tokens
2. 点击 "Generate new token (classic)"
3. 输入 Token 名称（如：`SMS Agent Release Upload`）
4. 选择过期时间
5. 勾选权限：`repo`（需要完整权限）
6. 点击 "Generate token"
7. **复制生成的 token**（只会显示一次，请妥善保存）

### 2. 设置环境变量（可选）

```powershell
# 在 PowerShell 中设置（仅当前会话有效）
$env:GITHUB_TOKEN = "your_token_here"

# 或者永久设置（需要管理员权限）
[System.Environment]::SetEnvironmentVariable("GITHUB_TOKEN", "your_token_here", "User")
```

### 3. 运行上传脚本

```powershell
# 使用最新的 Git Tag
.\scripts\upload_apk_to_github.ps1

# 或者指定 Tag 和 Token
.\scripts\upload_apk_to_github.ps1 -TagName "v1.4-express-card-status-fix" -GitHubToken "your_token_here"

# 或者指定自定义 APK 路径
.\scripts\upload_apk_to_github.ps1 -APKPath "path/to/your.apk" -TagName "v1.5.0"
```

### 脚本参数说明

- `-TagName`: Git Tag 名称（可选，默认使用最新的 tag）
- `-GitHubToken`: GitHub Token（可选，可使用环境变量）
- `-APKPath`: APK 文件路径（默认：`android/app/build/outputs/apk/release/app-release.apk`）
- `-Owner`: GitHub 用户名（默认：`jencyfei`）
- `-Repo`: 仓库名（默认：`sms_agent`）

## 方法二：使用 GitHub CLI（如果已安装）

### 1. 安装 GitHub CLI

```powershell
# 使用 Chocolatey
choco install gh

# 或使用 Scoop
scoop install gh

# 或从官网下载
# https://cli.github.com/
```

### 2. 登录 GitHub

```powershell
gh auth login
```

### 3. 创建或更新 Release

```powershell
# 使用最新的 Tag 创建 Release
gh release create v1.4.0 `
  android/app/build/outputs/apk/release/app-release.apk `
  --title "版本 v1.4.0" `
  --notes "更新内容：
- 修复已取页面快递卡片状态显示问题
- 优化搜索功能
"

# 或者将 APK 添加到已存在的 Release
gh release upload v1.4.0 android/app/build/outputs/apk/release/app-release.apk --clobber
```

## 方法三：手动上传（最简单）

### 1. 创建 Release

1. 访问 https://github.com/jencyfei/sms_agent/releases
2. 点击 "Draft a new release" 或编辑已存在的 release
3. 选择 Tag（如：`v1.4-express-card-status-fix`）
4. 输入 Release 标题和说明
5. **不要点击 "Publish release"**

### 2. 上传 APK

1. 在 Release 编辑页面，找到 "Attach binaries" 部分
2. 将 APK 文件（`android/app/build/outputs/apk/release/app-release.apk`）拖拽到上传区域
3. 等待上传完成
4. 点击 "Publish release"

## 方法四：使用 GitHub API（命令行）

```powershell
# 设置变量
$Token = "your_token_here"
$TagName = "v1.4.0"
$APKPath = "android/app/build/outputs/apk/release/app-release.apk"
$Owner = "jencyfei"
$Repo = "sms_agent"

# 1. 创建 Release（如果不存在）
$ReleaseBody = @{
    tag_name = $TagName
    name = "版本 $TagName"
    body = "更新内容..."
    draft = $false
    prerelease = $false
} | ConvertTo-Json

$Headers = @{
    "Authorization" = "token $Token"
    "Accept" = "application/vnd.github.v3+json"
}

$Release = Invoke-RestMethod -Uri "https://api.github.com/repos/$Owner/$Repo/releases" -Method Post -Headers $Headers -Body $ReleaseBody

# 2. 上传 APK
$UploadUrl = $Release.upload_url -replace "\{.*\}", "?name=app-release.apk"
$FileBytes = [System.IO.File]::ReadAllBytes((Resolve-Path $APKPath))

$UploadHeaders = @{
    "Authorization" = "token $Token"
    "Accept" = "application/vnd.github.v3+json"
    "Content-Type" = "application/vnd.android.package-archive"
}

Invoke-RestMethod -Uri $UploadUrl -Method Post -Headers $UploadHeaders -Body $FileBytes
```

## 完整工作流程示例

```powershell
# 1. 确保代码已提交并推送
git add .
git commit -m "feat: 新功能"
git push

# 2. 创建新的 Tag（如果需要）
git tag -a v1.5.0 -m "版本 v1.5.0"
git push origin v1.5.0

# 3. 打包 APK（如果还没有）
cd android
.\gradlew.bat assembleRelease
cd ..

# 4. 上传 APK 到 Release
$env:GITHUB_TOKEN = "your_token_here"
.\scripts\upload_apk_to_github.ps1 -TagName "v1.5.0"
```

## 注意事项

1. **APK 文件大小限制**: GitHub 单文件限制为 100MB（Release 附件限制为 2GB）
2. **Token 权限**: 需要 `repo` 权限才能上传文件到 Release
3. **Tag 必须存在**: 上传前确保对应的 Git Tag 已创建并推送
4. **重复上传**: 如果 Release 中已存在同名文件，脚本会自动删除旧文件

## 故障排除

### 问题：Token 无效
- 检查 Token 是否过期
- 确认 Token 有 `repo` 权限
- 尝试重新生成 Token

### 问题：文件上传失败
- 检查文件大小是否超过 100MB
- 检查网络连接
- 尝试使用 GitHub CLI 或手动上传

### 问题：找不到 Tag
- 确认 Tag 已创建：`git tag --list`
- 确认 Tag 已推送：`git push origin v1.4.0`

### 问题：找不到 APK 文件
- 确认已执行打包：`cd android && .\gradlew.bat assembleRelease`
- 检查 APK 路径：`android/app/build/outputs/apk/release/app-release.apk`

## 参考链接

- [GitHub Releases API 文档](https://docs.github.com/en/rest/releases/releases)
- [GitHub CLI 文档](https://cli.github.com/manual/)
- [GitHub Token 创建](https://github.com/settings/tokens)

