# 付费买断制功能开发 TODO

## 需求概述
为短信管理APP添加付费买断制功能，采用**机器码绑定 + 有限激活码**方案。
- **买断价格**：12元/码（一次性付费）
- **激活次数**：每个码默认限3次激活
- **到期日期**：2026年1月后提示购买激活码
- **核心原则**：纯本地运行、无需联网、确保隐私安全

## 开发任务清单

### 阶段1: 基础架构（设备ID与激活码生成）

#### ✅ TODO-1: 创建设备ID生成工具
**文件**: `android/app/src/main/java/com/sms/tagger/util/DeviceIdManager.kt`

**任务**:
- [ ] 使用 `Settings.Secure.ANDROID_ID` 作为基础设备标识
- [ ] 实现 `SHA-256` 哈希加密
- [ ] 提供 `getDeviceId(context: Context): String` 方法
- [ ] 处理异常情况（ANDROID_ID 为 null 的情况）

**参考代码**:
```kotlin
import android.provider.Settings
import java.security.MessageDigest

fun getDeviceId(context: Context): String {
    val androidId = Settings.Secure.getString(
        context.contentResolver, 
        Settings.Secure.ANDROID_ID
    ) ?: "fallback_device_id_${System.currentTimeMillis()}"
    
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(androidId.toByteArray())
    return hash.joinToString("") { "%02x".format(it) }
}
```

---

#### ✅ TODO-2: 创建激活码生成工具（开发者侧）
**文件**: `tools/generate_activation_code.py`

**任务**:
- [ ] 创建Python脚本用于开发者生成激活码
- [ ] 使用 `cryptography.fernet` 进行加密
- [ ] 实现 `generate_activation_code(device_id, max_activations=3)` 函数
- [ ] 生成格式：`设备ID:激活次数:随机盐`
- [ ] Base64编码输出激活码
- [ ] 保存密钥到安全位置（`.env` 或配置文件）
- [ ] 添加命令行参数支持

**使用场景**: 用户支付12元后，提供设备ID，开发者运行此脚本生成激活码

---

### 阶段2: 激活状态管理（APP内核心逻辑）

#### ✅ TODO-3: 创建激活状态管理器
**文件**: `android/app/src/main/java/com/sms/tagger/util/ActivationManager.kt`

**任务**:
- [ ] 使用 `SharedPreferences` 或加密文件存储激活状态
- [ ] 实现 `validateActivationCode(code: String): Result<String>` 方法
  - 解密激活码
  - 验证设备ID匹配
  - 检查激活次数是否 > 0
  - 递减激活次数并保存
- [ ] 实现 `isActivated(): Boolean` 方法
  - 检查本地存储的激活状态
  - 验证设备ID是否匹配
  - 检查剩余激活次数 >= 0
- [ ] 实现 `getRemainingActivations(): Int` 方法
- [ ] 实现 `getDeviceIdForUser(): String` 方法（供用户提供给开发者）
- [ ] 添加激活文件完整性校验（哈希校验防篡改）
- [ ] 错误处理（无效码、设备不匹配、次数用尽等）

**存储结构**:
```kotlin
data class ActivationData(
    val code: String,           // 激活码
    val deviceId: String,       // 设备ID
    val remaining: Int,         // 剩余激活次数
    val activatedAt: Long,      // 激活时间戳
    val checksum: String        // 完整性校验和
)
```

---

### 阶段3: 用户界面（激活UI）

#### ✅ TODO-4: 创建激活对话框组件
**文件**: `android/app/src/main/java/com/sms/tagger/ui/screens/ActivationDialog.kt`

**任务**:
- [ ] 创建 Compose Dialog 组件
- [ ] 添加激活码输入 TextField
- [ ] 显示设备ID供用户复制（用于提供给开发者）
- [ ] 添加"复制设备ID"按钮
- [ ] 添加"激活"和"取消"按钮
- [ ] 显示错误提示（设备不匹配、次数用尽、无效码等）
- [ ] 显示激活成功提示
- [ ] 加载状态显示

**UI设计要点**:
- 使用与现有UI风格一致的毛玻璃效果
- 清晰的错误提示
- 友好的用户引导

---

#### ✅ TODO-5: 集成激活流程到主界面
**文件**: `android/app/src/main/java/com/sms/tagger/MainActivity.kt`

