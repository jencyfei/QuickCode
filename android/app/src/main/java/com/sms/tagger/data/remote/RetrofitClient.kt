package com.sms.tagger.data.remote

import android.content.Context
import com.google.gson.GsonBuilder
import com.sms.tagger.util.PreferencesManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit客户端配置
 * 单例模式
 */
object RetrofitClient {
    
    // 基础URL - 使用实际后端服务地址
    // 后端服务运行在 localhost:10043 (开发环境)
    // 真机测试时改为电脑的局域网IP，如：http://192.168.x.x:10043/
    private const val BASE_URL = "http://10.24.153.46:10043/"
    
    // 超时时间
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L
    
    @Volatile
    private var apiService: ApiService? = null
    
    /**
     * 获取API服务实例
     */
    fun getApiService(context: Context): ApiService {
        return apiService ?: synchronized(this) {
            apiService ?: createApiService(context).also {
                apiService = it
            }
        }
    }
    
    /**
     * 创建API服务实例
     */
    private fun createApiService(context: Context): ApiService {
        val preferencesManager = PreferencesManager(context)
        
        // 日志拦截器
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        // 认证拦截器
        val authInterceptor = AuthInterceptor(preferencesManager)
        
        // OkHttp客户端
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
        
        // Gson配置
        val gson = GsonBuilder()
            .setLenient()
            .create()
        
        // Retrofit实例
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        
        return retrofit.create(ApiService::class.java)
    }
    
    /**
     * 重置API服务（用于切换用户或登出）
     */
    fun reset() {
        apiService = null
    }
    
    /**
     * 创建带Token的API服务（用于临时请求）
     */
    fun createService(token: String): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
        
        val gson = GsonBuilder()
            .setLenient()
            .create()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        
        return retrofit.create(ApiService::class.java)
    }
}
