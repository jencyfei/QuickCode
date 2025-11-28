# 体验版（Trial）开发 TODO 清单

## 📋 总体原则

1. **不影响现有激活版功能**：所有改动都通过 `BuildConfig.IS_TRIAL` 条件判断
2. **使用 productFlavors**：通过 Gradle 构建配置分离 trial 和 full 版本
3. **代码隔离**：Trial 版逻辑通过条件编译隐藏，不影响 Full 版

---

## 阶段1: 构建配置（Gradle ProductFlavors）

### ✅ TODO-1.1: 配置 productFlavors

**文件**: `android/app/build.gradle`

**任务**:
- [ ] 在 `android {}` 块中添加 `productFlavors` 配置
- [ ] 创建 `full` flavor（完整版，默认）
- [ ] 创建 `trial` flavor（体验版）
  - `applicationIdSuffix = ".trial"`
  - `versionNameSuffix = "-trial"`
  - `buildConfigField "boolean", "IS_TRIAL", "true"`
- [ ] Full 版设置 `buildConfigField "boolean", "IS_TRIAL", "false"`

**预期效果**:
```gradle
productFlavors {
    full {
        dimension = "version"
        buildConfigField "boolean", "IS_TRIAL", "false"
    }
    trial {
        dimension = "version"
        applicationIdSuffix ".trial"
        versionNameSuffix "-trial"
        buildConfigField "boolean", "IS_TRIAL", "true"
    }
}
```

**验证**:
- [ ] 运行 `./gradlew assembleFullRelease` 生成完整版 APK
- [ ] 运行 `./gradlew assembleTrialRelease` 生成体验版 APK
- [ ] 验证两个 APK 的包名不同（Full: `com.sms.tagger`, Trial: `com.sms.tagger.trial`）

---

### ✅ TODO-1.2: 更新应用名称和图标（Trial 版）

**文件**: `android/app/src/main/res/values/strings.xml`（可能需要创建 flavor-specific 版本）

**任务**:
- [ ] 创建 `android/app/src/trial/res/values/strings.xml`
- [ ] 设置应用名称：`快递取件码助手（体验版）`
- [ ] 创建 Trial 版图标（可选，在图标角落添加 "体验" 或 "Trial" 标记）
- [ ] 图标路径：`android/app/src/trial/res/mipmap-*/ic_launcher.png`

**注意**: Full 版保持原有名称和图标不变

---

## 阶段2: Trial 有效期管理

### ✅ TODO-2.1: 创建 TrialManager

**文件**: `android/app/src/main/java/com/sms/tagger/util/TrialManager.kt`（新建）

**任务**:
- [ ] 创建 `TrialManager` object
- [ ] 实现 `getTrialStartTime(context: Context): Long?` - 获取首次启动时间戳
- [ ] 实现 `setTrialStartTime(context: Context)` - 记录首次启动时间（仅在首次调用时写入）
- [ ] 实现 `getRemainingDays(context: Context): Int` - 计算剩余天数（15 - 已用天数）
- [ ] 实现 `isTrialExpired(context: Context): Boolean` - 判断是否过期
- [ ] 使用 `SharedPreferences` 存储，Key: `trial_start_time`

**代码结构**:
```kotlin
object TrialManager {
    private const val PREF_NAME = "trial_prefs"
    private const val KEY_START_TIME = "trial_start_time"
    private const val TRIAL_DURATION_DAYS = 15
    
    fun setTrialStartTime(context: Context) { /* 首次启动时记录 */ }
    fun getRemainingDays(context: Context): Int { /* 返回剩余天数，0表示过期 */ }
    fun isTrialExpired(context: Context): Boolean { /* 返回 true 表示过期 */ }
}
```

**验证**:
- [ ] 首次启动后检查 `SharedPreferences` 是否有 `trial_start_time`
- [ ] 验证天数计算逻辑正确
- [ ] 验证过期判断逻辑

---

### ✅ TODO-2.2: 集成 Trial 有效期检查到主流程

**文件**: `android/app/src/main/java/com/sms/tagger/MainActivity.kt`

**任务**:
- [ ] 在 `MainAppScreen()` 中添加 Trial 版首次启动时间记录逻辑
  - 条件：`BuildConfig.IS_TRIAL && 首次启动`
  - 调用：`TrialManager.setTrialStartTime(context)`
