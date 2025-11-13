package com.sms.tagger.data.remote

import com.sms.tagger.util.PreferencesManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 认证拦截器
 * 自动为请求添加Authorization头
 */
class AuthInterceptor(
    private val preferencesManager: PreferencesManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 如果是登录或注册请求，不添加Token
        val url = originalRequest.url.toString()
        if (url.contains("/api/auth/login") || url.contains("/api/auth/register")) {
            return chain.proceed(originalRequest)
        }
        
        // 获取Token
        val token = preferencesManager.getAccessToken()
        
        // 如果没有Token，直接发送原始请求
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }
        
        // 添加Authorization头
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        
        return chain.proceed(newRequest)
    }
}
