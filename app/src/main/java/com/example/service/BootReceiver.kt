package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.LockPreferences
import com.example.util.PermissionUtils

/**
 * Ensures App Locker protection automatically restarts upon device reboot.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.d(TAG, "Device booted, evaluating auto-start conditions ($action)")
            val prefs = LockPreferences(context)
            if (prefs.isServiceActive && prefs.hasPatternSet()) {
                val hasUsage = PermissionUtils.hasUsageStatsPermission(context)
                val hasOverlay = PermissionUtils.hasOverlayPermission(context)
                if (hasUsage && hasOverlay) {
                    try {
                        val serviceIntent = Intent(context, AppLockService::class.java)
                        ContextCompat.startForegroundService(context, serviceIntent)
                        Log.d(TAG, "Successfully started AppLockService on boot")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to start AppLockService on boot", e)
                    }
                } else {
                    Log.w(TAG, "Permissions missing on boot (usage=$hasUsage, overlay=$hasOverlay)")
                }
            }
        }
    }
}
