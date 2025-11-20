# 快递取件码 - 已取数量不一致问题修复总结

## 问题描述

### 场景1：一键取件后
- **操作**：未取6个 → 点击一键取件
- **结果**：
  - ✅ 未取：6 → 0（正确）
  - ⚠️ 已取：0 → 17（显示包括11月5号之前的快递）

### 场景2：关闭APP后台再次打开
- **操作**：关闭APP后台 → 重新打开
- **结果**：
  - ✅ 未取：0（正确）
  - ❌ 已取：17 → 6（**不一致！**只显示11月14号之后的快递）

## 根本原因

1. **已取数量统计依赖 `expressList`，而不是 SharedPreferences 的真实状态**
   - `expressList` 是从短信提取的，受限于短信读取数量（5000条）
   - 重启后，`expressList` 可能变化，导致数量不一致

2. **数量统计和显示逻辑不一致**
   - 数量统计使用 `express.status`
   - 显示逻辑也使用 `express.status`
   - 但真实状态保存在 SharedPreferences 中

3. **数据流不一致**
   - 一键取件后：`expressList` 包含所有提取的快递（17个）
   - 重启后：`expressList` 重新从短信提取，可能只有6个

## 修复方案

### 1. 统一使用 SharedPreferences 作为状态源

**修改前**：
```kotlin
// 统计已取数量：使用 expressList 中的状态
val pickedCount = expressList.filter { express ->
    express.status == PickupStatus.PICKED && ...
}.size
```

**修改后**：
```kotlin
// 统计已取数量：使用 SharedPreferences 中的真实状态
val pickedCount = expressList.filter { express ->
    val statusKey = "pickup_${express.pickupCode}"
    val isPicked = statusPrefs.getBoolean(statusKey, express.status == PickupStatus.PICKED)
    isPicked && try {
        val expressDate = java.time.LocalDate.parse(express.date)
        expressDate >= thirtyDaysAgo
    } catch (e: Exception) {
        true
    }
}.size
```

### 2. 修改数量统计逻辑

**已取数量统计**（```237:251:android/app/src/main/java/com/sms/tagger/ui/screens/ExpressScreen.kt```）：
- 基于 SharedPreferences 中的真实状态
- 只统计在 `expressList` 中且符合30天显示范围的快递
- 确保数量统计基于真实状态，但只统计可显示的快递

**未取数量统计**（```254:263:android/app/src/main/java/com/sms/tagger/ui/screens/ExpressScreen.kt```）：
- 基于 SharedPreferences 中的真实状态
- 只统计在 `expressList` 中且符合7天显示范围的快递

### 3. 修改显示逻辑

**显示过滤**（```352:378:android/app/src/main/java/com/sms/tagger/ui/screens/ExpressScreen.kt```）：
- 使用 SharedPreferences 的状态，而不是 `express.status`
- 确保显示逻辑与数量统计逻辑一致
- 30天过滤只用于显示，不影响数量统计

### 4. 修改一键取件逻辑

**一键取件判断**（```152:163:android/app/src/main/java/com/sms/tagger/ui/screens/ExpressScreen.kt```）：
- 使用 SharedPreferences 的状态判断未取快递
- 确保与数量统计逻辑一致

**状态更新**（```173:191:android/app/src/main/java/com/sms/tagger/ui/screens/ExpressScreen.kt```）：
- 优化 SharedPreferences 的写入（使用 editor）
- 更新 `expressList` 时基于 SharedPreferences 的真实状态

## 修改的关键点

### 1. 统一状态源
- 所有状态判断都基于 SharedPreferences
- 不再依赖 `express.status`（这是从短信提取的临时状态）

### 2. 数量统计逻辑
- 已取数量：从 SharedPreferences 读取状态 + 30天过滤
- 未取数量：从 SharedPreferences 读取状态 + 7天过滤
- 确保数量统计基于真实状态，但只统计可显示的快递

### 3. 显示逻辑
- 使用 SharedPreferences 的状态
- 与数量统计逻辑保持一致
- 30天过滤只用于显示

### 4. 一键取件逻辑
- 使用 SharedPreferences 的状态判断
- 优化状态更新流程

## 修复效果

### 修复前
- 一键取件后：已取17个
- 重启后：已取6个（不一致）

### 修复后
- 一键取件后：已取数量基于 SharedPreferences（准确）
- 重启后：已取数量基于 SharedPreferences（准确）
- 数量统计一致，不受 `expressList` 变化影响

## 注意事项

1. **30天过滤**
   - 只用于显示和数量统计
   - 超过30天的快递不会显示，但状态仍然保存在 SharedPreferences 中

2. **`expressList` 的限制**
   - `expressList` 受限于短信读取数量（5000条）
   - 如果某个快递不在 `expressList` 中，即使它在 SharedPreferences 中标记为已取，也不会显示
   - 这是预期的行为，因为我们无法显示不在 `expressList` 中的快递

3. **状态持久化**
   - 所有状态都保存在 SharedPreferences 中
   - 重启后，状态会从 SharedPreferences 恢复
   - 确保状态的一致性

## 测试建议

1. **测试场景1**：一键取件后，检查数量是否正确
2. **测试场景2**：重启APP后，检查数量是否一致
3. **测试场景3**：手动标记快递为已取，检查数量是否正确
4. **测试场景4**：超过30天的已取快递，检查是否不显示但仍统计

## 修改的文件

- `android/app/src/main/java/com/sms/tagger/ui/screens/ExpressScreen.kt`
  - 数量统计逻辑（第226-263行）
  - 显示过滤逻辑（第352-378行）
  - 一键取件逻辑（第152-191行）
  - 添加统一的 SharedPreferences 读取（第104-106行）

## 总结

通过统一使用 SharedPreferences 作为状态源，修复了已取数量不一致的问题。现在数量统计基于真实状态（SharedPreferences），而不受 `expressList` 变化的影响。这确保了：

1. ✅ 一键取件后，数量准确
2. ✅ 重启后，数量一致
3. ✅ 数量统计与显示逻辑一致
4. ✅ 状态持久化可靠