- [ ] 添加 Trial 过期检查
  - 条件：`BuildConfig.IS_TRIAL && TrialManager.isTrialExpired(context)`
  - 显示过期提示弹窗（TODO-3.1 中实现）
- [ ] 确保 Full 版不受影响（使用 `!BuildConfig.IS_TRIAL` 保护原有逻辑）

**验证**:
- [ ] Full 版启动不受影响
- [ ] Trial 版首次启动记录时间
- [ ] Trial 版过期后显示提示

---

## 阶段3: UI 修改（隐藏激活相关内容）

### ✅ TODO-3.1: 创建 Trial 过期提示弹窗

**文件**: `android/app/src/main/java/com/sms/tagger/ui/components/TrialExpiredDialog.kt`（新建）

**任务**:
- [ ] 创建 Compose Dialog 组件
- [ ] 标题：「体验版功能已到期」
- [ ] 内容：「如需持续使用本应用，请联系开发者获取帮助。」
- [ ] 显示技术支持联系方式（QQ/邮箱）
- [ ] 只提供「确定」按钮（关闭弹窗）
- [ ] 符合商店审核的文案（不使用"购买/付费/激活码"等敏感词）

**文案要求**:
- ✅ 允许："如需技术支持，请联系开发者"
- ❌ 禁止："购买完整版"、"激活码"、"付费"等

---

### ✅ TODO-3.2: 修改 ActivationManager（Trial 版行为）

**文件**: `android/app/src/main/java/com/sms/tagger/util/ActivationManager.kt`

**任务**:
- [ ] 修改 `isActivated(context: Context): Boolean` 方法
  - 添加条件：`if (BuildConfig.IS_TRIAL) return false`
  - 确保 Trial 版永远返回 false
- [ ] 修改 `getActivationInfo(context: Context): ActivationInfo?` 方法
  - 添加条件：`if (BuildConfig.IS_TRIAL) return null`
- [ ] 修改 `validateActivationCode()` 方法（可选，Trial 版不应该调用）
  - 添加条件：`if (BuildConfig.IS_TRIAL) return Result.failure(...)`

**注意**: 确保所有修改都不影响 Full 版逻辑

**验证**:
- [ ] Full 版激活功能正常
- [ ] Trial 版 `isActivated()` 永远返回 false

---

### ✅ TODO-3.3: 修改设置页面（隐藏激活相关 UI）

**文件**: `android/app/src/main/java/com/sms/tagger/ui/screens/SettingsScreen.kt`

**任务**:
- [ ] 在 `SettingsHome()` 中添加条件判断：`if (!BuildConfig.IS_TRIAL) { ... }` 包裹激活相关 UI
- [ ] 隐藏以下内容（Trial 版）：
  - `BindDeviceCard` 整个卡片
  - 激活按钮
  - 设备 ID 显示
  - 激活状态显示
- [ ] 添加 Trial 版专属显示内容：
  - "当前版本：体验版 Trial"
  - "有效期剩余 X 天"（使用 `TrialManager.getRemainingDays()`）
  - "技术支持：QQ xxxxx / 邮箱 xxxxx"
- [ ] 创建 `TrialInfoCard` composable（Trial 版专用）
- [ ] 确保 Full 版显示内容不变

**UI 结构**:
```kotlin
if (BuildConfig.IS_TRIAL) {
    TrialInfoCard(
        remainingDays = TrialManager.getRemainingDays(context),
        onContactDeveloper = { /* 显示联系方式 */ }
    )
} else {
    BindDeviceCard(...) // 原有激活卡片
}
```

**验证**:
- [ ] Full 版设置页面显示激活相关内容
- [ ] Trial 版设置页面显示 Trial 信息，隐藏激活内容

---

### ✅ TODO-3.4: 隐藏激活弹窗（Trial 版）

**文件**: `android/app/src/main/java/com/sms/tagger/MainActivity.kt`

**任务**:
- [ ] 在 `MainAppScreen()` 中修改激活弹窗显示逻辑
  - 添加条件：`if (!BuildConfig.IS_TRIAL && isExpired && !isActivated)`
  - Trial 版不显示激活弹窗
- [ ] 确保 Full 版逻辑不受影响

**验证**:
- [ ] Full 版到期后显示激活弹窗
- [ ] Trial 版不显示激活弹窗

---

### ✅ TODO-3.5: 隐藏其他激活相关 UI

**文件**: 全局搜索激活相关 UI

