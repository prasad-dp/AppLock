package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.UnlockActivity
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.LockPreferences
import kotlinx.coroutines.*

class AppLockAccessibilityService : AccessibilityService() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    private lateinit var repository: AppRepository
    private val lockPrefs by lazy { LockPreferences(this) }
    private val lockedPackages = java.util.Collections.synchronizedSet(mutableSetOf<String>())
    private val lastSeenForegroundTime = java.util.Collections.synchronizedMap(mutableMapOf<String, Long>())
    private var lastForegroundPackage: String? = null
    private var lastLaunchTime = 0L
    @Volatile
    private var isUnlockActivityInForeground = false

    private val screenLockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                lastSeenForegroundTime.clear()
                lastForegroundPackage = null
                isUnlockActivityInForeground = false
                AppLockSession.clearSession()
                Log.d(TAG, "Screen off: Accessibility session cleared.")
            }
        }
    }

    companion object {
        private const val TAG = "AppLockAccessibility"
        var isAccessibilityRunning = false
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isAccessibilityRunning = true
        Log.d(TAG, "Accessibility Service Connected: 0ms Instant Lock Engine Active")

        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenLockReceiver, filter)

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOWS_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 0
        }
        this.serviceInfo = info

        val database = AppDatabase.getInstance(this)
        repository = AppRepository(database.lockedAppDao(), database.intruderAlertDao())

        serviceScope.launch {
            repository.allLockedAppsStateFlow.collect { list ->
                val packages = list.filter { it.isLocked }.map { it.packageName }
                lockedPackages.clear()
                lockedPackages.addAll(packages)
                Log.d(TAG, "Accessibility Sync: locked packages updated -> count: ${lockedPackages.size}")
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val type = event.eventType
        if (type != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && type != AccessibilityEvent.TYPE_WINDOWS_CHANGED) return

        val pkgName = event.packageName?.toString() ?: return
        val isTransient = isSystemOrTransientPackage(pkgName)

        if (isTransient) {
            val now = System.currentTimeMillis()
            for (unlockedApp in AppLockSession.getUnlockedAppsCopy()) {
                lastSeenForegroundTime[unlockedApp] = now
                AppLockSession.updateActiveTime(unlockedApp)
            }
            return
        }

        lastForegroundPackage = pkgName
        lastSeenForegroundTime[pkgName] = System.currentTimeMillis()
        AppLockSession.updateActiveTime(pkgName)

        // Check relock for other unlocked apps
        val currentUnlockedApps = AppLockSession.getUnlockedAppsCopy()
        for (unlockedApp in currentUnlockedApps) {
            if (unlockedApp != pkgName) {
                val lastSeen = lastSeenForegroundTime[unlockedApp] ?: AppLockSession.getLastActiveTime(unlockedApp)
                val outOfForegroundDuration = System.currentTimeMillis() - lastSeen

                val perAppPolicy = if (lockPrefs.isPremiumUser) lockPrefs.getPerAppRelockTimeout(unlockedApp) else null
                val relockPolicy = perAppPolicy ?: lockPrefs.reLockTimeout
                val relockThresholdMs = when (relockPolicy) {
                    "immediately" -> 0L
                    "15_sec" -> 15_000L
                    "30_sec" -> 30_000L
                    "1_min" -> 60_000L
                    "5_min" -> 300_000L
                    else -> 0L
                }

                if (outOfForegroundDuration >= relockThresholdMs) {
                    AppLockSession.lockApp(unlockedApp)
                    lastSeenForegroundTime.remove(unlockedApp)
                }
            }
        }

        if (pkgName == packageName) {
            isUnlockActivityInForeground = true
            return // Don't lock our own app
        } else {
            isUnlockActivityInForeground = false
        }

        if (AppLockSession.activeUnlockingPackage != null && pkgName != AppLockSession.activeUnlockingPackage) {
            AppLockSession.activeUnlockingPackage = null
        }

        if (lockedPackages.contains(pkgName)) {
            val isUnlocked = AppLockSession.isUnlocked(pkgName)
            val isUnlockingNow = AppLockSession.activeUnlockingPackage == pkgName
            // If the unlock screen is already in the foreground, do NOT re-trigger launch (prevents input interruption)
            val isLaunchBlocked = isUnlockingNow && !isUnlockActivityInForeground && (System.currentTimeMillis() - lastLaunchTime > 800)

            if (!isUnlocked && (!isUnlockingNow || isLaunchBlocked)) {
                Log.d(TAG, "Instant 0ms Intercept: Locking $pkgName")
                launchUnlockScreen(pkgName)
            }
        }
    }

    private fun isSystemOrTransientPackage(pkg: String?): Boolean {
        if (pkg == null) return true
        if (pkg == packageName) return true
        val lower = pkg.lowercase()
        return lower == "android" ||
                lower == "com.android.systemui" ||
                lower == "com.google.android.gms" ||
                lower == "com.google.android.packageinstaller" ||
                lower.contains("permissioncontroller") ||
                lower.contains("biometric") ||
                lower.contains("inputmethod") ||
                lower.contains("keyboard") ||
                lower.contains("fingerprint") ||
                lower.contains("keyguard") ||
                lower.contains("systemui") ||
                lower.contains("credentials") ||
                lower.contains("confirm_device_credential") ||
                lower.contains("autofill")
    }

    @Suppress("DEPRECATION")
    private fun launchUnlockScreen(targetPackage: String) {
        AppLockSession.activeUnlockingPackage = targetPackage
        lastLaunchTime = System.currentTimeMillis()
        val intent = Intent(this, UnlockActivity::class.java).apply {
            putExtra("EXTRA_PACKAGE_NAME", targetPackage)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }

        val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
        try {
            startActivity(intent, options.toBundle())
        } catch (e: Exception) {
            Log.e(TAG, "Failed instant startActivity unlock screen", e)
            try {
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val bgOptions = ActivityOptions.makeBasic().apply {
                        setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                    }.toBundle()
                    pendingIntent.send(this, 0, null, null, null, null, bgOptions)
                } else {
                    pendingIntent.send()
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Failed fallback pending intent launch", ex)
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        isAccessibilityRunning = false
        try {
            unregisterReceiver(screenLockReceiver)
        } catch (_: Exception) {}
        serviceJob.cancel()
    }
}
