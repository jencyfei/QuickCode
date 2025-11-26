"""
激活码生成工具（开发者侧使用）

协议严格参考 docs/加密.md：
  明文格式: "{device_id}:{max_activations}:{salt}"
  加密:     Fernet(key).encrypt(plaintext)
  输出:     base64.urlsafe_b64encode(encrypted).decode()

注意：
  1. 本脚本仅运行在开发者环境，不随 APK 分发。
  2. Fernet 密钥必须妥善保管，不要提交到 Git 或泄露给用户。
"""

import argparse
import base64
import os
import secrets
import sys
from pathlib import Path

from cryptography.fernet import Fernet


DEFAULT_KEY_FILE = Path(__file__).with_name("activation.key")


def load_key(key_file: Path = DEFAULT_KEY_FILE) -> bytes:
    """
    从本地 key 文件加载 Fernet 密钥。

    - 若文件不存在，提示开发者先生成并保存密钥：
        python -c "from cryptography.fernet import Fernet; print(Fernet.generate_key().decode())"
      然后将输出内容写入 tools/activation.key
    """
    if not key_file.exists():
        print(
            f"[错误] 未找到密钥文件: {key_file}\n"
            "请先在开发者环境中生成 Fernet 密钥，例如：\n\n"
            "  python -c \"from cryptography.fernet import Fernet; print(Fernet.generate_key().decode())\"\n\n"
            "然后将输出内容保存到:\n"
            f"  {key_file}\n",
            file=sys.stderr,
        )
        sys.exit(1)

    data = key_file.read_text(encoding="utf-8").strip()
    if not data:
        print(f"[错误] 密钥文件 {key_file} 为空，请重新写入有效 Fernet key。", file=sys.stderr)
        sys.exit(1)

    try:
        # Fernet key 本身就是 base64 字符串，这里直接返回字节
        return data.encode("utf-8")
    except Exception as exc:  # pragma: no cover - 极端场景
        print(f"[错误] 读取密钥失败: {exc}", file=sys.stderr)
        sys.exit(1)


def generate_activation_code(device_id: str, max_activations: int, key: bytes) -> str:
    """
    生成单个激活码：
      明文: "{device_id}:{max_activations}:{salt}"
      加密: Fernet(key).encrypt(明文)
      输出: base64.urlsafe_b64encode(encrypted).decode()
    """
    salt = secrets.token_hex(8)
    payload = f"{device_id}:{max_activations}:{salt}"

    cipher = Fernet(key)
    encrypted = cipher.encrypt(payload.encode("utf-8"))

    # 再包一层 urlsafe base64，与 docs/加密.md 示例保持一致
    code = base64.urlsafe_b64encode(encrypted).decode("utf-8")
    return code


def parse_args(argv=None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="生成短信管理 APP 的买断激活码（开发者使用）"
    )
    parser.add_argument(
        "--device-id",
        required=True,
        help="目标设备 ID（由 APP 显示，开发者复制过来）",
    )
    parser.add_argument(
        "--max-activations",
        type=int,
        default=3,
        help="该激活码允许的最大激活次数（默认 3）",
    )
    parser.add_argument(
        "--count",
        type=int,
        default=1,
        help="批量生成的数量（默认 1）",
    )
    parser.add_argument(
        "--output-csv",
        type=str,
        default="",
        help="可选：将结果追加写入 CSV 文件（device_id,max_activations,code）",
    )
    parser.add_argument(
        "--key-file",
        type=str,
        default=str(DEFAULT_KEY_FILE),
        help=f"Fernet 密钥文件路径（默认: {DEFAULT_KEY_FILE})",
    )
    return parser.parse_args(argv)


def append_to_csv(path: Path, rows) -> None:
    header_needed = not path.exists()
    with path.open("a", encoding="utf-8") as f:
        if header_needed:
            f.write("device_id,max_activations,code\n")
        for device_id, max_acts, code in rows:
            # 简单转义逗号和引号场景可以按需扩展，这里不做复杂 CSV 处理
            f.write(f"{device_id},{max_acts},{code}\n")


def main(argv=None) -> None:
    args = parse_args(argv)

    key_file = Path(args.key_file)
    key = load_key(key_file)

    device_id = args.device_id.strip()
    if not device_id:
        print("[错误] device-id 不能为空。", file=sys.stderr)
        sys.exit(1)

    if args.max_activations <= 0:
        print("[错误] max-activations 必须为正整数。", file=sys.stderr)
        sys.exit(1)

    if args.count <= 0:
        print("[错误] count 必须为正整数。", file=sys.stderr)
        sys.exit(1)

    rows_for_csv = []
    print("==== 激活码生成结果 ====")
    print(f"设备 ID          : {device_id}")
    print(f"最大激活次数     : {args.max_activations}")
    print(f"生成数量         : {args.count}")
    print("-----------------------------")

    for i in range(args.count):
        code = generate_activation_code(device_id, args.max_activations, key)
        print(f"[{i+1}] {code}")
        rows_for_csv.append((device_id, args.max_activations, code))

    if args.output_csv:
        csv_path = Path(args.output_csv)
        append_to_csv(csv_path, rows_for_csv)
        print(f"\n已追加写入 CSV 文件: {csv_path.resolve()}")


if __name__ == "__main__":
    main()


