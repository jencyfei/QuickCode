# 短信导入功能测试指南

## 功能概述

Android应用现在支持从手机自动导入所有短信,并自动进行分类和打标签。

## 已实现的功能

### 1. 短信读取
- 使用 `SmsReader.kt` 读取手机所有短信
- 支持读取收件箱中的所有短信
- 最多读取5000条短信

### 2. 批量上传
- 每批上传50条短信到后端
- 显示实时上传进度
- 自动去重(基于内容和时间)

### 3. 自动标签
后端会自动识别短信类型并打标签:
- **快递** - 菜鸟驿站、京东、顺丰等快递通知
- **验证码** - 登录验证码、动态密码
- **银行** - 银行卡交易、账单通知
- **通知** - 系统通知、服务通知
- **营销** - 促销活动、广告信息

## 测试步骤

### 前提条件
1. 确保后端服务运行在 `http://localhost:10043/`
2. 确保已登录Android应用
3. 确保已授予短信读取权限

### 测试流程

#### 1. 进入设置页面
- 打开Android应用
- 点击底部导航栏的"设置"标签

#### 2. 开始导入
- 找到"导入短信"选项
- 点击后会弹出确认对话框
- 点击"确定"开始导入

#### 3. 观察进度
导入过程会显示以下状态:
```
正在读取手机短信...
已读取 X 条短信,正在上传...
已上传 X / Y 条...
导入成功!
```

#### 4. 验证结果
- 切换到"短信"标签页
- 应该能看到所有导入的短信
- 切换到"标签"标签页
- 应该能看到自动创建的标签(快递、验证码、银行等)
- 点击标签可以查看该类型的所有短信

### 预期结果

根据您提供的截图,手机中有以下类型的短信:
1. ✅ **菜鸟驿站** - 应该被标记为"快递"
2. ✅ **验证码** - 应该被标记为"验证码"
3. ✅ **京东** - 应该被标记为"快递"或"营销"
4. ✅ **广发银行** - 应该被标记为"银行"
5. ✅ **建设银行** - 应该被标记为"银行"
6. ✅ **中国移动** - 应该被标记为"通知"
7. ✅ **天猫医药** - 应该被标记为"营销"

## 常见问题

### Q: 为什么只显示菜鸟驿站的短信?
A: 这是因为之前没有导入功能,只能手动添加。现在添加了导入功能后,所有短信都会被导入。

### Q: 导入会重复吗?
A: 不会。后端会根据短信内容和接收时间自动去重。

### Q: 标签不准确怎么办?
A: 可以手动修改标签。后端的分类算法会持续优化。

### Q: 导入失败怎么办?
A: 检查以下几点:
1. 是否授予了短信读取权限
2. 后端服务是否正常运行
3. 网络连接是否正常
4. 查看错误提示信息

## 技术细节

### 短信读取逻辑
```kotlin
// SmsReader.kt
fun readAllSms(limit: Int = 1000): List<SmsCreate> {
    // 从 content://sms/inbox 读取短信
    // 按时间倒序排列
    // 返回 SmsCreate 对象列表
}
```

### 批量上传逻辑
```kotlin
// SettingsScreen.kt
private suspend fun importAllSms(...) {
    // 1. 读取本地短信
    val localSmsList = smsReader.readAllSms(limit = 5000)
    
    // 2. 分批上传(每批50条)
    localSmsList.chunked(50).forEach { batch ->
        repository.createSmsBatch(batch)
    }
}
```

### 后端自动标签逻辑
```python
# sms_classifier.py
def auto_tag_sms(content: str, sender: str) -> List[str]:
    # 基于关键词匹配识别短信类型
    # 返回标签名称列表
```

## 下一步优化

1. ✅ 添加导入进度条
2. ✅ 添加导入结果统计
3. ⏳ 支持增量导入(只导入新短信)
4. ⏳ 支持导入历史记录
5. ⏳ 优化标签识别算法

## 相关文件

- `android/app/src/main/java/com/sms/tagger/util/SmsReader.kt` - 短信读取工具
- `android/app/src/main/java/com/sms/tagger/ui/screens/SettingsScreen.kt` - 设置页面(包含导入功能)
- `android/app/src/main/java/com/sms/tagger/data/repository/SmsRepository.kt` - 数据仓库
- `backend/app/routers/sms.py` - 短信API(包含批量创建和自动标签)
- `backend/app/services/sms_classifier.py` - 短信分类器
