# Gradle 缓存清理脚本
# 用途：清理不需要的Gradle版本和缓存，节省磁盘空间
# 使用方法：.\scripts\cleanup_gradle_cache.ps1

param(
    [Parameter(Mandatory=$false)]
    [switch]$DryRun = $false,  # 预览模式，不实际删除
    
    [Parameter(Mandatory=$false)]
    [switch]$CleanAll = $false,  # 清理所有缓存（包括依赖库）
    
    [Parameter(Mandatory=$false)]
    [string]$GradleHome = "$env:USERPROFILE\.gradle"
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
Write-ColorOutput Green "  Gradle 缓存清理工具"
Write-ColorOutput Green "=========================================="
Write-Host ""

# 检查Gradle目录是否存在
$gradleCachePath = Join-Path $GradleHome "caches"
if (-not (Test-Path $gradleCachePath)) {
    Write-ColorOutput Red "错误: Gradle缓存目录不存在: $gradleCachePath"
    exit 1
}

Write-ColorOutput Yellow "Gradle缓存目录: $gradleCachePath"
Write-Host ""

# 1. 检测当前项目使用的Gradle版本
Write-ColorOutput Yellow "正在检测项目使用的Gradle版本..."
$projectGradleVersion = $null
$wrapperProperties = Get-ChildItem -Path . -Recurse -Filter "gradle-wrapper.properties" -ErrorAction SilentlyContinue | Select-Object -First 1

if ($wrapperProperties) {
    $wrapperContent = Get-Content $wrapperProperties.FullName -Raw
    if ($wrapperContent -match 'gradle-(\d+\.\d+)-') {
        $projectGradleVersion = $matches[1]
        Write-ColorOutput Green "✅ 检测到项目使用 Gradle 版本: $projectGradleVersion"
    }
} else {
    Write-ColorOutput Yellow "⚠️  未找到 gradle-wrapper.properties，将保留所有Gradle版本"
}

Write-Host ""

# 2. 分析缓存目录
Write-ColorOutput Yellow "正在分析缓存目录..."
$cachesDir = Get-ChildItem -Path $gradleCachePath -Directory -ErrorAction SilentlyContinue
$totalSize = 0
$itemsToClean = @()
$itemsToKeep = @()

foreach ($dir in $cachesDir) {
    $dirSize = (Get-ChildItem -Path $dir.FullName -Recurse -ErrorAction SilentlyContinue | 
                Measure-Object -Property Length -Sum).Sum
    $sizeGB = [math]::Round($dirSize / 1GB, 2)
    $totalSize += $dirSize
    
    $item = @{
        Name = $dir.Name
        Path = $dir.FullName
        Size = $dirSize
        SizeGB = $sizeGB
        CanDelete = $false
        Reason = ""
    }
    
    # 判断是否可以删除
    if ($dir.Name -match '^\d+\.\d+') {
        # Gradle版本目录
        if ($null -ne $projectGradleVersion -and $dir.Name -eq $projectGradleVersion) {
            $item.CanDelete = $false
            $item.Reason = "项目使用的Gradle版本"
            $itemsToKeep += $item
        } elseif ($dir.Name -match '^\d+\.\d+') {
            $item.CanDelete = $true
            $item.Reason = "不需要的Gradle版本"
            $itemsToClean += $item
        }
    } elseif ($dir.Name -eq "build-cache-1") {
        # 构建缓存
        $item.CanDelete = $true
        $item.Reason = "构建缓存（可安全删除）"
        $itemsToClean += $item
    } elseif ($dir.Name -eq "transforms-3") {
        # 转换缓存
        $item.CanDelete = $true
        $item.Reason = "转换缓存（可安全删除）"
        $itemsToClean += $item
    } elseif ($dir.Name -match "modules-") {
        # 依赖库缓存
        if ($CleanAll) {
            $item.CanDelete = $true
            $item.Reason = "依赖库缓存（彻底清理模式）"
            $itemsToClean += $item
        } else {
            $item.CanDelete = $false
            $item.Reason = "依赖库缓存（建议保留）"
            $itemsToKeep += $item
        }
    } elseif ($dir.Name -match "jars-") {
        # JAR文件缓存
        if ($CleanAll) {
            $item.CanDelete = $true
            $item.Reason = "JAR文件缓存（彻底清理模式）"
            $itemsToClean += $item
        } else {
            $item.CanDelete = $false
            $item.Reason = "JAR文件缓存（建议保留）"
            $itemsToKeep += $item
        }
    } else {
        # 其他目录，默认保留
        $item.CanDelete = $false
        $item.Reason = "未知目录，保留"
        $itemsToKeep += $item
    }
}

$totalSizeGB = [math]::Round($totalSize / 1GB, 2)
$cleanSize = ($itemsToClean | Measure-Object -Property Size -Sum).Sum
$cleanSizeGB = [math]::Round($cleanSize / 1GB, 2)

# 3. 显示分析结果
Write-ColorOutput Cyan "=========================================="
Write-ColorOutput Cyan "  分析结果"
Write-ColorOutput Cyan "=========================================="
Write-Host ""
Write-ColorOutput Green "总缓存大小: $totalSizeGB GB"
Write-ColorOutput Yellow "可清理大小: $cleanSizeGB GB"
Write-ColorOutput Green "预计剩余: $([math]::Round(($totalSize - $cleanSize) / 1GB, 2)) GB"
Write-Host ""

# 4. 显示要清理的目录
if ($itemsToClean.Count -gt 0) {
    Write-ColorOutput Yellow "=========================================="
    Write-ColorOutput Yellow "  将清理以下目录（共 $($itemsToClean.Count) 个）"
    Write-ColorOutput Yellow "=========================================="
    Write-Host ""
    
    foreach ($item in $itemsToClean | Sort-Object SizeGB -Descending) {
        Write-Host "  ❌ $($item.Name.PadRight(20)) $($item.SizeGB.ToString('0.00').PadLeft(6)) GB - $($item.Reason)"
    }
    Write-Host ""
} else {
    Write-ColorOutput Green "✅ 没有需要清理的缓存目录"
    Write-Host ""
}

# 5. 显示要保留的目录
if ($itemsToKeep.Count -gt 0) {
    Write-ColorOutput Green "=========================================="
    Write-ColorOutput Green "  将保留以下目录（共 $($itemsToKeep.Count) 个）"
    Write-ColorOutput Green "=========================================="
    Write-Host ""
    
    foreach ($item in $itemsToKeep | Sort-Object SizeGB -Descending) {
        Write-Host "  ✅ $($item.Name.PadRight(20)) $($item.SizeGB.ToString('0.00').PadLeft(6)) GB - $($item.Reason)"
    }
    Write-Host ""
}

# 6. 确认删除
if ($itemsToClean.Count -gt 0) {
    if ($DryRun) {
        Write-ColorOutput Yellow "=========================================="
        Write-ColorOutput Yellow "  预览模式 - 不会实际删除文件"
        Write-ColorOutput Yellow "=========================================="
        Write-Host ""
        Write-ColorOutput Yellow "要实际删除，请运行: .\scripts\cleanup_gradle_cache.ps1"
    } else {
        Write-ColorOutput Yellow "=========================================="
        Write-ColorOutput Yellow "  准备清理 $cleanSizeGB GB 缓存"
        Write-ColorOutput Yellow "=========================================="
        Write-Host ""
        Write-ColorOutput Red "⚠️  警告: 这将删除上述缓存目录！"
        Write-Host ""
        Write-ColorOutput Yellow "清理后首次构建可能需要重新下载依赖。"
        Write-Host ""
        
        $confirm = Read-Host "确认删除？(Y/N)"
        if ($confirm -ne "Y" -and $confirm -ne "y") {
            Write-ColorOutput Yellow "已取消清理操作"
            exit 0
        }
        
        Write-Host ""
        Write-ColorOutput Yellow "正在清理..."
        
        # 7. 停止Gradle守护进程
        Write-ColorOutput Yellow "正在停止Gradle守护进程..."
        $gradlewPath = Get-ChildItem -Path . -Recurse -Filter "gradlew.bat" -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($gradlewPath) {
            Push-Location $gradlewPath.Directory.Parent.FullName
            try {
                & .\gradlew.bat --stop 2>&1 | Out-Null
                Write-ColorOutput Green "✅ Gradle守护进程已停止"
            } catch {
                Write-ColorOutput Yellow "⚠️  无法停止Gradle守护进程（可能未运行）"
            }
            Pop-Location
        }
        
        # 8. 删除目录
        $deletedCount = 0
        $deletedSize = 0
        
        foreach ($item in $itemsToClean) {
            try {
                Write-ColorOutput Yellow "正在删除: $($item.Name)..."
                Remove-Item -Path $item.Path -Recurse -Force -ErrorAction Stop
                $deletedCount++
                $deletedSize += $item.Size
                Write-ColorOutput Green "  ✅ 已删除: $($item.Name) ($($item.SizeGB) GB)"
            } catch {
                Write-ColorOutput Red "  ❌ 删除失败: $($item.Name) - $($_.Exception.Message)"
            }
        }
        
        Write-Host ""
        Write-ColorOutput Green "=========================================="
        Write-ColorOutput Green "  清理完成！"
        Write-ColorOutput Green "=========================================="
        Write-Host ""
        Write-ColorOutput Green "已删除 $deletedCount 个目录"
        Write-ColorOutput Green "释放空间: $([math]::Round($deletedSize / 1GB, 2)) GB"
        Write-Host ""
        Write-ColorOutput Yellow "提示: 下次构建时会重新生成缓存"
    }
} else {
    Write-ColorOutput Green "✅ 没有需要清理的缓存"
}

Write-Host ""

# 9. 使用说明
Write-ColorOutput Cyan "=========================================="
Write-ColorOutput Cyan "  使用说明"
Write-ColorOutput Cyan "=========================================="
Write-Host ""
Write-Host "预览模式（不实际删除）:"
Write-Host "  .\scripts\cleanup_gradle_cache.ps1 -DryRun"
Write-Host ""
Write-Host "彻底清理（包括依赖库缓存）:"
Write-Host "  .\scripts\cleanup_gradle_cache.ps1 -CleanAll"
Write-Host ""
Write-Host "自定义Gradle目录:"
Write-Host "  .\scripts\cleanup_gradle_cache.ps1 -GradleHome 'C:\path\to\.gradle'"
Write-Host ""

