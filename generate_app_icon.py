#!/usr/bin/env python3
"""
生成符合"柔和玻璃"风格的应用图标
"""

from PIL import Image, ImageDraw, ImageFilter
import os

def create_soft_glass_icon(size=192):
    """
    创建柔和玻璃风格的应用图标
    
    Args:
        size: 图标大小（像素）
    
    Returns:
        PIL Image 对象
    """
    # 创建背景渐变
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    
    # 创建渐变背景
    gradient = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    pixels = gradient.load()
    
    # 柔和玻璃风格颜色
    bg_main = (249, 248, 255)      # #F9F8FF
    gradient_pink = (250, 208, 196)  # #FAD0C4
    gradient_purple = (217, 200, 255) # #D9C8FF
    
    # 绘制渐变背景
    for y in range(size):
        for x in range(size):
            # 计算渐变比例
            ratio_x = x / size
            ratio_y = y / size
            ratio = (ratio_x + ratio_y) / 2
            
            # 混合颜色
            if ratio < 0.5:
                # 从主色到粉色
                t = ratio * 2
                r = int(bg_main[0] * (1 - t) + gradient_pink[0] * t)
                g = int(bg_main[1] * (1 - t) + gradient_pink[1] * t)
                b = int(bg_main[2] * (1 - t) + gradient_pink[2] * t)
            else:
                # 从粉色到紫色
                t = (ratio - 0.5) * 2
                r = int(gradient_pink[0] * (1 - t) + gradient_purple[0] * t)
                g = int(gradient_pink[1] * (1 - t) + gradient_purple[1] * t)
                b = int(gradient_pink[2] * (1 - t) + gradient_purple[2] * t)
            
            pixels[x, y] = (r, g, b, 255)
    
    # 合并背景
    img.paste(gradient, (0, 0), gradient)
    
    # 创建主卡片（玻璃拟态）
    draw = ImageDraw.Draw(img)
    
    # 卡片参数
    card_margin = int(size * 0.125)  # 12.5% 边距
    card_x1 = card_margin
    card_y1 = card_margin
    card_x2 = size - card_margin
    card_y2 = size - card_margin
    card_radius = int(size * 0.1875)  # 18.75% 圆角
    
    # 绘制卡片背景（半透明白色）
    card_img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    card_draw = ImageDraw.Draw(card_img)
    
    # 绘制圆角矩形
    card_draw.rounded_rectangle(
        [(card_x1, card_y1), (card_x2, card_y2)],
        radius=card_radius,
        fill=(255, 255, 255, 128),  # 50% 透明白色
        outline=(255, 255, 255, 179)  # 70% 透明白色边框
    )
    
    # 应用模糊效果（模拟玻璃拟态）
    card_img = card_img.filter(ImageFilter.GaussianBlur(radius=2))
    img.paste(card_img, (0, 0), card_img)
    
    # 绘制装饰圆点
    dot1_x = int(size * 0.25)
    dot1_y = int(size * 0.25)
    dot1_r = int(size * 0.042)
    draw.ellipse(
        [(dot1_x - dot1_r, dot1_y - dot1_r), (dot1_x + dot1_r, dot1_y + dot1_r)],
        fill=(102, 126, 234, 38)  # 蓝紫色，15% 透明
    )
    
    dot2_x = int(size * 0.75)
    dot2_y = int(size * 0.75)
    dot2_r = int(size * 0.031)
    draw.ellipse(
        [(dot2_x - dot2_r, dot2_y - dot2_r), (dot2_x + dot2_r, dot2_y + dot2_r)],
        fill=(212, 200, 255, 51)  # 紫色，20% 透明
    )
    
    # 绘制主图标：信封 + 消息气泡
    icon_center_x = size // 2
    icon_center_y = size // 2
    icon_size = int(size * 0.35)
    
    # 信封主体
    envelope_x1 = icon_center_x - icon_size
    envelope_y1 = icon_center_y - int(icon_size * 0.6)
    envelope_x2 = icon_center_x + icon_size
    envelope_y2 = icon_center_y + int(icon_size * 0.6)
    
    # 蓝紫色渐变色
    accent_color = (102, 126, 234, 255)
    
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


def save_icons(output_dir="D:\\tools\\python\\mypro\\sms_agent\\android\\app\\src\\main\\res"):
    """
    生成并保存多个尺寸的图标
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
        icon = create_soft_glass_icon(size)
        
        # 确定输出目录
        if density == 'xxxhdpi':
            dir_name = 'mipmap-xxxhdpi'
        else:
            dir_name = f'mipmap-{density}'
        
        output_path = os.path.join(output_dir, dir_name)
        os.makedirs(output_path, exist_ok=True)
        
        # 保存图标
        icon_file = os.path.join(output_path, 'ic_launcher.png')
        icon.save(icon_file, 'PNG')
        print(f"✓ 已生成: {icon_file} ({size}x{size})")
    
    print("\n✅ 所有图标已生成完成！")


if __name__ == '__main__':
    save_icons()
