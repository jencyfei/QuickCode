# 设计风格指南

## 🎨 设计理念

**简约可爱风** - 温暖、友好、轻松的视觉体验

---

## 🌈 配色方案

### 主色调
- **粉红色**: `#FF6B9D` - 主要按钮、标题、强调元素
- **浅粉色**: `#FF8FAB` - 渐变色、次要元素
- **淡粉色**: `#FFE5E5` - 边框、背景装饰

### 背景色
- **主背景**: `linear-gradient(180deg, #FFF5F5 0%, #FFE5E5 50%, #FFF0F0 100%)`
- **卡片背景**: `#FFFFFF`
- **输入框背景**: `#F8F9FA`

### 文字颜色
- **主文字**: `#333333`
- **次要文字**: `#666666`
- **提示文字**: `#999999`
- **占位符**: `#BBBBBB`

---

## 🎯 设计元素

### 1. Emoji 图标
使用 Emoji 代替传统图标，增加可爱感：
- 📱 短信/手机
- 📥 导入
- 📭 空状态
- 🏷️ 标签
- 👤 用户
- 🔒 密码
- ✨ 成功/完成
- 🚀 开始/启动
- 💡 提示
- ✍️ 编辑/输入
- 🎨 创建/设计

### 2. 圆角设计
- **大圆角**: `24px` - 卡片、表单容器
- **中圆角**: `16px` - 输入框、按钮组
- **小圆角**: `12px` - 小元素、标签
- **圆形按钮**: `round` - 所有按钮使用圆形

### 3. 阴影效果
```css
/* 轻阴影 - 卡片 */
box-shadow: 0 4px 16px rgba(255, 107, 157, 0.08);

/* 中阴影 - 按钮 */
box-shadow: 0 4px 16px rgba(255, 107, 157, 0.3);

/* 导航栏阴影 */
box-shadow: 0 2px 12px rgba(255, 107, 157, 0.08);
```

### 4. 动画效果
```css
/* 浮动动画 - 图标 */
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

/* 弹跳动画 - Logo */
@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

/* 旋转动画 - 装饰 */
@keyframes rotate {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(-10deg); }
  75% { transform: rotate(10deg); }
}

/* 按钮点击 */
.button:active {
  transform: scale(0.98);
}
```

---

## 🎨 组件样式

### 按钮
```vue
<!-- 主按钮 -->
<van-button round block class="cute-button primary-button">
  <span class="button-text">登录 ✨</span>
</van-button>

<style>
.cute-button {
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border: none;
  transition: all 0.3s;
}

.primary-button {
  background: linear-gradient(135deg, #FF6B9D 0%, #FF8FAB 100%);
  box-shadow: 0 4px 16px rgba(255, 107, 157, 0.3);
}

.primary-button:active {
  transform: scale(0.98);
}
</style>
```

### 输入框
```vue
<div class="input-group">
  <van-field
    v-model="value"
    placeholder="请输入"
    :border="false"
    class="cute-input"
  >
    <template #left-icon>
      <span class="input-icon">👤</span>
    </template>
  </van-field>
</div>

<style>
.input-group {
  background: #F8F9FA;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s;
}

.input-group:focus-within {
  background: #FFF5F5;
  box-shadow: 0 0 0 2px rgba(255, 107, 157, 0.2);
}

.input-icon {
  font-size: 20px;
  margin-right: 8px;
}
</style>
```

### 导航栏
```vue
<van-nav-bar
  title="📱 我的短信"
  fixed
  class="cute-navbar"
/>

<style>
.cute-navbar {
  background: white;
  box-shadow: 0 2px 12px rgba(255, 107, 157, 0.08);
}

.cute-navbar :deep(.van-nav-bar__title) {
  font-size: 18px;
  font-weight: 600;
  color: #FF6B9D;
}
</style>
```

### 空状态
```vue
<div class="empty-state">
  <div class="empty-icon">📭</div>
  <p class="empty-title">还没有短信哦</p>
  <p class="empty-desc">点击右上角"导入"按钮开始添加短信吧</p>
  <van-button round type="primary" class="cute-button">
    立即导入 ✨
  </van-button>
</div>

<style>
.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
  animation: float 3s ease-in-out infinite;
}

.empty-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: #999;
  margin-bottom: 24px;
  line-height: 1.6;
}
</style>
```

---

## 📐 布局规范

### 间距
- **页面边距**: `20px`
- **卡片间距**: `16px`
- **元素间距**: `12px`
- **小间距**: `8px`

### 字体大小
- **大标题**: `28px` (Logo)
- **标题**: `20px` (页面标题)
- **导航标题**: `18px`
- **正文**: `16px`
- **按钮文字**: `16px`
- **输入框**: `15px`
- **次要文字**: `14px`
- **提示文字**: `13px`

### 字体粗细
- **粗体**: `700` (Logo)
- **半粗**: `600` (标题、按钮)
- **常规**: `400` (正文)

---

## 🎭 页面风格示例

### 登录页面
- 温暖的粉色渐变背景
- 装饰性圆圈元素
- 跳动的手机图标
- 圆润的输入框和按钮
- 友好的提示文案

### 列表页面
- 清爽的浅色背景
- 可爱的空状态图标
- 浮动动画效果
- 圆形导航按钮

### 导入页面
- 卡片式布局
- 清晰的区域划分
- 友好的提示卡片
- 圆润的文本框

---

## ✅ 设计原则

### 1. 简约
- 去除不必要的装饰
- 保持页面清爽
- 突出核心功能

### 2. 可爱
- 使用 Emoji 图标
- 温暖的粉色系配色
- 圆润的设计元素
- 友好的文案

### 3. 友好
- 清晰的视觉层次
- 明确的操作引导
- 温馨的提示信息
- 流畅的动画效果

### 4. 一致性
- 统一的配色方案
- 统一的圆角大小
- 统一的间距规范
- 统一的动画效果

---

## 🚫 避免的设计

### ❌ 不要使用
- 深色、冷色调（如紫色、蓝色）
- 尖锐的直角
- 复杂的渐变
- 过多的阴影
- 严肃的图标
- 冷冰冰的文案

### ✅ 应该使用
- 温暖的粉色系
- 圆润的圆角
- 简单的渐变
- 轻柔的阴影
- 可爱的 Emoji
- 友好的文案

---

## 📱 移动端适配

### 触摸友好
- 按钮最小高度：`44px`
- 可点击区域充足
- 间距适中，避免误触

### 响应式
- 最大宽度：`414px`
- 自适应不同屏幕
- 保持布局一致

---

## 🎨 未来扩展

### 可能的配色方案
- **春天**: 粉色 + 绿色
- **夏天**: 粉色 + 橙色
- **秋天**: 粉色 + 黄色
- **冬天**: 粉色 + 蓝色

### 暗黑模式
- 保持可爱风格
- 使用深色背景
- 调整粉色亮度

---

**设计师**: Cascade AI  
**最后更新**: 2025-11-04  
**版本**: v1.0
