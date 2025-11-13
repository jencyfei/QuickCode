"""
测试数据库连接
"""
import sys
from pathlib import Path

# 添加项目根目录到路径
sys.path.insert(0, str(Path(__file__).parent))

try:
    from app.config import settings
    from sqlalchemy import create_engine, text
    
    print("=" * 50)
    print("  数据库连接测试")
    print("=" * 50)
    print()
    
    # 显示配置（隐藏密码）
    db_url = settings.DATABASE_URL
    if '@' in db_url:
        parts = db_url.split('@')
        user_pass = parts[0].split('://')[-1]
        if ':' in user_pass:
            user = user_pass.split(':')[0]
            masked_url = db_url.replace(user_pass, f"{user}:****")
            print(f"数据库URL: {masked_url}")
        else:
            print(f"数据库URL: {db_url}")
    else:
        print(f"数据库URL: {db_url}")
    
    print()
    print("正在连接...")
    
    # 尝试连接
    engine = create_engine(settings.DATABASE_URL)
    
    with engine.connect() as conn:
        # 测试查询
        result = conn.execute(text("SELECT version()"))
        version = result.fetchone()[0]
        
        # 获取数据库名称
        result = conn.execute(text("SELECT current_database()"))
        db_name = result.fetchone()[0]
        
        print()
        print("✅ 数据库连接成功!")
        print()
        print(f"数据库名称: {db_name}")
        print(f"PostgreSQL 版本: {version.split(',')[0]}")
        print()
        
        # 检查表是否存在
        result = conn.execute(text("""
            SELECT table_name 
            FROM information_schema.tables 
            WHERE table_schema = 'public'
            ORDER BY table_name
        """))
        
        tables = [row[0] for row in result]
        
        if tables:
            print(f"已存在的表 ({len(tables)}):")
            for table in tables:
                print(f"  - {table}")
        else:
            print("⚠️  数据库中还没有表")
            print("   首次启动后端服务时会自动创建")
        
        print()
        print("=" * 50)
        print("  测试完成")
        print("=" * 50)
        
except ImportError as e:
    print()
    print("❌ 导入错误:", e)
    print()
    print("请确保:")
    print("  1. 在 backend 目录下运行此脚本")
    print("  2. 已安装所有依赖: pip install -r requirements.txt")
    print()
    sys.exit(1)
    
except FileNotFoundError:
    print()
    print("❌ 配置文件错误")
    print()
    print("请确保 .env 文件存在于 backend 目录")
    print("可以复制 .env.example 并修改:")
    print("  copy .env.example .env")
    print()
    sys.exit(1)
    
except Exception as e:
    print()
    print("❌ 数据库连接失败!")
    print()
    print(f"错误信息: {e}")
    print()
    print("可能的原因:")
    print("  1. PostgreSQL 服务未运行")
    print("     解决: net start postgresql-x64-14")
    print()
    print("  2. 数据库密码错误")
    print("     解决: 检查 .env 文件中的 DATABASE_URL")
    print()
    print("  3. 数据库不存在")
    print("     解决: psql -U postgres -c \"CREATE DATABASE sms_agent;\"")
    print()
    print("  4. PostgreSQL 端口错误")
    print("     解决: 检查 PostgreSQL 是否运行在 5432 端口")
    print()
    sys.exit(1)
