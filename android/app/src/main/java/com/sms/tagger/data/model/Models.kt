package com.sms.tagger.data.model

import com.google.gson.annotations.SerializedName

// ==================== 标签相关 ====================

/**
 * 标签数据模型
 */
data class Tag(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("color")
    val color: String,
    
    @SerializedName("icon")
    val icon: String?,
    
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("sms_count")
    val smsCount: Int = 0,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String?
)

/**
 * 创建标签请求
 */
data class TagCreate(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("color")
    val color: String,
    
    @SerializedName("icon")
    val icon: String?
)

/**
 * 更新标签请求
 */
data class TagUpdate(
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("color")
    val color: String?,
    
    @SerializedName("icon")
    val icon: String?
)

/**
 * 标签列表响应
 */
data class TagListResponse(
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("tags")
    val tags: List<Tag>
)

// ==================== 用户相关 ====================

/**
 * 用户数据模型
 */
data class User(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("email")
    val email: String?,
    
    @SerializedName("phone")
    val phone: String?,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String?
)

/**
 * 注册请求
 */
data class RegisterRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String
)

/**
 * 登录响应
 */
data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String,
    
    @SerializedName("token_type")
    val tokenType: String
)

// ==================== 短信相关 ====================

/**
 * 短信数据模型
 */
data class Sms(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("sender")
    val sender: String,
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("received_at")
    val receivedAt: String,
    
    @SerializedName("phone_number")
    val phoneNumber: String?,
    
    @SerializedName("user_id")
    val userId: Int,
    
    @SerializedName("tags")
    val tags: List<Tag> = emptyList(),
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String?
)

/**
 * 创建短信请求
 */
data class SmsCreate(
    @SerializedName("sender")
    val sender: String,
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("received_at")
    val receivedAt: String,
    
    @SerializedName("phone_number")
    val phoneNumber: String?
)

/**
 * 批量创建短信请求
 */
data class SmsBatchCreate(
    @SerializedName("messages")
    val messages: List<SmsCreate>
)

/**
 * 短信列表响应
 */
data class SmsListResponse(
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("page_size")
    val pageSize: Int,
    
    @SerializedName("items")
    val items: List<Sms>
)

/**
 * 为短信添加标签请求
 */
data class SmsAddTags(
    @SerializedName("tag_ids")
    val tagIds: List<Int>
)

/**
 * 批量为短信添加标签请求
 */
data class SmsBatchAddTags(
    @SerializedName("sms_ids")
    val smsIds: List<Int>,
    
    @SerializedName("tag_ids")
    val tagIds: List<Int>
)

/**
 * 批量删除短信请求
 */
data class SmsBatchDelete(
    @SerializedName("ids")
    val ids: List<Int>
)

/**
 * 批量操作结果
 */
data class BatchResult(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("added_relations")
    val addedRelations: Int? = null
)
