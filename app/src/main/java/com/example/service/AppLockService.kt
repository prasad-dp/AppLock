package com.example.service

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.UnlockActivity
import com.example.data.AppDatabase
import com.example.data.AppRepository
import kotlinx.coroutines.*

class AppLockService : Service() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    
    private lateinit var repository: AppRepository
    private val lockPrefs by lazy { com.example.data.LockPreferences(this) }
    private val lockedPackages = java.util.Collections.synchronizedSet(mutableSetOf<String>())
    private val lastSeenForegroundTime = java.util.Collections.synchronizedMap(mutableMapOf<String, Long>())
    private var lastKnownForegroundPackage: String? = null
    
    private val screenLockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                AppLockSession.clearSession()
                Log.d("AppLockService", "Screen off logic: Sessions cleared, apps re-locked.")
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "app_lock_service_channel"
        private const val NOTIFICATION_ID = 101
        private const val TAG = "AppLockService"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service Created")
        AppLockSession.setServiceRunning(true)
        
        // Initialize Room Repo
        val database = AppDatabase.getInstance(this)
        repository = AppRepository(database.lockedAppDao(), database.intruderAlertDao())

        // Cache and dynamically track locked packages in real-time
        serviceScope.launch {
            repository.allLockedAppsStateFlow.collect { list ->
                val packages = list.filter { it.isLocked }.map { it.packageName }
                lockedPackages.clear()
                lockedPackages.addAll(packages)
                Log.d(TAG, "Sync: locked packages updated -> size: ${lockedPackages.size}")
            }
        }

        // Register screen off receiver to lock apps when phone is locked
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenLockReceiver, filter)

        // Start Foreground Service with notification safely
        createNotificationChannel()
        val notification = createNotification()
        var startedForeground = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
                startedForeground = true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start service as foreground with special use type", e)
            }
        }
        if (!startedForeground) {
            try {
                startForeground(NOTIFICATION_ID, notification)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start service as foreground fallback", e)
            }
        }

        // Start background polling loop
        startCheckingLoop()
    }

    private var lastLaunchTime = 0L

    private fun startCheckingLoop() {
        serviceScope.launch {
            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            val powerManager = getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
            if (usm == null) {
                Log.e(TAG, "UsageStatsManager was not available")
                return@launch
            }

            while (isActive) {
                var currentApp: String? = null
                try {
                    // Battery Saver Optimization: Avoid active polling of usage statistics or locks when screen is off
                    if (powerManager != null && !powerManager.isInteractive) {
                        delay(1200) // Sleep peacefully while non-interactive
                        continue
                    }

                    currentApp = getForegroundPackageName(usm)
                    val isTransient = isSystemOrTransientPackage(currentApp)

                    if (isTransient) {
                        // User is interacting with system UI, biometric dialog, keyboard, or system window.
                        // Keep lastSeenForegroundTime updated for all unlocked apps so session does NOT expire while on system prompts!
                        val now = System.currentTimeMillis()
                        for (unlockedApp in AppLockSession.getUnlockedAppsCopy()) {
                            lastSeenForegroundTime[unlockedApp] = now
                        }
                    } else if (currentApp != null) {
                        lastSeenForegroundTime[currentApp] = System.currentTimeMillis()
                    }

                    // Auto-relock any unlocked app that is no longer in the foreground
                    val currentUnlockedApps = AppLockSession.getUnlockedAppsCopy()
                    for (unlockedApp in currentUnlockedApps) {
                        if (unlockedApp != currentApp && !isTransient) {
                            val unlockTime = AppLockSession.getUnlockTime(unlockedApp)
                            val timeSinceUnlock = System.currentTimeMillis() - unlockTime

                            val perAppPolicy = if (lockPrefs.isPremiumUser) lockPrefs.getPerAppRelockTimeout(unlockedApp) else null
                            val relockPolicy = perAppPolicy ?: lockPrefs.reLockTimeout
                            val relockThresholdMs = when (relockPolicy) {
                                "immediately" -> 1_500L // Re-lock immediately upon leaving app
                                "15_sec" -> 15_000L // Re-lock 15 seconds after leaving app
                                "30_sec" -> 30_000L // Re-lock 30 seconds after leaving app (Default)
                                "1_min" -> 60_000L // Re-lock 1 minute after leaving app
                                "5_min" -> 300_000L // Re-lock 5 minutes after leaving app
                                else -> 30_000L
                            }

                            // Internal Lock Grace Window: capped at relockThresholdMs so immediate/15sec policies are respected
                            val maxGraceWindowMs = minOf(30_000L, relockThresholdMs)
                            if (timeSinceUnlock < maxGraceWindowMs) {
                                lastSeenForegroundTime[unlockedApp] = System.currentTimeMillis()
                                continue
                            }

                            val lastSeen = lastSeenForegroundTime[unlockedApp] ?: System.currentTimeMillis()
                            val outOfForegroundDuration = System.currentTimeMillis() - lastSeen

                            if (outOfForegroundDuration > relockThresholdMs) {
                                AppLockSession.lockApp(unlockedApp)
                                Log.d(TAG, "Auto-relocked app: $unlockedApp after $outOfForegroundDuration ms out of foreground (Policy: $relockPolicy)")
                            }
                        }
                    }

                    if (currentApp != null && currentApp != packageName && !isTransient) {
                        // Reset activeUnlockingPackage if user navigated to a different app
                        if (AppLockSession.activeUnlockingPackage != null && currentApp != AppLockSession.activeUnlockingPackage) {
                            AppLockSession.activeUnlockingPackage = null
                        }

                        // Check if this package is locked (extremely fast in-memory check)
                        val isLocked = lockedPackages.contains(currentApp)
                        if (isLocked) {
                            // Check if already unlocked in active session
                            val isUnlocked = AppLockSession.isUnlocked(currentApp)
                            val isUnlockingNow = AppLockSession.activeUnlockingPackage == currentApp

                            // If overlay launch took longer than 800ms without covering the app, retry launch
                            val isLaunchBlocked = isUnlockingNow && (System.currentTimeMillis() - lastLaunchTime > 800)

                            if (!isUnlocked && (!isUnlockingNow || isLaunchBlocked)) {
                                Log.d(TAG, "Locked app detected: $currentApp. Launching unlock screen. Blocked retry: $isLaunchBlocked")
                                launchUnlockScreen(currentApp)
                            }
                        }
                    } else if (currentApp == packageName) {
                        // Unlock screen or AppLocker is currently in foreground
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in checking loop", e)
                }
                
                // Adaptive Polling: 50ms when app switching occurs or when on locked apps; 180ms when sitting stably in an unlocked app
                val nextDelay = if (lastKnownForegroundPackage != currentApp) {
                    lastKnownForegroundPackage = currentApp
                    50L
                } else if (currentApp != null && lockedPackages.contains(currentApp)) {
                    50L
                } else {
                    180L
                }
                delay(nextDelay)
            }
        }
    }

    private fun isSystemOrTransientPackage(pkg: String?): Boolean {
        if (pkg == null) return true
        if (pkg == packageName) return true // AppLocker itself & UnlockActivity
        val lower = pkg.lowercase()
        return lower == "android" ||
                lower == "com.android.systemui" ||
                lower == "com.google.android.gms" ||
                lower.contains("permissioncontroller") ||
                lower.contains("biometric") ||
                lower.contains("inputmethod") ||
                lower.contains("keyboard") ||
                lower.contains("fingerprint") ||
                lower.contains("keyguard") ||
                lower.contains("systemui") ||
                lower.contains("launcher")
    }

    private fun getForegroundPackageName(usm: UsageStatsManager): String? {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 15000

        val usageEvents = try {
            usm.queryEvents(startTime, endTime)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to query usage events", e)
            null
        } ?: return lastKnownForegroundPackage

        val event = UsageEvents.Event()
        val packageStates = mutableMapOf<String, Int>()
        val packageLastTime = mutableMapOf<String, Long>()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            val pkg = event.packageName ?: continue
            val type = event.eventType
            if (type == UsageEvents.Event.ACTIVITY_RESUMED ||
                type == UsageEvents.Event.ACTIVITY_PAUSED ||
                type == UsageEvents.Event.ACTIVITY_STOPPED ||
                type == 1 || type == 2) {

                val prevTime = packageLastTime[pkg] ?: 0L
                if (event.timeStamp >= prevTime) {
                    packageLastTime[pkg] = event.timeStamp
                    packageStates[pkg] = type
                }
            }
        }

        // Find packages whose latest lifecycle state is RESUMED
        val resumedPackages = packageStates.filter { it.value == UsageEvents.Event.ACTIVITY_RESUMED || it.value == 1 }
        if (resumedPackages.isNotEmpty()) {
            val topPkg = resumedPackages.keys.maxByOrNull { packageLastTime[it] ?: 0L }
            if (topPkg != null) {
                lastKnownForegroundPackage = topPkg
                return topPkg
            }
        }

        // Fallback: Query UsageStats
        try {
            val fallbackStart = endTime - 8000
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, fallbackStart, endTime)
            if (!stats.isNullOrEmpty()) {
                val sortedStats = stats.sortedByDescending { it.lastTimeUsed }
                val top = sortedStats.firstOrNull()
                if (top != null && (endTime - top.lastTimeUsed) < 5000) {
                    lastKnownForegroundPackage = top.packageName
                    return top.packageName
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed fallback usage stats query", e)
        }

        return lastKnownForegroundPackage
    }

    @Suppress("DEPRECATION")
    private fun launchUnlockScreen(targetPackage: String) {
        AppLockSession.activeUnlockingPackage = targetPackage
        lastLaunchTime = System.currentTimeMillis()
        val intent = Intent(this, UnlockActivity::class.java).apply {
            putExtra("EXTRA_PACKAGE_NAME", targetPackage)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed startActivity launch unlock screen", e)
            try {
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val options = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ActivityOptions.makeBasic().apply {
                        setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                    }.toBundle()
                } else null
                if (options != null) {
                    pendingIntent.send(this, 0, null, null, null, null, options)
                } else {
                    pendingIntent.send()
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Failed fallback pending intent launch", ex)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service Destroyed")
        AppLockSession.setServiceRunning(false)
        try {
            unregisterReceiver(screenLockReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering screenLockReceiver", e)
        }
        serviceJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("App Locker Active")
            .setContentText("Your selected apps are currently locked and protected.")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "App Shield Running Services",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }
}
