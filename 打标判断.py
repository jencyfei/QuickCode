import re  # 用于正则匹配，如果需要更复杂模式

def tag_sms(message):
    message = message.lower().strip()  # 转换为小写并去除空格，便于匹配
    
    # 1. 验证码：通常包含“验证码”“code”“otp”和数字
    if re.search(r'(验证码|code|otp|verification|动态码|确认码)\s*\d{4,6}', message):
        return "验证码"
    
    # 2. 银行：包含银行名称、余额、交易、转账等
    bank_keywords = ["银行", "余额", "交易", "转账", "信用卡", "debit", "credit", "alipay", "wechat pay", "消费", "入账"]
    if any(kw in message for kw in bank_keywords):
        return "银行"
    
    # 3. 快递：包含物流、包裹、签收、派送等
    express_keywords = ["快递", "包裹", "物流", "签收", "派送", "ems", "sf express", "jd logistics", "取件码", "运单"]
    if any(kw in message for kw in express_keywords):
        return "快递"
    
    # 4. 营销：包含促销、优惠、广告等，非紧急
    marketing_keywords = ["优惠", "促销", "折扣", "特价", "活动", "coupon", "sale", "广告", "推广"]
    if any(kw in message for kw in marketing_keywords):
        return "营销"
    
    # 5. 通知：通用通知，如预约、提醒、更新（放在最后，作为兜底）
    notification_keywords = ["通知", "提醒", "预约", "更新", "会议", "alert", "notice", "reminder"]
    if any(kw in message for kw in notification_keywords):
        return "通知"
    
    # 默认：不匹配任何标签
    return "未知"

# 测试示例
test_messages = [
    "您的验证码是123456，请勿泄露。",
    "中国银行：您的账户余额变动，消费100元。",
    "顺丰快递：您的包裹已派送，请查收。",
    "双11大促销，享9折优惠！",
    "会议通知：明天10点准时参加。"
]

for msg in test_messages:
    print(f"消息: {msg} -> 标签: {tag_sms(msg)}")