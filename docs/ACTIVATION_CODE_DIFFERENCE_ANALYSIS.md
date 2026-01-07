# 激活码生成差异分析

## 问题描述

将加密函数移动到其他项目后，生成的激活码结果不同。

## 加密流程说明

### Python 端（生成激活码）

```python
# 1. 生成随机 salt
salt = secrets.token_hex(8)  # 16 个十六进制字符

# 2. 构造明文
payload = f"{device_id}:{max_activations}:{salt}"

# 3. Fernet 加密
cipher = Fernet(key)  # key 是 bytes 类型
encrypted = cipher.encrypt(payload.encode("utf-8"))
# 注意：Fernet.encrypt() 返回的是 base64 编码的字符串（bytes）

# 4. 外层 base64 编码
code = base64.urlsafe_b64encode(encrypted).decode("utf-8")
```

### Android 端（解密激活码）

```kotlin
// 1. 双层 base64 解码
val fernetTokenBytes = Base64.decode(token, Base64.URL_SAFE)
val tokenBytes = Base64.decode(fernetTokenBytes, Base64.URL_SAFE)

// 2. Fernet 解密（AES-128-CBC + HMAC-SHA256）
// 3. 得到明文: "{device_id}:{max_activations}:{salt}"
```

## 可能导致差异的原因

### 1. ⚠️ **密钥不同（最可能的原因）**

**问题**：
- Python 端从 `tools/activation.key` 文件读取密钥
- Android 端硬编码密钥：`"7n24jlz6St4P__berMBC8KFygY_t4RKdESLGZuenfaE="`
- **如果新项目使用了不同的密钥，生成的激活码肯定不同**

**检查方法**：
```bash
# 检查 Python 端使用的密钥
cat tools/activation.key

# 检查 Android 端使用的密钥
# 查看 android/app/src/main/java/com/sms/tagger/util/ActivationManager.kt
# 找到 FERNET_KEY 常量
```

**解决方案**：
1. 确保新项目的密钥文件内容与 Android 端硬编码的密钥一致
2. 或者将 Android 端的密钥更新为新项目的密钥

### 2. **Salt 随机性（正常现象）**

**问题**：
- 每次生成激活码时，都会生成一个随机的 `salt`
- 即使相同的 `device_id` 和 `max_activations`，每次生成的激活码都不同
- **这是正常的设计，不是 bug**

**验证方法**：
```python
# 连续生成两次，结果应该不同
python generate_activation_code.py --device-id "test123" --max-activations 3
python generate_activation_code.py --device-id "test123" --max-activations 3
```

**说明**：
- 这是预期的行为
- 每个激活码都是唯一的，但都能正确解密和验证

### 3. **密钥格式问题**

**问题**：
- Fernet 密钥必须是 32 字节（base64 编码后是 44 个字符）
- 如果密钥格式不正确，加密结果会不同

**检查方法**：
```python
from cryptography.fernet import Fernet
import base64

# 检查密钥格式
key = "7n24jlz6St4P__berMBC8KFygY_t4RKdESLGZuenfaE="
key_bytes = base64.urlsafe_b64decode(key)
print(f"密钥长度: {len(key_bytes)} 字节")  # 应该是 32 字节
```

### 4. **编码问题**

**问题**：
- Python 端使用 `base64.urlsafe_b64encode()`
- Android 端使用 `Base64.URL_SAFE` 标志
- 如果编码方式不一致，会导致解码失败

**检查点**：
- ✅ Python: `base64.urlsafe_b64encode()` - URL-safe base64
- ✅ Android: `Base64.URL_SAFE` - URL-safe base64
- ✅ 两者应该一致

### 5. **输入数据格式问题**

**问题**：
- 明文格式：`"{device_id}:{max_activations}:{salt}"`
- 如果 `device_id` 或 `max_activations` 格式不同，结果会不同

**检查点**：
- `device_id` 是否包含空格或特殊字符
- `max_activations` 是否为整数
- 分隔符是否为单个冒号 `:`

## 诊断步骤

### 步骤 1: 检查密钥一致性

```bash
# 1. 查看 Python 端密钥
cat tools/activation.key

# 2. 查看 Android 端密钥（在 ActivationManager.kt 中）
# 应该完全一致
```

### 步骤 2: 验证加密流程

