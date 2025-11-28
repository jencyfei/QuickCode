# 激活码生成工具文件复制脚本
# 目标：将必要的文件复制到新项目目录 D:\tools\python\mypro\jmq

# 设置源目录和目标目录
$sourceDir = "D:\tools\python\mypro\sms_agent"
$targetDir = "D:\tools\python\mypro\jmq"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "激活码生成工具 - 文件复制脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查源目录是否存在
if (-not (Test-Path $sourceDir)) {
    Write-Host "[错误] 源目录不存在: $sourceDir" -ForegroundColor Red
    exit 1
}

Write-Host "源目录: $sourceDir" -ForegroundColor Green
Write-Host "目标目录: $targetDir" -ForegroundColor Green
Write-Host ""

# 创建目标目录结构
Write-Host "创建目录结构..." -ForegroundColor Yellow
New-Item -ItemType Directory -Force -Path "$targetDir\tools" | Out-Null
New-Item -ItemType Directory -Force -Path "$targetDir\docs" | Out-Null
Write-Host "✓ 目录结构创建完成" -ForegroundColor Green
Write-Host ""

# 定义要复制的文件
$filesToCopy = @(
    @{
        Source = "$sourceDir\tools\generate_activation_code.py"
        Target = "$targetDir\tools\generate_activation_code.py"
        Name = "激活码生成脚本"
    },
    @{
        Source = "$sourceDir\docs\激活码生成系统说明.md"
        Target = "$targetDir\docs\激活码生成系统说明.md"
        Name = "系统说明文档"
    },
    @{
        Source = "$sourceDir\docs\加密.md"
        Target = "$targetDir\docs\加密.md"
        Name = "加密协议文档"
    },
    @{
        Source = "$sourceDir\docs\流程.md"
        Target = "$targetDir\docs\流程.md"
        Name = "激活流程文档（可选）"
    }
)

# 复制文件
Write-Host "开始复制文件..." -ForegroundColor Yellow
$successCount = 0
$failCount = 0

foreach ($file in $filesToCopy) {
    $sourcePath = $file.Source
    $targetPath = $file.Target
    $fileName = $file.Name
    
    if (Test-Path $sourcePath) {
        try {
            Copy-Item -Path $sourcePath -Destination $targetPath -Force
            Write-Host "  ✓ $fileName" -ForegroundColor Green
            $successCount++
        } catch {
            Write-Host "  ✗ $fileName (复制失败: $_)" -ForegroundColor Red
            $failCount++
        }
    } else {
        Write-Host "  ⚠ $fileName (源文件不存在，跳过)" -ForegroundColor Yellow
        $failCount++
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "复制完成统计" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "成功: $successCount 个文件" -ForegroundColor Green
Write-Host "失败/跳过: $failCount 个文件" -ForegroundColor $(if ($failCount -eq 0) { "Green" } else { "Yellow" })
Write-Host ""

# 检查是否需要生成密钥文件
$keyFile = "$targetDir\tools\activation.key"
if (-not (Test-Path $keyFile)) {
    Write-Host "⚠ 注意：密钥文件不存在" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "需要生成新的密钥文件，请运行以下命令：" -ForegroundColor Yellow
    Write-Host "  cd $targetDir\tools" -ForegroundColor Cyan
    Write-Host "  python -c `"from cryptography.fernet import Fernet; print(Fernet.generate_key().decode())`" > activation.key" -ForegroundColor Cyan
    Write-Host ""
} else {
    Write-Host "✓ 密钥文件已存在" -ForegroundColor Green
    Write-Host ""
}

# 检查是否需要创建 requirements.txt
$requirementsFile = "$targetDir\requirements.txt"
if (-not (Test-Path $requirementsFile)) {
    Write-Host "创建 requirements.txt..." -ForegroundColor Yellow
    @"
cryptography>=41.0.0
"@ | Out-File -FilePath $requirementsFile -Encoding UTF8
    Write-Host "✓ requirements.txt 已创建" -ForegroundColor Green
    Write-Host ""
}

# 检查是否需要创建 .gitignore
$gitignoreFile = "$targetDir\.gitignore"
if (-not (Test-Path $gitignoreFile)) {
    Write-Host "创建 .gitignore..." -ForegroundColor Yellow
    @"
# 密钥文件（敏感信息，不要提交）
tools/activation.key
*.key

# Python
__pycache__/
*.py[cod]
*`$py.class
*.so
.Python
venv/
env/
ENV/

# 生成的文件
*.csv
*.log

# IDE
.vscode/
.idea/
*.swp

# OS
.DS_Store
Thumbs.db
"@ | Out-File -FilePath $gitignoreFile -Encoding UTF8
    Write-Host "✓ .gitignore 已创建" -ForegroundColor Green
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ 所有操作完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步：" -ForegroundColor Yellow
Write-Host "1. 进入新项目目录: cd $targetDir" -ForegroundColor Cyan
Write-Host "2. 安装依赖: pip install -r requirements.txt" -ForegroundColor Cyan
Write-Host "3. 生成密钥（如果还没有）: cd tools && python -c `"from cryptography.fernet import Fernet; print(Fernet.generate_key().decode())`" > activation.key" -ForegroundColor Cyan
Write-Host "4. 测试脚本: python tools/generate_activation_code.py --help" -ForegroundColor Cyan
Write-Host ""

