# 体验版（Trial）实现计划 - 执行摘要

## 🎯 目标

在不影响当前激活版功能的前提下，通过 `productFlavors` 创建体验版（Trial），满足应用商店上架要求。

---

## 📊 核心策略

### 1. 代码隔离
- ✅ 使用 `BuildConfig.IS_TRIAL` 条件判断隔离 Trial 和 Full 版代码
- ✅ 所有新增代码不影响 Full 版编译和运行

### 2. 构建分离
- ✅ 通过 `productFlavors` 生成两个独立 APK
- ✅ 包名不同：Full 版 `com.sms.tagger`，Trial 版 `com.sms.tagger.trial`
- ✅ 可同时安装在设备上，互不干扰

### 3. 功能差异

| 功能点 | Full 版 | Trial 版 |
|--------|---------|----------|
| 激活机制 | ✅ 支持 | ❌ 完全隐藏 |
| 有效期 | ❌ 无限制 | ✅ 15 天 |
| 每日识别 | 激活后不限 | 2 次/天 |
| 历史记录 | 激活后全部 | 3 条 |
| 付费引导 | ✅ 有 | ❌ 无 |

---

## 🔧 关键技术点

### 1. BuildConfig 使用
```kotlin
if (BuildConfig.IS_TRIAL) {
    // Trial 版逻辑
} else {
    // Full 版逻辑（原有代码）
}
```

### 2. 激活状态处理
- Trial 版：`ActivationManager.isActivated()` 永远返回 `false`
- Full 版：保持原有逻辑不变

### 3. 限制逻辑增强
- Trial 版：在现有限制基础上，添加 15 天有效期检查
- 过期后：所有功能锁定（识别次数 0，历史记录 0）

---

## 📋 实施阶段概览

### 阶段1: 构建配置（2-3 小时）
- 配置 `productFlavors`
- 设置应用名称和图标

### 阶段2: 有效期管理（2-3 小时）
- 创建 `TrialManager`
- 集成到主流程

### 阶段3: UI 修改（4-6 小时）
- 隐藏激活相关 UI
- 创建 Trial 版设置页面
- 创建过期提示弹窗

### 阶段4: 限制逻辑（2-3 小时）
- 增强 `UsageLimitManager`
- 禁用过期后的功能

### 阶段5: 隐私政策（1 小时）
- 调整 Trial 版文案

### 阶段6: 测试（4-6 小时）
- Full 版回归测试
- Trial 版功能测试
- 构建验证

### 阶段7: 文档（1 小时）
- 更新构建文档

**总预计时间：16-23 小时**

---

## ⚠️ 关键注意事项

### 代码修改原则

1. **任何新功能都必须用条件判断包裹**
   ```kotlin
   if (BuildConfig.IS_TRIAL) {
       // Trial 版新增代码
   }
   ```

2. **任何原有代码的修改必须保护 Full 版逻辑**
   ```kotlin
   if (!BuildConfig.IS_TRIAL) {
       // 原有 Full 版逻辑
   }
   ```

3. **Trial 版使用新的管理器，不修改 ActivationManager 的核心逻辑**
   ```kotlin
   // ❌ 错误：直接修改 ActivationManager
   // ✅ 正确：在调用处判断
   if (BuildConfig.IS_TRIAL) {
       return false // Trial 版永远未激活
   }
   return ActivationManager.isActivated(context) // Full 版原有逻辑
   ```

### 文案审核要求

**Trial 版必须避免的敏感词**：
- ❌ "购买"、"付费"、"激活"、"激活码"
- ❌ "扫码支付"、"加入 QQ 群付费"
- ❌ "永久版下载链接"

**Trial 版允许使用的替代词**：
- ✅ "联系开发者"、"技术支持"
- ✅ "体验版"、"试用"、"有效期"
- ✅ "部分功能已限制，仅供体验"

---

## 🚀 快速开始

### 1. 配置 Gradle（第一步）

在 `android/app/build.gradle` 中添加：

```gradle
android {
    // ... existing code ...
    
    flavorDimensions = ["version"]
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
}
```

### 2. 测试构建

```bash
# 构建 Full 版
./gradlew assembleFullRelease

# 构建 Trial 版
./gradlew assembleTrialRelease
```

### 3. 验证包名

```bash
# Full 版 APK 包名应该是：com.sms.tagger
# Trial 版 APK 包名应该是：com.sms.tagger.trial
```

---

## ✅ 验收标准

### Full 版验证
- [ ] 所有原有功能正常
- [ ] 激活流程完整可用
- [ ] 限制逻辑正确
- [ ] 设置页面显示激活相关内容

### Trial 版验证
- [ ] 15 天有效期逻辑正确
- [ ] 过期后功能锁定
- [ ] 无任何激活相关 UI
- [ ] 无任何付费引导文案
- [ ] 限制逻辑正确（2 次/天，3 条历史记录）

### 构建验证
- [ ] 两个版本可独立构建
- [ ] 两个 APK 可同时安装
- [ ] 包名和应用名称不同

### 商店审核准备
- [ ] 应用名称包含"体验版"
- [ ] 无敏感词
- [ ] 隐私政策符合要求

---

## 📚 详细文档

完整的任务清单和实现细节，请参考：
- [TRIAL_VERSION_TODO.md](TRIAL_VERSION_TODO.md) - 详细的 TODO 清单

---

**最后更新**: 2025-11-28  
**状态**: 🟡 待开始

