# ADB连接诊断脚本
# 用途：帮助诊断为什么 adb devices 返回空

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "ADB连接诊断工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 检查ADB是否安装
Write-Host "[1/8] 检查ADB是否安装..." -ForegroundColor Yellow
try {
    $adbVersion = adb version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ ADB已安装" -ForegroundColor Green
        Write-Host "  版本信息：" -ForegroundColor Gray
        $adbVersion | ForEach-Object { Write-Host "    $_" -ForegroundColor Gray }
    } else {
        Write-Host "✗ ADB未安装或无法运行" -ForegroundColor Red
        Write-Host "  请安装Android SDK Platform Tools" -ForegroundColor Yellow
        exit 1
    }
} catch {
    Write-Host "✗ ADB未安装或不在PATH中" -ForegroundColor Red
    exit 1
}
Write-Host ""

# 2. 重启ADB服务
Write-Host "[2/8] 重启ADB服务..." -ForegroundColor Yellow
try {
    adb kill-server 2>&1 | Out-Null
    Start-Sleep -Seconds 2
    adb start-server 2>&1 | Out-Null
    Write-Host "✓ ADB服务已重启" -ForegroundColor Green
} catch {
    Write-Host "✗ 无法重启ADB服务" -ForegroundColor Red
}
Write-Host ""

