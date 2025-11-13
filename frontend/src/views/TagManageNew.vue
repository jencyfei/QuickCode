<template>
  <div class="tag-manage-page glass-background">
    <!-- 顶部导航栏 -->
    <van-nav-bar
      title="标签管理"
      fixed
      class="cute-navbar"
    >
      <template #left>
        <span class="nav-icon">🏷️</span>
      </template>
      <template #right>
        <van-icon name="replay" @click="refreshTags" style="margin-right: 12px; font-size: 18px;" />
        <van-button
          icon="edit"
          type="primary"
          size="small"
          round
          @click="showManagePanel = true"
          class="add-btn"
        >
          管理
        </van-button>
      </template>
    </van-nav-bar>

    <!-- 标签列表 -->
    <div class="tag-list">
      <div
        v-for="tag in tags"
        :key="tag.id"
        class="tag-card"
        @click="viewTagSms(tag)"
      >
        <div class="tag-icon" :style="{ background: tag.color }">
          {{ tag.icon || '🏷️' }}
        </div>
        <div class="tag-info">
          <div class="tag-name">{{ tag.name }}</div>
        </div>
        <div class="tag-right">
          <span class="tag-count">{{ tag.sms_count }}</span>
        </div>
      </div>

      <van-empty v-if="tags.length === 0" description="暂无标签，快去创建吧" />
    </div>

    <!-- 标签管理面板 -->
    <van-popup v-model:show="showManagePanel" position="bottom" round :style="{ height: '70%' }">
      <div class="manage-panel">
        <div class="panel-header">
          <h3>标签管理</h3>
          <van-button 
            type="primary" 
            size="small" 
            icon="plus"
            @click="openAddTag"
          >
            新建标签
          </van-button>
        </div>
        
        <div class="manage-list">
          <div
            v-for="tag in tags"
            :key="tag.id"
            class="manage-item"
          >
            <div class="manage-left">
              <div class="tag-icon-small" :style="{ background: tag.color }">
                {{ tag.icon || '🏷️' }}
              </div>
              <div class="manage-info">
                <div class="manage-name">{{ tag.name }}</div>
                <div class="manage-count">{{ tag.sms_count }} 条短信</div>
              </div>
            </div>
            <div class="manage-actions">
              <van-button 
                size="small" 
                type="primary" 
                plain
                @click="editTag(tag)"
              >
                编辑
              </van-button>
              <van-button 
                size="small" 
                type="danger" 
                plain
                @click="confirmDelete(tag)"
              >
                删除
              </van-button>
            </div>
          </div>
          
          <van-empty v-if="tags.length === 0" description="暂无标签" />
        </div>
      </div>
    </van-popup>

    <!-- 添加/编辑标签弹窗 -->
    <van-popup v-model:show="showAddTagPopup" position="bottom" round>
      <div class="tag-form-popup">
        <div class="popup-header">
          <h3>{{ editingTag ? '编辑标签' : '新建标签' }}</h3>
        </div>
        
        <van-form @submit="onSubmit">
          <van-field
            v-model="tagForm.name"
            label="标签名称"
            placeholder="请输入标签名称"
            :rules="[{ required: true, message: '请输入标签名称' }]"
          />
          
          <van-field
            v-model="tagForm.icon"
            label="标签图标"
            placeholder="选择一个Emoji"
          >
            <template #input>
              <div class="emoji-selector">
                <span
                  v-for="emoji in emojiList"
                  :key="emoji"
                  class="emoji-item"
                  :class="{ active: tagForm.icon === emoji }"
                  @click="tagForm.icon = emoji"
                >
                  {{ emoji }}
                </span>
              </div>
            </template>
          </van-field>
          
          <van-field label="标签颜色">
            <template #input>
              <div class="color-selector">
                <div
                  v-for="color in colorList"
                  :key="color"
                  class="color-item"
                  :style="{ background: color }"
                  :class="{ active: tagForm.color === color }"
                  @click="tagForm.color = color"
                />
              </div>
            </template>
          </van-field>
          
          <div class="form-footer">
            <van-button type="primary" native-type="submit" block>
              {{ editingTag ? '保存' : '创建' }}
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>

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
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { getTags, createTag, updateTag, deleteTag } from '@/api/tag'
import { shouldShowImportFeature } from '@/utils/environment'

const router = useRouter()

// 数据
const tags = ref([])
const activeTab = ref(1)

// 是否显示导入功能
const showImport = ref(shouldShowImportFeature())

// 弹窗控制
const showManagePanel = ref(false)
const showAddTagPopup = ref(false)
const editingTag = ref(null)

// 表单
const tagForm = ref({
  name: '',
  icon: '🏷️',
  color: '#FF6B9D'
})

// Emoji列表
const emojiList = ['🏷️', '📦', '🔐', '🏦', '💼', '🎉', '📧', '🛒', '🎁', '⭐', '💰', '📱', '🚗', '✈️', '🏠', '❤️']

