package com.sms.tagger

import android.app.Application
import com.sms.tagger.util.NotificationHelper

class SmsAgentApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 确保通知渠道在应用启动时注册，解决部分机型通知总开关灰色的问题
        NotificationHelper.ensureChannelCreated(this)
    }
}

