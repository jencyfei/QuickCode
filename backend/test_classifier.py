"""
测试短信分类器
"""
from app.services.sms_classifier import auto_tag_sms


def test_classifier():
    """测试分类器"""
    
    test_cases = [
        {
            'sender': '菜鸟驿站',
            'content': '【菜鸟驿站】您的包裹已到站，凭7-5-3028到郑州市北文雅小区6号楼102店取件。',
            'expected': ['快递']
        },
        {
            'sender': '95533',
            'content': '【验证码】您的验证码是123456，5分钟内有效。',
            'expected': ['验证码']
        },
        {
            'sender': '招商银行',
            'content': '您尾号8888的储蓄卡消费500.00元',
            'expected': ['银行']
        },
        {
            'sender': '10086',
            'content': '尊敬的客户，您本月话费账单已出，请及时查看。',
            'expected': ['通知']
        },
        {
            'sender': '106',
            'content': '限时优惠！全场5折，赶快来抢购！回T退订',
            'expected': ['营销']
        },
        {
            'sender': '顺丰速递',
            'content': '您的快递已到达小区门口快递柜，取件码：A1B2C3',
            'expected': ['快递']
        },
    ]
    
    print("=" * 60)
    print("短信分类器测试")
    print("=" * 60)
    
    success_count = 0
    total_count = len(test_cases)
    
    for i, case in enumerate(test_cases, 1):
        sender = case['sender']
        content = case['content']
        expected = set(case['expected'])
        
        result = set(auto_tag_sms(content, sender))
        
        is_correct = result == expected
        status = "[PASS]" if is_correct else "[FAIL]"
        
        print(f"\n测试 {i}: {status}")
        print(f"发件人: {sender}")
        print(f"内容: {content}")
        print(f"预期标签: {expected}")
        print(f"实际标签: {result}")
        
        if is_correct:
            success_count += 1
    
    print("\n" + "=" * 60)
    print(f"测试结果: {success_count}/{total_count} 通过")
    print("=" * 60)


if __name__ == '__main__':
    test_classifier()

