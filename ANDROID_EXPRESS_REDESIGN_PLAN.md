# Android快递取件码页面重新设计计划 - 2025-11-18

## 📋 设计目标

根据`express_pickup_redesign.html`的设计，改进Android快递取件码页面，实现以下功能：

---

## 🎯 主要改进点

### 1. 顶部AppBar改进
**当前**:
- 简单的标题 + 设置按钮

**改进**:
- 添加"今日取件：X件"摘要信息
- 添加"全选"复选框
- 应用玻璃拟态背景效果

### 2. 多选功能
**当前**:
- 无多选功能

**改进**:
- 每个快递卡片左侧添加复选框
- 支持单个选中/取消
- 支持"全选"功能
- 已取出的快递自动禁用复选框

### 3. 卡片样式改进
**当前**:
- 基础的白色卡片
- 简单的边框和阴影

**改进**:
- 应用玻璃拟态效果（半透明 + 背景模糊）
- 增加圆角（16px）
- 改进阴影效果
- 悬停时有浮起动画

### 4. 底部操作栏
**当前**:
- 无固定底部操作栏

**改进**:
- 添加固定底部操作栏
- "批量复制"按钮 - 复制所有选中的取件码
- "批量取出"按钮 - 批量标记为已取
- 无选中时按钮禁用

### 5. 交互反馈
**当前**:
- 基础的状态显示

**改进**:
- 添加Toast提示（复制成功、取出成功等）
- 添加确认对话框（批量取出前确认）
- 改进选中状态的视觉反馈

---

## 📊 实现步骤

### 第1步: 数据模型扩展
- 为`ExpressInfo`添加`selected`字段（是否被选中）
- 或在`ExpressScreen`中维护选中状态

### 第2步: 顶部AppBar改进
- 添加摘要信息显示（今日取件数）
- 添加"全选"复选框
- 应用玻璃拟态背景

### 第3步: 卡片改进
- 左侧添加复选框
- 应用玻璃拟态效果
- 改进样式和间距

### 第4步: 底部操作栏
- 创建固定底部操作栏
- 实现批量操作按钮
- 处理按钮禁用状态

### 第5步: 交互功能
- 实现复选框逻辑
- 实现全选/取消全选
- 实现批量操作
- 添加Toast和对话框

### 第6步: 测试和优化
- 功能测试
- 样式优化
- 性能优化

---

## 🔧 技术细节

### 状态管理
```kotlin
// 选中状态管理
var selectedExpressIds by remember { mutableStateOf(setOf<String>()) }
var selectAllChecked by remember { mutableStateOf(false) }

// 更新选中状态
fun toggleExpressSelect(id: String) {
    selectedExpressIds = if (selectedExpressIds.contains(id)) {
        selectedExpressIds - id
    } else {
        selectedExpressIds + id
    }
}

// 全选/取消全选
fun toggleSelectAll() {
    selectAllChecked = !selectAllChecked
    selectedExpressIds = if (selectAllChecked) {
        expressList.map { it.pickupCode }.toSet()
    } else {
        emptySet()
    }
}
```

### 玻璃拟态效果
```kotlin
// 使用 Modifier.background() 和 blur 效果
// 注：Compose中的blur效果需要使用 GraphicsLayer

Card(
    modifier = Modifier
        .fillMaxWidth()
        .graphicsLayer {
            alpha = 0.5f
            // 背景模糊效果
        }
        .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(16.dp)),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
        containerColor = Color.White.copy(alpha = 0.5f)
    )
)
```

### 批量操作
```kotlin
// 批量复制
fun copyAllSelected() {
    val selectedCodes = expressList
        .filter { selectedExpressIds.contains(it.pickupCode) }
        .map { it.pickupCode }
        .joinToString(", ")
    clipboardManager.setText(AnnotatedString(selectedCodes))
    showToast("已复制${selectedCodes.split(", ").size}个码")
}

// 批量取出
fun pickupAllSelected() {
    showConfirmDialog(
        title = "批量取出",
        message = "确认取出${selectedExpressIds.size}个快递吗？",
        onConfirm = {
            selectedExpressIds.forEach { id ->
                // 标记为已取
            }
            showToast("已更新${selectedExpressIds.size}个快递")
            selectedExpressIds = emptySet()
        }
    )
}
```

---

## 📱 UI布局

```
┌─────────────────────────────────┐
│ 快速取件码      ⚙️             │  <- AppBar
│ 今日取件：2件  [全选]          │
├─────────────────────────────────┤
│                                 │
│ ☐ 6-5 | 12:42:25              │
│   6-5-3002                      │  <- 快递卡片
│   📍 郑州北文雅小区...          │
│              [📋] [✓]           │
│                                 │
│ ☐ 6-2 | 12:42:25              │
│   6-2-3006                      │
│   📍 郑州北文雅小区...          │
│              [📋] [✓]           │
│                                 │
├─────────────────────────────────┤
│ [📋 批量复制]  [📦 批量取出]   │  <- 底部操作栏
└─────────────────────────────────┘
```

---

## 🎨 样式参数

### 颜色
- 主色: #667EEA (紫蓝色)
- 背景: #F9F8FF-#D9C8FF (粉彩渐变)
- 玻璃填充: rgba(255,255,255,0.5)
- 玻璃描边: rgba(255,255,255,0.7)
- 文本主色: #333333
- 文本副色: #8A8A8A

### 尺寸
- 圆角: 16px (卡片)、12px (次要)、8px (图标)
- 间距: 12px (卡片间)、16px (内部)
- 字体: 20px (日期)、32px (取件码)、16px (地址)

### 效果
- 背景模糊: 20px (主要)、10px (次要)
- 过渡时间: 0.3s
- 阴影: 0 4px 15px rgba(0,0,0,0.05)

---

## ✅ 完成清单

- [ ] 数据模型扩展
- [ ] 顶部AppBar改进
- [ ] 卡片复选框添加
- [ ] 玻璃拟态效果应用
- [ ] 底部操作栏实现
- [ ] 批量操作功能
- [ ] Toast和对话框
- [ ] 测试和优化
- [ ] 编译成功
- [ ] APK生成

---

## 📝 预期效果

- ✅ 支持多选功能
- ✅ 支持全选功能
- ✅ 支持批量操作
- ✅ 现代玻璃拟态风格
- ✅ 更好的用户体验
- ✅ 更高的操作效率

---

## 🚀 后续步骤

1. 实现数据模型扩展
2. 修改UI布局
3. 实现交互逻辑
4. 测试功能
5. 优化样式
6. 编译和发布

