"""
初始化规则模板数据
"""
from sqlalchemy.orm import Session
from app.database import SessionLocal, init_db
from app.models.extraction_rule import RuleTemplate


def init_rule_templates():
    """初始化规则模板"""
    db: Session = SessionLocal()
    
    try:
        # 检查是否已有模板
        existing_count = db.query(RuleTemplate).count()
        if existing_count > 0:
            print(f"已存在 {existing_count} 个规则模板，跳过初始化")
            return
        
        # 快递取件码模板
        express_templates = [
            RuleTemplate(
                name="标准取件码格式",
                category="快递",
                rule_type="pickup_code",
                pattern=r'取件码[：:是为]?\s*([A-Z0-9-]{4,12})',
                extract_group=1,
                priority=10,
                description="匹配 '取件码: ABC123' 格式"
            ),
            RuleTemplate(
                name="提货码格式",
                category="快递",
                rule_type="pickup_code",
                pattern=r'提货码[：:是为]?\s*([A-Z0-9-]{4,12})',
                extract_group=1,
                priority=9,
                description="匹配 '提货码: XYZ789' 格式"
            ),
            RuleTemplate(
                name="凭X-X-XXXX格式（优先级高）",
                category="快递",
                rule_type="pickup_code",
                pattern=r'凭\s*(\d+-\d+-\d+)',
                extract_group=1,
                priority=8,
                description="匹配 '凭2-4-2029' 或 '凭6-5-5011' 格式（优先匹配数字格式）"
            ),
            RuleTemplate(
                name="凭XX取件格式（其他格式）",
                category="快递",
                rule_type="pickup_code",
                pattern=r'凭\s*([A-Z0-9-]{3,12})',
                extract_group=1,
                priority=7,
                description="匹配 '凭ABC123' 或其他非数字格式"
            ),
            RuleTemplate(
                name="横杠分隔数字（X-X-XXXX）",
                category="快递",
                rule_type="pickup_code",
                pattern=r'(\d+-\d+-\d+)',
                extract_group=1,
                priority=6,
                description="匹配 '2-4-2029' 或 '7-5-3028' 格式"
            ),
            RuleTemplate(
                name="纯数字取件码",
                category="快递",
                rule_type="pickup_code",
                pattern=r'(?:取件码|提货码|验证码)[：:是为]?\s*(\d{4,8})',
                extract_group=1,
                priority=5,
                description="匹配纯数字取件码"
            ),
        ]
        
        # 地址提取模板
        address_templates = [
            RuleTemplate(
                name="驿站/快递柜地址",
                category="快递",
                rule_type="address",
                pattern=r'([^\s，。！？]{2,20}?(?:驿站|快递柜|门卫|保安室|代收点|自提点|丰巢|菜鸟)(?:[^\s，。！？]{0,10})?)',
                extract_group=1,
                priority=10,
                description="匹配包含驿站、快递柜等关键词的地址"
            ),
            RuleTemplate(
                name="路名+驿站",
                category="快递",
                rule_type="address",
                pattern=r'([^\s，。！？]{2,15}?[路街道巷][^\s，。！？]{0,15}?(?:驿站|快递柜|门卫))',
                extract_group=1,
                priority=9,
                description="匹配 '人民路菜鸟驿站' 格式"
            ),
            RuleTemplate(
                name="小区/大厦+位置",
                category="快递",
                rule_type="address",
                pattern=r'([^\s，。！？]{0,15}?(?:小区|大厦|广场|商场|公寓|写字楼)(?:[^\s，。！？]{0,15}?(?:门口|大门|北门|南门|东门|西门|正门|侧门|1号门|2号门|3号门|快递柜|驿站|门卫))?)',
                extract_group=1,
                priority=8,
                description="匹配 '阳光小区北门' 格式"
            ),
            RuleTemplate(
                name="菜鸟驿站",
                category="快递",
                rule_type="address",
                pattern=r'(菜鸟驿站[^\s，。！？]{0,15})',
                extract_group=1,
                priority=7,
                description="匹配菜鸟驿站相关地址"
            ),
            RuleTemplate(
                name="丰巢快递柜",
                category="快递",
                rule_type="address",
                pattern=r'(丰巢[^\s，。！？]{0,15})',
                extract_group=1,
                priority=6,
                description="匹配丰巢快递柜相关地址"
            ),
        ]
        
        # 发件人提取模板
        sender_templates = [
            RuleTemplate(
                name="【发件人】格式",
                category="通用",
                rule_type="sender",
                pattern=r'【(.+?)】',
                extract_group=1,
                priority=10,
                description="提取【】中的发件人"
            ),
            RuleTemplate(
                name="[发件人]格式",
                category="通用",
                rule_type="sender",
                pattern=r'\[(.+?)\]',
                extract_group=1,
                priority=9,
                description="提取[]中的发件人"
            ),
        ]
        
        # 验证码模板
        verification_templates = [
            RuleTemplate(
                name="标准验证码格式",
                category="验证码",
                rule_type="pickup_code",
                pattern=r'验证码[：:是为]?\s*(\d{4,8})',
                extract_group=1,
                priority=10,
                description="匹配 '验证码: 123456' 格式"
            ),
            RuleTemplate(
                name="英文验证码格式",
                category="验证码",
                rule_type="pickup_code",
                pattern=r'code[：:is]?\s*(\d{4,8})',
                extract_group=1,
                priority=9,
                description="匹配 'code: 123456' 格式"
            ),
        ]
        
        # 添加所有模板
        all_templates = (
            express_templates +
            address_templates +
            sender_templates +
            verification_templates
        )
        
        for template in all_templates:
            db.add(template)
        
        db.commit()
        print(f"✅ 成功初始化 {len(all_templates)} 个规则模板")
        
    except Exception as e:
        print(f"❌ 初始化规则模板失败: {e}")
        db.rollback()
    finally:
        db.close()


if __name__ == "__main__":
    # 确保数据库表已创建
    init_db()
    # 初始化规则模板
    init_rule_templates()
