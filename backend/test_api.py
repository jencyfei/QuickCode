"""
API功能测试脚本
测试用户注册、登录、标签管理、短信管理等功能
"""
import requests
import json
from datetime import datetime

BASE_URL = "http://localhost:8000"

def print_response(title, response):
    """打印响应结果"""
    print(f"\n{'='*60}")
    print(f"📌 {title}")
    print(f"{'='*60}")
    print(f"Status: {response.status_code}")
    try:
        print(f"Response: {json.dumps(response.json(), indent=2, ensure_ascii=False)}")
    except:
        print(f"Response: {response.text}")

def test_api():
    """测试API功能"""
    
    # 1. 健康检查
    print("\n🔍 测试1: 健康检查")
    response = requests.get(f"{BASE_URL}/api/health")
    print_response("健康检查", response)
    
    # 2. 用户注册
    print("\n🔍 测试2: 用户注册")
    register_data = {
        "email": "test@example.com",
        "password": "test123456"
    }
    response = requests.post(f"{BASE_URL}/api/auth/register", json=register_data)
    print_response("用户注册", response)
    
    # 3. 用户登录
    print("\n🔍 测试3: 用户登录")
    login_data = {
        "username": "test@example.com",
        "password": "test123456"
    }
    response = requests.post(f"{BASE_URL}/api/auth/login", data=login_data)
    print_response("用户登录", response)
    
    if response.status_code == 200:
        token = response.json()["access_token"]
        headers = {"Authorization": f"Bearer {token}"}
        
        # 4. 获取当前用户信息
        print("\n🔍 测试4: 获取当前用户信息")
        response = requests.get(f"{BASE_URL}/api/auth/me", headers=headers)
        print_response("当前用户信息", response)
        
        # 5. 创建标签
        print("\n🔍 测试5: 创建标签")
        tags_data = [
            {"name": "验证码", "color": "#FF6B9D", "icon": "🔐"},
            {"name": "快递", "color": "#FF8FAB", "icon": "📦"},
            {"name": "银行", "color": "#FFB6C1", "icon": "🏦"},
            {"name": "工作", "color": "#87CEEB", "icon": "💼"},
        ]
        
        created_tags = []
        for tag_data in tags_data:
            response = requests.post(f"{BASE_URL}/api/tags", json=tag_data, headers=headers)
            print_response(f"创建标签: {tag_data['name']}", response)
            if response.status_code == 201:
                created_tags.append(response.json())
        
        # 6. 获取标签列表
        print("\n🔍 测试6: 获取标签列表")
        response = requests.get(f"{BASE_URL}/api/tags", headers=headers)
        print_response("标签列表", response)
        
        # 7. 创建短信
        print("\n🔍 测试7: 创建短信")
        sms_data = [
            {
                "sender": "95533",
                "content": "【验证码】您的验证码是123456，5分钟内有效。",
                "received_at": datetime.now().isoformat(),
                "phone_number": "95533"
            },
            {
                "sender": "菜鸟驿站",
                "content": "您的快递已到达小区门口，取件码：1234",
                "received_at": datetime.now().isoformat(),
                "phone_number": "10086"
            },
            {
                "sender": "招商银行",
                "content": "您尾号8888的储蓄卡消费500.00元",
                "received_at": datetime.now().isoformat(),
                "phone_number": "95555"
            }
        ]
        
        created_sms = []
        for sms in sms_data:
            response = requests.post(f"{BASE_URL}/api/sms", json=sms, headers=headers)
            print_response(f"创建短信: {sms['sender']}", response)
            if response.status_code == 201:
                created_sms.append(response.json())
        
        # 8. 获取短信列表
        print("\n🔍 测试8: 获取短信列表")
        response = requests.get(f"{BASE_URL}/api/sms", headers=headers)
        print_response("短信列表", response)
        
        # 9. 为短信添加标签
        if created_sms and created_tags:
            print("\n🔍 测试9: 为短信添加标签")
            sms_id = created_sms[0]["id"]
            tag_ids = [created_tags[0]["id"]]
            response = requests.post(
                f"{BASE_URL}/api/sms/{sms_id}/tags",
                json={"tag_ids": tag_ids},
                headers=headers
            )
            print_response("添加标签", response)
        
        # 10. 批量为短信添加标签
        if len(created_sms) >= 2 and len(created_tags) >= 2:
            print("\n🔍 测试10: 批量为短信添加标签")
            sms_ids = [sms["id"] for sms in created_sms[:2]]
            tag_ids = [tag["id"] for tag in created_tags[:2]]
            response = requests.post(
                f"{BASE_URL}/api/sms/batch-add-tags",
                json={"sms_ids": sms_ids, "tag_ids": tag_ids},
                headers=headers
            )
            print_response("批量添加标签", response)
        
        # 11. 按标签筛选短信
        if created_tags:
            print("\n🔍 测试11: 按标签筛选短信")
            tag_id = created_tags[0]["id"]
            response = requests.get(
                f"{BASE_URL}/api/sms?tag_ids={tag_id}",
                headers=headers
            )
            print_response("按标签筛选", response)
        
        # 12. 搜索短信
        print("\n🔍 测试12: 搜索短信")
        response = requests.get(
            f"{BASE_URL}/api/sms?keyword=验证码",
            headers=headers
        )
        print_response("搜索短信", response)
        
        print("\n" + "="*60)
        print("✅ API测试完成！")
        print("="*60)
    else:
        print("\n❌ 登录失败，无法继续测试")

if __name__ == "__main__":
    test_api()
