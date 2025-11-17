<template>
  <div class="sms-list-page">
    <!-- 顶部导航栏 -->
    <div class="navbar">
      <span class="navbar-icon" @click="goBack">{{ currentTagName ? '←' : '☰' }}</span>
      <span class="navbar-title">{{ navTitle }}</span>
      <div class="navbar-actions">
        <span class="navbar-icon" @click="showSearchPopup = true">🔍</span>
        <span class="navbar-icon" @click="$router.push('/settings')">⚙️</span>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <button 
        class="filter-btn" 
        :class="{ active: activeFilter === 'all' }"
        @click="activeFilter = 'all'"
      >
        全部 ▼
      </button>
      <button 
        class="filter-btn" 
        :class="{ active: activeFilter === 'time' }"
        @click="showTimeFilter = true"
      >
        时间 ▼
      </button>
      <button 
        class="filter-btn" 
        :class="{ active: activeFilter === 'tag' }"
        @click="showTagFilter = true"
      >
        标签 ▼
      </button>
    </div>

    <!-- 短信列表 -->
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
        class="sms-list"
      >
        <div
          v-for="sms in smsList"
          :key="sms.id"
          class="sms-card"
          @click="handleCardClick(sms.id)"
        >
          <!-- 复选框 -->
          <div 
            class="sms-checkbox" 
            :class="{ checked: selectedIds.includes(sms.id) }"
            @click.stop="toggleSelect(sms.id)"
          ></div>
          
          <div class="sms-content">
            <!-- 发件人和时间 -->
            <div class="sms-header">
              <span class="sms-sender">{{ sms.sender }}</span>
              <span class="sms-time">{{ formatTime(sms.received_at) }}</span>
            </div>
            
            <!-- 短信内容 -->
            <div class="sms-text">{{ sms.content }}</div>
            
            <!-- 标签 -->
            <div v-if="sms.tags && sms.tags.length > 0" class="sms-tags">
              <span
                v-for="tag in sms.tags"
                :key="tag.id"
                class="tag"
                :style="{ backgroundColor: tag.color }"
              >
                {{ tag.name }}
              </span>
            </div>
          </div>
        </div>
        
        <van-empty v-if="smsList.length === 0 && !loading" description="暂无短信" />
      </van-list>
    </van-pull-refresh>

    <!-- 底部操作栏 -->
    <div v-if="selectedIds.length > 0" class="action-bar">
      <span class="action-bar-text">已选 {{ selectedIds.length }}条</span>
      <div class="action-buttons">
        <span class="action-btn" @click="showBatchTagPopup = true">🏷️</span>
        <span class="action-btn" @click="copySelected">📋</span>
        <span class="action-btn" @click="batchDelete">🗑️</span>
      </div>
    </div>

    <!-- 底部导航 -->
    <van-tabbar v-if="selectedIds.length === 0" v-model="activeTab" route class="cute-tabbar">
      <van-tabbar-item to="/express-detail" icon="logistics">快递</van-tabbar-item>
      <van-tabbar-item to="/tag-manage" icon="label-o">标签</van-tabbar-item>
      <van-tabbar-item v-if="showImport" to="/sms-import" icon="plus">导入</van-tabbar-item>
      <van-tabbar-item to="/settings" icon="setting-o">设置</van-tabbar-item>
    </van-tabbar>

    <!-- 搜索弹窗 -->
    <van-popup v-model:show="showSearchPopup" position="top" :style="{ height: '50%' }">
      <div class="search-popup">
        <van-search
          v-model="searchKeyword"
          placeholder="搜索发件人或内容"
          @search="onSearch"
          @cancel="showSearchPopup = false"
        />
      </div>
    </van-popup>

    <!-- 标签筛选弹窗 -->
    <van-popup v-model:show="showTagFilter" position="bottom" round>
      <div class="tag-filter-popup">
        <div class="popup-header">
          <h3>选择标签</h3>
          <van-button size="small" @click="clearTagFilter">清除</van-button>
        </div>
        <div class="tag-list">
          <van-checkbox-group v-model="selectedTagIds">
            <van-checkbox
              v-for="tag in allTags"
              :key="tag.id"
              :name="tag.id"
              class="tag-checkbox"
            >
              <van-tag :color="tag.color">{{ tag.icon }} {{ tag.name }}</van-tag>
            </van-checkbox>
          </van-checkbox-group>
        </div>
        <div class="popup-footer">
          <van-button block type="primary" @click="applyTagFilter">确定</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 批量添加标签弹窗 -->
    <van-popup v-model:show="showBatchTagPopup" position="bottom" round>
      <div class="batch-tag-popup">
        <div class="popup-header">
          <h3>添加标签</h3>
        </div>
        <div class="tag-list">
          <van-checkbox-group v-model="batchTagIds">
            <van-checkbox
              v-for="tag in allTags"
              :key="tag.id"
              :name="tag.id"
              class="tag-checkbox"
            >
              <van-tag :color="tag.color">{{ tag.icon }} {{ tag.name }}</van-tag>
            </van-checkbox>
          </van-checkbox-group>
        </div>
        <div class="popup-footer">
          <van-button block type="primary" @click="batchAddTags">确定</van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { getSmsList, batchDeleteSms, batchAddTagsToSms } from '@/api/sms'
