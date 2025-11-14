package com.sms.tagger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 快递页面 ViewModel
 * 用于管理快递状态的持久化
 */
class ExpressViewModel : ViewModel() {
    
    // 存储快递取件状态：key = pickupCode, value = isPicked
    private val _pickupStatusMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val pickupStatusMap: StateFlow<Map<String, Boolean>> = _pickupStatusMap
    
    /**
     * 更新快递取件状态
     */
    fun updatePickupStatus(pickupCode: String, isPicked: Boolean) {
        viewModelScope.launch {
            val currentMap = _pickupStatusMap.value.toMutableMap()
            currentMap[pickupCode] = isPicked
            _pickupStatusMap.value = currentMap
        }
    }
    
    /**
     * 获取快递的取件状态
     */
    fun getPickupStatus(pickupCode: String): Boolean {
        return _pickupStatusMap.value[pickupCode] ?: false
    }
    
    /**
     * 清空所有状态
     */
    fun clearAllStatus() {
        viewModelScope.launch {
            _pickupStatusMap.value = emptyMap()
        }
    }
}
