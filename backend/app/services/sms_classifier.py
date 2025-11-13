"""
短信分类服务
根据短信内容自动识别类型并建议标签
"""
import re
from typing import List, Set


class SmsClassifier:
    """短信分类器"""
    
    # 快递相关关键词
    EXPRESS_KEYWORDS = [
        '快递', '取件', '包裹', '已到', '驿站', '菜鸟', '速递', '取货',
        '快件', '代收点', '自提', '提货码', '取件码', '快递柜', '丰巢',
        '中通', '圆通', '申通', '韵达', '顺丰', 'EMS', '邮政', '京东',
        '取货', '到店', '已入柜'
    ]
    
    # 验证码相关关键词
    VERIFICATION_KEYWORDS = [
        '验证码', '动态码', '校验码', 'code', 'verification', '确认码',
        '短信验证', '登录验证', '身份验证', '安全码', '动态密码'
    ]
    
    # 银行相关关键词
    BANK_KEYWORDS = [
        '银行', '消费', '支付', '转账', '余额', '储蓄卡', '信用卡',
        '交易', '账户', '入账', '出账', '财付通', '支付宝', '微信支付',
        '扣款', '还款', '存款', '取款'
    ]
    
    # 通知相关关键词
    NOTIFICATION_KEYWORDS = [
        '通知', '提醒', '到期', '账单', '会议', '预约', '审核',
        '申请', '办理', '变更', '缴费'
    ]
    
    # 营销相关关键词
    MARKETING_KEYWORDS = [
        '优惠', '促销', '打折', '活动', '领取', '福利', '红包',
        '积分', '会员', '特惠', '限时', '抢购'
    ]
    
    @classmethod
    def classify(cls, content: str, sender: str = '') -> Set[str]:
        """
        分类短信内容（一条短信只返回一个标签）
        
        按优先级顺序：验证码 > 快递 > 银行 > 通知 > 营销
        
        Args:
            content: 短信内容
            sender: 发件人
            
        Returns:
            标签名称集合（只包含一个标签）
        """
        content_lower = content.lower()
        sender_lower = sender.lower()
        
        # 优先级1: 验证码（最高优先级）
        if cls._is_verification_code(content_lower, sender_lower):
            return {'验证码'}
        
        # 优先级2: 快递
        if cls._is_express(content_lower, sender_lower):
            return {'快递'}
        
        # 优先级3: 银行
        if cls._is_bank(content_lower, sender_lower):
            return {'银行'}
        
        # 优先级4: 通知
        if cls._is_notification(content_lower, sender_lower):
            return {'通知'}
        
        # 优先级5: 营销
        if cls._is_marketing(content_lower, sender_lower):
            return {'营销'}
        
        # 无法分类，返回空集合
        return set()
    
    @classmethod
    def _is_verification_code(cls, content: str, sender: str) -> bool:
        """判断是否为验证码短信"""
        # 检查关键词
        for keyword in cls.VERIFICATION_KEYWORDS:
            if keyword.lower() in content:
                return True
        
        # 检查验证码格式（4-8位数字）
        if re.search(r'(?:验证码|code|校验码|动态码)[：:是为]?\s*\d{4,8}', content, re.IGNORECASE):
            return True
        
        return False
    
    @classmethod
    def _is_express(cls, content: str, sender: str) -> bool:
        """判断是否为快递短信"""
        # 先排除营销短信（京东营销不应该被识别为快递）
        if any(word in content for word in ['优惠', '促销', '打折', '活动', '领取', '福利', '红包']):
            # 如果包含营销关键词，必须同时包含明确的快递关键词才算快递
            if not any(word in content for word in ['取件', '包裹', '已到', '驿站', '取件码', '提货码']):
                return False
        
        # 检查发件人（排除"京东"单独判断）
        express_senders = ['菜鸟', '驿站', '快递', '速递', '丰巢', '中通', '圆通', 
                          '申通', '韵达', '顺丰', 'ems', '邮政']
        for s in express_senders:
            if s in sender:
                return True
        
        # 检查关键词（至少匹配2个关键词更可靠）
        match_count = 0
        for keyword in cls.EXPRESS_KEYWORDS:
            if keyword in content:
                match_count += 1
                if match_count >= 2:
                    return True
        
        # 单独检查取件码格式
        if re.search(r'(?:取件码|提货码|取货码)[：:是为]?', content):
            return True
        
        # 检查"凭...取件"格式
        if re.search(r'凭\s*[A-Z0-9-]{3,12}', content, re.IGNORECASE):
            return True
        
        return False
    
    @classmethod
    def _is_bank(cls, content: str, sender: str) -> bool:
        """判断是否为银行短信"""
        # 检查发件人（银行号码通常是955xx）
        if re.match(r'955\d{2,3}', sender):
            return True
        
        # 检查关键词
        for keyword in cls.BANK_KEYWORDS:
            if keyword in content:
                return True
        
        # 检查金额格式
        if re.search(r'(?:消费|支付|转账|余额)[：:为]?\s*[\d,]+\.?\d*\s*元', content):
            return True
        
        return False
    
    @classmethod
    def _is_notification(cls, content: str, sender: str) -> bool:
        """判断是否为通知短信"""
        # 检查关键词（至少匹配1个）
        for keyword in cls.NOTIFICATION_KEYWORDS:
            if keyword in content:
                return True
        
        return False
    
    @classmethod
    def _is_marketing(cls, content: str, sender: str) -> bool:
        """判断是否为营销短信"""
        # 检查关键词
        match_count = 0
        for keyword in cls.MARKETING_KEYWORDS:
            if keyword in content:
                match_count += 1
        
        # 至少匹配2个关键词，或1个关键词+退订特征
        if match_count >= 2:
            return True
        
        # 单独检查回复退订等营销特征
        has_unsubscribe = '退订' in content or 'td' in content.lower() or '回t' in content.lower()
        if has_unsubscribe and match_count >= 1:
            return True
        
        # 检查常见营销发件人
        marketing_senders = ['京东', '天猫', '淘宝', '拼多多', '美团', '饿了么']
        if any(s in sender for s in marketing_senders):
            # 如果是营销平台发件人且有营销关键词
            if match_count >= 1:
                return True
        
        return False


def auto_tag_sms(content: str, sender: str = '') -> List[str]:
    """
    自动为短信打标签（便捷函数）
    
    Args:
        content: 短信内容
        sender: 发件人
        
    Returns:
        标签名称列表
    """
    tags = SmsClassifier.classify(content, sender)
    return list(tags)

