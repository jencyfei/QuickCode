# Android 快递页面 V2 快速参考

## 📱 用户界面

### 顶部栏
```
┌─────────────────────────────────┐
│ 未取快递        [编辑] [⚙️]     │
├─────────────────────────────────┤
│ [未取]  [已取]                  │
├─────────────────────────────────┤
│ 已选 3 件 ▶                      │
└─────────────────────────────────┘
```

### 快递列表（非编辑模式）
```
2025-11-19 (3件)
  📍 郑州市北文雅小区6号楼102取件
    ┌─────────────────────────────┐
    │ 2-4-2029                    │
    │ ⭕ 未取              [⭕]   │
    └─────────────────────────────┘
    ┌─────────────────────────────┐
    │ 6-5-5011                    │
    │ ⭕ 未取              [⭕]   │
    └─────────────────────────────┘
```

### 快递列表（编辑模式）
```
2025-11-19 (3件)
  📍 郑州市北文雅小区6号楼102取件
    ┌─────────────────────────────┐
    │ ☑ 2-4-2029                  │
    │   ⭕ 未取                   │
    └─────────────────────────────┘
    ┌─────────────────────────────┐
    │ ☐ 6-5-5011                  │
    │   ⭕ 未取                   │
    └─────────────────────────────┘
```

### 底部操作栏（编辑模式）
```
┌─────────────────────────────────┐
│ [✓全选] [✕清空] [📋复制] [✓已取]│
└─────────────────────────────────┘
```

## 🎯 用户操作流程

### 查看快递
1. 打开应用 → 快递页面
2. 查看日期分组的快递列表
3. 点击日期行可折叠/展开

### 编辑模式
1. 点击"编辑"按钮进入编辑模式
2. 点击"编辑"按钮变为"取消"（红色）
3. 复选框出现在每个快递卡片左侧
4. 底部操作栏出现

### 多选操作
1. **单选**：点击复选框选中单个快递
2. **全选**：点击"全选"按钮选中所有快递
3. **清空**：点击"清空"按钮取消所有选择
4. **反选**：再次点击"全选"取消全选

### 批量操作
1. **复制取件码**：选中快递 → 点击"复制" → 复制到剪贴板
2. **标记为已取**：选中快递 → 点击"已取" → 标记为已取
3. **退出编辑**：点击"取消"按钮退出编辑模式

### 页签切换
1. **未取页签**：查看未取的快递
2. **已取页签**：查看已取的快递
3. **切换方式**：点击页签按钮切换

## 🔧 代码结构

### 状态管理
```kotlin
var isEditMode by remember { mutableStateOf(false) }        // 编辑模式
var currentTab by remember { mutableStateOf("pending") }    // 当前页签
var selectedExpressIds by remember { mutableStateOf(setOf<String>()) }  // 选中的快递
var selectAllChecked by remember { mutableStateOf(false) }  // 全选状态
```

### 主要组件
- **ExpressScreen** - 主页面组件
- **DateGroup** - 日期分组组件
- **LocationGroup** - 地址分组组件
- **ExpressItemCard** - 快递卡片组件

### 函数签名

#### DateGroup
```kotlin
@Composable
fun DateGroup(
    date: String,
    expressItems: List<ExpressInfo>,
    isExpanded: Boolean = true,
    isEditMode: Boolean = false,
    selectedExpressIds: Set<String> = emptySet(),
    onExpandedChange: (Boolean) -> Unit = {},
    onSelectionChange: ((String, Boolean) -> Unit)? = null
)
```

#### LocationGroup
```kotlin
@Composable
fun LocationGroup(
    location: String,
    expressItems: List<ExpressInfo>,
    isEditMode: Boolean = false,
    selectedExpressIds: Set<String> = emptySet(),
    onSelectionChange: ((String, Boolean) -> Unit)? = null
)
```

#### ExpressItemCard
```kotlin
@Composable
fun ExpressItemCard(
    express: ExpressInfo,
    isEditMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectionChange: ((Boolean) -> Unit)? = null
)
```

## 🎨 样式说明

### 颜色方案
- **主色**：蓝色 `#667EEA`
- **成功色**：绿色 `#4CAF50`
- **警告色**：橙色 `#FF9800`
- **文字**：深灰 `#333333`
- **辅助文字**：浅灰 `#8A8A8A`

### 圆角
- **大圆角**：16dp（卡片）
- **中圆角**：12dp（按钮、分组）
- **小圆角**：8dp（标签）

### 透明度
- **背景**：0.5f - 0.7f
- **边框**：0.3f - 0.7f
- **禁用**：0.2f

## 📊 状态转换

### 编辑模式状态机
```
┌─────────────┐
│  非编辑模式  │
│ (显示按钮)  │
└──────┬──────┘
       │ 点击"编辑"
       ▼
┌─────────────┐
│  编辑模式    │
│(显示复选框) │
└──────┬──────┘
       │ 点击"取消"
       ▼
┌─────────────┐
│  非编辑模式  │
│ (显示按钮)  │
└─────────────┘
```

### 选择状态转换
```
┌──────────┐
│ 未选中    │
└────┬─────┘
     │ 点击复选框
     ▼
┌──────────┐
│ 已选中    │
└────┬─────┘
     │ 点击复选框
     ▼
┌──────────┐
│ 未选中    │
└──────────┘
```

## 🐛 常见问题

### Q: 编辑模式下复选框不显示？
A: 检查 `isEditMode` 状态是否为 `true`，以及 `ExpressItemCard` 的 `isEditMode` 参数是否传递正确。

### Q: 底部操作栏不显示？
A: 底部操作栏仅在编辑模式下显示，检查 `isEditMode` 状态。

### Q: 复选框选中后没有反应？
A: 检查 `onSelectionChange` 回调是否正确传递，以及 `selectedExpressIds` 状态是否更新。

### Q: 页签切换不工作？
A: 检查 `currentTab` 状态是否更新，以及页签按钮的 `onClick` 回调是否正确。

## 📝 测试清单

- [ ] 编辑模式进入/退出
- [ ] 复选框显示/隐藏
- [ ] 单选快递
- [ ] 全选快递
- [ ] 清空选择
- [ ] 复制取件码
- [ ] 标记为已取
- [ ] 页签切换
- [ ] 日期分组折叠/展开
- [ ] 地址分组显示
- [ ] 非编辑模式状态按钮
- [ ] 编辑模式复选框禁用（已取快递）

## 🚀 性能优化建议

1. **减少重组**：使用 `remember` 缓存分组结果
2. **懒加载**：使用 `LazyColumn` 显示长列表
3. **状态提升**：将状态提升到父组件
4. **记忆化**：使用 `derivedStateOf` 缓存派生状态

## 📚 相关文件

- `ExpressScreen.kt` - 主页面代码
- `ExpressInfo.kt` - 数据模型
- `ExpressExtractor.kt` - 数据提取
- `ANDROID_EXPRESS_REDESIGN_V2_COMPLETE.md` - 完整文档

---

**版本**: v2.0  
**更新时间**: 2025-11-19  
**状态**: ✅ 完成并可用
