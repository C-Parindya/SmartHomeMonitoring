package com.example.smarthome

import android.app.Application
import com.example.smarthome.util.NotificationHelper

class SmartHomeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        NotificationHelper.init(this)
    }

    companion object {
        lateinit var instance: SmartHomeApp
            private set
    }
}
