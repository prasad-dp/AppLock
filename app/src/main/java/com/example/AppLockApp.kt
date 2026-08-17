package com.example

import android.app.Application
import com.example.billing.BillingManager
import com.example.data.LockPreferences

class AppLockApp : Application() {

    lateinit var prefs: LockPreferences
        private set

    lateinit var billingManager: BillingManager
        private set

    companion object {
        lateinit var instance: AppLockApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        prefs = LockPreferences(this)
        billingManager = BillingManager(this, prefs)
        // Check and sync Google Play Store entitlements on application startup
        billingManager.queryPurchases()
    }
}