**任务**:
- [ ] 搜索所有包含"激活"、"付费"、"购买"等敏感词的 UI 文本
- [ ] 在每个位置添加 `if (!BuildConfig.IS_TRIAL)` 条件判断
- [ ] 确保 Trial 版中完全隐藏这些内容

**需要检查的文件**:
- [ ] `ActivationDialog.kt` - 整个对话框在 Trial 版中不显示
- [ ] `SettingsScreen.kt` - 已处理
- [ ] `ExpressScreen.kt` - 检查是否有"去激活"按钮（如果有，需隐藏）
- [ ] `LimitDialogs.kt` - 检查是否有"立即激活"按钮（如果有，需隐藏或替换文案）

**文案替换规则**:
- ❌ "立即激活" → ✅ "联系开发者"
- ❌ "购买完整版" → ✅ "获取技术支持"
- ❌ "激活码" → ✅ 完全隐藏

---

## 阶段4: 限制逻辑调整（Trial 版）

### ✅ TODO-4.1: 修改 UsageLimitManager（支持 Trial 过期锁定）

**文件**: `android/app/src/main/java/com/sms/tagger/util/UsageLimitManager.kt`

**任务**:
- [ ] 在 `limitHistoryList()` 方法中添加 Trial 过期检查
  - 条件：`if (BuildConfig.IS_TRIAL && TrialManager.isTrialExpired(context)) return emptyList()`
  - Trial 过期后，历史记录限制为 0 条
- [ ] 在 `isDailyLimitReached()` 方法中添加 Trial 过期检查
  - 条件：`if (BuildConfig.IS_TRIAL && TrialManager.isTrialExpired(context)) return true`
  - Trial 过期后，识别次数限制为 0
- [ ] 确保 Full 版逻辑不受影响

**验证**:
- [ ] Full 版限制逻辑不变
- [ ] Trial 版过期后，历史记录为 0，识别次数为 0

---

### ✅ TODO-4.2: 禁用识别按钮（Trial 过期后）

**文件**: `android/app/src/main/java/com/sms/tagger/ui/screens/ExpressScreen.kt`

**任务**:
- [ ] 在识别按钮的逻辑中添加 Trial 过期检查
  - 条件：`if (BuildConfig.IS_TRIAL && TrialManager.isTrialExpired(context))`
  - 过期后禁用识别按钮，显示提示："体验版已到期"
- [ ] 在 `handleExtractExpress()` 方法开头添加过期检查
  - 过期后直接返回，不执行识别逻辑

**验证**:
- [ ] Trial 版过期后，识别按钮禁用
- [ ] Full 版不受影响

---

### ✅ TODO-4.3: 禁用批量操作（Trial 过期后）

**文件**: `android/app/src/main/java/com/sms/tagger/ui/screens/ExpressScreen.kt`

**任务**:
- [ ] 在"批量"和"一键"按钮的 `onClick` 中添加过期检查
  - 条件：`if (BuildConfig.IS_TRIAL && TrialManager.isTrialExpired(context))`
  - 过期后显示提示："体验版已到期，批量操作不可用"
- [ ] 确保 Full 版逻辑不受影响

**验证**:
- [ ] Trial 版过期后，批量操作不可用
- [ ] Full 版批量操作正常

---

## 阶段5: 隐私政策调整（Trial 版）

### ✅ TODO-5.1: 修改隐私政策文案（Trial 版）

**文件**: `android/app/src/main/java/com/sms/tagger/ui/components/PrivacyPolicyDialog.kt`

**任务**:
- [ ] 检查隐私政策文案中是否提及"付费"、"激活"等敏感词
- [ ] 如有，在 Trial 版中替换或移除这些内容
- [ ] 添加 Trial 版专属说明："本体验版无任何联网付费逻辑，所有功能均在本地完成"

**验证**:
- [ ] Trial 版隐私政策符合商店审核要求
- [ ] Full 版隐私政策保持不变

---

## 阶段6: 测试验证

### ✅ TODO-6.1: Full 版回归测试

**测试场景**:
- [ ] Full 版正常激活流程
- [ ] Full 版激活后所有功能正常
- [ ] Full 版限制逻辑正常（未激活状态）
- [ ] Full 版设置页面显示激活相关内容
- [ ] Full 版到期后显示激活弹窗

---

### ✅ TODO-6.2: Trial 版功能测试

