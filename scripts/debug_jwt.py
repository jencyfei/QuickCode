"""
调试JWT token
"""
import sys
import os
from pathlib import Path

# 添加backend目录到Python路径
backend_path = Path(__file__).parent.parent / "backend"
sys.path.insert(0, str(backend_path))
os.chdir(backend_path)

from app.utils.security import create_access_token, decode_access_token
from datetime import timedelta

# 创建一个测试token
print("创建测试token...")
test_data = {"sub": 1}
token = create_access_token(data=test_data, expires_delta=timedelta(minutes=30))
print(f"Token: {token[:50]}...")

# 解码token
print("\n解码token...")
# 直接测试解码，捕获异常
from jose import jwt, JWTError
from app.config import settings

try:
    payload = jwt.decode(token, settings.SECRET_KEY, algorithms=[settings.ALGORITHM])
    print(f"✅ 解码成功!")
    print(f"Payload: {payload}")
    print(f"Payload type: {type(payload)}")
except JWTError as e:
    print(f"❌ JWT解码失败: {e}")
    print(f"错误类型: {type(e).__name__}")
except Exception as e:
    print(f"❌ 其他错误: {e}")
    print(f"错误类型: {type(e).__name__}")

# 使用封装的函数
print("\n使用封装函数解码...")
payload = decode_access_token(token)
print(f"Payload: {payload}")
print(f"Payload type: {type(payload)}")

if payload:
    sub_value = payload.get("sub")
    print(f"\nsub value: {sub_value}")
    print(f"sub type: {type(sub_value)}")
    
    # 测试类型转换
    try:
        user_id = int(sub_value)
        print(f"转换后的user_id: {user_id} (type: {type(user_id)})")
    except Exception as e:
        print(f"转换失败: {e}")
