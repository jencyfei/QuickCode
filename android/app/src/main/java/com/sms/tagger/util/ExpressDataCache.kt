package com.sms.tagger.util

/**
 * 简单的快递信息缓存，用于减轻页面重复进入时的重计算
 */
object ExpressDataCache {
    private const val MAX_CACHE_TTL_MS = 5 * 60_000L

    private var lastLoadedAt: Long = 0L
    private var cachedList: List<ExpressInfo> = emptyList()
    private var cachedLatestTimestamp: String? = null
    private var cachedLatestSmsId: Long? = null

    data class Snapshot(
        val list: List<ExpressInfo>,
        val latestTimestamp: String?,
        val latestSmsId: Long?,
        val loadedAt: Long
    )

    fun getIfFresh(latestTimestamp: String?, latestSmsId: Long?): List<ExpressInfo>? {
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

    fun update(list: List<ExpressInfo>, latestTimestamp: String?, latestSmsId: Long?) {
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

