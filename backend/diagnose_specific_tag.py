"""
诊断特定标签的计数问题
"""
from sqlalchemy import func
from app.database import SessionLocal
from app.models.tag import Tag, SMSTag
from app.models.sms import SMSMessage

def diagnose_specific_tag(tag_name="验证码"):
    """诊断特定标签的计数"""
    db = SessionLocal()
    
    try:
        print(f"=== 诊断标签: {tag_name} ===\n")
        
        # 查找标签
        tag = db.query(Tag).filter(Tag.name == tag_name).first()
        
        if not tag:
            print(f"❌ 标签 '{tag_name}' 不存在")
            return
        
        print(f"标签ID: {tag.id}")
        print(f"标签名称: {tag.name}")
        print(f"用户ID: {tag.user_id}\n")
        
        # 方法1: 后端API使用的查询方式
        print("方法1: 后端API查询 (COUNT)")
        count_api = db.query(func.count(SMSTag.sms_id)).filter(
            SMSTag.tag_id == tag.id
        ).scalar() or 0
        print(f"  结果: {count_api} 条\n")
        
        # 方法2: 使用DISTINCT COUNT
        print("方法2: DISTINCT COUNT")
        count_distinct = db.query(func.count(func.distinct(SMSTag.sms_id))).filter(
            SMSTag.tag_id == tag.id
        ).scalar() or 0
        print(f"  结果: {count_distinct} 条\n")
        
        # 方法3: 查询所有关联记录
        print("方法3: 查询所有关联记录")
        relations = db.query(SMSTag).filter(SMSTag.tag_id == tag.id).all()
        print(f"  关联记录数: {len(relations)}")
        
        sms_ids = [r.sms_id for r in relations]
        unique_sms_ids = list(set(sms_ids))
        print(f"  唯一短信ID数: {len(unique_sms_ids)}")
        
        if len(sms_ids) != len(unique_sms_ids):
            print(f"  ⚠️ 发现重复关联！")
            from collections import Counter
            duplicates = [sms_id for sms_id, count in Counter(sms_ids).items() if count > 1]
            for dup_id in duplicates:
                print(f"    短信ID {dup_id} 重复了 {sms_ids.count(dup_id)} 次")
        print()
        
        # 方法4: 实际查询短信列表 (前端使用的方式)
        print("方法4: JOIN查询短信列表")
        sms_list = db.query(SMSMessage).join(
            SMSTag, SMSMessage.id == SMSTag.sms_id
        ).filter(
            SMSTag.tag_id == tag.id,
            SMSMessage.user_id == tag.user_id
        ).all()
        print(f"  查询结果 (无DISTINCT): {len(sms_list)} 条")
        
        # 方法5: 使用DISTINCT
        sms_list_distinct = db.query(SMSMessage).join(
            SMSTag, SMSMessage.id == SMSTag.sms_id
        ).filter(
            SMSTag.tag_id == tag.id,
            SMSMessage.user_id == tag.user_id
        ).distinct().all()
        print(f"  查询结果 (DISTINCT): {len(sms_list_distinct)} 条\n")
        
        # 详细列出所有短信
        print("=== 详细短信列表 ===")
        for i, sms in enumerate(sms_list_distinct, 1):
            print(f"{i}. ID: {sms.id}")
            print(f"   发件人: {sms.sender}")
            print(f"   内容: {sms.content[:50]}...")
            print(f"   时间: {sms.received_at}")
            
            # 查询这条短信的所有标签
            tags = db.query(Tag).join(
                SMSTag, Tag.id == SMSTag.tag_id
            ).filter(
                SMSTag.sms_id == sms.id
            ).all()
            print(f"   标签: {', '.join([t.name for t in tags])}")
            print()
        
        # 检查是否有孤立的关联
        print("=== 检查孤立关联 ===")
        orphan_relations = db.query(SMSTag).outerjoin(
            SMSMessage, SMSTag.sms_id == SMSMessage.id
        ).filter(
            SMSTag.tag_id == tag.id,
            SMSMessage.id == None
        ).all()
        
        if orphan_relations:
            print(f"⚠️ 发现 {len(orphan_relations)} 条孤立关联")
            for rel in orphan_relations:
                print(f"  短信ID: {rel.sms_id} (短信已删除)")
        else:
            print("✅ 没有孤立关联")
        
        print("\n=== 总结 ===")
        print(f"后端API统计: {count_api}")
        print(f"实际短信数: {len(sms_list_distinct)}")
        if count_api != len(sms_list_distinct):
            print(f"⚠️ 数据不一致！差异: {abs(count_api - len(sms_list_distinct))}")
        else:
            print("✅ 数据一致")
        
    finally:
        db.close()


if __name__ == "__main__":
    import sys
    tag_name = sys.argv[1] if len(sys.argv) > 1 else "验证码"
    diagnose_specific_tag(tag_name)