// 颜色列表
const colorList = [
  '#FF6B9D', '#FF8FAB', '#FFB6C1', '#FFA07A',
  '#87CEEB', '#98D8C8', '#F7DC6F', '#BB8FCE',
  '#85C1E2', '#F8B88B', '#FAD7A0', '#D7BDE2'
]

// 加载标签列表
const loading = ref(false)
const loadTags = async () => {
  try {
    loading.value = true
    const response = await getTags()
    tags.value = response.tags
  } catch (error) {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

// 刷新标签列表
const refreshTags = async () => {
  showToast('刷新中...')
  await loadTags()
  showToast('刷新成功')
}

// 查看标签的短信
const viewTagSms = (tag) => {
  // 跳转到短信列表页面，并传递标签ID
  router.push({
    path: '/sms-list',
    query: {
      tag_id: tag.id,
      tag_name: tag.name
    }
  }).catch(err => {
    console.error('路由跳转错误:', err)
    showToast('跳转失败')
  })
}

// 打开新建标签弹窗
const openAddTag = () => {
  resetForm()
  showManagePanel.value = false
  showAddTagPopup.value = true
}

// 编辑标签
const editTag = (tag) => {
  editingTag.value = tag
  tagForm.value = {
    name: tag.name,
    icon: tag.icon || '🏷️',
    color: tag.color
  }
  showManagePanel.value = false
  showAddTagPopup.value = true
}

// 确认删除标签
const confirmDelete = async (tag) => {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: `确定要删除标签"${tag.name}"吗？删除后关联的短信不会被删除。`
    })

    await deleteTag(tag.id)
    showToast('删除成功')
    loadTags()
  } catch (error) {
    if (error !== 'cancel') {
      showToast('删除失败')
    }
  }
}

// 提交表单
const onSubmit = async () => {
  try {
    if (editingTag.value) {
      // 更新标签
      await updateTag(editingTag.value.id, tagForm.value)
      showToast('更新成功')
    } else {
      // 创建标签
      await createTag(tagForm.value)
      showToast('创建成功')
    }
    
    showAddTagPopup.value = false
    resetForm()
    loadTags()
  } catch (error) {
    showToast(error.response?.data?.detail || '操作失败')
  }
}

// 重置表单
const resetForm = () => {
  editingTag.value = null
  tagForm.value = {
    name: '',
    icon: '🏷️',
    color: '#FF6B9D'
  }
}

// 初始化
onMounted(() => {
  loadTags()
})
</script>

<style scoped>
.tag-manage-page {
  min-height: 100vh;
  padding-top: 46px;
  padding-bottom: 60px;
}

/* 玻璃导航栏 */
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
  font-size: 24px;
}

/* 管理按钮 - 玻璃效果 */
.add-btn {
  background: var(--glass-fill);
  backdrop-filter: blur(var(--glass-blur-light));
  -webkit-backdrop-filter: blur(var(--glass-blur-light));
  border: 1px solid var(--glass-border);
  color: var(--glass-text-primary);
  font-size: 13px;
  padding: 0 12px;
  height: 28px;
}

.add-btn :deep(.van-icon) {
  color: var(--glass-text-primary);
}

/* 标签列表 */
.tag-list {
  padding: 16px;
}

.tag-card {
  background: var(--glass-fill);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
  border-radius: var(--glass-radius);
  padding: 16px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--glass-shadow);
  transition: all 0.3s;
  position: relative;
  z-index: 1;
}

.tag-card:active {
  transform: scale(0.98);
}

.tag-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.tag-info {
  flex: 1;
}

.tag-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--glass-text-primary);
}

.tag-right {
  display: flex;
  align-items: center;
}

.tag-count {
  font-size: 18px;
  font-weight: 600;
  color: var(--glass-text-primary);
  min-width: 30px;
  text-align: right;
  background: rgba(102, 126, 234, 0.2);
  padding: 4px 12px;
  border-radius: var(--glass-radius-small);
}

/* 管理面板 */
.manage-panel {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--glass-text-primary);
  margin: 0;
}

.manage-list {
  flex: 1;
  overflow-y: auto;
}

.manage-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  margin-bottom: 8px;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: var(--glass-radius-medium);
}

.manage-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.tag-icon-small {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.manage-info {
  flex: 1;
}

.manage-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--glass-text-primary);
  margin-bottom: 2px;
}

.manage-count {
  font-size: 12px;
  color: var(--glass-text-secondary);
}

.manage-actions {
  display: flex;
  gap: 8px;
}

/* 弹窗 */
.tag-form-popup {
  padding: 20px;
}

.popup-header {
  margin-bottom: 20px;
}

.popup-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--glass-text-primary);
}

/* Emoji选择器 */
.emoji-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px 0;
}

.emoji-item {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.emoji-item:active {
  transform: scale(0.9);
}

.emoji-item.active {
  background: rgba(102, 126, 234, 0.1);
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.5);
}

/* 颜色选择器 */
.color-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 8px 0;
}

.color-item {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.color-item:active {
  transform: scale(0.9);
}

.color-item.active::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-size: 20px;
  font-weight: bold;
}

/* 表单底部 */
.form-footer {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
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
