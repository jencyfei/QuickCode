# 查看 SMS 相关日志的 PowerShell 脚本
# 使用方法: .\scripts\view_sms_logcat.ps1

param(
    [Parameter(Mandatory=$false)]
    [switch]$Clear = $false,  # 是否先清除日志
    
    [Parameter(Mandatory=$false)]
    [switch]$Save = $false,  # 是否保存到文件
    
    [Parameter(Mandatory=$false)]
    [string]$Search = "",  # 搜索关键词
    
    [Parameter(Mandatory=$false)]
    [string]$AdbPath = "adb"  # ADB 路径
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

Write-ColorOutput Cyan "=========================================="
Write-ColorOutput Cyan "  SMS 日志查看工具"
Write-ColorOutput Cyan "=========================================="
Write-Host ""

# 检查 ADB 是否可用
try {
    $null = & $AdbPath version 2>&1
} catch {
    Write-ColorOutput Red "错误: 无法找到 adb 命令"
    Write-Host "请确保："
    Write-Host "  1. 已安装 Android SDK Platform Tools"
    Write-Host "  2. adb 在 PATH 环境变量中"
    Write-Host "  3. 或修改脚本中的 AdbPath 参数指向 adb.exe 的完整路径"
    exit 1
}

# 检查设备连接
Write-ColorOutput Yellow "检查设备连接..."
$devices = & $AdbPath devices 2>&1
Write-Host $devices

if ($devices -notmatch "device\s*$") {
    Write-ColorOutput Red "错误: 没有检测到已连接的设备！"
    Write-Host ""
    Write-Host "请确保："
    Write-Host "  1. 手机已通过 USB 连接到电脑"
    Write-Host "  2. 手机上已启用 '开发者选项' 中的 'USB 调试'"
    Write-Host "  3. 手机上已授权此电脑的 USB 调试"
    Write-Host ""
    Write-Host "如果仍未检测到，请尝试："
    Write-Host "  - 重新插拔 USB 线"
    Write-Host "  - 在手机上重新授权 USB 调试"
    Write-Host "  - 检查 USB 驱动是否正确安装"
    exit 1
}

Write-Host ""
Write-ColorOutput Green "✅ 设备已连接"

# 清除日志（如果需要）
if ($Clear) {
    Write-ColorOutput Yellow "清除日志缓冲区..."
    & $AdbPath logcat -c
    Write-ColorOutput Green "✅ 日志已清除"
    Write-Host ""
}

# 准备日志命令
$logTags = "SmsReader:D SmsListScreen:D ExpressExtractor:D *:S"
$logCommand = "$AdbPath logcat $logTags"

Write-Host ""
Write-ColorOutput Cyan "=========================================="
Write-ColorOutput Cyan "  开始查看日志"
Write-ColorOutput Cyan "=========================================="
Write-Host ""
Write-Host "日志标签过滤: $logTags"
Write-Host ""
if ($Search) {
    Write-ColorOutput Yellow "搜索关键词: $Search"
    Write-Host ""
}
Write-Host "提示: 按 Ctrl+C 停止查看"
Write-Host ""

# 如果保存到文件
if ($Save) {
    $logFile = "logcat_sms_$(Get-Date -Format 'yyyyMMdd_HHmmss').txt"
    Write-ColorOutput Yellow "日志将保存到: $logFile"
    Write-Host ""
    
    if ($Search) {
        # 搜索模式：保存所有日志，但在控制台只显示匹配的
        & $AdbPath logcat $logTags | Tee-Object -FilePath $logFile | Select-String $Search
    } else {
        # 正常模式：保存并显示所有日志
        & $AdbPath logcat $logTags | Tee-Object -FilePath $logFile
    }
} else {
    # 不保存到文件
    if ($Search) {
        # 搜索模式：只显示匹配的日志
        & $AdbPath logcat $logTags | Select-String $Search
    } else {
        # 正常模式：显示所有日志
        & $AdbPath logcat $logTags
    }
}