import { getTags } from '@/api/tag'
import { shouldShowImportFeature } from '@/utils/environment'

const router = useRouter()
const route = useRoute()

// 数据
const smsList = ref([])
const allTags = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)

// 状态
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const isSelectMode = ref(false)
const selectedIds = ref([])

// 筛选
const activeTimeFilter = ref('all')
const searchKeyword = ref('')
const selectedTagIds = ref([])
const batchTagIds = ref([])

// 弹窗
const showSearchPopup = ref(false)
const showTagFilter = ref(false)
const showBatchTagPopup = ref(false)
const showTimeFilter = ref(false)
const showMenu = ref(false)

// 筛选状态
const activeFilter = ref('all')
const activeTab = ref(0)

// 当前标签筛选
const currentTagName = ref('')

// 是否显示导入功能
const showImport = ref(shouldShowImportFeature())

// 计算属性
const selectedTags = computed(() => {
  return allTags.value.filter(tag => selectedTagIds.value.includes(tag.id))
})

// 导航栏标题
const navTitle = computed(() => {
  if (currentTagName.value) {
    return `${currentTagName.value} 的短信`
  }
  return '📱 我的短信'
})

// 处理卡片点击
const handleCardClick = (id) => {
  if (selectedIds.value.length > 0) {
    // 如果已有选中项，则切换选中状态
    toggleSelect(id)
  } else {
    // 否则跳转到详情
    goToDetail(id)
  }
}

// 复制选中的短信
const copySelected = () => {
  const selectedSms = smsList.value.filter(sms => selectedIds.value.includes(sms.id))
  const text = selectedSms.map(sms => `${sms.sender}: ${sms.content}`).join('\n\n')
  
  if (navigator.clipboard) {
    navigator.clipboard.writeText(text)
    showToast('已复制')
  } else {
    const textarea = document.createElement('textarea')
    textarea.value = text
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    showToast('已复制')
  }
}

