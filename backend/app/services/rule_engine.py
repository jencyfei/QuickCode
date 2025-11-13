"""
规则引擎服务
用于管理和执行自定义提取规则
"""
import re
from typing import List, Dict, Optional, Tuple
from sqlalchemy.orm import Session
from ..models.extraction_rule import ExtractionRule


class ExtractionRuleItem:
    """提取规则项"""
    def __init__(self, rule_id: int, pattern: str, extract_group: int = 1, priority: int = 0):
        self.rule_id = rule_id
        self.extract_group = extract_group
        self.priority = priority
        try:
            self.pattern = re.compile(pattern)
            self.is_valid = True
        except re.error:
            self.pattern = None
            self.is_valid = False
    
    def extract(self, text: str) -> Optional[str]:
        """从文本中提取信息"""
        if not self.is_valid or not self.pattern:
            return None
        
        match = self.pattern.search(text)
        if match:
            try:
                return match.group(self.extract_group)
            except IndexError:
                # 如果指定的组不存在，返回整个匹配
                return match.group(0)
        return None


class RuleEngine:
    """规则引擎"""
    
    def __init__(self):
        self.rules: Dict[str, List[ExtractionRuleItem]] = {
            'pickup_code': [],
            'address': [],
            'sender': []
        }
    
    def load_user_rules(self, db: Session, user_id: int):
        """从数据库加载用户规则"""
        # 查询用户的所有启用规则
        rules = db.query(ExtractionRule).filter(
            ExtractionRule.user_id == user_id,
            ExtractionRule.is_active == True
        ).all()
        
        # 清空现有规则
        for rule_type in self.rules:
            self.rules[rule_type] = []
        
        # 加载规则
        for rule in rules:
            if rule.rule_type in self.rules:
                rule_item = ExtractionRuleItem(
                    rule_id=rule.id,
                    pattern=rule.pattern,
                    extract_group=rule.extract_group,
                    priority=rule.priority
                )
                if rule_item.is_valid:
                    self.rules[rule.rule_type].append(rule_item)
        
        # 按优先级排序（优先级高的在前）
        for rule_type in self.rules:
            self.rules[rule_type].sort(key=lambda r: r.priority, reverse=True)
    
    def add_rule(self, rule_type: str, rule: ExtractionRuleItem):
        """添加规则"""
        if rule_type in self.rules and rule.is_valid:
            self.rules[rule_type].append(rule)
            # 按优先级排序
            self.rules[rule_type].sort(key=lambda r: r.priority, reverse=True)
    
    def extract(self, rule_type: str, text: str) -> Optional[str]:
        """使用规则提取信息"""
        if rule_type not in self.rules:
            return None
        
        for rule in self.rules[rule_type]:
            result = rule.extract(text)
            if result:
                return result
        
        return None
    
    def extract_all(self, text: str) -> Dict[str, Optional[str]]:
        """提取所有类型的信息"""
        return {
            'pickup_code': self.extract('pickup_code', text),
            'address': self.extract('address', text),
            'sender': self.extract('sender', text)
        }
    
    def test_pattern(self, pattern: str, text: str, extract_group: int = 1) -> Tuple[bool, Optional[str], Optional[str]]:
        """
        测试正则表达式
        
        Returns:
            (success, extracted, error)
        """
        try:
            regex = re.compile(pattern)
            match = regex.search(text)
            if match:
                try:
                    extracted = match.group(extract_group)
                    return (True, extracted, None)
                except IndexError:
                    extracted = match.group(0)
                    return (True, extracted, f"组 {extract_group} 不存在，返回整个匹配")
            else:
                return (True, None, None)
        except re.error as e:
            return (False, None, f"正则表达式错误: {str(e)}")


# 全局规则引擎实例
_rule_engine = RuleEngine()


def get_rule_engine() -> RuleEngine:
    """获取规则引擎实例"""
    return _rule_engine


def load_default_rules(engine: RuleEngine):
    """加载默认规则（用于没有自定义规则的用户）"""
    # 取件码默认规则
    default_pickup_rules = [
        ExtractionRuleItem(0, r'取件码[：:是为]?\s*([A-Z0-9-]{4,12})', 1, 10),
        ExtractionRuleItem(0, r'提货码[：:是为]?\s*([A-Z0-9-]{4,12})', 1, 9),
        ExtractionRuleItem(0, r'凭\s*([A-Z0-9-]{3,12})', 1, 8),
        ExtractionRuleItem(0, r'(\d+-\d+-\d+)', 1, 7),
    ]
    
    # 地址默认规则（更精确的匹配，避免包含前缀）
    default_address_rules = [
        # 路名 + 驿站/快递柜（优先级最高，最具体）
        ExtractionRuleItem(0, r'([^\s，。！？【】\[\]]{2,15}?[路街道巷][^\s，。！？]{0,15}?(?:驿站|快递柜|门卫))', 1, 10),
        # 具体地址 + 驿站/快递柜/门卫等
        ExtractionRuleItem(0, r'([^\s，。！？【】\[\]]{2,20}?(?:驿站|快递柜|门卫|保安室|代收点|自提点|丰巢|菜鸟)(?:[^\s，。！？]{0,10})?)', 1, 9),
        # 小区/大厦 + 具体位置
        ExtractionRuleItem(0, r'([^\s，。！？【】\[\]]{0,15}?(?:小区|大厦|广场|商场|公寓|写字楼)(?:[^\s，。！？]{0,15}?(?:门口|大门|北门|南门|东门|西门|正门|侧门|1号门|2号门|3号门|快递柜|驿站|门卫))?)', 1, 8),
    ]
    
    for rule in default_pickup_rules:
        engine.add_rule('pickup_code', rule)
    
    for rule in default_address_rules:
        engine.add_rule('address', rule)
