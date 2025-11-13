package com.sms.tagger.data.repository

import android.content.Context
import com.sms.tagger.data.model.*
import com.sms.tagger.data.remote.RetrofitClient
import com.sms.tagger.util.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 认证数据仓库
 * 负责用户认证相关的数据操作
 */
class AuthRepository(private val context: Context) {
    
    private val apiService = RetrofitClient.getApiService(context)
    private val preferencesManager = PreferencesManager(context)
    
    /**
     * 用户注册
     */
    suspend fun register(email: String, password: String): Result<User> = 
        withContext(Dispatchers.IO) {
            try {
                val request = RegisterRequest(email, password)
                val response = apiService.register(request)
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("注册失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * 用户登录
     */
    suspend fun login(email: String, password: String): Result<LoginResponse> = 
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(email, password)
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    // 保存Token
                    preferencesManager.saveAccessToken(
                        loginResponse.accessToken,
                        loginResponse.tokenType
                    )
                    Result.success(loginResponse)
                } else {
                    Result.failure(Exception("登录失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    
    /**
     * 获取当前用户信息
     */
    suspend fun getCurrentUser(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!
                // 保存用户信息
                preferencesManager.saveUserInfo(user.id, user.email)
                Result.success(user)
            } else {
                Result.failure(Exception("获取用户信息失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 登出
     */
    fun logout() {
        preferencesManager.logout()
        RetrofitClient.reset()
    }
    
    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Boolean {
        return preferencesManager.isLoggedIn()
    }
}
