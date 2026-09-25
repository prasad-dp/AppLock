package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.AppIconCache
import com.example.R
import com.example.UnlockActivity
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.LockPreferences
import com.example.util.AppLockPackageHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Premium Privacy Notification Service for protected applications.
 * Intercepts notifications from locked apps and displays a high-fidelity, polished,
 * privacy-shielded placeholder with authentic app branding and zero sensitive data leakage.
 */
class AppLockNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val lockedPackages = mutableSetOf<String>()
    private lateinit var prefs: LockPreferences
    private lateinit var repository: AppRepository
    private lateinit var notificationManager: NotificationManager

    companion object {
        private const val TAG = "NotifPrivacyService"
        const val CHANNEL_ID = "applock_notification_protection"
        const val NOTIF_TAG_PREFIX = "masked_"

        private val notificationCountMap = ConcurrentHashMap<String, Int>()
        private val pendingOriginalIntents = ConcurrentHashMap<String, PendingIntent>()

        @Volatile
        var isServiceConnected = false
            private set

        /**
         * Clears masked privacy notifications for a given package once the user has unlocked the app.
         */
        fun clearMaskedNotifications(context: Context, packageName: String) {
            try {
                notificationCountMap.remove(packageName)
                pendingOriginalIntents.remove(packageName)
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                nm?.cancel(NOTIF_TAG_PREFIX + packageName, packageName.hashCode())
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing masked notification for $packageName", e)
            }
        }

        /**
         * Called when the user unlocks via the notification unlock screen.
         * Dismisses the masked notification and smoothly directs the user into the target app
         * or the specific chat/message intent.
         */
        fun onAppUnlocked(context: Context, packageName: String) {
            val originalIntent = pendingOriginalIntents[packageName]
            clearMaskedNotifications(context, packageName)

            if (originalIntent != null) {
                try {
                    originalIntent.send()
                    return
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to launch original notification pending intent", e)
                }
            }

            // Fallback: launch target package main launcher intent
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch target app $packageName", e)
            }
        }

        fun getNotificationCount(packageName: String): Int = notificationCountMap[packageName] ?: 0
    }

    fun setLockedPackageForTesting(packageName: String) {
        synchronized(lockedPackages) {
            lockedPackages.add(packageName)
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = LockPreferences(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        val database = AppDatabase.getInstance(this)
        repository = AppRepository(database.lockedAppDao(), database.intruderAlertDao())

        // Sync locked packages list
        serviceScope.launch(Dispatchers.IO) {
            try {
                val initialList = repository.getAllLockedApps().filter { it.isLocked }.map { it.packageName }
                synchronized(lockedPackages) {
                    lockedPackages.addAll(initialList)
                }
                Log.d(TAG, "Initial locked packages loaded: ${lockedPackages.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load initial locked packages", e)
            }
        }

        serviceScope.launch {
            repository.allLockedAppsStateFlow.collect { list ->
                val packages = list.filter { it.isLocked }.map { it.packageName }
                synchronized(lockedPackages) {
                    lockedPackages.clear()
                    lockedPackages.addAll(packages)
                }
                Log.d(TAG, "Locked packages sync update: ${lockedPackages.size}")
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isServiceConnected = true
        Log.d(TAG, "NotificationListenerService connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isServiceConnected = false
        Log.d(TAG, "NotificationListenerService disconnected")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceConnected = false
        serviceScope.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val targetPkg = sbn.packageName ?: return

        // 1. Never intercept App Locker's own notifications (prevents infinite loops)
        if (targetPkg == packageName) return

        // 2. Ensure global protection and notification privacy feature are active
        if (!prefs.isServiceActive || !prefs.isNotificationPrivacyEnabled) return

        // 3. Verify if target package is in the locked apps list
        val isLocked = synchronized(lockedPackages) { targetPkg in lockedPackages }
        if (!isLocked) return

        // 4. If the target app is actively unlocked by the user, allow real-time notifications to pass through
        if (AppLockSession.isUnlocked(targetPkg)) return

        val notification = sbn.notification ?: return

        // 5. Never cancel ongoing background services, foreground tasks, music players, phone calls, or alarms
        val isOngoing = sbn.isOngoing ||
                (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0 ||
                (notification.flags and Notification.FLAG_FOREGROUND_SERVICE) != 0
        if (isOngoing) return

        val category = notification.category
        if (category == Notification.CATEGORY_CALL || category == Notification.CATEGORY_ALARM) return

        val mode = prefs.notificationPrivacyMode

        // 6. Mode: Stealth / Block completely
        if (mode == LockPreferences.NOTIF_MODE_BLOCK) {
            try { cancelNotification(sbn.key) } catch (_: Exception) {}
            return
        }

        // 7. Retain original intent for seamless transition after user unlocks
        if (notification.contentIntent != null) {
            pendingOriginalIntents[targetPkg] = notification.contentIntent
        }

        // Cancel original notification containing sensitive text/media
        try { cancelNotification(sbn.key) } catch (_: Exception) {}

        // Track accumulated notification counts per package
        val count = (notificationCountMap[targetPkg] ?: 0) + 1
        notificationCountMap[targetPkg] = count

        // 8. Resolve app metadata and visual assets
        val appLabel = AppLockPackageHelper.getAppLabel(this, targetPkg)
        val extras = notification.extras
        val origTitle = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim() ?: ""

        val appIconBitmap = try {
            val drawable = AppIconCache.get(targetPkg) ?: packageManager.getApplicationIcon(targetPkg)
            drawableToBitmap(drawable)
        } catch (_: Exception) {
            null
        }

        val displayTitle: String
        val shortContentText: String
        val expandedBigText: String
        val subTextHeader: String

        if (mode == LockPreferences.NOTIF_MODE_HIDE_CONTENT && origTitle.isNotBlank() && !origTitle.equals(appLabel, ignoreCase = true)) {
            // Mode 2: Show Sender Name, Conceal Content
            displayTitle = origTitle
            subTextHeader = "$appLabel • Protected"

            if (count > 1) {
                shortContentText = "🔒 $count new messages • Tap to view"
                expandedBigText = "🔒 $count new messages from $origTitle.\nTap to authenticate with App Locker and open conversation."
            } else {
                shortContentText = "🔒 New message hidden"
                expandedBigText = "🔒 New message from $origTitle.\nTap to authenticate with App Locker and open conversation."
            }
        } else {
            // Mode 1 (Strict Shield): Conceal both Sender Name & Content
            displayTitle = appLabel
            subTextHeader = "Protected"

            if (count > 1) {
                shortContentText = "🔒 $count new messages received"
                expandedBigText = "🔒 $count new messages hidden for your privacy.\nTap to authenticate with App Locker and view in $appLabel."
            } else {
                shortContentText = "🔒 New message received"
                expandedBigText = "🔒 Sensitive content hidden for your privacy.\nTap to authenticate with App Locker and view in $appLabel."
            }
        }

        // Create PendingIntent to launch UnlockActivity directly to unlock target package
        val unlockIntent = Intent(this, UnlockActivity::class.java).apply {
            putExtra("EXTRA_PACKAGE_NAME", targetPkg)
            putExtra("EXTRA_FROM_NOTIFICATION", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            targetPkg.hashCode(),
            unlockIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val maskedBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(displayTitle)
            .setContentText(shortContentText)
            .setSubText(subTextHeader)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(displayTitle)
                    .setSummaryText(subTextHeader)
                    .bigText(expandedBigText)
            )
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_lock_lock,
                "🔓 Unlock & View",
                pendingIntent
            )
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(true)
            .setWhen(sbn.postTime)
            .setNumber(count)
            .setColor(0xFF006C4C.toInt()) // Security Emerald Accent
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (appIconBitmap != null) {
            maskedBuilder.setLargeIcon(appIconBitmap)
        }

        notificationManager.notify(NOTIF_TAG_PREFIX + targetPkg, targetPkg.hashCode(), maskedBuilder.build())
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null && !drawable.bitmap.isRecycled) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceIn(48, 192) else 128
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceIn(48, 192) else 128
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Protected App Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Privacy-shielded notifications for locked applications"
                setShowBadge(true)
                enableVibration(false)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
