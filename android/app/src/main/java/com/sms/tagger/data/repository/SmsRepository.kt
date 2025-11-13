package com.sms.tagger.data.repository

import android.content.Context
import com.sms.tagger.data.model.*
import com.sms.tagger.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 短信数据仓库
 * 负责短信相关的数据操作
 */
class SmsRepository(private val context: Context) {
    
    private val apiService = RetrofitClient.getApiService(context)
    
    /**
     * 获取短信列表
     */
    suspend fun getSmsList(
        keyword: String? = null,
        tagIds: List<Int>? = null,
        startDate: String? = null,
        endDate: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): Result<SmsListResponse> = withContext(Dispatchers.IO) {
        try {
            val tagIdsStr = tagIds?.joinToString(",")
            val response = apiService.getSmsList(
                keyword = keyword,
                tagIds = tagIdsStr,
                startDate = startDate,
                endDate = endDate,
                page = page,
                pageSize = pageSize
            )
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("获取短信列表失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取单条短信详情
     */
    suspend fun getSms(id: Int): Result<Sms> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSms(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("获取短信详情失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 创建单条短信
     */
    suspend fun createSms(sms: SmsCreate): Result<Sms> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createSms(sms)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("创建短信失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 批量创建短信
     */
    suspend fun createSmsBatch(messages: List<SmsCreate>): Result<List<Sms>> = 
        withContext(Dispatchers.IO) {
            try {
                val request = SmsBatchCreate(messages)
                val response = apiService.createSmsBatch(request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("批量创建短信失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * 删除单条短信
     */
    suspend fun deleteSms(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteSms(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("删除短信失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 批量删除短信
     */
    suspend fun batchDeleteSms(ids: List<Int>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = SmsBatchDelete(ids)
            val response = apiService.batchDeleteSms(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("批量删除短信失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 为短信添加标签
     */
    suspend fun addTagsToSms(smsId: Int, tagIds: List<Int>): Result<Sms> = 
        withContext(Dispatchers.IO) {
            try {
                val request = SmsAddTags(tagIds)
                val response = apiService.addTagsToSms(smsId, request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("添加标签失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * 批量为短信添加标签
     */
    suspend fun batchAddTags(smsIds: List<Int>, tagIds: List<Int>): Result<BatchResult> = 
        withContext(Dispatchers.IO) {
            try {
                val request = SmsBatchAddTags(smsIds, tagIds)
                val response = apiService.batchAddTags(request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("批量添加标签失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * 移除短信的标签
     */
    suspend fun removeTagFromSms(smsId: Int, tagId: Int): Result<Unit> = 
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.removeTagFromSms(smsId, tagId)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("移除标签失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
