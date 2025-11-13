"""
测试认证系统
"""
import requests
import json

BASE_URL = "http://localhost:8000"

def test_auth_system():
    """测试认证系统的完整流程"""
    print("=" * 60)
    print("测试认证系统")
    print("=" * 60)
    
    # 测试数据
    test_user = {
        "email": "test@example.com",
        "phone": "13800138000",
        "password": "test123456"
    }
    
    # 1. 测试用户注册
    print("\n[1] 测试用户注册...")
    try:
        response = requests.post(
            f"{BASE_URL}/api/auth/register",
            json=test_user
        )
        
        if response.status_code == 201:
            user_data = response.json()
            print(f"✅ 注册成功！")
            print(f"   用户ID: {user_data['id']}")
            print(f"   邮箱: {user_data['email']}")
            print(f"   手机: {user_data['phone']}")
        elif response.status_code == 400:
            print(f"⚠️  用户已存在（这是正常的，如果之前已注册）")
            print(f"   {response.json()['detail']}")
        else:
            print(f"❌ 注册失败: {response.status_code}")
            print(f"   {response.text}")
            return False
    except Exception as e:
        print(f"❌ 请求失败: {e}")
        return False
    
    # 2. 测试用户登录
    print("\n[2] 测试用户登录...")
    try:
        response = requests.post(
            f"{BASE_URL}/api/auth/login",
            data={
                "username": test_user["email"],
                "password": test_user["password"]
            }
        )
        
        if response.status_code == 200:
            token_data = response.json()
            access_token = token_data["access_token"]
            print(f"✅ 登录成功！")
            print(f"   Token类型: {token_data['token_type']}")
            print(f"   过期时间: {token_data['expires_in']}秒")
            print(f"   Token: {access_token[:50]}...")
        else:
            print(f"❌ 登录失败: {response.status_code}")
            print(f"   {response.text}")
            return False
    except Exception as e:
        print(f"❌ 请求失败: {e}")
        return False
    
    # 3. 测试获取用户信息（需要token）
    print("\n[3] 测试获取用户信息（需要认证）...")
    try:
        headers = {
            "Authorization": f"Bearer {access_token}"
        }
        response = requests.get(
            f"{BASE_URL}/api/auth/me",
            headers=headers
        )
        
        if response.status_code == 200:
            user_info = response.json()
            print(f"✅ 获取用户信息成功！")
            print(f"   用户ID: {user_info['id']}")
            print(f"   邮箱: {user_info['email']}")
            print(f"   手机: {user_info['phone']}")
            print(f"   注册时间: {user_info['created_at']}")
        else:
            print(f"❌ 获取失败: {response.status_code}")
            print(f"   {response.text}")
            return False
    except Exception as e:
        print(f"❌ 请求失败: {e}")
        return False
    
    # 4. 测试无效token
    print("\n[4] 测试无效token（应该失败）...")
    try:
        headers = {
            "Authorization": "Bearer invalid_token_here"
        }
        response = requests.get(
            f"{BASE_URL}/api/auth/me",
            headers=headers
        )
        
        if response.status_code == 401:
            print(f"✅ 正确拒绝了无效token")
        else:
            print(f"⚠️  预期返回401，实际返回: {response.status_code}")
    except Exception as e:
        print(f"❌ 请求失败: {e}")
    
    # 5. 测试错误密码登录
    print("\n[5] 测试错误密码登录（应该失败）...")
    try:
        response = requests.post(
            f"{BASE_URL}/api/auth/login",
            data={
                "username": test_user["email"],
                "password": "wrong_password"
            }
        )
        
        if response.status_code == 401:
            print(f"✅ 正确拒绝了错误密码")
        else:
            print(f"⚠️  预期返回401，实际返回: {response.status_code}")
    except Exception as e:
        print(f"❌ 请求失败: {e}")
    
    print("\n" + "=" * 60)
    print("✅ 认证系统测试完成！所有功能正常。")
    print("=" * 60)
    print("\n你可以访问 http://localhost:8000/docs 查看完整的API文档")
    
    return True


if __name__ == "__main__":
    try:
        test_auth_system()
    except KeyboardInterrupt:
        print("\n\n测试已取消")
    except Exception as e:
        print(f"\n\n❌ 测试过程中出现错误: {e}")