**任务**:
- [ ] 在 `MainAppScreen` 中检查激活状态
- [ ] 首次启动时，如果未激活且已过期，显示激活弹窗
- [ ] 修改现有的 `BetaExpirationDialog` 逻辑，与激活弹窗集成
  - 到期后如果未激活：显示激活弹窗
  - 点击取消：继续基本功能，但高级功能锁定
- [ ] 在设置页面添加激活入口
- [ ] 激活成功后，更新UI状态

**集成逻辑**:
```kotlin
// 伪代码
val isActivated = ActivationManager.isActivated()
val isExpired = checkExpiry() // 2026年1月后
val showActivation = !isActivated && isExpired

if (showActivation) {
    ActivationDialog(...)
}
```

---

### 阶段4: 功能锁定（高级功能保护）

#### ✅ TODO-6: 锁定高级功能
**需要修改的文件**:
- `android/app/src/main/java/com/sms/tagger/ui/screens/SmsListScreen.kt`
- `android/app/src/main/java/com/sms/tagger/ui/screens/ExpressScreen.kt`
- `android/app/src/main/java/com/sms/tagger/ui/screens/TagManageScreen.kt`
- `android/app/src/main/java/com/sms/tagger/data/repository/SmsRepository.kt`

**任务**:
- [ ] 识别所有高级功能点：
  - [ ] 批量删除短信
  - [ ] 批量添加标签
  - [ ] 批量处理（如果有）
  - [ ] 批量打标
  - [ ] 其他高级功能
- [ ] 在每个高级功能处添加 `ActivationManager.isActivated()` 检查
- [ ] 未激活时：
  - 禁用相关按钮
  - 显示"请激活买断版"提示
  - 引导用户到激活页面
- [ ] 激活后：正常启用所有功能

**示例**:
```kotlin
fun onBatchDeleteClick() {
    if (!ActivationManager.isActivated()) {
        // 显示激活提示
        showActivationRequiredDialog()
        return
    }
    // 执行批量删除
}
```

---

### 阶段5: 设置页面更新

#### ✅ TODO-7: 更新设置页面
**文件**: `android/app/src/main/java/com/sms/tagger/ui/screens/SettingsScreen.kt`

**任务**:
- [ ] 添加激活状态显示卡片
  - 显示"已激活"或"未激活"状态
  - 显示剩余激活次数
  - 显示激活时间
- [ ] 添加设备ID显示（可复制）
- [ ] 添加"激活/重新激活"按钮（如果未激活）
- [ ] 添加买断制说明：
  - "本APP买断制：12元/码，一码一机，限3次激活"
  - 购买方式说明
- [ ] 集成现有的软件声明和反馈邮箱（709662224@qq.com）
- [ ] 添加"换设备"说明

---

### 阶段6: 到期弹窗集成

#### ✅ TODO-8: 更新到期弹窗逻辑
**文件**: `android/app/src/main/java/com/sms/tagger/MainActivity.kt`

**任务**:
- [ ] 修改现有的 `expirationDate` 检查逻辑（从2025年11月24日改为2026年1月1日）
- [ ] 到期后检查激活状态：
  - 如果已激活：不显示弹窗
  - 如果未激活：显示激活弹窗（替换现有的Beta弹窗）
- [ ] 点击取消后：继续基本功能，但高级功能锁定
- [ ] 保持与现有 `BetaExpirationDialog` 的视觉一致性

---

### 阶段7: 安全增强

#### ✅ TODO-9: 添加代码混淆和安全措施
**文件**: `android/app/proguard-rules.pro` 和 `android/app/build.gradle`

**任务**:
- [ ] 在 `build.gradle` 中启用代码混淆（ProGuard/R8）
- [ ] 在 `proguard-rules.pro` 中添加激活相关类的混淆规则
- [ ] 防止关键激活代码被反编译
- [ ] 对激活文件添加哈希校验和防篡改
- [ ] 考虑使用 `androidx.security.crypto.EncryptedSharedPreferences` 加密存储

**注意**: 保护密钥安全，不要将密钥硬编码在代码中（可使用 BuildConfig 或资源文件）

---

### 阶段8: 测试

#### ✅ TODO-10: 单元测试
**文件**: `android/app/src/test/java/com/sms/tagger/util/ActivationManagerTest.kt`

