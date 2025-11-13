<template>
  <div class="rule-config-page">
    <!-- 导航栏 -->
    <van-nav-bar
      title="⚙️ 规则配置"
      left-arrow
      @click-left="$router.back()"
      class="cute-navbar"
    >
      <template #right>
        <van-icon name="plus" @click="showAddDialog" />
      </template>
    </van-nav-bar>

    <!-- 规则类型选择 -->
    <van-tabs v-model:active="activeTab" @change="onTabChange" sticky>
      <van-tab title="取件码" name="pickup_code">
        <rule-list
          :rules="pickupCodeRules"
          :loading="loading"
          @edit="editRule"
          @delete="confirmDelete"
          @toggle="toggleRule"
        />
      </van-tab>
      
      <van-tab title="地址" name="address">
        <rule-list
          :rules="addressRules"
          :loading="loading"
          @edit="editRule"
          @delete="confirmDelete"
          @toggle="toggleRule"
        />
      </van-tab>
      
      <van-tab title="发件人" name="sender">
        <rule-list
          :rules="senderRules"
          :loading="loading"
          @edit="editRule"
          @delete="confirmDelete"
          @toggle="toggleRule"
        />
      </van-tab>
    </van-tabs>

    <!-- 添加规则按钮 -->
    <van-button
      type="primary"
      size="large"
      class="add-rule-btn"
      @click="showAddDialog"
    >
      <van-icon name="plus" /> 添加规则
    </van-button>

    <!-- 规则模板按钮 -->
    <van-button
      plain
      type="primary"
      size="large"
      class="template-btn"
      @click="showTemplateDialog"
    >
      <van-icon name="apps-o" /> 规则模板
    </van-button>

    <!-- 规则编辑弹窗 -->
    <van-popup
      v-model:show="showDialog"
      position="bottom"
      :style="{ height: '80%' }"
      round
    >
      <div class="dialog-content">
        <div class="dialog-header">
          <h3>{{ isEdit ? '编辑规则' : '添加规则' }}</h3>
          <van-icon name="cross" @click="closeDialog" />
        </div>

        <van-form @submit="saveRule">
          <van-cell-group inset>
            <van-field
              v-model="currentRule.name"
              label="规则名称"
              placeholder="请输入规则名称"
              required
              :rules="[{ required: true, message: '请输入规则名称' }]"
            />

            <van-field
              v-model="currentRule.pattern"
              label="正则表达式"
              placeholder="请输入正则表达式"
              type="textarea"
              rows="3"
              required
              :rules="[{ required: true, message: '请输入正则表达式' }]"
            />

            <van-field
              v-model.number="currentRule.extract_group"
              label="提取组"
              type="number"
              placeholder="提取第几个捕获组"
            />

            <van-field
              v-model.number="currentRule.priority"
              label="优先级"
              type="number"
              placeholder="数字越大优先级越高"
            />

            <van-field
              v-model="currentRule.description"
              label="描述"
              placeholder="规则描述（可选）"
              type="textarea"
              rows="2"
            />
          </van-cell-group>

          <!-- 测试区域 -->
          <div class="test-section">
            <h4>测试规则</h4>
            <van-field
              v-model="testText"
              placeholder="输入测试文本"
              type="textarea"
              rows="2"
            />
            <van-button size="small" type="primary" @click="testCurrentRule">
              测试
            </van-button>
            <div v-if="testResult" class="test-result">
              <div v-if="testResult.matched" class="success">
                ✅ 匹配成功: {{ testResult.extracted }}
              </div>
              <div v-else-if="testResult.error" class="error">
                ❌ {{ testResult.error }}
              </div>
              <div v-else class="warning">
                ⚠️ 未匹配到内容
              </div>
            </div>
          </div>

          <div class="dialog-actions">
            <van-button block type="primary" native-type="submit" :loading="saving">
              保存
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>

    <!-- 规则模板弹窗 -->
    <van-popup
      v-model:show="showTemplates"
      position="bottom"
      :style="{ height: '70%' }"
      round
    >
      <div class="dialog-content">
        <div class="dialog-header">
          <h3>规则模板</h3>
          <van-icon name="cross" @click="showTemplates = false" />
        </div>

        <van-loading v-if="loadingTemplates" class="loading-center" />

        <div v-else class="template-list">
          <div
            v-for="template in templates"
            :key="template.id"
            class="template-item"
            @click="applyTemplate(template.id)"
          >
            <div class="template-header">
              <span class="template-name">{{ template.name }}</span>
              <van-tag :type="getCategoryColor(template.category)">
                {{ template.category }}
              </van-tag>
            </div>
            <div class="template-pattern">{{ template.pattern }}</div>
            <div class="template-desc">{{ template.description }}</div>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import {
  getRules,
  createRule,
  updateRule,
  deleteRule,
  testRule,
  getRuleTemplates,
  applyRuleTemplate
} from '@/api/extractionRules'
import RuleList from '@/components/RuleList.vue'

const router = useRouter()

// 数据
const activeTab = ref('pickup_code')
const rules = ref([])
const loading = ref(false)
const showDialog = ref(false)
const showTemplates = ref(false)
const loadingTemplates = ref(false)
const templates = ref([])
const isEdit = ref(false)
const saving = ref(false)

const currentRule = ref({
  name: '',
  rule_type: 'pickup_code',
  pattern: '',
  extract_group: 1,
  priority: 0,
  is_active: true,
  description: ''
})

