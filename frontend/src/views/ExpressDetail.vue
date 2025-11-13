<template>
  <div class="express-detail-page glass-background">
    <!-- 导航栏 -->
    <van-nav-bar
      title="📦 快递 - 取件码"
      fixed
      class="cute-navbar"
    >
      <template #left>
        <span class="nav-icon" @click="showMenu = true">☰</span>
      </template>
      <template #right>
        <van-icon name="ellipsis" />
      </template>
    </van-nav-bar>

    <!-- 统计信息 -->
    <div class="express-header">
      <div class="express-stats-v2">
        <div class="express-stat-primary">
          <div class="express-stat-number-large">{{ unpickedCount }}</div>
          <div class="express-stat-label">待取快递</div>
        </div>
        <div class="express-stat-secondary">
          <div class="express-stat-info">{{ locationCount }}个取件地址</div>
        </div>
      </div>
    </div>

    <!-- 快递列表 -->
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div class="express-list">
      <!-- 按地址分组 -->
      <div
        v-for="location in groupedExpressList"
        :key="location.address"
        class="express-location-section"
      >
        <!-- 地址标题 -->
        <div class="express-location-header">
          <div class="express-location-title">
            <span>📍</span>
            <span>{{ location.address }}</span>
          </div>
          <div class="express-location-count">
            <span class="sort-indicator">未取{{ location.unpickedCount }}件</span>
          </div>
        </div>

        <!-- 取件码列表 -->
        <div class="express-code-list">
          <div
            v-for="express in location.items"
            :key="express.id"
            class="express-code-item"
            :class="{ picked: express.picked }"
          >
            <div class="express-code-left">
              <div class="express-code-number" @click="copySingleCode(express.code)">
                {{ express.code }}
              </div>
              <div class="express-code-info">
                {{ formatDate(express.received_at) }}
                <span v-if="!isSenderInAddress(express.sender, express.address)"> | {{ express.sender }}</span>
                <span 
                  v-if="!express.picked && getDaysAgo(express.received_at) >= 3" 
                  class="warning-badge"
                  :class="getDaysAgo(express.received_at) >= 5 ? 'danger' : 'warning'"
                >
                  已{{ getDaysAgo(express.received_at) }}天 ⚠️
                </span>
              </div>
            </div>
            <div class="express-code-actions">
              <button
                class="express-status-btn"
                :class="express.picked ? 'picked' : 'unpicked'"
                @click="toggleStatus(express)"
              >
                {{ express.picked ? '已取' : '未取' }}
              </button>
            </div>
          </div>
        </div>
      </div>

        <!-- 空状态 -->
        <van-empty v-if="expressList.length === 0" description="暂无待取快递">
          <template #image>
            <div class="empty-icon">🎉</div>
          </template>
          <template #description>
            <div class="empty-text">太棒了！</div>
            <div class="empty-subtext">暂无待取快递</div>
          </template>
        </van-empty>
      </div>
    </van-pull-refresh>

    <!-- 一键清除已取快递按钮 -->
    <button v-if="pickedCount > 0" class="clear-picked-btn" @click="clearPicked">
      <span>🗑️</span>
      <span>清除已取</span>
    </button>

    <!-- 快捷复制按钮 -->
    <button v-if="unpickedCount > 0" class="express-quick-copy" @click="copyAllCodes">
      📋
    </button>

    <!-- 底部导航 -->
    <van-tabbar v-model="activeTab" route class="cute-tabbar">
      <van-tabbar-item to="/express-detail" icon="logistics">快递</van-tabbar-item>
      <van-tabbar-item to="/tag-manage" icon="label-o">标签</van-tabbar-item>
      <van-tabbar-item v-if="showImport" to="/sms-import" icon="plus">导入</van-tabbar-item>
      <van-tabbar-item to="/settings" icon="setting-o">设置</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { getSmsList } from '@/api/sms'
import { extractPickupCode } from '@/utils/smsParser'
import { shouldShowImportFeature } from '@/utils/environment'

const router = useRouter()

// 数据
const expressList = ref([])
const loading = ref(false)
const activeTab = ref(0)
const showMenu = ref(false)
const refreshing = ref(false)

// 是否显示导入功能
const showImport = ref(shouldShowImportFeature())

