"""
测试真实短信的分类效果
基于用户提供的截图中的短信内容
"""
from app.services.sms_classifier import auto_tag_sms

# 测试用例 - 来自用户截图
test_cases = [
    {
        "sender": "04526102561",
        "content": "【菜鸟驿站】您的包裹已到站,凭2-4-2029到郑州市北文雅小区6号楼102店取件。",
        "expected": "快递"
    },
    {
        "sender": "中国移动",
        "content": "【流量使用提醒】尊敬的动感地带客户，截至11月12日8时44分，您(152****5115)11月国...",
        "expected": "通知"
    },
    {
        "sender": "验证码",
        "content": "您好李飞，登录动态密码：353103，有效期5分钟。【防止口令外泄，严禁账号...",
        "expected": "验证码"
    },
    {
        "sender": "京东",
        "content": "【京东】双11狂欢！送您5元话费券，仅限今日，速领取使用！3.cn.v/1Bwys-Yb 拒收请...",
        "expected": "营销"
    },
    {
        "sender": "广发银行",
        "content": "【广发银行】您尾号3896信用卡11月账单￥24,996.52，还款日12月02日，查账分期...",
        "expected": "银行"
    },
    {
        "sender": "建设银行",
        "content": "【建设银行】截至11月10日，您的10月龙卡信用卡账还还款9.48元，请您在到期款日1...",
        "expected": "银行"
    },
    {
        "sender": "天猫医药",
        "content": "【天猫医药】双11优惠活动...",
        "expected": "营销"
    }
]

def test_classifier():
    """测试分类器"""
    print("=" * 60)
    print("短信分类测试")
    print("=" * 60)
    
    correct = 0
    total = len(test_cases)
    
    for i, case in enumerate(test_cases, 1):
        sender = case["sender"]
        content = case["content"]
        expected = case["expected"]
        
        # 执行分类
        result = auto_tag_sms(content, sender)
        actual = result[0] if result else "未分类"
        
        # 判断结果
        is_correct = actual == expected
        if is_correct:
            correct += 1
            status = "✅"
        else:
            status = "❌"
        
        # 打印结果
        print(f"\n测试 {i}:")
        print(f"  发件人: {sender}")
        print(f"  内容: {content[:50]}...")
        print(f"  预期标签: {expected}")
        print(f"  实际标签: {actual}")
        print(f"  结果: {status}")
    
    # 统计
    print("\n" + "=" * 60)
    print(f"测试结果: {correct}/{total} 正确")
    print(f"准确率: {correct/total*100:.1f}%")
    print("=" * 60)

if __name__ == "__main__":
    test_classifier()
