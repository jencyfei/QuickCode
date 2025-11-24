# 上传 APK 到 GitHub Release 脚本
# 使用方法: .\scripts\upload_apk_to_github.ps1 -TagName "v1.4-express-card-status-fix" -GitHubToken "your_token_here"

param(
    [Parameter(Mandatory=$false)]
    [string]$TagName = "",
    
    [Parameter(Mandatory=$false)]
    [string]$GitHubToken = "",
    
    [Parameter(Mandatory=$false)]
    [string]$APKPath = "android/app/build/outputs/apk/release/app-release.apk",
    
    [Parameter(Mandatory=$false)]
    [string]$Owner = "jencyfei",
    
    [Parameter(Mandatory=$false)]
    [string]$Repo = "sms_agent"
)

# 颜色输出函数
function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

Write-ColorOutput Green "=========================================="
Write-ColorOutput Green "  上传 APK 到 GitHub Release"
Write-ColorOutput Green "=========================================="

# 1. 检查 GitHub Token
if ([string]::IsNullOrEmpty($GitHubToken)) {
    $GitHubToken = $env:GITHUB_TOKEN
    if ([string]::IsNullOrEmpty($GitHubToken)) {
        Write-ColorOutput Red "错误: 未提供 GitHub Token"
        Write-ColorOutput Yellow "请通过以下方式之一提供:"
        Write-ColorOutput Yellow "  1. 使用参数: -GitHubToken 'your_token_here'"
        Write-ColorOutput Yellow "  2. 设置环境变量: `$env:GITHUB_TOKEN = 'your_token_here'"
        Write-ColorOutput Yellow ""
        Write-ColorOutput Yellow "获取 Token 步骤:"
        Write-ColorOutput Yellow "  1. 访问 https://github.com/settings/tokens"
        Write-ColorOutput Yellow "  2. 点击 'Generate new token (classic)'"
        Write-ColorOutput Yellow "  3. 选择权限: repo (全部权限)"
        Write-ColorOutput Yellow "  4. 复制生成的 token"
        exit 1
    }
}

# 2. 获取最新的 Tag
if ([string]::IsNullOrEmpty($TagName)) {
    Write-ColorOutput Yellow "正在获取最新的 Git Tag..."
    $TagName = git describe --tags --abbrev=0 2>$null
    if ([string]::IsNullOrEmpty($TagName)) {
        Write-ColorOutput Red "错误: 未找到 Git Tag"
        Write-ColorOutput Yellow "请先创建 Tag: git tag v1.0.0"
        exit 1
    }
    Write-ColorOutput Green "使用最新的 Tag: $TagName"
} else {
    Write-ColorOutput Green "使用指定的 Tag: $TagName"
}

# 3. 检查 APK 文件是否存在
if (-not (Test-Path $APKPath)) {
    Write-ColorOutput Red "错误: APK 文件不存在: $APKPath"
    Write-ColorOutput Yellow "请先打包 APK: cd android && .\gradlew.bat assembleRelease"
    exit 1
}

$APKFullPath = Resolve-Path $APKPath
$APKFileName = Split-Path -Leaf $APKFullPath
$APKSize = (Get-Item $APKFullPath).Length / 1MB

Write-ColorOutput Green "APK 文件: $APKFullPath"
Write-ColorOutput Green "文件大小: $([math]::Round($APKSize, 2)) MB"

# 4. 准备 API 请求
$ApiBaseUrl = "https://api.github.com/repos/$Owner/$Repo"
$Headers = @{
    "Authorization" = "token $GitHubToken"
    "Accept" = "application/vnd.github.v3+json"
}