// 加载快递列表
const loadExpressList = async () => {
  try {
    loading.value = true
    const response = await getSmsList({
      page: 1,
      page_size: 100
    })
    
    // 过滤出快递短信并提取取件码
    const allSms = response.items || []
    expressList.value = allSms
      .filter(sms => {
        // 检查是否是快递短信
        const content = sms.content.toLowerCase()
        return content.includes('取件') || 
               content.includes('快递') || 
               content.includes('菜鸟') ||
               content.includes('驿站')
      })
      .map(sms => {
        // 提取取件码
        const code = extractPickupCode(sms.content)
        // 提取地址
        const address = extractAddress(sms.content, sms.sender)
        
        return {
          id: sms.id,
          code: code || '未知',
          sender: sms.sender,
          content: sms.content,
          received_at: sms.received_at,
          address: address,
          picked: false // 默认未取
        }
      })
      .filter(item => item.code !== '未知') // 过滤掉没有取件码的
      
  } catch (error) {
    console.error('加载快递列表失败:', error)
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

// 提取地址
const extractAddress = (content, sender) => {
  // 1. 先清理内容，移除常见的无关前缀和后缀
  let cleanContent = content
    // 移除【发件人】格式
    .replace(/^.*?[【\[].*?[】\]]\s*/g, '')
    // 移除常见前缀
    .replace(/速递[】\]]\s*/g, '')
    .replace(/您的快递已到达/g, '')
    .replace(/您的快递已存入/g, '')
    .replace(/已到达/g, '')
    .replace(/快递已到达/g, '')
    .replace(/快递到达/g, '')
    .replace(/包裹到达/g, '')
    // 移除常见后缀
    .replace(/请.*?取件.*$/g, '')
    .replace(/凭.*?取件.*$/g, '')
    .replace(/，.*$/g, '')
    .replace(/。.*$/g, '')
  
  // 2. 地址提取模式（按优先级排序）
  const addressPatterns = [
    // 具体地址 + 驿站/快递柜/门卫等
    /([^\s，。！？]{2,20}?(?:驿站|快递柜|门卫|保安室|代收点|自提点|丰巢|菜鸟)(?:[^\s，。！？]{0,10})?)/,
    // 路名 + 驿站/快递柜
    /([^\s，。！？]{2,15}?[路街道巷][^\s，。！？]{0,15}?(?:驿站|快递柜|门卫))/,
    // 小区/大厦 + 具体位置（如门口、大门等）- 允许关键词在开头
    /([^\s，。！？]{0,15}?(?:小区|大厦|广场|商场|公寓|写字楼)(?:[^\s，。！？]{0,15}?(?:门口|大门|北门|南门|东门|西门|正门|侧门|1号门|2号门|3号门|快递柜|驿站|门卫))?)/,
    // 纯驿站名称
    /(菜鸟驿站[^\s，。！？]{0,15})/,
    /(丰巢[^\s，。！？]{0,15})/,
  ]
  
  for (const pattern of addressPatterns) {
    const match = cleanContent.match(pattern)
    if (match) {
      let address = match[1].trim()
      
      // 3. 进一步清理提取结果
      address = address
        // 移除开头的标点和空格
        .replace(/^[，。！？\s、]+/, '')
        // 移除结尾的标点和空格
        .replace(/[，。！？\s、]+$/, '')
        // 移除"请"、"取"等动词
        .replace(/请.*?取/, '')
        .replace(/到$/, '')
        .replace(/在$/, '')
        .replace(/处$/, '')
        // 移除开头的"快递"、"您的快递已"等
        .replace(/^快递/, '')
        .replace(/^您的快递已/, '')
      
      // 4. 验证地址长度和有效性
      if (address.length >= 2 && address.length <= 30) {
        return address
      }
    }
  }
  
  // 5. 如果没有匹配到，尝试从发件人提取
  if (sender) {
    // 清理发件人
    const cleanSender = sender
      .replace(/^.*?[【\[]/, '')
      .replace(/[】\]].*$/, '')
      .trim()
    
    if (cleanSender.includes('驿站') || cleanSender.includes('快递') || 
        cleanSender.includes('丰巢') || cleanSender.includes('菜鸟')) {
      return cleanSender
    }
  }
  
  return '未知地址'
}

// 按地址分组
const groupedExpressList = computed(() => {
  const groups = {}
  
  // 分组
  expressList.value.forEach(express => {
    const address = express.address
    if (!groups[address]) {
      groups[address] = []
    }
    groups[address].push(express)
  })
  
  // 转换为数组并排序
  return Object.keys(groups).map(address => {
    const items = groups[address]
    
    // 排序：未取优先 → 日期倒序 → 取件码顺序
    items.sort((a, b) => {
      // 1. 状态优先（未取在前）
      if (a.picked !== b.picked) {
        return a.picked ? 1 : -1
      }
      
      // 2. 日期倒序（最新在前）
      const dateA = new Date(a.received_at)
      const dateB = new Date(b.received_at)
      if (dateA.getTime() !== dateB.getTime()) {
        return dateB.getTime() - dateA.getTime()
      }
      
      // 3. 取件码顺序
      return a.code.localeCompare(b.code)
    })
    
    return {
      address,
      items,
      unpickedCount: items.filter(item => !item.picked).length
    }
  }).sort((a, b) => {
    // 按未取数量排序
    return b.unpickedCount - a.unpickedCount
  })
})

// 统计信息
const unpickedCount = computed(() => {
  return expressList.value.filter(item => !item.picked).length
})

const pickedCount = computed(() => {
  return expressList.value.filter(item => item.picked).length
})

const locationCount = computed(() => {
  return groupedExpressList.value.length
})

const totalCount = computed(() => {
  return expressList.value.length
})

// 切换状态
const toggleStatus = (express) => {
  express.picked = !express.picked
  showToast(express.picked ? '已标记为已取' : '已标记为未取')
}

// 清除已取快递
const clearPicked = async () => {
  try {
    await showConfirmDialog({
      title: '确认清除',
      message: `确定要清除 ${pickedCount.value} 条已取快递吗？`
    })
    
    expressList.value = expressList.value.filter(item => !item.picked)
    showToast('清除成功')
  } catch {
    // 用户取消
  }
}

// 复制所有取件码
const copyAllCodes = () => {
  const text = groupedExpressList.value
    .map(location => {
      const codes = location.items
        .filter(item => !item.picked)
        .map(item => `${item.code} | ${formatDate(item.received_at)}`)
        .join('\n')
      return `📍 ${location.address}\n${codes}`
    })
    .join('\n\n')
  
  // 复制到剪贴板
  if (navigator.clipboard) {
    navigator.clipboard.writeText(text)
    showToast('已复制所有取件码')
  } else {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = text
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    showToast('已复制所有取件码')
  }
}

// 格式化日期（人性化）
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  const now = new Date()
  const diffDays = Math.floor((now - date) / 86400000)
  
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const timeStr = `${hours}:${minutes}`
  
  if (diffDays === 0) {
    return `今天 ${timeStr}`
  } else if (diffDays === 1) {
    return `昨天 ${timeStr}`
  } else if (diffDays === 2) {
    return `前天 ${timeStr}`
  } else if (diffDays <= 7) {
    return `${diffDays}天前`
  } else {
    const month = date.getMonth() + 1
    const day = date.getDate()
    return `${month}-${String(day).padStart(2, '0')}`
  }
}

