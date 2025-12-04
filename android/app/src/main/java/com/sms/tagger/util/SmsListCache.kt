package com.sms.tagger.util

import com.sms.tagger.data.model.SmsCreate

/**
 * 短信列表简单缓存，降低频繁进入列表时的读取成本
 */
object SmsListCache {
    private const val MAX_CACHE_TTL_MS = 5 * 60_000L

    private var lastLoadedAt: Long = 0L
    private var cachedList: List<SmsCreate> = emptyList()
    private var cachedLatestTimestamp: String? = null
    private var cachedLatestSmsId: Long? = null

    data class Snapshot(
        val list: List<SmsCreate>,
        val latestTimestamp: String?,
        val latestSmsId: Long?,
        val loadedAt: Long
    )

    fun getIfFresh(latestTimestamp: String?, latestSmsId: Long?): List<SmsCreate>? {
        val now = System.currentTimeMillis()
        return if (
            cachedList.isNotEmpty() &&
            now - lastLoadedAt <= MAX_CACHE_TTL_MS &&
            (latestTimestamp == null || cachedLatestTimestamp == latestTimestamp) &&
            (latestSmsId == null || cachedLatestSmsId == latestSmsId)
        ) {
            cachedList
        } else {
            null
        }
    }

    fun getSnapshot(): Snapshot? {
        return if (cachedList.isNotEmpty()) {
            Snapshot(cachedList, cachedLatestTimestamp, cachedLatestSmsId, lastLoadedAt)
        } else {
            null
        }
    }

    fun update(list: List<SmsCreate>, latestTimestamp: String?, latestSmsId: Long?) {
        cachedList = list
        cachedLatestTimestamp = latestTimestamp
        cachedLatestSmsId = latestSmsId
        lastLoadedAt = System.currentTimeMillis()
    }

    fun clear() {
        cachedList = emptyList()
        lastLoadedAt = 0L
        cachedLatestTimestamp = null
        cachedLatestSmsId = null
    }
}