**测试场景**:
- [ ] Trial 版首次启动记录时间
- [ ] Trial 版显示剩余天数（15 天开始倒计时）
- [ ] Trial 版限制逻辑正常（2 次/天，3 条历史记录）
- [ ] Trial 版过期后锁定所有功能
- [ ] Trial 版设置页面显示 Trial 信息，不显示激活相关内容
- [ ] Trial 版不显示激活弹窗
- [ ] Trial 版不显示任何"购买/付费/激活码"文案

---

### ✅ TODO-6.3: 构建验证

**测试场景**:
- [ ] `./gradlew assembleFullRelease` 成功生成 Full 版 APK
- [ ] `./gradlew assembleTrialRelease` 成功生成 Trial 版 APK
- [ ] 两个 APK 包名不同，可同时安装
- [ ] 两个 APK 功能互不干扰

---

### ✅ TODO-6.4: 商店审核准备

**检查清单**:
- [ ] 应用名称包含"体验版"标识
- [ ] 包名包含 `.trial` 后缀
- [ ] 无任何付费引导文案
- [ ] 无激活码相关内容
- [ ] 隐私政策明确说明无联网付费逻辑
- [ ] 所有敏感词已替换或移除

---

## 阶段7: 文档更新

### ✅ TODO-7.1: 更新构建文档

**文件**: 创建或更新 `docs/BUILD_GUIDE.md`

**任务**:
- [ ] 文档说明如何构建 Full 版和 Trial 版
- [ ] 说明两个版本的区别
- [ ] 说明如何测试两个版本

---

### ✅ TODO-7.2: 更新项目说明

**文件**: `README.md` 或项目文档

**任务**:
- [ ] 说明项目支持两种构建版本
- [ ] 说明版本差异和使用场景

---

## 📝 关键注意事项

### ⚠️ 代码隔离原则

1. **所有 Trial 版逻辑必须用 `BuildConfig.IS_TRIAL` 条件包裹**
2. **所有 Full 版原有逻辑必须用 `!BuildConfig.IS_TRIAL` 保护**
3. **新增 Trial 版功能不影响 Full 版编译和运行**

### ⚠️ 文案审核要求

**Trial 版禁止使用**:
- "购买"、"付费"、"激活"、"激活码"
- "扫码支付"、"加入 QQ 群付费"
- "永久版下载链接"

**Trial 版允许使用**:
- "体验版"、"试用"、"有效期"
- "联系开发者"、"技术支持"
- "部分功能已限制，仅供体验"

### ⚠️ 测试优先级

1. **优先保证 Full 版功能不受影响**（回归测试）
2. **然后验证 Trial 版功能符合需求**
3. **最后进行商店审核检查**

---

## 📊 进度跟踪

### 阶段1: 构建配置
- [ ] TODO-1.1: 配置 productFlavors
- [ ] TODO-1.2: 更新应用名称和图标

### 阶段2: Trial 有效期管理
- [ ] TODO-2.1: 创建 TrialManager
- [ ] TODO-2.2: 集成到主流程

### 阶段3: UI 修改
- [ ] TODO-3.1: 创建过期提示弹窗
- [ ] TODO-3.2: 修改 ActivationManager
- [ ] TODO-3.3: 修改设置页面
- [ ] TODO-3.4: 隐藏激活弹窗
- [ ] TODO-3.5: 隐藏其他激活相关 UI

### 阶段4: 限制逻辑调整
- [ ] TODO-4.1: 修改 UsageLimitManager
- [ ] TODO-4.2: 禁用识别按钮
- [ ] TODO-4.3: 禁用批量操作

### 阶段5: 隐私政策调整
- [ ] TODO-5.1: 修改隐私政策文案

### 阶段6: 测试验证
- [ ] TODO-6.1: Full 版回归测试
- [ ] TODO-6.2: Trial 版功能测试
- [ ] TODO-6.3: 构建验证
- [ ] TODO-6.4: 商店审核准备

### 阶段7: 文档更新
- [ ] TODO-7.1: 更新构建文档
- [ ] TODO-7.2: 更新项目说明

---

## 🎯 完成标准

当以下所有条件满足时，视为完成：

1. ✅ Full 版功能完全正常，不受任何影响
2. ✅ Trial 版可以独立构建和运行
3. ✅ Trial 版符合应用商店审核要求
4. ✅ 所有测试用例通过
5. ✅ 文档已更新

---

**最后更新**: 2025-11-28  
**状态**: 🟡 待开始

