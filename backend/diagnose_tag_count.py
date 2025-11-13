"""
诊断标签计数问题
"""
from sqlalchemy import func
from app.database import SessionLocal
from app.models.tag import Tag, SMSTag
from app.models.sms import SMSMessage

def diagnose_tag_counts():
    """诊断标签计数问题"""
    db = SessionLocal()
    
    try:
        print("=== 标签计数诊断 ===\n")
        
        # 获取所有标签
        tags = db.query(Tag).all()
        
        for tag in tags:
            print(f"标签: {tag.name} (ID: {tag.id})")
            
            # 方法1: 使用 COUNT 统计
            count_method1 = db.query(func.count(SMSTag.sms_id)).filter(
                SMSTag.tag_id == tag.id
            ).scalar() or 0
            print(f"  方法1 (COUNT): {count_method1}")
            
            # 方法2: 使用 DISTINCT COUNT 统计
            count_method2 = db.query(func.count(func.distinct(SMSTag.sms_id))).filter(
                SMSTag.tag_id == tag.id
            ).scalar() or 0
            print(f"  方法2 (DISTINCT COUNT): {count_method2}")
            
            # 方法3: 查询所有关联记录
            relations = db.query(SMSTag).filter(SMSTag.tag_id == tag.id).all()
            print(f"  方法3 (查询关联): {len(relations)} 条记录")
            
            # 检查是否有重复
            sms_ids = [r.sms_id for r in relations]
            unique_sms_ids = set(sms_ids)
            print(f"  唯一短信ID数: {len(unique_sms_ids)}")
            
            if len(sms_ids) != len(unique_sms_ids):
                print(f"  ⚠️ 发现重复关联！")
                # 找出重复的
                from collections import Counter
                duplicates = [sms_id for sms_id, count in Counter(sms_ids).items() if count > 1]
                for dup_id in duplicates:
                    print(f"    短信ID {dup_id} 重复了 {sms_ids.count(dup_id)} 次")
            
            # 方法4: 实际查询短信列表
            sms_list = db.query(SMSMessage).join(
                SMSTag, SMSMessage.id == SMSTag.sms_id
            ).filter(
                SMSTag.tag_id == tag.id
            ).distinct().all()
            print(f"  方法4 (JOIN + DISTINCT): {len(sms_list)} 条短信")
            
            print()
        
        # 检查是否有孤立的关联记录
        print("=== 检查孤立关联 ===")
        orphan_relations = db.query(SMSTag).outerjoin(
            SMSMessage, SMSTag.sms_id == SMSMessage.id
        ).filter(
            SMSMessage.id == None
        ).all()
        
        if orphan_relations:
            print(f"⚠️ 发现 {len(orphan_relations)} 条孤立关联（短信已删除但关联未删除）")
            for rel in orphan_relations:
                print(f"  标签ID: {rel.tag_id}, 短信ID: {rel.sms_id}")
        else:
            print("✅ 没有孤立关联")
        
    finally:
        db.close()


if __name__ == "__main__":
    diagnose_tag_counts()
