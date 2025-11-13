package com.sms.tagger.data.repository

import android.content.Context
import com.sms.tagger.data.model.*
import com.sms.tagger.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 标签数据仓库
 * 负责标签相关的数据操作
 */
class TagRepository(private val context: Context) {
    
    private val apiService = RetrofitClient.getApiService(context)
    
    /**
     * 获取标签列表
     */
    suspend fun getTags(): Result<TagListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTags()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("获取标签列表失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 创建标签
     */
    suspend fun createTag(tag: TagCreate): Result<Tag> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createTag(tag)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("创建标签失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取单个标签详情
     */
    suspend fun getTag(id: Int): Result<Tag> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTag(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("获取标签详情失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 更新标签
     */
    suspend fun updateTag(id: Int, tag: TagUpdate): Result<Tag> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateTag(id, tag)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("更新标签失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 删除标签
     */
    suspend fun deleteTag(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteTag(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("删除标签失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
