# Build Capacitor Web APK
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Build Capacitor Web APK" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$ErrorActionPreference = "Stop"
$rootDir = $PSScriptRoot

try {
    # Step 1: Build frontend
    Write-Host "[1/5] Building frontend..." -ForegroundColor Yellow
    Set-Location "$rootDir\frontend"
    npm run build
    if ($LASTEXITCODE -ne 0) { throw "Frontend build failed" }

    # Step 2: Sync to Capacitor
    Write-Host ""
    Write-Host "[2/5] Syncing to Capacitor..." -ForegroundColor Yellow
    npx cap sync android
    if ($LASTEXITCODE -ne 0) { throw "Capacitor sync failed" }

    # Step 3: Fix Gradle versions
    Write-Host ""
    Write-Host "[3/5] Fixing Gradle compatibility..." -ForegroundColor Yellow
    Set-Location "$rootDir\frontend\android"
    
    $cordovaPluginBuild = "capacitor-cordova-android-plugins\build.gradle"
    if (Test-Path $cordovaPluginBuild) {
        (Get-Content $cordovaPluginBuild) -replace '8\.7\.2', '8.1.2' | Set-Content $cordovaPluginBuild
        Write-Host "Fixed Gradle version in capacitor-cordova-android-plugins" -ForegroundColor Green
    }

    # Step 4: Clean
    Write-Host ""
    Write-Host "[4/5] Cleaning old builds..." -ForegroundColor Yellow
    .\gradlew.bat clean --no-daemon

    # Step 5: Build APK
    Write-Host ""
    Write-Host "[5/5] Building APK..." -ForegroundColor Yellow
    .\gradlew.bat assembleDebug --no-daemon

    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "  Build Success!" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "APK Location:" -ForegroundColor Cyan
        Write-Host "  $rootDir\frontend\android\app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor White
        Write-Host ""
        Write-Host "Install to phone:" -ForegroundColor Cyan
        Write-Host "  adb install -r app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor White
        Write-Host ""
    } else {
        throw "APK build failed"
    }

} catch {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "  Build Failed!" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host ""
    Set-Location $rootDir
    exit 1
}

Set-Location $rootDir