```python
# 测试脚本
from cryptography.fernet import Fernet
import base64

# 使用相同的密钥
key_str = "7n24jlz6St4P__berMBC8KFygY_t4RKdESLGZuenfaE="
key = key_str.encode("utf-8")

# 使用固定的 salt（用于测试）
device_id = "test123"
max_activations = 3
salt = "0000000000000000"  # 固定 salt，用于对比

payload = f"{device_id}:{max_activations}:{salt}"
cipher = Fernet(key)
encrypted = cipher.encrypt(payload.encode("utf-8"))
code = base64.urlsafe_b64encode(encrypted).decode("utf-8")

print(f"明文: {payload}")
print(f"激活码: {code}")
```

### 步骤 3: 对比新旧项目

1. **检查密钥**：
   - 旧项目密钥：`tools/activation.key` 的内容
   - 新项目密钥：新项目中的密钥文件或硬编码值
   - 两者必须完全一致

2. **检查加密函数**：
   - 确保使用相同的 Fernet 库版本
   - 确保加密流程一致

3. **测试解密**：
   - 用旧项目生成的激活码在新项目中解密
   - 用新项目生成的激活码在旧项目中解密
   - 如果都能解密，说明只是 salt 不同（正常）
   - 如果解密失败，说明密钥或流程有问题

## 常见问题解答

### Q1: 为什么每次生成的激活码都不同？

**A**: 这是正常的设计。每次生成时都会生成一个随机的 `salt`，确保每个激活码都是唯一的。即使相同的 `device_id` 和 `max_activations`，生成的激活码也不同，但都能正确解密和验证。

### Q2: 如何确保新旧项目生成的激活码兼容？

**A**: 
1. 确保使用相同的 Fernet 密钥
2. 确保加密流程一致（Fernet + 双层 base64）
3. 确保解密流程一致

### Q3: 密钥应该存储在哪里？

**A**: 
- **Python 端**：存储在 `tools/activation.key` 文件中（不要提交到 Git）
- **Android 端**：硬编码在 `ActivationManager.kt` 中（生产环境应该做混淆处理）

### Q4: 如何迁移密钥到新项目？

**A**: 
1. 复制 `tools/activation.key` 文件到新项目
2. 或者将 Android 端的 `FERNET_KEY` 常量复制到新项目
3. 确保两者完全一致

## 解决方案

### 方案 1: 统一密钥（推荐）

1. **确定使用哪个密钥**：
   - 如果旧项目已在使用，使用旧项目的密钥
   - 如果是全新项目，可以生成新密钥（但需要更新所有客户端）

2. **更新密钥**：
   ```bash
   # Python 端
   echo "7n24jlz6St4P__berMBC8KFygY_t4RKdESLGZuenfaE=" > tools/activation.key
   
   # Android 端
   # 更新 ActivationManager.kt 中的 FERNET_KEY 常量
   ```

### 方案 2: 验证加密流程

创建一个测试脚本验证加密流程：

```python
# test_encryption.py
from cryptography.fernet import Fernet
import base64

# 使用固定参数测试
key_str = "7n24jlz6St4P__berMBC8KFygY_t4RKdESLGZuenfaE="
key = key_str.encode("utf-8")

device_id = "f09672ff8cc9df5a39668bec7d74b12a44552dd5a87a37fa269609e93f1f2342"
max_activations = 3
salt = "test1234567890ab"  # 固定 salt

payload = f"{device_id}:{max_activations}:{salt}"
cipher = Fernet(key)
encrypted = cipher.encrypt(payload.encode("utf-8"))
code = base64.urlsafe_b64encode(encrypted).decode("utf-8")

print(f"密钥: {key_str}")
print(f"明文: {payload}")
print(f"激活码: {code}")

# 验证解密
decrypted = cipher.decrypt(base64.urlsafe_b64decode(code))
print(f"解密后: {decrypted.decode('utf-8')}")
```

## 检查清单

- [ ] Python 端和 Android 端使用相同的密钥
- [ ] 密钥格式正确（32 字节，base64 编码后 44 字符）
- [ ] 加密流程一致（Fernet + 双层 base64）
- [ ] 解密流程一致（双层 base64 解码 + Fernet 解密）
- [ ] 输入数据格式一致（`device_id:max_activations:salt`）
- [ ] 编码方式一致（UTF-8 + URL-safe base64）

## 总结

**最可能的原因**：密钥不同

**解决方法**：
1. 检查并统一密钥
2. 确保新旧项目使用相同的密钥文件或硬编码值
3. 验证加密/解密流程一致性

**注意**：每次生成的激活码不同是正常现象（因为 salt 是随机的），只要都能正确解密和验证即可。