# 5. 检查 Release 是否存在
Write-ColorOutput Yellow "正在检查 Release 是否存在..."
$ReleaseUrl = "$ApiBaseUrl/releases/tags/$TagName"
$ReleaseResponse = try {
    Invoke-RestMethod -Uri $ReleaseUrl -Headers $Headers -Method Get -ErrorAction Stop
    Write-ColorOutput Green "Release 已存在: $($ReleaseResponse.html_url)"
    $ReleaseId = $ReleaseResponse.id
    $IsNewRelease = $false
} catch {
    if ($_.Exception.Response.StatusCode -eq 404) {
        Write-ColorOutput Yellow "Release 不存在，正在创建新的 Release..."
        
        # 获取 Tag 信息用于创建 Release
        $TagCommitUrl = "$ApiBaseUrl/git/refs/tags/$TagName"
        try {
            $TagRef = Invoke-RestMethod -Uri $TagCommitUrl -Headers $Headers -Method Get
            $CommitSha = $TagRef.object.sha
            
            # 如果是 annotated tag，需要获取实际的 commit
            if ($TagRef.object.type -eq "tag") {
                $TagObjectUrl = "$ApiBaseUrl/git/tags/$CommitSha"
                $TagObject = Invoke-RestMethod -Uri $TagObjectUrl -Headers $Headers -Method Get
                $CommitSha = $TagObject.object.sha
            }
            
            # 获取 commit 信息
            $CommitUrl = "$ApiBaseUrl/git/commits/$CommitSha"
            $Commit = Invoke-RestMethod -Uri $CommitUrl -Headers $Headers -Method Get
            
            $ReleaseTitle = $TagName
            $ReleaseBody = @"
## 版本 $TagName

### 更新内容
- 请在此处添加更新说明

### 下载
- [下载 APK](下载链接将在上传后自动更新)

### 安装说明
1. 下载 APK 文件
2. 在 Android 设备上允许安装来自未知来源的应用
3. 安装 APK

---
**提交信息**: $($Commit.message)
**提交时间**: $($Commit.committer.date)
"@
        } catch {
            $ReleaseTitle = $TagName
            $ReleaseBody = "## 版本 $TagName`n`n请在 GitHub 上查看详细的更新内容。"
        }
        
        # 创建 Release
        $CreateReleaseBody = @{
            tag_name = $TagName
            name = $ReleaseTitle
            body = $ReleaseBody
            draft = $false
            prerelease = $false
        } | ConvertTo-Json
        
        $ReleaseResponse = Invoke-RestMethod -Uri "$ApiBaseUrl/releases" -Headers $Headers -Method Post -Body $CreateReleaseBody -ContentType "application/json"
        Write-ColorOutput Green "Release 创建成功: $($ReleaseResponse.html_url)"
        $ReleaseId = $ReleaseResponse.id
        $IsNewRelease = $true
    } else {
        Write-ColorOutput Red "错误: 无法检查 Release 状态"
        Write-ColorOutput Red $_.Exception.Message
        exit 1
    }
}

# 6. 检查 APK 是否已经上传
Write-ColorOutput Yellow "正在检查 APK 是否已经上传..."
$ExistingAssets = $ReleaseResponse.assets | Where-Object { $_.name -eq $APKFileName }
if ($ExistingAssets) {
    Write-ColorOutput Yellow "发现已存在的 APK 文件，正在删除..."
    foreach ($Asset in $ExistingAssets) {
        $DeleteUrl = "$ApiBaseUrl/releases/assets/$($Asset.id)"
        try {
            Invoke-RestMethod -Uri $DeleteUrl -Headers $Headers -Method Delete
            Write-ColorOutput Green "已删除旧的 APK 文件: $($Asset.name)"
        } catch {
            Write-ColorOutput Red "警告: 删除旧文件失败: $($_.Exception.Message)"
        }
    }
}

# 7. 上传 APK 文件
Write-ColorOutput Yellow "正在上传 APK 文件..."
$UploadUrl = "https://uploads.github.com/repos/$Owner/$Repo/releases/$ReleaseId/assets"

# 读取文件内容
$FileBytes = [System.IO.File]::ReadAllBytes($APKFullPath)
$EncodedFileName = [System.Web.HttpUtility]::UrlEncode($APKFileName)

# GitHub API 需要直接上传二进制数据
$UploadHeaders = @{
    "Authorization" = "token $GitHubToken"
    "Accept" = "application/vnd.github.v3+json"
    "Content-Type" = "application/vnd.android.package-archive"
}

try {
    $ProgressPreference = 'SilentlyContinue'
    # 使用完整的 URL 参数
    $FullUploadUrl = "$UploadUrl?name=$EncodedFileName"
    Write-ColorOutput Yellow "上传 URL: $FullUploadUrl"
    Write-ColorOutput Yellow "文件大小: $([math]::Round($APKSize, 2)) MB - 上传可能需要一些时间..."
    
    $UploadResponse = Invoke-RestMethod -Uri $FullUploadUrl -Headers $UploadHeaders -Method Post -Body $FileBytes
    
    Write-ColorOutput Green "=========================================="
    Write-ColorOutput Green "  ✅ APK 上传成功！"
    Write-ColorOutput Green "=========================================="
    Write-ColorOutput Green "Release 链接: $($ReleaseResponse.html_url)"
    Write-ColorOutput Green "APK 下载链接: $($UploadResponse.browser_download_url)"
    Write-ColorOutput Green "文件大小: $($UploadResponse.size) bytes"
    Write-ColorOutput Green "上传时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
    Write-ColorOutput Green ""
    Write-ColorOutput Yellow "提示: 你可以在 GitHub 上编辑 Release 说明，添加更详细的更新内容。"
} catch {
    Write-ColorOutput Red "=========================================="
    Write-ColorOutput Red "  ❌ APK 上传失败！"
    Write-ColorOutput Red "=========================================="
    Write-ColorOutput Red "错误信息: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $Reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $ResponseBody = $Reader.ReadToEnd()
        Write-ColorOutput Red "响应内容: $ResponseBody"
    }
    exit 1
}

