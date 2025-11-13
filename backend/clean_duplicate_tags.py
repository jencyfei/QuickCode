"""
清理重复标签 - 确保每条短信只有一个标签

规则：
1. 如果一条短信有多个标签，按优先级保留一个
2. 优先级：验证码 > 快递 > 银行 > 通知 > 营销
3. 删除其他标签
"""
from sqlalchemy import func
from app.database import SessionLocal
from app.models.tag import Tag, SMSTag
from app.models.sms import SMSMessage

# 标签优先级（数字越小优先级越高）
TAG_PRIORITY = {
    '验证码': 1,
    '快递': 2,
    '银行': 3,
    '通知': 4,
    '营销': 5,
}


def clean_duplicate_tags():
    """清理重复标签"""
    db = SessionLocal()
    
    try:
        print("=== 清理重复标签 ===\n")
        
        # 1. 找出所有有多个标签的短信
        sms_with_multiple_tags = db.query(
            SMSTag.sms_id,
            func.count(SMSTag.tag_id).label('tag_count')
        ).group_by(
            SMSTag.sms_id
        ).having(
            func.count(SMSTag.tag_id) > 1
        ).all()
        
        if not sms_with_multiple_tags:
            print("✅ 没有发现重复标签的短信")
            return
        
        print(f"发现 {len(sms_with_multiple_tags)} 条短信有多个标签\n")
        
        cleaned_count = 0
        removed_tags_count = 0
        
        # 2. 处理每条有多个标签的短信
        for sms_id, tag_count in sms_with_multiple_tags:
            # 获取这条短信的所有标签
            sms_tags = db.query(SMSTag, Tag).join(
                Tag, SMSTag.tag_id == Tag.id
            ).filter(
                SMSTag.sms_id == sms_id
            ).all()
            
            if not sms_tags:
                continue
            
            # 获取短信内容（用于显示）
            sms = db.query(SMSMessage).filter(SMSMessage.id == sms_id).first()
            sender = sms.sender if sms else "未知"
            content_preview = sms.content[:30] + "..." if sms and len(sms.content) > 30 else (sms.content if sms else "")
            
            # 按优先级排序标签
            tag_list = []
            for sms_tag, tag in sms_tags:
                priority = TAG_PRIORITY.get(tag.name, 999)
                tag_list.append({
                    'sms_id': sms_tag.sms_id,
                    'tag_id': sms_tag.tag_id,
                    'tag_name': tag.name,
                    'priority': priority
                })
            
            # 按优先级排序
            tag_list.sort(key=lambda x: x['priority'])
            
            # 保留优先级最高的标签
            keep_tag = tag_list[0]
            remove_tags = tag_list[1:]
            
            print(f"短信 ID: {sms_id}")
            print(f"  发件人: {sender}")
            print(f"  内容: {content_preview}")
            print(f"  当前标签: {', '.join([t['tag_name'] for t in tag_list])}")
            print(f"  保留: {keep_tag['tag_name']}")
            print(f"  删除: {', '.join([t['tag_name'] for t in remove_tags])}")
            
            # 删除其他标签关联
            for tag in remove_tags:
                db.query(SMSTag).filter(
                    SMSTag.sms_id == tag['sms_id'],
                    SMSTag.tag_id == tag['tag_id']
                ).delete()
                removed_tags_count += 1
            
            cleaned_count += 1
            print(f"  ✅ 已清理\n")
        
        # 提交更改
        db.commit()
        
        print("=== 清理完成 ===")
        print(f"处理短信数: {cleaned_count}")
        print(f"删除标签关联数: {removed_tags_count}")
        print(f"✅ 现在每条短信只有一个标签")
        
    except Exception as e:
        db.rollback()
        print(f"❌ 清理失败: {e}")
        raise
    finally:
        db.close()


def verify_cleanup():
    """验证清理结果"""
    db = SessionLocal()
    
    try:
        print("\n=== 验证清理结果 ===\n")
        
        # 检查是否还有多标签的短信
        sms_with_multiple_tags = db.query(
            SMSTag.sms_id,
            func.count(SMSTag.tag_id).label('tag_count')
        ).group_by(
            SMSTag.sms_id
        ).having(
            func.count(SMSTag.tag_id) > 1
        ).all()
        
        if sms_with_multiple_tags:
            print(f"⚠️  仍有 {len(sms_with_multiple_tags)} 条短信有多个标签")
            for sms_id, tag_count in sms_with_multiple_tags:
                print(f"  短信 ID {sms_id}: {tag_count} 个标签")
        else:
            print("✅ 验证通过：所有短信都只有一个或零个标签")
        
        # 统计标签分布
        print("\n标签分布统计:")
        tag_stats = db.query(
            Tag.name,
            func.count(SMSTag.sms_id).label('sms_count')
        ).outerjoin(
            SMSTag, Tag.id == SMSTag.tag_id
        ).group_by(
            Tag.id
        ).order_by(
            func.count(SMSTag.sms_id).desc()
        ).all()
        
        for tag_name, sms_count in tag_stats:
            print(f"  {tag_name}: {sms_count} 条短信")
        
    finally:
        db.close()


if __name__ == '__main__':
    print("开始清理重复标签...\n")
    clean_duplicate_tags()
    verify_cleanup()
    print("\n完成！")
