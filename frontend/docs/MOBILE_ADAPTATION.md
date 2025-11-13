# 移动端适配说明

## 问题描述

初始版本的页面在桌面浏览器上显示为全屏宽度，不符合移动端应用的要求。

## 解决方案

### 1. 限制页面最大宽度

在 `src/assets/styles/global.css` 中设置：

```css
#app {
  width: 100%;
  max-width: 414px; /* iPhone 11 Pro Max 宽度 */
  height: 100%;
  margin: 0 auto;
  background-color: #fff;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
  position: relative;
}
```

**效果**：
- 在手机上：页面宽度 = 屏幕宽度
- 在桌面浏览器上：页面最大宽度 = 414px，居中显示，模拟手机屏幕

### 2. 优化 viewport 设置

在 `index.html` 中设置：

```html
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
```

**说明**：
- `width=device-width`：页面宽度等于设备宽度
- `initial-scale=1.0`：初始缩放比例为 1
- `maximum-scale=1.0`：最大缩放比例为 1
- `user-scalable=no`：禁止用户缩放（移动端应用标准做法）

## 支持的设备尺寸

| 设备 | 宽度 | 说明 |
|------|------|------|
| iPhone SE | 375px | 小屏手机 |
| iPhone 11 Pro | 375px | 标准手机 |
| iPhone 11 Pro Max | 414px | 大屏手机 |
| 桌面浏览器 | 最大 414px | 居中显示，模拟手机 |

## 测试方法

### 桌面浏览器测试

1. 打开 Chrome DevTools（F12）
2. 点击设备工具栏图标（Ctrl+Shift+M）
3. 选择设备：iPhone 11 Pro Max
4. 查看页面效果

### 真机测试

1. 确保手机和电脑在同一局域网
2. 访问：`http://[电脑IP]:3000`
3. 例如：`http://192.168.200.130:3000`

## 页面布局原则

### ✅ 正确做法

1. **使用相对单位**
   - 使用 `%`、`vw`、`vh` 等相对单位
   - 使用 `rem` 进行字体大小适配

2. **响应式设计**
   - 使用 Flexbox 或 Grid 布局
   - 避免固定宽度（除非是图标等小元素）

3. **触摸友好**
   - 按钮最小尺寸：44x44px
   - 间距充足，避免误触

### ❌ 错误做法

1. **固定宽度**
   ```css
   /* 错误 */
   .container {
     width: 1200px;
   }
   ```

2. **小字体**
   ```css
   /* 错误 - 移动端字体太小 */
   .text {
     font-size: 10px;
   }
   ```

3. **悬停效果**
   ```css
   /* 错误 - 移动端没有 hover */
   .button:hover {
     background: blue;
   }
   ```

## Vant 组件适配

Vant 组件库已经做好了移动端适配，直接使用即可：

```vue
<van-button type="primary">按钮</van-button>
<van-field v-model="value" placeholder="输入框" />
<van-nav-bar title="标题" />
```

## 未来优化

- [ ] 支持横屏模式
- [ ] 支持平板尺寸（768px+）
- [ ] 暗黑模式适配
- [ ] 更多设备尺寸测试

---

**最后更新**: 2025-11-04
