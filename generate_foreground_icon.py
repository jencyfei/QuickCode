#!/usr/bin/env python3
"""
生成前景图标（用于自适应图标）
"""

from PIL import Image, ImageDraw
import os

def create_foreground_icon(size=192):
    """
    创建前景图标（透明背景）
    
    Args:
        size: 图标大小（像素）
    
    Returns:
        PIL Image 对象
    """
    # 创建透明背景
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # 蓝紫色渐变色
    accent_color = (102, 126, 234, 255)
    
    # 绘制主图标：信封 + 消息气泡
    icon_center_x = size // 2
    icon_center_y = size // 2
    icon_size = int(size * 0.35)
    
    # 信封主体
    envelope_x1 = icon_center_x - icon_size
    envelope_y1 = icon_center_y - int(icon_size * 0.6)
    envelope_x2 = icon_center_x + icon_size
    envelope_y2 = icon_center_y + int(icon_size * 0.6)
    
    # 绘制信封
    draw.rectangle(
        [(envelope_x1, envelope_y1), (envelope_x2, envelope_y2)],
        outline=accent_color,
        width=3
    )
    
    # 绘制信封盖（三角形）
    envelope_top = [
        (envelope_x1, envelope_y1),
        (icon_center_x, envelope_y1 + int(icon_size * 0.4)),
        (envelope_x2, envelope_y1)
    ]
    draw.polygon(envelope_top, outline=accent_color, width=3)
    
    # 绘制消息气泡（右下叠加）
    bubble_x = icon_center_x + int(icon_size * 0.5)
    bubble_y = icon_center_y + int(icon_size * 0.3)
    bubble_r = int(icon_size * 0.35)
    
    # 气泡圆形
    draw.ellipse(
        [(bubble_x - bubble_r, bubble_y - bubble_r), (bubble_x + bubble_r, bubble_y + bubble_r)],
        fill=accent_color
    )
    
    # 气泡尖角
    tail_points = [
        (bubble_x - int(bubble_r * 0.3), bubble_y + bubble_r),
        (bubble_x - int(bubble_r * 0.8), bubble_y + int(bubble_r * 1.3)),
        (bubble_x, bubble_y + bubble_r)
    ]
    draw.polygon(tail_points, fill=accent_color)
    
    return img


def save_foreground_icons(output_dir="D:\\tools\\python\\mypro\\sms_agent\\android\\app\\src\\main\\res"):
    """
    生成并保存前景图标
    """
    sizes = {
        'ldpi': 36,
        'mdpi': 48,
        'hdpi': 72,
        'xhdpi': 96,
        'xxhdpi': 144,
        'xxxhdpi': 192,
    }
    
    for density, size in sizes.items():
        # 创建图标
        icon = create_foreground_icon(size)
        
        # 确定输出目录
        if density == 'xxxhdpi':
            dir_name = 'mipmap-xxxhdpi'
        else:
            dir_name = f'mipmap-{density}'
        
        output_path = os.path.join(output_dir, dir_name)
        os.makedirs(output_path, exist_ok=True)
        
        # 保存图标
        icon_file = os.path.join(output_path, 'ic_launcher_foreground.png')
        icon.save(icon_file, 'PNG')
        print(f"✓ 已生成: {icon_file} ({size}x{size})")
    
    print("\n✅ 所有前景图标已生成完成！")


if __name__ == '__main__':
    save_foreground_icons()
