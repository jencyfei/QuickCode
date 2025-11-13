"""
测试数据库连接
"""
import sys

print("=" * 60)
print("测试 PostgreSQL 数据库连接")
print("=" * 60)
print()

# 测试不同的连接字符串
connection_strings = [
    "postgresql://postgres:123123@localhost:5432/sms_tagger",
    "postgresql://postgres:123123@127.0.0.1:5432/sms_tagger",
    "postgresql://postgres@localhost:5432/sms_tagger",
]

for i, conn_str in enumerate(connection_strings, 1):
    print(f"测试 {i}: {conn_str}")
    try:
        import psycopg2
        # 解析连接字符串
        parts = conn_str.replace("postgresql://", "").split("@")
        user_pass = parts[0].split(":")
        host_db = parts[1].split("/")
        host_port = host_db[0].split(":")
        
        user = user_pass[0]
        password = user_pass[1] if len(user_pass) > 1 else None
        host = host_port[0]
        port = host_port[1] if len(host_port) > 1 else "5432"
        database = host_db[1]
        
        print(f"  用户: {user}")
        print(f"  密码: {'*' * len(password) if password else '(无)'}")
        print(f"  主机: {host}")
        print(f"  端口: {port}")
        print(f"  数据库: {database}")
        
        # 尝试连接
        if password:
            conn = psycopg2.connect(
                host=host,
                port=port,
                user=user,
                password=password,
                database=database
            )
        else:
            conn = psycopg2.connect(
                host=host,
                port=port,
                user=user,
                database=database
            )
        
        print(f"  结果: [成功] ✓")
        conn.close()
        print()
        print("=" * 60)
        print(f"找到可用的连接字符串！")
        print(f"请使用: {conn_str}")
        print("=" * 60)
        sys.exit(0)
        
    except Exception as e:
        print(f"  结果: [失败] ✗")
        print(f"  错误: {str(e)}")
    print()

print("=" * 60)
print("所有连接尝试都失败了")
print("=" * 60)
print()
print("可能的原因：")
print("1. PostgreSQL 服务未运行")
print("2. 密码不正确")
print("3. 数据库 sms_tagger 不存在")
print("4. PostgreSQL 未配置允许本地连接")
print()
print("请检查：")
print("1. 运行: services.msc 查看 PostgreSQL 服务")
print("2. 确认密码（默认可能是空密码或 postgres）")
print("3. 使用 pgAdmin 或 psql 手动连接测试")
