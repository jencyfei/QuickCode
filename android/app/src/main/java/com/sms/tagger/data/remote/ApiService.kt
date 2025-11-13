package com.sms.tagger.data.remote

import com.sms.tagger.data.model.Tag
import com.sms.tagger.data.model.TagCreate
import com.sms.tagger.data.model.TagUpdate
import com.sms.tagger.data.model.TagListResponse
import com.sms.tagger.data.model.User
import com.sms.tagger.data.model.RegisterRequest
import com.sms.tagger.data.model.LoginResponse
import com.sms.tagger.data.model.Sms
import com.sms.tagger.data.model.SmsCreate
import com.sms.tagger.data.model.SmsBatchCreate
import com.sms.tagger.data.model.SmsListResponse
import com.sms.tagger.data.model.SmsAddTags
import com.sms.tagger.data.model.SmsBatchAddTags
import com.sms.tagger.data.model.SmsBatchDelete
import com.sms.tagger.data.model.BatchResult
import retrofit2.Response
import retrofit2.http.*

/**
 * API服务接口
 * 定义所有后端API端点
 */
interface ApiService {
    
    // ==================== 认证相关 ====================
    
    /**
     * 用户注册
     */
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<User>
    
    /**
     * 用户登录
     */
    @FormUrlEncoded
    @POST("api/auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>
    
    /**
     * 获取当前用户信息
     */
    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<User>
    
    // ==================== 标签相关 ====================
    
    /**
     * 获取标签列表
     */
    @GET("api/tags")
    suspend fun getTags(): Response<TagListResponse>
    
    /**
     * 创建标签
     */
    @POST("api/tags")
    suspend fun createTag(@Body tag: TagCreate): Response<Tag>
    
    /**
     * 获取单个标签详情
     */
    @GET("api/tags/{id}")
    suspend fun getTag(@Path("id") id: Int): Response<Tag>
    
    /**
     * 更新标签
     */
    @PUT("api/tags/{id}")
    suspend fun updateTag(
        @Path("id") id: Int,
        @Body tag: TagUpdate
    ): Response<Tag>
    
    /**
     * 删除标签
     */
    @DELETE("api/tags/{id}")
    suspend fun deleteTag(@Path("id") id: Int): Response<Unit>
    
    // ==================== 短信相关 ====================
    
    /**
     * 获取短信列表
     */
    @GET("api/sms")
    suspend fun getSmsList(
        @Query("keyword") keyword: String? = null,
        @Query("tag_ids") tagIds: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): Response<SmsListResponse>
    
    /**
     * 获取单条短信详情
     */
    @GET("api/sms/{id}")
    suspend fun getSms(@Path("id") id: Int): Response<Sms>
    
    /**
     * 创建单条短信
     */
    @POST("api/sms")
    suspend fun createSms(@Body sms: SmsCreate): Response<Sms>
    
    /**
     * 批量创建短信
     */
    @POST("api/sms/batch")
    suspend fun createSmsBatch(@Body request: SmsBatchCreate): Response<List<Sms>>
    
    /**
     * 更新短信
     */
    @PUT("api/sms/{id}")
    suspend fun updateSms(
        @Path("id") id: Int,
        @Body sms: SmsCreate
    ): Response<Sms>
    
    /**
     * 删除单条短信
     */
    @DELETE("api/sms/{id}")
    suspend fun deleteSms(@Path("id") id: Int): Response<Unit>
    
    /**
     * 批量删除短信
     */
    @POST("api/sms/batch-delete")
    suspend fun batchDeleteSms(@Body request: SmsBatchDelete): Response<Unit>
    
    /**
     * 为短信添加标签
     */
    @POST("api/sms/{id}/tags")
    suspend fun addTagsToSms(
        @Path("id") smsId: Int,
        @Body request: SmsAddTags
    ): Response<Sms>
    
    /**
     * 批量为短信添加标签
     */
    @POST("api/sms/batch-add-tags")
    suspend fun batchAddTags(@Body request: SmsBatchAddTags): Response<BatchResult>
    
    /**
     * 移除短信的标签
     */
    @DELETE("api/sms/{sms_id}/tags/{tag_id}")
    suspend fun removeTagFromSms(
        @Path("sms_id") smsId: Int,
        @Path("tag_id") tagId: Int
    ): Response<Unit>
}
