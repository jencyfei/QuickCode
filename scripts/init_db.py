"""
数据库初始化脚本
创建所有表结构
"""
import sys
import os
from pathlib import Path

# 添加backend目录到Python路径
backend_path = Path(__file__).parent.parent / "backend"
sys.path.insert(0, str(backend_path))

# 切换工作目录到backend，以便正确加载.env文件
os.chdir(backend_path)

from app.database import engine, Base
from app.models import User, SMSMessage, Tag, SMSTag


def init_database():
    """初始化数据库，创建所有表"""
    print("🔧 开始初始化数据库...")
    
    try:
        # 创建所有表
        Base.metadata.create_all(bind=engine)
        print("✅ 数据库表创建成功！")
        print("\n已创建的表:")
        print("  - users (用户表)")
        print("  - sms_messages (短信表)")
        print("  - tags (标签表)")
        print("  - sms_tags (短信-标签关联表)")
        
    except Exception as e:
        print(f"❌ 数据库初始化失败: {e}")
        sys.exit(1)


if __name__ == "__main__":
    init_database()
