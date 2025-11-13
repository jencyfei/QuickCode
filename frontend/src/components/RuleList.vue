<template>
  <div class="rule-list">
    <van-loading v-if="loading" class="loading-center" />
    
    <van-empty v-else-if="rules.length === 0" description="暂无规则">
      <template #image>
        <div class="empty-icon">📝</div>
      </template>
    </van-empty>
    
    <div v-else class="rule-items">
      <div
        v-for="rule in sortedRules"
        :key="rule.id"
        class="rule-item"
        :class="{ inactive: !rule.is_active }"
      >
        <div class="rule-header">
          <div class="rule-title">
            <span class="rule-name">{{ rule.name }}</span>
            <van-tag v-if="!rule.is_active" type="warning" size="small">
              已禁用
            </van-tag>
          </div>
          <div class="rule-actions">
            <van-switch
              :model-value="rule.is_active"
              size="20px"
              @update:model-value="$emit('toggle', rule)"
            />
          </div>
        </div>
        
        <div class="rule-pattern">
          <span class="label">正则:</span>
          <code>{{ rule.pattern }}</code>
        </div>
        
        <div class="rule-meta">
          <span class="meta-item">
            <van-icon name="star" />
            优先级: {{ rule.priority }}
          </span>
          <span class="meta-item">
            <van-icon name="records" />
            提取组: {{ rule.extract_group }}
          </span>
        </div>
        
        <div v-if="rule.description" class="rule-desc">
          {{ rule.description }}
        </div>
        
        <div class="rule-footer">
          <van-button
            size="small"
            type="primary"
            plain
            @click="$emit('edit', rule)"
          >
            编辑
          </van-button>
          <van-button
            size="small"
            type="danger"
            plain
            @click="$emit('delete', rule)"
          >
            删除
          </van-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  rules: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

defineEmits(['edit', 'delete', 'toggle'])

// 按优先级和创建时间排序
const sortedRules = computed(() => {
  return [...props.rules].sort((a, b) => {
    // 优先级高的在前
    if (a.priority !== b.priority) {
      return b.priority - a.priority
    }
    // 创建时间新的在前
    return new Date(b.created_at) - new Date(a.created_at)
  })
})
</script>

<style scoped>
.rule-list {
  padding: 16px;
  min-height: 400px;
}

.loading-center {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.rule-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rule-item {
  background: white;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.2s;
}

.rule-item.inactive {
  opacity: 0.6;
}

.rule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.rule-title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.rule-name {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
}

.rule-pattern {
  background: #f7f8fa;
  padding: 8px 12px;
  border-radius: 6px;
  margin-bottom: 8px;
  font-size: 12px;
  overflow-x: auto;
}

.rule-pattern .label {
  color: #969799;
  margin-right: 8px;
}

.rule-pattern code {
  font-family: 'Courier New', monospace;
  color: #FF6B9D;
}

.rule-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
  font-size: 13px;
  color: #646566;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.rule-desc {
  font-size: 13px;
  color: #969799;
  margin-bottom: 12px;
  line-height: 1.5;
}

.rule-footer {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}
</style>
