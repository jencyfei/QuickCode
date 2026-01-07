# Trial 版过期限制修复说明

## 问题描述

之前的实现中，Trial 版的 15 天试用期仅存储在应用的 SharedPreferences 中。当用户卸载应用后重装，SharedPreferences 数据会被清除，导致试用期重新开始计算，用户可以无限次通过卸载重装来规避试用期限制。

## 修复方案

### 核心改进

1. **使用设备ID追踪试用期**
   - 将试用期开始时间与设备ID绑定
   - 存储格式：`trial_start_time_{deviceId}` -> 首次使用时间戳
   - 即使卸载重装，只要设备ID相同，就能检测到之前的使用记录

2. **向后兼容**
   - 保留对旧版本数据的支持（`trial_start_time` 键）
   - 自动迁移旧数据到新格式
   - 确保现有用户不受影响

### 技术实现

**修改文件**: `android/app/src/main/java/com/sms/tagger/util/TrialManager.kt`

**关键变更**:
- `ensureTrialStartTime()`: 使用设备ID作为键的一部分
- `getTrialStartTime()`: 优先查找设备ID对应的记录，回退到旧格式
- 新增 `resetAll()`: 用于测试，清除所有设备的试用期记录

### 工作原理

1. **首次安装**:
   ```
   设备ID: abc123...
   存储: trial_start_time_abc123... -> 2025-12-08 10:00:00
   ```

2. **卸载重装**:
   ```
   设备ID: abc123... (相同)
   检测: trial_start_time_abc123... 已存在
   使用: 已存储的时间戳（2025-12-08 10:00:00）
   结果: 试用期继续计算，不会重置
   ```

3. **新设备**:
   ```
   设备ID: xyz789... (不同)
   检测: trial_start_time_xyz789... 不存在
   存储: trial_start_time_xyz789... -> 当前时间
   结果: 新设备获得 15 天试用期
   ```

## 设备ID说明

- **来源**: `Settings.Secure.ANDROID_ID`（经过 SHA-256 哈希）
- **稳定性**: 
  - ✅ 卸载重装后保持不变
  - ✅ 应用更新后保持不变
  - ❌ 恢复出厂设置后会改变
  - ❌ 刷机后可能会改变

## 测试验证

### 测试场景 1: 正常使用
1. 首次安装 Trial 版
2. 记录试用期开始时间
3. 使用应用，验证剩余天数递减
4. ✅ 预期：15 天后过期

### 测试场景 2: 卸载重装规避（修复前）
1. 安装 Trial 版，使用 10 天
2. 卸载应用
3. 重新安装
4. ❌ 修复前：试用期重新开始（剩余 15 天）
5. ✅ 修复后：试用期继续（剩余 5 天）

### 测试场景 3: 多设备支持
1. 设备 A 安装使用 5 天
2. 设备 B 首次安装
3. ✅ 预期：设备 B 获得完整的 15 天试用期

### 测试场景 4: 向后兼容
1. 使用旧版本安装，已有 `trial_start_time` 记录
2. 升级到新版本
3. ✅ 预期：自动迁移到新格式，试用期不受影响

## 注意事项

1. **设备ID限制**:
   - 如果用户恢复出厂设置或刷机，设备ID可能改变，试用期会重新开始
   - 这是 Android 系统的限制，无法完全避免

2. **隐私考虑**:
   - 设备ID 使用 SHA-256 哈希，不直接暴露原始 ANDROID_ID
   - 所有数据存储在本地，不上传到服务器

3. **测试工具**:
   - `TrialManager.reset(context)`: 清除当前设备的试用期记录
   - `TrialManager.resetAll(context)`: 清除所有设备的试用期记录（仅用于测试）

## 验证步骤

1. **编译项目**:
   ```bash
   cd android
   ./gradlew assembleTrialRelease
   ```

2. **安装 APK**:
   ```bash
   adb install app/build/outputs/apk/trial/release/app-trial-release.apk
   ```

3. **验证试用期**:
   - 打开应用，查看设置页面显示的剩余天数
   - 记录设备ID（设置页面可查看）

4. **测试卸载重装**:
   ```bash
   adb uninstall com.sms.tagger.trial
   adb install app/build/outputs/apk/trial/release/app-trial-release.apk
   ```

5. **验证结果**:
   - 打开应用，检查剩余天数是否与卸载前一致
   - ✅ 如果一致，说明修复成功
   - ❌ 如果重置为 15 天，说明修复失败

## 相关文件

- `android/app/src/main/java/com/sms/tagger/util/TrialManager.kt` - 核心实现
- `android/app/src/main/java/com/sms/tagger/util/DeviceIdManager.kt` - 设备ID管理
- `android/app/src/main/java/com/sms/tagger/MainActivity.kt` - 主界面，调用试用期检查
- `android/app/src/main/java/com/sms/tagger/ui/screens/ExpressScreen.kt` - 快递页面，检查试用期
- `android/app/src/main/java/com/sms/tagger/ui/screens/SettingsScreen.kt` - 设置页面，显示剩余天数

## 更新日期

2025-12-08

