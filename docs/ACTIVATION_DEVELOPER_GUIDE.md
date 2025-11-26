# 激活码生成与管理指南（开发者版）

本文档说明如何为用户生成激活码，以及整个激活流程。

## 激活流程概览

```
用户手机APP → 获取设备ID → 发送给开发者 → 开发者生成激活码 → 用户输入激活码 → 激活成功
```

## 步骤1：用户获取设备ID

用户在手机上通过以下方式获取设备ID：

### 方式1：通过激活对话框
1. 打开APP后，如果未激活且已过期，会自动弹出激活对话框
2. 在对话框中可以看到设备ID
3. **点击复制按钮**，设备ID会被复制到剪贴板
4. 将设备ID发送给开发者（通过微信、QQ、邮件等方式）

### 方式2：通过设置页面
1. 打开APP，进入"设置"页面
2. 在"买断激活状态"卡片中可以看到设备ID
3. **点击复制图标**，设备ID会被复制到剪贴板
4. 将设备ID发送给开发者

### 设备ID格式示例
```
a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2
```
（这是一个64字符的十六进制字符串，由设备的ANDROID_ID经过SHA-256哈希生成）

## 步骤2：开发者生成激活码

### 2.1 准备工作

#### 安装依赖
```bash
pip install cryptography
```

#### 初始化Fernet密钥（首次使用）
首次使用时，脚本会自动生成一个Fernet密钥并保存到 `tools/activation.key` 文件中。

**重要提示**：
- 这个密钥用于加密所有激活码
- **请妥善保管这个密钥文件**，丢失后旧激活码将无法解密
- 建议备份密钥文件到安全的位置（例如加密的云存储或密码管理器）
- **不要将密钥文件提交到Git仓库**

### 2.2 生成激活码

#### 基本用法
```bash
cd tools
python generate_activation_code.py --device-id <用户提供的设备ID>
```

#### 参数说明
- `--device-id`：**必需**，用户从手机APP中获取的设备ID
- `--max-activations`：可选，最大激活次数（默认：3）
- `--count`：可选，一次生成多个激活码（默认：1）
- `--output-csv`：可选，将生成的激活码保存到CSV文件
- `--key-file`：可选，Fernet密钥文件路径（默认：`activation.key`）

#### 示例1：为单个用户生成激活码
```bash
python generate_activation_code.py \
    --device-id a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2
```

输出示例：
```
Generated Code 1: gAAAAABm...（很长的Base64字符串）
```

#### 示例2：自定义激活次数
```bash
python generate_activation_code.py \
    --device-id a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2 \
    --max-activations 5
```

#### 示例3：批量生成并保存到CSV
```bash
python generate_activation_code.py \
    --device-id a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2 \
    --count 3 \
    --output-csv activation_codes_2025-11-25.csv
```

输出示例：
```
Generated Code 1: gAAAAABm...（很长的Base64字符串）
Generated Code 2: gAAAAABn...（很长的Base64字符串）
Generated Code 3: gAAAAABo...（很长的Base64字符串）

Successfully saved 3 codes to activation_codes_2025-11-25.csv
```

CSV文件格式：
```csv
device_id,max_activations,activation_code
a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2,3,gAAAAABm...
a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2,3,gAAAAABn...
a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f2,3,gAAAAABo...
```

## 步骤3：发送激活码给用户

将生成的激活码（Base64字符串）发送给用户。激活码是一个很长的字符串，例如：
```
gAAAAABmXyZ123...（省略大部分字符）
```

**建议方式**：
- 通过微信/QQ直接发送（文本消息）
- 通过邮件发送
- 确保激活码完整，不要截断

## 步骤4：用户在APP中输入激活码

1. 用户在激活对话框中粘贴激活码
2. 点击"激活"按钮
3. APP会验证激活码：
   - 检查设备ID是否匹配
   - 检查激活次数是否有效
   - 解密并保存激活信息
4. 激活成功后，用户可以继续使用高级功能

## 常见问题

### Q1：用户说无法复制设备ID
**A**：确保APP版本是最新的（v1.1.0+），激活对话框和设置页面都有复制按钮。

### Q2：用户提供了错误的设备ID
**A**：设备ID必须是64字符的十六进制字符串。如果格式不对，请让用户重新复制。

### Q3：激活码生成后用户说无效
**A**：检查以下几点：
- 设备ID是否匹配（区分大小写）
- 激活码是否完整复制（没有截断）
- 是否使用了正确的密钥文件生成激活码

### Q4：密钥文件丢失了怎么办？
**A**：
- 如果密钥文件丢失，**所有旧激活码都将失效**
- 需要重新生成密钥文件
- 已激活的用户需要重新获取新的激活码
- **强烈建议定期备份密钥文件**

### Q5：一个设备ID可以生成多个激活码吗？
**A**：可以。每个激活码可以独立使用。但如果一个设备ID生成多个激活码，用户需要分别激活每个激活码。

### Q6：激活码的激活次数用完了怎么办？
**A**：用户需要重新购买新的激活码。开发者使用相同的设备ID生成新的激活码即可。

## 安全建议

1. **密钥文件安全**：
   - 密钥文件存储在本地，不在Git仓库中
   - 定期备份密钥文件到加密存储
   - 不要将密钥文件发送给他人

2. **激活码分发**：
   - 通过私密渠道发送激活码（微信、QQ、邮件）
   - 不要公开分享激活码

3. **记录管理**：
   - 建议使用CSV文件记录已生成的激活码
   - 记录生成时间、用户信息、激活状态等

## 技术细节

### 激活码格式
激活码使用Fernet对称加密，具体流程：
1. 构造明文：`{device_id}:{max_activations}:{random_salt}`
2. 使用Fernet密钥加密明文
3. Base64 URL-safe编码得到最终激活码

### 设备ID生成
- 使用Android系统的 `Settings.Secure.ANDROID_ID`
- 经过SHA-256哈希得到64字符的十六进制字符串
- 同一设备每次获取的设备ID相同

### 激活验证
APP端会：
1. 使用相同的Fernet密钥解密激活码
2. 验证设备ID是否匹配
3. 验证激活次数是否有效
4. 计算校验和确保数据完整性
5. 保存激活信息到本地SharedPreferences

## 联系与支持

如有问题，请参考：
- `docs/ACTIVATION_IMPLEMENTATION_PLAN.md` - 完整实现计划
- `docs/加密.md` - 原始需求文档
- `tools/generate_activation_code.py` - 生成脚本源码

