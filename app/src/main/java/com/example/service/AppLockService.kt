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
import com.example.util.AppLockPackageHelper
import kotlinx.coroutines.*

class AppLockService : Service() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    
    private lateinit var repository: AppRepository
    private val lockPrefs by lazy { com.example.data.LockPreferences(this) }
    private val lockedPackages = java.util.Collections.synchronizedSet(mutableSetOf<String>())
    private val lastSeenForegroundTime = java.util.Collections.synchronizedMap(mutableMapOf<String, Long>())
    private var lastKnownForegroundPackage: String? = null

    // Reusable objects for zero-allocation background polling
    private val reusableEvent = UsageEvents.Event()
    private val reusablePackageStates = mutableMapOf<String, Int>()
    private val reusablePackageLastTime = mutableMapOf<String, Long>()
    
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

        // Immediately load locked packages from database to guarantee protection on start
        serviceScope.launch(Dispatchers.IO) {
            try {
                val initialList = repository.getAllLockedApps().filter { it.isLocked }.map { it.packageName }
                lockedPackages.addAll(initialList)
                Log.d(TAG, "Service initial locked packages loaded: ${lockedPackages.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load initial locked packages", e)
            }
        }

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
                            AppLockSession.updateActiveTime(unlockedApp)
                        }
                    } else if (currentApp != null) {
                        if (AppLockPackageHelper.isLauncherPackage(this@AppLockService, currentApp)) {
                            AppLockSession.setCurrentForeground(currentApp)
                            AppLockSession.clearGoToHome()
                            AppLockSession.activeUnlockingPackage = null
                            lastKnownForegroundPackage = null
                        } else {
                            lastSeenForegroundTime[currentApp] = System.currentTimeMillis()
                            AppLockSession.updateActiveTime(currentApp)

                            // Check auto-relock for other unlocked apps
                            val currentUnlockedApps = AppLockSession.getUnlockedAppsCopy()
                            for (unlockedApp in currentUnlockedApps) {
                                if (unlockedApp != currentApp) {
                                    // If the app was just unlocked within the grace period, do NOT relock
                                    if (AppLockSession.isRecentlyUnlocked(unlockedApp, gracePeriodMs = 2500L)) {
                                        continue
                                    }

                                    val lastSeen = lastSeenForegroundTime[unlockedApp] ?: AppLockSession.getLastActiveTime(unlockedApp)
                                    val outOfForegroundDuration = System.currentTimeMillis() - lastSeen

                                    val perAppPolicy = if (lockPrefs.isPremiumUser) lockPrefs.getPerAppRelockTimeout(unlockedApp) else null
                                    val relockPolicy = perAppPolicy ?: lockPrefs.reLockTimeout
                                    val relockThresholdMs = when (relockPolicy) {
                                        "immediately" -> 1500L // 1.5s safe debounce prevents micro-transition glitches
                                        "15_sec" -> 15_000L // Re-lock 15 seconds after leaving app
                                        "30_sec" -> 30_000L // Re-lock 30 seconds after leaving app (Default)
                                        "1_min" -> 60_000L // Re-lock 1 minute after leaving app
                                        "5_min" -> 300_000L // Re-lock 5 minutes after leaving app
                                        else -> 1500L
                                    }

                                    if (outOfForegroundDuration >= relockThresholdMs) {
                                        AppLockSession.lockApp(unlockedApp, force = true)
                                        lastSeenForegroundTime.remove(unlockedApp)
                                        Log.d(TAG, "Auto-relocked app: $unlockedApp after $outOfForegroundDuration ms out of foreground (Policy: $relockPolicy)")
                                    }
                                }
                            }

                            // Check if this package or its family alias is locked
                            val isLocked = AppLockPackageHelper.isPackageLocked(currentApp, lockedPackages)
                            if (isLocked) {
                                val isUnlocked = AppLockSession.isUnlocked(currentApp)
                                val isRecentlyUnlocked = AppLockSession.isRecentlyUnlocked(currentApp, gracePeriodMs = 2000L)
                                val isUnlockingNow = AppLockSession.activeUnlockingPackage == currentApp
                                val isLaunchBlocked = isUnlockingNow && (System.currentTimeMillis() - lastLaunchTime > 800L)

                                if (!isUnlocked && !isRecentlyUnlocked && (!isUnlockingNow || isLaunchBlocked)) {
                                    Log.d(TAG, "Locked app detected: $currentApp. Launching unlock screen. Blocked retry: $isLaunchBlocked")
                                    launchUnlockScreen(currentApp)
                                } else if (isUnlocked) {
                                    AppLockSession.setCurrentForeground(currentApp)
                                }
                            } else {
                                AppLockSession.setCurrentForeground(currentApp)
                            }

                            // Reset activeUnlockingPackage if user navigated to a different app
                            if (AppLockSession.activeUnlockingPackage != null && currentApp != AppLockSession.activeUnlockingPackage) {
                                AppLockSession.activeUnlockingPackage = null
                            }
                        }
                    } else if (currentApp == packageName) {
                        // Unlock screen or AppLocker is currently in foreground
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in checking loop", e)
                }
                
                val nextDelay = if (AppLockAccessibilityService.isAccessibilityRunning) {
                    250L
                } else if (lockedPackages.isEmpty()) {
                    600L
                } else if (lastKnownForegroundPackage != currentApp) {
                    lastKnownForegroundPackage = currentApp
                    15L
                } else if (currentApp != null && AppLockPackageHelper.isPackageLocked(currentApp, lockedPackages)) {
                    20L
                } else {
                    40L
                }
                delay(nextDelay)
            }
        }
    }

    private fun isSystemOrTransientPackage(pkg: String?): Boolean {
        return AppLockPackageHelper.isSystemOrTransientPackage(this, pkg)
    }

    private fun getForegroundPackageName(usm: UsageStatsManager): String? {
        // 1. If Accessibility Service is actively running, prefer its 0ms verified foreground window
        if (AppLockAccessibilityService.isAccessibilityRunning) {
            val accPkg = AppLockSession.currentForegroundPackage
            if (accPkg != null && (System.currentTimeMillis() - AppLockSession.lastForegroundUpdateTime < 2500L)) {
                lastKnownForegroundPackage = accPkg
                return accPkg
            }
        }

        val endTime = System.currentTimeMillis()
        val startTime = endTime - 4000L

        val usageEvents = try {
            usm.queryEvents(startTime, endTime)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to query usage events", e)
            null
        }

        reusablePackageStates.clear()
        reusablePackageLastTime.clear()

        if (usageEvents != null) {
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(reusableEvent)
                val pkg = reusableEvent.packageName ?: continue
                val type = reusableEvent.eventType
                if (type == UsageEvents.Event.ACTIVITY_RESUMED ||
                    type == UsageEvents.Event.ACTIVITY_PAUSED ||
                    type == UsageEvents.Event.ACTIVITY_STOPPED ||
                    type == 1 || type == 2) {

                    val prevTime = reusablePackageLastTime[pkg] ?: 0L
                    if (reusableEvent.timeStamp >= prevTime) {
                        reusablePackageLastTime[pkg] = reusableEvent.timeStamp
                        reusablePackageStates[pkg] = type
                    }
                }
            }
        }

        // Find package whose latest lifecycle state is RESUMED via single-pass scan
        var topPkg: String? = null
        var maxTime = 0L
        for ((pkg, type) in reusablePackageStates) {
            if (type == UsageEvents.Event.ACTIVITY_RESUMED || type == 1) {
                val time = reusablePackageLastTime[pkg] ?: 0L
                if (time >= maxTime) {
                    maxTime = time
                    topPkg = pkg
                }
            }
        }

        if (topPkg != null) {
            lastKnownForegroundPackage = topPkg
            return topPkg
        }

        // Fallback: Query UsageStats with single-pass max evaluation
        try {
            val fallbackStart = endTime - 5000L
            val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, fallbackStart, endTime)
            if (!stats.isNullOrEmpty()) {
                var topStat: android.app.usage.UsageStats? = null
                for (stat in stats) {
                    if (topStat == null || stat.lastTimeUsed > topStat.lastTimeUsed) {
                        topStat = stat
                    }
                }
                if (topStat != null && (endTime - topStat.lastTimeUsed) < 5000) {
                    lastKnownForegroundPackage = topStat.packageName
                    return topStat.packageName
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed fallback usage stats query", e)
        }

        return lastKnownForegroundPackage
    }

    @Suppress("DEPRECATION")
    private fun launchUnlockScreen(targetPackage: String) {
        if (AppLockSession.isUnlocked(targetPackage) || AppLockSession.isRecentlyUnlocked(targetPackage, 2000L)) {
            return
        }
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
            Log.e(TAG, "Failed startActivity launch unlock screen", e)
            try {
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val bgOptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ActivityOptions.makeBasic().apply {
                        setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                    }.toBundle()
                } else null
                if (bgOptions != null) {
                    pendingIntent.send(this, 0, null, null, null, null, bgOptions)
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
