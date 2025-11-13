# 新增功能说明文档

## 概述

本文档描述了短信标签助手新增的两个核心功能：
1. **时间范围筛选功能**
2. **快递取件码分类与排序功能**

---

## 功能1：时间范围筛选

### 功能描述
用户可以按时间范围筛选短信，快速查看特定时间段内的短信记录。

### 页面位置
- **主页面**：短信列表页
- **入口**：顶部时间筛选栏

### 功能特性

#### 1. 快捷筛选按钮
- **今天**：显示当天的所有短信
- **本周**：显示本周（周一至周日）的所有短信
- **本月**：显示本月的所有短信（默认选中）
- **自定义**：用户自定义时间范围

#### 2. 默认行为
- 应用启动时，默认显示**本月**的所有短信
- 时间范围：本月1日 00:00 - 本月最后一天 23:59

#### 3. 自定义时间选择
- 点击"自定义"按钮，弹出日期选择器
- 用户可以选择：
  - 开始日期
  - 结束日期
- 点击"确定"后，列表自动刷新

#### 4. 时间范围显示
- 顶部显示当前筛选的时间范围
- 显示该时间范围内的短信总数
- 格式：`📅 2025年11月 (共28条短信)`

### 技术实现

#### 前端实现
```javascript
// 时间筛选逻辑
const timeFilters = {
  today: () => {
    const today = new Date();
    return {
      start: startOfDay(today),
      end: endOfDay(today)
    };
  },
  
  thisWeek: () => {
    const today = new Date();
    return {
      start: startOfWeek(today),
      end: endOfWeek(today)
    };
  },
  
  thisMonth: () => {
    const today = new Date();
    return {
      start: startOfMonth(today),
      end: endOfMonth(today)
    };
  },
  
  custom: (startDate, endDate) => {
    return {
      start: startOfDay(startDate),
      end: endOfDay(endDate)
    };
  }
};
```

#### 后端API
```python
# GET /api/sms/list
# Query Parameters:
# - start_date: 开始日期 (YYYY-MM-DD)
# - end_date: 结束日期 (YYYY-MM-DD)
# - tag_id: 标签ID（可选）
# - page: 页码
# - page_size: 每页数量

@router.get("/sms/list")
async def get_sms_list(
    start_date: str,
    end_date: str,
    tag_id: Optional[int] = None,
    page: int = 1,
    page_size: int = 20,
    current_user: User = Depends(get_current_user)
):
    # 解析日期
    start = datetime.strptime(start_date, "%Y-%m-%d")
    end = datetime.strptime(end_date, "%Y-%m-%d") + timedelta(days=1)
    
    # 查询短信
    query = db.query(SMS).filter(
        SMS.user_id == current_user.id,
        SMS.received_at >= start,
        SMS.received_at < end
    )
    
    if tag_id:
        query = query.filter(SMS.tags.any(Tag.id == tag_id))
    
    total = query.count()
    sms_list = query.offset((page - 1) * page_size).limit(page_size).all()
    
    return {
        "total": total,
        "data": sms_list,
        "time_range": {
            "start": start_date,
            "end": end_date
        }
    }
```

---

## 功能2：快递取件码分类与排序

### 功能描述
针对"快递"标签的短信，提供专门的详情页，按取件地址分组显示，取件码按数字大小排序，方便用户一键查看所有快递取件码。

### 使用场景
用户去驿站取快递时，打开这个页面，可以：
1. 看到所有待取快递的取件码
2. 按地址分组，知道哪些快递在同一个地方
3. 取件码已排序，按顺序取件不会遗漏

### 页面位置
- **入口1**：标签管理页 → 点击"快递"标签
- **入口2**：短信列表页 → 筛选"快递"标签 → 点击"查看详情"

### 功能特性

#### 1. 统计信息
- **待取快递**：当前时间范围内未取的快递数量
- **取件地址**：不同取件地址的数量
- **本月总计**：本月收到的快递短信总数

#### 2. 按地址分组
- 自动识别短信中的取件地址
- 相同地址的快递归为一组
- 每组显示该地址的快递数量

#### 3. 取件码排序
- 每组内的取件码按数字从小到大排序
- 例如：1234 → 5678 → 8888
- 方便用户按顺序取件

#### 4. 快递信息显示
- **取件码**：大号粗体显示，易于识别
- **时间**：快递到达时间
- **快递公司**：发件人信息

