"""
测试规则引擎
"""
from app.services.rule_engine import RuleEngine, ExtractionRuleItem, load_default_rules


def test_rule_engine():
    """测试规则引擎基本功能"""
    print("=== 测试规则引擎 ===\n")
    
    # 创建规则引擎
    engine = RuleEngine()
    
    # 加载默认规则
    load_default_rules(engine)
    
    # 辅助函数：清理文本（模拟前端的清理逻辑）
    def clean_text(text):
        """清理文本，移除常见前缀"""
        import re
        cleaned = text
        # 移除【发件人】
        cleaned = re.sub(r'^.*?[【\[].*?[】\]]\s*', '', cleaned)
        # 移除常见前缀
        cleaned = cleaned.replace('您的快递已到达', '')
        cleaned = cleaned.replace('包裹到达', '')
        cleaned = cleaned.replace('快递到达', '')
        return cleaned.strip()
    
    # 测试用例
    test_cases = [
        {
            'name': '测试1: 标准取件码',
            'text': '【菜鸟驿站】您的快递已到达，取件码: ABC123',
            'expected_pickup': 'ABC123',
            'clean': False  # 取件码不需要清理
        },
        {
            'name': '测试2: 横杠分隔取件码',
            'text': '您的快递已到达小区门口，凭7-5-3028取件',
            'expected_pickup': '7-5-3028',
            'clean': False
        },
        {
            'name': '测试3: 地址提取 - 驿站',
            'text': '【速递】您的快递已到达人民路菜鸟驿站',
            'expected_address': '人民路菜鸟驿站',
            'clean': True  # 地址需要清理
        },
        {
            'name': '测试4: 地址提取 - 小区门口',
            'text': '您的快递已到达阳光小区北门',
            'expected_address': '阳光小区北门',
            'clean': True
        },
        {
            'name': '测试5: 地址提取 - 快递柜',
            'text': '包裹到达绿城小区快递柜',
            'expected_address': '绿城小区快递柜',
            'clean': True
        },
    ]
    
    passed = 0
    failed = 0
    
    for test in test_cases:
        print(f"📝 {test['name']}")
        print(f"   输入: {test['text']}")
        
        # 根据需要清理文本
        text_to_process = clean_text(test['text']) if test.get('clean', False) else test['text']
        if test.get('clean', False):
            print(f"   清理后: {text_to_process}")
        
        # 提取取件码
        if 'expected_pickup' in test:
            pickup_code = engine.extract('pickup_code', text_to_process)
            expected = test['expected_pickup']
            if pickup_code == expected:
                print(f"   ✅ 取件码: {pickup_code}")
                passed += 1
            else:
                print(f"   ❌ 取件码: 期望 '{expected}', 实际 '{pickup_code}'")
                failed += 1
        
        # 提取地址
        if 'expected_address' in test:
            address = engine.extract('address', text_to_process)
            expected = test['expected_address']
            if address == expected:
                print(f"   ✅ 地址: {address}")
                passed += 1
            else:
                print(f"   ❌ 地址: 期望 '{expected}', 实际 '{address}'")
                failed += 1
        
        print()
    
    print("=== 测试结果 ===")
    print(f"通过: {passed}")
    print(f"失败: {failed}")
    print(f"成功率: {passed / (passed + failed) * 100:.1f}%")


def test_pattern_validation():
    """测试正则表达式验证"""
    print("\n=== 测试正则表达式验证 ===\n")
    
    engine = RuleEngine()
    
    test_cases = [
        {
            'name': '有效的正则',
            'pattern': r'取件码[：:]\s*(\w+)',
            'text': '取件码: ABC123',
            'should_succeed': True
        },
        {
            'name': '无效的正则',
            'pattern': r'取件码[：:(\w+',  # 缺少闭合括号
            'text': '取件码: ABC123',
            'should_succeed': False
        },
        {
            'name': '提取组不存在',
            'pattern': r'取件码',  # 没有捕获组
            'text': '取件码: ABC123',
            'extract_group': 1,
            'should_succeed': True  # 应该返回整个匹配
        },
    ]
    
    for test in test_cases:
        print(f"📝 {test['name']}")
        print(f"   模式: {test['pattern']}")
        
        extract_group = test.get('extract_group', 1)
        success, extracted, error = engine.test_pattern(
            test['pattern'],
            test['text'],
            extract_group
        )
        
        if success == test['should_succeed']:
            print(f"   ✅ 验证结果符合预期")
            if extracted:
                print(f"   提取内容: {extracted}")
            if error:
                print(f"   错误信息: {error}")
        else:
            print(f"   ❌ 验证结果不符合预期")
        
        print()


def test_priority():
    """测试规则优先级"""
    print("\n=== 测试规则优先级 ===\n")
    
    engine = RuleEngine()
    
    # 添加两个规则，优先级不同
    rule1 = ExtractionRuleItem(1, r'取件码[：:]\s*(\w+)', 1, priority=5)
    rule2 = ExtractionRuleItem(2, r'(\d+-\d+-\d+)', 1, priority=10)  # 更高优先级
    
    engine.add_rule('pickup_code', rule1)
    engine.add_rule('pickup_code', rule2)
    
    # 测试文本同时匹配两个规则
    text = '取件码: 7-5-3028'
    result = engine.extract('pickup_code', text)
    
    print(f"测试文本: {text}")
    print(f"提取结果: {result}")
    print(f"预期: 7-5-3028 (优先级更高的规则)")
    
    if result == '7-5-3028':
        print("✅ 优先级测试通过")
    else:
        print("❌ 优先级测试失败")


if __name__ == '__main__':
    test_rule_engine()
    test_pattern_validation()
    test_priority()