// 计算距离现在的天数
const getDaysAgo = (dateStr) => {
  const date = new Date(dateStr)
  const now = new Date()
  return Math.floor((now - date) / 86400000)
}

// 复制单个取件码
const copySingleCode = (code) => {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(code)
    showToast(`已复制: ${code}`)
  } else {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = code
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    showToast(`已复制: ${code}`)
  }
}

// 判断发件人是否包含在地址中
const isSenderInAddress = (sender, address) => {
  if (!sender || !address) return false
  // 提取关键词进行匹配
  const senderKeywords = ['菜鸟', '驿站', '快递柜', '丰巢', '中通', '圆通', '申通', '韵达', '顺丰', '京东', '邮政', 'EMS']
  
  for (const keyword of senderKeywords) {
    if (sender.includes(keyword) && address.includes(keyword)) {
      return true
    }
  }
  
  return false
}

// 下拉刷新
const onRefresh = async () => {
  await loadExpressList()
  refreshing.value = false
  showToast('刷新成功')
}

// 初始化
onMounted(() => {
  loadExpressList()
})
</script>

<style scoped>
.express-detail-page {
  min-height: 100vh;
  padding-top: 46px;
  padding-bottom: 60px;
}

/* 导航栏 - 玻璃拟态 */
.cute-navbar {
  background: var(--glass-fill);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border-bottom: 1px solid var(--glass-border);
  box-shadow: var(--glass-shadow);
}

.cute-navbar :deep(.van-nav-bar__title) {
  color: var(--glass-text-primary);
  font-weight: 600;
}

.cute-navbar :deep(.van-icon) {
  color: var(--glass-text-primary);
}

.nav-icon {
  color: var(--glass-text-primary);
  font-size: 20px;
}

/* 统计信息 - 玻璃面板 */
.express-header {
  background: var(--glass-fill);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--glass-radius);
  padding: 20px 16px;
  margin: 12px 16px;
  box-shadow: var(--glass-shadow);
  position: relative;
  z-index: 1;
}