// 加载短信列表
const loadSmsList = async (isRefresh = false) => {
  // 防止重复加载
  if (loading.value) {
    return
  }
  
  if (isRefresh) {
    page.value = 1
    finished.value = false
  }

  try {
    loading.value = true
    
    const params = {
      page: page.value,
      page_size: pageSize.value,
      keyword: searchKeyword.value || undefined,
      tag_ids: selectedTagIds.value.length > 0 ? selectedTagIds.value.join(',') : undefined,
      ...getTimeRange()
    }
    
    const response = await getSmsList(params)
    
    if (isRefresh) {
      smsList.value = response.items
    } else {
      // 添加去重逻辑，防止重复数据
      const existingIds = new Set(smsList.value.map(s => s.id))
      const newItems = response.items.filter(item => !existingIds.has(item.id))
      smsList.value.push(...newItems)
    }
    
    total.value = response.total
    
    // 修复finished判断
    if (smsList.value.length >= response.total || response.items.length === 0) {
      finished.value = true
    }
    
    page.value++
  } catch (error) {
    console.error('加载短信失败:', error)
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

// 获取时间范围
const getTimeRange = () => {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  
  switch (activeTimeFilter.value) {
    case 'today':
      return {
        start_date: today.toISOString().split('T')[0],
        end_date: today.toISOString().split('T')[0]
      }
    case 'week':
      const weekStart = new Date(today)
      weekStart.setDate(today.getDate() - today.getDay())
      return {
        start_date: weekStart.toISOString().split('T')[0],
        end_date: today.toISOString().split('T')[0]
      }
    case 'month':
      const monthStart = new Date(today.getFullYear(), today.getMonth(), 1)
      return {
        start_date: monthStart.toISOString().split('T')[0],
        end_date: today.toISOString().split('T')[0]
      }
    default:
      return {}
  }
}

// 加载标签列表
const loadTags = async () => {
  try {
    const response = await getTags()
    allTags.value = response.tags
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

// 下拉刷新
const onRefresh = async () => {
  refreshing.value = true
  await loadSmsList(true)
  refreshing.value = false
  showToast('刷新成功')
}

// 上拉加载
const onLoad = async () => {
  await loadSmsList()
}

// 时间筛选变化
const onTimeFilterChange = () => {
  onRefresh()
}

// 搜索
const onSearch = () => {
  showSearchPopup.value = false
  onRefresh()
}

// 应用标签筛选
const applyTagFilter = () => {
  showTagFilter.value = false
  onRefresh()
}

// 清除标签筛选
const clearTagFilter = () => {
  selectedTagIds.value = []
  showTagFilter.value = false
  onRefresh()
}

// 移除标签
const removeTag = (tagId) => {
  selectedTagIds.value = selectedTagIds.value.filter(id => id !== tagId)
  onRefresh()
}

// 切换选择模式
const toggleSelect = (id) => {
  isSelectMode.value = true
  if (selectedIds.value.includes(id)) {
    selectedIds.value = selectedIds.value.filter(i => i !== id)
  } else {
    selectedIds.value.push(id)
  }
}

// 取消选择
const cancelSelect = () => {
  isSelectMode.value = false
  selectedIds.value = []
}

// 批量删除
const batchDelete = async () => {
  if (selectedIds.value.length === 0) {
    showToast('请选择要删除的短信')
    return
  }

  try {
    await showConfirmDialog({
      title: '确认删除',
      message: `确定要删除选中的 ${selectedIds.value.length} 条短信吗？`
    })

    await batchDeleteSms({ ids: selectedIds.value })
    showToast('删除成功')
    cancelSelect()
    onRefresh()
  } catch (error) {
    if (error !== 'cancel') {
      showToast('删除失败')
    }
  }
}

// 批量添加标签
const batchAddTags = async () => {
  if (selectedIds.value.length === 0) {
    showToast('请选择短信')
    return
  }
  if (batchTagIds.value.length === 0) {
    showToast('请选择标签')
    return
  }

  try {
    await batchAddTagsToSms({
      sms_ids: selectedIds.value,
      tag_ids: batchTagIds.value
    })
    showToast('添加成功')
    showBatchTagPopup.value = false
    batchTagIds.value = []
    cancelSelect()
    onRefresh()
  } catch (error) {
    showToast('添加失败')
  }
}

// 跳转到详情
const goToDetail = (id) => {
  if (!isSelectMode.value) {
    router.push(`/sms-detail/${id}`)
  }
}

// 格式化时间
const formatTime = (timeStr) => {
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 返回上一页
const goBack = () => {
  if (currentTagName.value) {
    // 如果是从标签页进入的，返回标签页
    router.push('/tag-manage')
  } else {
    // 否则打开菜单
    showMenu.value = true
  }
}

// 初始化URL参数
const initFromUrlParams = () => {
  const tagId = route.query.tag_id
  const tagName = route.query.tag_name
  
  if (tagId && tagName) {
    // 只有当同时提供了tag_id和tag_name时才应用过滤
    // 这确保只有从标签管理页面明确点击标签时才会过滤
    currentTagName.value = tagName
    selectedTagIds.value = [parseInt(tagId)]
    activeFilter.value = 'tag'
  } else {
    // 清除任何URL参数中的标签过滤
    selectedTagIds.value = []
    currentTagName.value = ''
    activeFilter.value = 'all'
  }
}

// 初始化
onMounted(() => {
  loadTags()
  initFromUrlParams()
  loadSmsList(true)
})
</script>

<style scoped>
.sms-list-page {
  min-height: 100vh;
  background: #FFF5F5;
  padding-bottom: 60px;
}

/* 导航栏 */
.navbar {
  background: linear-gradient(135deg, #FF6B9D 0%, #FF8FAB 100%);
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
  position: sticky;
  top: 0;
  z-index: 100;
}

.navbar-icon {
  font-size: 20px;
  cursor: pointer;
  padding: 4px;
}

.navbar-title {
  font-size: 16px;
  font-weight: 600;
}

.navbar-actions {
  display: flex;
  gap: 12px;
}

/* 筛选栏 */
.filter-bar {
  background: white;
  padding: 12px 16px;
  display: flex;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.filter-btn {
  flex: 1;
  padding: 8px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: white;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.filter-btn.active {
  background: #FFE5E5;
  color: #FF6B9D;
  border-color: #FF6B9D;
}

/* 短信列表 */
.sms-list {
  padding: 0;
  background: #FFF5F5;
}

.sms-card {
  background: white;
  padding: 12px 16px;
  display: flex;
  gap: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}

.sms-card:active {
  background: #f8f8f8;
}

/* 复选框 */
.sms-checkbox {
  width: 20px;
  height: 20px;
  border: 2px solid #ddd;
  border-radius: 4px;
  flex-shrink: 0;
  margin-top: 2px;
  cursor: pointer;
  transition: all 0.3s;
}

.sms-checkbox.checked {
  background: #FF6B9D;
  border-color: #FF6B9D;
  position: relative;
}

.sms-checkbox.checked::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-size: 14px;
  font-weight: bold;
}

/* 短信内容 */
.sms-content {
  flex: 1;
  min-width: 0;
}

.sms-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.sms-sender {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.sms-time {
  font-size: 12px;
  color: #999;
}

.sms-text {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 标签 */
.sms-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  color: white;
}

/* 底部操作栏 */
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.action-bar-text {
  font-size: 14px;
  color: #666;
}

.action-buttons {
  display: flex;
  gap: 20px;
}

.action-btn {
  font-size: 24px;
  cursor: pointer;
  transition: transform 0.2s;
}

.action-btn:active {
  transform: scale(0.9);
}

/* 底部导航 */
.cute-tabbar {
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.05);
}

.cute-tabbar :deep(.van-tabbar-item--active) {
  color: #FF6B9D;
}

/* 弹窗 */
.search-popup {
  padding: 16px;
}

.tag-filter-popup,
.batch-tag-popup {
  padding: 20px;
  max-height: 70vh;
  overflow-y: auto;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.popup-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.tag-list {
  margin-bottom: 16px;
}

.tag-checkbox {
  margin-bottom: 12px;
}

.popup-footer {
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
