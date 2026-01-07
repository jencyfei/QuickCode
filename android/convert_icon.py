#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
将 JPG 图标转换为 Android 应用图标
生成所有需要的尺寸和格式
"""

import os
from PIL import Image
import sys

# 源文件路径
SOURCE_ICON = r"D:\tools\python\mypro\sms_agent\frontend\src\assets\icons\icon1.jpg"

# Android 图标尺寸配置（密度目录 -> 尺寸）
ICON_SIZES = {
    "mipmap-ldpi": 36,
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

# 资源目录基础路径（相对于脚本所在目录）
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)
RES_BASE_PATH = os.path.join(PROJECT_ROOT, "android", "app", "src", "main", "res")

def convert_icon():
    """转换图标"""
    if not os.path.exists(SOURCE_ICON):
        print(f"错误: 源文件不存在: {SOURCE_ICON}")
        return False
    
    # 打开源图片
    try:
        source_img = Image.open(SOURCE_ICON)
        # 转换为 RGBA 模式（支持透明）
        if source_img.mode != 'RGBA':
            source_img = source_img.convert('RGBA')
        print(f"[OK] 成功加载源图片: {source_img.width}x{source_img.height} px")
    except Exception as e:
        print(f"[ERROR] 无法打开源图片: {e}")
        return False
    
    # 为每个密度目录生成图标
    for density_dir, size in ICON_SIZES.items():
        density_path = os.path.join(RES_BASE_PATH, density_dir)
        
        # 确保目录存在
        if not os.path.exists(density_path):
            os.makedirs(density_path)
            print(f"[OK] 创建目录: {density_path}")
        
        # 生成完整图标 (ic_launcher.png)
        icon_path = os.path.join(density_path, "ic_launcher.png")
        resized_icon = source_img.resize((size, size), Image.Resampling.LANCZOS)
        resized_icon.save(icon_path, "PNG", optimize=True)
        print(f"[OK] 生成: {icon_path} ({size}x{size})")
        
        # 生成前景层图标 (ic_launcher_foreground.png)
        foreground_path = os.path.join(density_path, "ic_launcher_foreground.png")
        resized_icon.save(foreground_path, "PNG", optimize=True)
        print(f"[OK] 生成: {foreground_path} ({size}x{size})")
    
    print("\n[OK] 所有图标已成功生成！")
    return True

if __name__ == "__main__":
    print("=" * 60)
    print("Android 应用图标转换工具")
    print("=" * 60)
    print(f"源文件: {SOURCE_ICON}")
    print(f"目标目录: {RES_BASE_PATH}")
    print("=" * 60)
    print()
    
    if convert_icon():
        print("\n完成！图标已替换。")
        print("\n注意: 当前使用相同的图标作为前景层和完整图标。")
        print("如果图标需要背景层，请修改 drawable/ic_launcher_background.xml")
    else:
        print("\n失败！请检查错误信息。")
        sys.exit(1)

