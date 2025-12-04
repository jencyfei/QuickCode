# 默认短信应用按钮点击无反应问题修复

## 问题描述

点击"设置为默认短信应用"按钮后无反应。

## 问题原因

1. **Intent可能无法解析**：某些设备或系统版本可能不支持`ACTION_CHANGE_DEFAULT` Intent
2. **缺少错误提示**：即使Intent启动失败，用户也看不到任何提示
3. **缺少备用方案**：如果首选方法失败，没有备用方案

## 修复方案

### 1. 添加Intent可解析性检查

在启动Intent之前，检查系统是否有Activity可以处理该Intent：

```kotlin
val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
if (resolveInfo != null) {
    // Intent可以解析，启动Activity
    context.startActivity(intent)
} else {
    // Intent无法解析，使用备用方案
}
```

### 2. 添加多重备用方案

**方法1**：使用 `ACTION_CHANGE_DEFAULT`（首选）
- 直接打开默认短信应用选择页面，并预先选择本应用

**方法2**：使用 `ACTION_MANAGE_DEFAULT_APPS_SETTINGS`（备用）
- 打开系统默认应用设置页面，用户需要手动选择"短信"应用

**方法3**：显示错误提示（最后手段）
- 如果以上方法都不可用，显示提示信息引导用户手动设置

### 3. 添加Toast提示

- 成功启动：显示"正在打开设置页面..."
- 使用备用方案：显示"请选择'短信'应用，然后选择'QuickCode'"
- 失败：显示具体错误信息

### 4. 添加FLAG_ACTIVITY_NEW_TASK标志

确保Intent可以在非Activity上下文中启动：

```kotlin
intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
```

## 修复后的代码结构

```kotlin
fun launchDefaultSmsSettings() {
    try {
        // 方法1: 尝试使用 ACTION_CHANGE_DEFAULT
        val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
            putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (resolveInfo != null) {
            context.startActivity(intent)
            showToast = "正在打开设置页面..."
            return
        }
        
        // 方法2: 使用备用方案
        val settingsIntent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        // ... 检查并启动备用Intent
        
    } catch (e: Exception) {
        // 显示错误提示
        showToast = "打开设置失败: ${e.message}"
    }
}
```

## 修改的文件

- `android/app/src/main/java/com/sms/tagger/ui/screens/DefaultSmsGuideScreen.kt`

## 新增的导入

```kotlin
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
```

## 测试验证

1. ✅ 编译成功
2. ⏳ 需要在设备上测试：
   - 点击按钮后是否能正常打开设置页面
   - Toast提示是否正确显示
   - 备用方案是否正常工作

## 后续优化建议

1. 根据设备品牌和系统版本选择不同的策略
2. 添加更多日志记录，便于问题排查
3. 考虑添加引导动画或加载状态

---

**修复日期**: 2025-12-01  
**版本**: v1.4.0