#### 5. 快捷操作
- **单个复制**：点击"复制"按钮，复制单个取件码
- **批量复制**：点击右下角浮动按钮，复制所有取件码（格式化）

### 地址识别逻辑

#### 识别关键词
```python
# 地址识别关键词
location_keywords = [
    "驿站", "快递柜", "门卫", "保安室",
    "自提点", "代收点", "收发室"
]

# 地址前缀
location_prefixes = [
    r"\w+路", r"\w+街", r"\w+巷",
    r"\w+小区", r"\w+大厦", r"\w+广场"
]
```

#### 提取逻辑
```python
import re

def extract_location(sms_content: str) -> str:
    """从短信内容中提取取件地址"""
    
    # 方法1：匹配关键词
    for keyword in location_keywords:
        pattern = rf"(.{{0,10}}{keyword})"
        match = re.search(pattern, sms_content)
        if match:
            return match.group(1).strip()
    
    # 方法2：匹配地址前缀
    for prefix in location_prefixes:
        pattern = rf"({prefix}.{{0,10}})"
        match = re.search(pattern, sms_content)
        if match:
            return match.group(1).strip()
    
    # 方法3：默认返回"其他地址"
    return "其他地址"

# 示例
sms1 = "您的快递已到达XX路菜鸟驿站，取件码：1234"
print(extract_location(sms1))  # 输出: XX路菜鸟驿站

sms2 = "快递已放在YY小区快递柜，取件码：5678"
print(extract_location(sms2))  # 输出: YY小区快递柜
```

### 取件码提取与排序

#### 提取逻辑
```python
def extract_pickup_code(sms_content: str) -> Optional[str]:
    """从短信内容中提取取件码（4-6位数字）"""
    
    # 匹配取件码关键词后的数字
    patterns = [
        r"取件码[：:]\s*(\d{4,6})",
        r"验证码[：:]\s*(\d{4,6})",
        r"提取码[：:]\s*(\d{4,6})",
        r"密码[：:]\s*(\d{4,6})"
    ]
    
    for pattern in patterns:
        match = re.search(pattern, sms_content)
        if match:
            return match.group(1)
    
    # 如果没有关键词，尝试提取4-6位连续数字
    match = re.search(r"\b(\d{4,6})\b", sms_content)
    if match:
        return match.group(1)
    
    return None
```

#### 排序逻辑
```python
def sort_express_by_code(express_list: List[Express]) -> List[Express]:
    """按取件码数字大小排序"""
    return sorted(express_list, key=lambda x: int(x.pickup_code or "0"))
```

### 批量复制格式

```
📍 XX路菜鸟驿站
1234 | 11-04 15:20 | 菜鸟驿站
5678 | 11-03 10:15 | 顺丰速运
8888 | 11-02 14:30 | 京东快递

📍 YY小区快递柜
2468 | 11-04 09:00 | 中通快递
3579 | 11-03 16:45 | 圆通速递
9999 | 11-01 11:20 | 韵达快递

📍 ZZ大厦门卫
1111 | 11-04 08:30 | 申通快递
6666 | 11-02 13:15 | 邮政EMS
```

### 技术实现

#### 后端API
```python
# GET /api/express/grouped
# Query Parameters:
# - start_date: 开始日期
# - end_date: 结束日期

@router.get("/express/grouped")
async def get_express_grouped(
    start_date: str,
    end_date: str,
    current_user: User = Depends(get_current_user)
):
    # 查询快递标签的短信
    express_tag = db.query(Tag).filter(
        Tag.user_id == current_user.id,
        Tag.name == "快递"
    ).first()
    
    if not express_tag:
        return {"locations": [], "stats": {"total": 0, "locations": 0, "pending": 0}}
    
    # 查询时间范围内的快递短信
    start = datetime.strptime(start_date, "%Y-%m-%d")
    end = datetime.strptime(end_date, "%Y-%m-%d") + timedelta(days=1)
    
    sms_list = db.query(SMS).filter(
        SMS.user_id == current_user.id,
        SMS.tags.any(Tag.id == express_tag.id),
        SMS.received_at >= start,
        SMS.received_at < end
    ).all()
    
    # 按地址分组
    location_groups = {}
    for sms in sms_list:
        location = extract_location(sms.content)
        pickup_code = extract_pickup_code(sms.content)
        
        if location not in location_groups:
            location_groups[location] = []
        
        location_groups[location].append({
            "id": sms.id,
            "pickup_code": pickup_code,
            "sender": sms.sender,
            "received_at": sms.received_at,
            "content": sms.content
        })
    
    # 每组内按取件码排序
    for location in location_groups:
        location_groups[location] = sort_express_by_code(location_groups[location])
    
    # 统计信息
    total_count = len(sms_list)
    location_count = len(location_groups)
    
    return {
        "locations": [
            {
                "name": location,
                "count": len(items),
                "items": items
            }
            for location, items in location_groups.items()
        ],
        "stats": {
            "total": total_count,
            "locations": location_count,
            "pending": total_count  # 实际应该根据是否已取判断
        }
    }
```