# 3. 检查设备连接
Write-Host "[3/8] 检查设备连接..." -ForegroundColor Yellow
Start-Sleep -Seconds 1
$devices = adb devices 2>&1
$deviceLines = $devices | Where-Object { $_ -match "^\w" -and $_ -notmatch "List of devices" }
if ($deviceLines.Count -gt 0) {
    Write-Host "✓ 检测到设备：" -ForegroundColor Green
    $deviceLines | ForEach-Object {
        if ($_ -match "device$") {
            Write-Host "  ✓ $_" -ForegroundColor Green
        } elseif ($_ -match "unauthorized") {
            Write-Host "  ✗ $_ (未授权)" -ForegroundColor Red
            Write-Host "    请在手机上点击'允许USB调试'" -ForegroundColor Yellow
        } else {
            Write-Host "  ? $_" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "✗ 未检测到设备" -ForegroundColor Red
    Write-Host "  请检查以下项目：" -ForegroundColor Yellow
    Write-Host "  1. USB线是否已连接" -ForegroundColor Gray
    Write-Host "  2. 是否启用了USB调试" -ForegroundColor Gray
    Write-Host "  3. 是否授权了电脑" -ForegroundColor Gray
    Write-Host "  4. USB连接模式是否为'文件传输'" -ForegroundColor Gray
}
Write-Host ""

# 4. 检查Windows设备管理器中的Android设备
Write-Host "[4/8] 检查Windows设备管理器..." -ForegroundColor Yellow
Write-Host "  提示：请手动打开设备管理器检查" -ForegroundColor Gray
Write-Host "  路径：Win+X → 设备管理器" -ForegroundColor Gray
Write-Host "  查找：'Android Phone'、'便携设备'或带感叹号的设备" -ForegroundColor Gray
Write-Host ""

# 5. 检查应用是否已安装
Write-Host "[5/8] 检查应用是否已安装..." -ForegroundColor Yellow
if ($deviceLines -match "device$") {
    try {
        $packages = adb shell pm list packages 2>&1 | Select-String "sms.tagger"
        if ($packages) {
            Write-Host "✓ 应用已安装" -ForegroundColor Green
            $packages | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
        } else {
            Write-Host "✗ 应用未安装" -ForegroundColor Red
        }
    } catch {
        Write-Host "? 无法检查应用状态（设备可能未连接）" -ForegroundColor Yellow
    }
} else {
    Write-Host "- 跳过（设备未连接）" -ForegroundColor Gray
}
Write-Host ""

# 6. 检查APK文件
Write-Host "[6/8] 检查APK文件..." -ForegroundColor Yellow
$apkPaths = @(
    "android/app/build/outputs/apk/full/release/sms-agent-fullRelease-1.3.0.apk",
    "android/app/build/outputs/apk/trial/release/sms-agent-trialRelease-1.3.0-trial.apk",
    "android/app/build/outputs/apk/debug/sms-agent-debug-1.3.0.apk"
)

$foundApks = @()
foreach ($apkPath in $apkPaths) {
    if (Test-Path $apkPath) {
        $fileInfo = Get-Item $apkPath
        $foundApks += $apkPath
        Write-Host "✓ 找到：$apkPath" -ForegroundColor Green
        Write-Host "  大小：$([math]::Round($fileInfo.Length / 1MB, 2)) MB" -ForegroundColor Gray
        Write-Host "  日期：$($fileInfo.LastWriteTime)" -ForegroundColor Gray
    }
}

if ($foundApks.Count -eq 0) {
    Write-Host "✗ 未找到APK文件" -ForegroundColor Red
    Write-Host "  需要先构建APK：cd android && .\gradlew.bat assembleFullRelease" -ForegroundColor Yellow
} else {
    Write-Host "✓ 找到 $($foundApks.Count) 个APK文件" -ForegroundColor Green
}
Write-Host ""

# 7. 提供诊断建议
Write-Host "[7/8] 诊断建议..." -ForegroundColor Yellow
if ($deviceLines -notmatch "device$") {
    Write-Host "设备未连接，请尝试以下步骤：" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "1. 检查USB连接" -ForegroundColor Cyan
    Write-Host "   - 确认USB线已连接" -ForegroundColor Gray
    Write-Host "   - 尝试更换USB端口（建议USB 2.0）" -ForegroundColor Gray
    Write-Host "   - 尝试更换USB线" -ForegroundColor Gray
    Write-Host ""
    Write-Host "2. 启用USB调试" -ForegroundColor Cyan
    Write-Host "   - 设置 → 关于手机 → 连续点击'版本号'7次" -ForegroundColor Gray
    Write-Host "   - 返回设置 → 开发者选项 → 启用'USB调试'" -ForegroundColor Gray
    Write-Host ""
    Write-Host "3. 授权电脑" -ForegroundColor Cyan
    Write-Host "   - 连接USB时，手机上弹出'允许USB调试'对话框" -ForegroundColor Gray
    Write-Host "   - 勾选'始终允许这台计算机'" -ForegroundColor Gray
    Write-Host "   - 点击'确定'" -ForegroundColor Gray
    Write-Host ""
    Write-Host "4. 检查USB连接模式" -ForegroundColor Cyan
    Write-Host "   - 确保选择'文件传输'或'MTP模式'" -ForegroundColor Gray
    Write-Host "   - 不要选择'仅充电'" -ForegroundColor Gray
    Write-Host ""
    Write-Host "5. 品牌特定设置" -ForegroundColor Cyan
    Write-Host "   - 小米：启用'仅充电模式下允许ADB调试'" -ForegroundColor Gray
    Write-Host "   - 华为：启用'USB调试（安全设置）'" -ForegroundColor Gray
    Write-Host "   - OPPO/VIVO：启用'USB调试（安全设置）'" -ForegroundColor Gray
    Write-Host ""
    Write-Host "6. 安装驱动" -ForegroundColor Cyan
    Write-Host "   - 检查设备管理器中是否有带感叹号的设备" -ForegroundColor Gray
    Write-Host "   - 安装手机厂商的USB驱动" -ForegroundColor Gray
} else {
    Write-Host "✓ 设备连接正常！" -ForegroundColor Green
    Write-Host ""
    Write-Host "现在您可以：" -ForegroundColor Cyan
    Write-Host "  1. 查看Logcat日志" -ForegroundColor Gray
    Write-Host "     adb logcat | Select-String -Pattern 'SmsReader|10684'" -ForegroundColor White
    Write-Host ""
    Write-Host "  2. 重新安装应用" -ForegroundColor Gray
    if ($foundApks.Count -gt 0) {
        Write-Host "     adb install -r $($foundApks[0])" -ForegroundColor White
    }
    Write-Host ""
    Write-Host "  3. 查看应用信息" -ForegroundColor Gray
    Write-Host "     adb shell pm list packages | Select-String 'sms.tagger'" -ForegroundColor White
}
Write-Host ""

# 8. 尝试WiFi连接（如果USB失败）
Write-Host "[8/8] WiFi连接选项..." -ForegroundColor Yellow
Write-Host "如果USB连接一直失败，可以尝试WiFi连接：" -ForegroundColor Gray
Write-Host ""
Write-Host "步骤：" -ForegroundColor Cyan
Write-Host "  1. 先用USB连接一次（如果可能）" -ForegroundColor Gray
Write-Host "  2. 运行: adb tcpip 5555" -ForegroundColor White
Write-Host "  3. 在手机上查看WiFi IP地址（设置 → WLAN）" -ForegroundColor Gray
Write-Host "  4. 运行: adb connect [IP地址]:5555" -ForegroundColor White
Write-Host "  5. 断开USB，验证: adb devices" -ForegroundColor White
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "诊断完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 提供详细文档链接
Write-Host "详细排查指南请查看：" -ForegroundColor Yellow
Write-Host "  docs/ADB_DEVICES_EMPTY_TROUBLESHOOTING.md" -ForegroundColor Cyan