.express-stats-v2 {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.express-stat-primary {
  text-align: left;
}

.express-stat-number-large {
  font-size: 36px;
  font-weight: 800;
  color: var(--glass-text-primary);
  line-height: 1;
}

.express-stat-label {
  font-size: 13px;
  color: var(--glass-text-secondary);
  margin-top: 6px;
}

.express-stat-secondary {
  text-align: right;
}

.express-stat-info {
  font-size: 14px;
  color: var(--glass-text-secondary);
  font-weight: 500;
}

/* 快递列表 */
.express-list {
  padding: 0 16px;
}

.express-location-section {
  margin-bottom: 16px;
}

.express-location-header {
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: var(--glass-radius) var(--glass-radius) 0 0;
  margin-bottom: 1px;
  border: 1px solid var(--glass-border);
  position: relative;
  z-index: 1;
}

.express-location-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--glass-text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}

.express-location-count {
  font-size: 14px;
  color: var(--glass-text-secondary);
}

.sort-indicator {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--glass-text-secondary);
  background: rgba(102, 126, 234, 0.2);
  padding: 4px 8px;
  border-radius: var(--glass-radius-small);
}

.express-code-list {
  background: var(--glass-fill);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-top: none;
  border-radius: 0 0 var(--glass-radius) var(--glass-radius);
  position: relative;
  z-index: 1;
}

.express-code-item {
  padding: 16px;
  border-bottom: 1px solid #f8f8f8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s;
}

.express-code-item:last-child {
  border-bottom: none;
}

.express-code-item.picked {
  opacity: 0.5;
}

.express-code-left {
  flex: 1;
}

.express-code-number {
  font-size: 28px;
  font-weight: 700;
  color: var(--glass-text-primary);
  font-family: 'Courier New', monospace;
  letter-spacing: 4px;
  cursor: pointer;
  user-select: none;
  transition: all 0.2s;
}

.express-code-number:active {
  transform: scale(0.95);
}

.express-code-item.picked .express-code-number {
  text-decoration: line-through;
  color: var(--glass-text-secondary);
  opacity: 0.6;
}

.express-code-info {
  font-size: 13px;
  color: var(--glass-text-secondary);
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.warning-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
}

.warning-badge.warning {
  background: #FFF3E0;
  color: #FF9800;
}

.warning-badge.danger {
  background: #FFEBEE;
  color: #F44336;
}

.express-code-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.express-status-btn {
  padding: 8px 20px;
  border-radius: 20px;
  border: none;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.express-status-btn.unpicked {
  background: var(--glass-accent-purple);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.express-status-btn.unpicked:active {
  transform: scale(0.95);
}

.express-status-btn.picked {
  background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3);
}

.express-status-btn.picked:active {
  transform: scale(0.95);
}

/* 一键清除按钮 - 玻璃效果 */
.clear-picked-btn {
  position: fixed;
  bottom: 160px;
  right: 20px;
  padding: 12px 20px;
  border-radius: var(--glass-radius);
  background: var(--glass-fill);
  backdrop-filter: blur(var(--glass-blur-light));
  -webkit-backdrop-filter: blur(var(--glass-blur-light));
  color: var(--glass-text-primary);
  border: 1px solid var(--glass-border);
  font-size: 14px;
  box-shadow: var(--glass-shadow);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  z-index: 100;
  transition: all 0.2s;
}

.clear-picked-btn:active {
  transform: scale(0.95);
}

/* 快捷复制按钮 - 玻璃FAB */
.express-quick-copy {
  position: fixed;
  bottom: 90px;
  right: 20px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--glass-fill);
  backdrop-filter: blur(var(--glass-blur-light));
  -webkit-backdrop-filter: blur(var(--glass-blur-light));
  color: var(--glass-text-primary);
  border: 1px solid var(--glass-border);
  font-size: 24px;
  box-shadow: var(--glass-shadow);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  transition: all 0.2s;
}

.express-quick-copy:active {
  transform: scale(0.95);
}

/* 空状态 */
.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.empty-text {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.empty-subtext {
  font-size: 14px;
  color: #999;
}

/* 底部导航 - 玻璃效果 */
.cute-tabbar {
  background: var(--glass-fill);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border-top: 1px solid var(--glass-border);
  box-shadow: 0 -8px 32px rgba(0, 0, 0, 0.05);
}

.cute-tabbar :deep(.van-tabbar-item--active) {
  color: var(--glass-text-primary);
}
</style>