#### Android端实现
```kotlin
// ExpressDetailViewModel.kt
class ExpressDetailViewModel : ViewModel() {
    private val _expressGroups = MutableLiveData<List<ExpressLocationGroup>>()
    val expressGroups: LiveData<List<ExpressLocationGroup>> = _expressGroups
    
    private val _stats = MutableLiveData<ExpressStats>()
    val stats: LiveData<ExpressStats> = _stats
    
    fun loadExpressData(startDate: String, endDate: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getExpressGrouped(startDate, endDate)
                _expressGroups.value = response.locations
                _stats.value = response.stats
            } catch (e: Exception) {
                // 错误处理
            }
        }
    }
    
    fun copyAllCodes(): String {
        val groups = _expressGroups.value ?: return ""
        
        return groups.joinToString("\n\n") { group ->
            val header = "📍 ${group.name}"
            val items = group.items.joinToString("\n") { item ->
                "${item.pickupCode} | ${formatDate(item.receivedAt)} | ${item.sender}"
            }
            "$header\n$items"
        }
    }
}
```

---

## 数据库设计

### 新增字段（可选优化）

```sql
-- SMS表新增字段
ALTER TABLE sms ADD COLUMN pickup_code VARCHAR(10);  -- 取件码
ALTER TABLE sms ADD COLUMN pickup_location VARCHAR(100);  -- 取件地址
ALTER TABLE sms ADD COLUMN is_picked_up BOOLEAN DEFAULT FALSE;  -- 是否已取件

-- 创建索引
CREATE INDEX idx_sms_received_at ON sms(received_at);
CREATE INDEX idx_sms_pickup_location ON sms(pickup_location);
```

---

## 页面预览文件

1. **时间筛选功能**：`docs/time_filter_page.html`
2. **快递详情页**：`docs/express_detail_page.html`

---

## 开发优先级

### Phase 1（必须）
- ✅ 时间范围筛选（今天、本周、本月）
- ✅ 自定义时间选择
- ✅ 快递按地址分组
- ✅ 取件码排序

### Phase 2（增强）
- ⏳ 地址识别优化（机器学习）
- ⏳ 取件状态标记
- ⏳ 快递物流追踪
- ⏳ 到期提醒（快递超过3天未取）

### Phase 3（高级）
- ⏳ 地图导航（显示驿站位置）
- ⏳ 批量取件模式（扫码取件）
- ⏳ 快递统计分析

---

## 用户体验优化建议

1. **默认行为**：启动App时默认显示本月短信，符合用户习惯
2. **快捷操作**：快递详情页提供一键复制所有取件码
3. **视觉突出**：取件码用大号粗体显示，易于识别
4. **智能分组**：自动识别地址，减少用户操作
5. **排序优化**：取件码排序，方便按顺序取件

---

## 测试用例

### 时间筛选功能
1. 默认显示本月短信
2. 切换到"今天"，显示当天短信
3. 切换到"本周"，显示本周短信
4. 自定义时间范围，显示指定时间段短信
5. 时间范围显示正确的短信数量

### 快递详情功能
1. 正确识别取件地址
2. 相同地址的快递归为一组
3. 取件码按数字大小排序
4. 单个取件码复制功能
5. 批量复制所有取件码
6. 空状态显示正确

---

## 总结

这两个新功能极大提升了用户体验：
- **时间筛选**：让用户快速找到特定时间段的短信
- **快递详情**：解决了用户取快递时的痛点，一键查看所有取件码

两个功能都遵循"简约可爱"的设计风格，操作简单直观。