**任务**:
- [ ] 测试设备ID生成（模拟不同 ANDROID_ID）
- [ ] 测试激活码验证逻辑
- [ ] 测试激活次数递减
- [ ] 测试设备不匹配场景
- [ ] 测试激活码格式错误场景
- [ ] 测试激活文件完整性校验

---

#### ✅ TODO-11: 端到端测试
**测试场景**:
- [ ] 在真实设备上安装APP
- [ ] 测试首次启动激活流程
- [ ] 测试激活码输入和验证
- [ ] 测试激活后高级功能解锁
- [ ] 测试重装APP后激活次数递减
- [ ] 测试文件复制到新设备后失效（设备不匹配）
- [ ] 测试到期弹窗和激活弹窗交互
- [ ] 测试无效码、超期弹窗、文件篡改等边缘案例
- [ ] 测试多设备场景（不同ANDROID_ID）

---

### 阶段9: 文档更新

#### ✅ TODO-12: 文档更新
**需要创建/更新的文件**:
- `README.md`
- `docs/ACTIVATION_USER_GUIDE.md`（用户使用指南）
- `docs/ACTIVATION_DEVELOPER_GUIDE.md`（开发者激活码生成指南）
- `docs/ACTIVATION_TODO.md`（本文档）

**任务**:
- [ ] 更新 README.md 添加买断制说明
- [ ] 创建用户使用指南：
  - 如何获取设备ID
  - 如何购买激活码（12元）
  - 如何输入激活码
  - 常见问题（换设备、次数用尽等）
- [ ] 创建开发者激活码生成工具使用说明
- [ ] 更新软件声明和法律合规说明
- [ ] 添加"一码一机"说明
- [ ] 说明退款政策（如码失效）

---

## 开发优先级

### 高优先级（核心功能）
1. TODO-1: 设备ID生成工具
2. TODO-3: 激活状态管理器
3. TODO-4: 激活对话框组件
4. TODO-5: 集成激活流程到主界面
5. TODO-6: 锁定高级功能

### 中优先级（完善体验）
6. TODO-2: 激活码生成工具（开发者侧）
7. TODO-7: 更新设置页面
8. TODO-8: 更新到期弹窗逻辑

### 低优先级（优化和测试）
9. TODO-9: 代码混淆和安全增强
10. TODO-10: 单元测试
11. TODO-11: 端到端测试
12. TODO-12: 文档更新

---

## 技术栈

### Android端（Kotlin）
- **设备ID**: `Settings.Secure.ANDROID_ID` + SHA-256
- **加密存储**: `SharedPreferences` 或 `EncryptedSharedPreferences`
- **UI框架**: Jetpack Compose
- **加密库**: `javax.crypto` 或 `androidx.security.crypto`

### 开发者侧（Python）
- **加密库**: `cryptography.fernet`
- **格式**: Base64编码的加密字符串

---

## 注意事项

### 安全
- ✅ 密钥保密，不要硬编码在代码中
- ✅ 使用代码混淆防止逆向工程
- ✅ 添加激活文件完整性校验
- ✅ 防止简单的文件篡改

### 用户体验
- ✅ 清晰的错误提示（设备不匹配、次数用尽等）
- ✅ 友好的激活引导流程
- ✅ 激活后无需重复输入

### 用户支持
- ✅ 处理"换设备"请求（手动发新码，收费或免费）
- ✅ 处理退款（如码失效）
- ✅ 提供清晰的购买和激活说明

### 兼容性
- ✅ 未来版本兼容旧激活码
- ✅ 考虑Android版本兼容性（ANDROID_ID在不同版本的行为）

---

## 参考资料
- 原始需求文档: `docs/加密.md`
- Android Secure ID: https://developer.android.com/reference/android/provider/Settings.Secure#ANDROID_ID
- Fernet 加密: https://cryptography.io/en/latest/fernet/
- EncryptedSharedPreferences: https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences

---

## 更新记录
- 2025-11-25: 创建初始TODO列表

下一步建议
按优先级开始开发：先完成 TODO-1 到 TODO-6（核心功能）
并行开发：TODO-2（开发者工具）可与 TODO-3（APP内逻辑）并行
测试驱动：完成核心功能后进行端到端测试
可以开始实现这些任务。需要我继续实现某个具体任务吗？