const testText = ref('')
const testResult = ref(null)

// 计算属性
const pickupCodeRules = computed(() => 
  rules.value.filter(r => r.rule_type === 'pickup_code')
)

const addressRules = computed(() => 
  rules.value.filter(r => r.rule_type === 'address')
)

const senderRules = computed(() => 
  rules.value.filter(r => r.rule_type === 'sender')
)

// 方法
const loadRules = async () => {
  try {
    loading.value = true
    const response = await getRules()
    rules.value = response
  } catch (error) {
    console.error('加载规则失败:', error)
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

const onTabChange = (name) => {
  activeTab.value = name
}

const showAddDialog = () => {
  isEdit.value = false
  currentRule.value = {
    name: '',
    rule_type: activeTab.value,
    pattern: '',
    extract_group: 1,
    priority: 0,
    is_active: true,
    description: ''
  }
  testText.value = ''
  testResult.value = null
  showDialog.value = true
}

const editRule = (rule) => {
  isEdit.value = true
  currentRule.value = { ...rule }
  testText.value = ''
  testResult.value = null
  showDialog.value = true
}

const closeDialog = () => {
  showDialog.value = false
}

const saveRule = async () => {
  try {
    saving.value = true
    
    if (isEdit.value) {
      await updateRule(currentRule.value.id, currentRule.value)
      showToast('更新成功')
    } else {
      await createRule(currentRule.value)
      showToast('创建成功')
    }
    
    closeDialog()
    loadRules()
  } catch (error) {
    console.error('保存规则失败:', error)
    showToast(error.response?.data?.detail || '保存失败')
  } finally {
    saving.value = false
  }
}

const confirmDelete = async (rule) => {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: `确定要删除规则"${rule.name}"吗？`
    })
    
    await deleteRule(rule.id)
    showToast('删除成功')
    loadRules()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除规则失败:', error)
      showToast('删除失败')
    }
  }
}

const toggleRule = async (rule) => {
  try {
    await updateRule(rule.id, { is_active: !rule.is_active })
    rule.is_active = !rule.is_active
    showToast(rule.is_active ? '已启用' : '已禁用')
  } catch (error) {
    console.error('切换规则状态失败:', error)
    showToast('操作失败')
  }
}

const testCurrentRule = async () => {
  if (!currentRule.value.pattern || !testText.value) {
    showToast('请输入正则表达式和测试文本')
    return
  }
  
  try {
    const response = await testRule({
      pattern: currentRule.value.pattern,
      extract_group: currentRule.value.extract_group,
      test_text: testText.value
    })
    testResult.value = response
  } catch (error) {
    console.error('测试规则失败:', error)
    testResult.value = {
      success: false,
      matched: false,
      error: error.response?.data?.detail || '测试失败'
    }
  }
}

const showTemplateDialog = async () => {
  showTemplates.value = true
  loadingTemplates.value = true
  
  try {
    const response = await getRuleTemplates(activeTab.value)
    templates.value = response
  } catch (error) {
    console.error('加载模板失败:', error)
    showToast('加载模板失败')
  } finally {
    loadingTemplates.value = false
  }
}

const applyTemplate = async (templateId) => {
  try {
    await applyRuleTemplate(templateId)
    showToast('应用成功')
    showTemplates.value = false
    loadRules()
  } catch (error) {
    console.error('应用模板失败:', error)
    showToast(error.response?.data?.detail || '应用失败')
  }
}

const getCategoryColor = (category) => {
  const colors = {
    '快递': 'primary',
    '验证码': 'success',
    '银行': 'warning',
    '通用': 'default'
  }
  return colors[category] || 'default'
}

// 初始化
onMounted(() => {
  loadRules()
})
</script>

<style scoped>
.rule-config-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 120px;
}

.cute-navbar {
  background: linear-gradient(135deg, #FF6B9D 0%, #FF8FAB 100%);
}

.cute-navbar :deep(.van-nav-bar__title) {
  color: white;
  font-weight: 600;
}

.cute-navbar :deep(.van-icon) {
  color: white;
}

.add-rule-btn,
.template-btn {
  position: fixed;
  bottom: 80px;
  left: 16px;
  right: 16px;
  z-index: 100;
}

.template-btn {
  bottom: 20px;
}

.dialog-content {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.dialog-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.test-section {
  margin: 20px 16px;
  padding: 16px;
  background: #f7f8fa;
  border-radius: 8px;
}

.test-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
}

.test-result {
  margin-top: 12px;
  padding: 12px;
  border-radius: 4px;
  font-size: 14px;
}

.test-result.success {
  background: #e8f5e9;
  color: #2e7d32;
}

.test-result.error {
  background: #ffebee;
  color: #c62828;
}

.test-result.warning {
  background: #fff3e0;
  color: #ef6c00;
}

.dialog-actions {
  margin-top: 20px;
  padding: 0 16px;
}

.loading-center {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.template-list {
  padding: 16px;
}

.template-item {
  background: white;
  padding: 16px;
  margin-bottom: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.template-item:active {
  transform: scale(0.98);
  background: #f7f8fa;
}

.template-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.template-name {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
}

.template-pattern {
  font-size: 12px;
  font-family: 'Courier New', monospace;
  color: #646566;
  background: #f7f8fa;
  padding: 8px;
  border-radius: 4px;
  margin-bottom: 8px;
  overflow-x: auto;
}

.template-desc {
  font-size: 13px;
  color: #969799;
}
</style>
