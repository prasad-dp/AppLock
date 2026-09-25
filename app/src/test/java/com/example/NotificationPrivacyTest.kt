package com.example

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import com.example.data.LockPreferences
import com.example.service.AppLockNotificationListenerService
import com.example.service.AppLockSession
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNotificationManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationPrivacyTest {

    private lateinit var context: Context
    private lateinit var prefs: LockPreferences
    private lateinit var shadowNotificationManager: ShadowNotificationManager
    private lateinit var service: AppLockNotificationListenerService

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        prefs = LockPreferences(context)
        prefs.isServiceActive = true
        prefs.isNotificationPrivacyEnabled = true
        prefs.notificationPrivacyMode = LockPreferences.NOTIF_MODE_STRICT

        AppLockSession.clearSession()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        shadowNotificationManager = shadowOf(notificationManager)
        notificationManager.cancelAll()

        service = Robolectric.buildService(AppLockNotificationListenerService::class.java).create().get()
        service.setLockedPackageForTesting("com.whatsapp")
    }

    @After
    fun tearDown() {
        AppLockSession.clearSession()
        AppLockNotificationListenerService.clearMaskedNotifications(context, "com.whatsapp")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    private fun createDummySbn(
        packageName: String,
        title: String,
        text: String,
        id: Int = 1001,
        key: String = "dummy_key_1"
    ): StatusBarNotification {
        val extras = Bundle().apply {
            putCharSequence(Notification.EXTRA_TITLE, title)
            putCharSequence(Notification.EXTRA_TEXT, text)
        }
        val notif = NotificationCompat.Builder(context, "test_channel")
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .addExtras(extras)
            .build()

        return StatusBarNotification(
            packageName,
            null,
            id,
            null,
            android.os.Process.myUid(),
            android.os.Process.myPid(),
            0,
            notif,
            android.os.Process.myUserHandle(),
            System.currentTimeMillis()
        )
    }

    @Test
    fun testMaskedNotificationInStrictShieldMode() {
        prefs.notificationPrivacyMode = LockPreferences.NOTIF_MODE_STRICT

        val sensitiveMessage = "Hey, here is your secret code 123456"
        val sbn = createDummySbn(
            packageName = "com.whatsapp",
            title = "John Doe",
            text = sensitiveMessage
        )

        service.onNotificationPosted(sbn)

        val postedNotifications = shadowNotificationManager.allNotifications
        assertEquals("Should have posted exactly 1 masked notification", 1, postedNotifications.size)

        val masked = postedNotifications.first()
        val extras = masked.extras

        // Strict Shield: app name as title, contact & text hidden
        assertEquals("WhatsApp", extras.getCharSequence(Notification.EXTRA_TITLE)?.toString())
        assertEquals("Protected", extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString())
        assertEquals("🔒 New message received", extras.getCharSequence(Notification.EXTRA_TEXT)?.toString())

        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
        assertTrue("BigText should indicate privacy shielding", bigText.contains("Sensitive content hidden for your privacy"))

        // Security check: sensitive text or sender name must NEVER appear in Strict Shield notification
        assertFalse("Sensitive text must be stripped", extras.toString().contains(sensitiveMessage))
        assertFalse("Sender name must be hidden in Strict Shield", extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() == "John Doe")

        // Action chip verification
        assertNotNull("Notification should contain action buttons", masked.actions)
        assertTrue("Action button should contain 'Unlock & View'", masked.actions.any { it.title.toString().contains("Unlock & View") })
    }

    @Test
    fun testMaskedNotificationInHideContentOnlyMode() {
        prefs.notificationPrivacyMode = LockPreferences.NOTIF_MODE_HIDE_CONTENT

        val sensitiveOtp = "Private Bank OTP: 8872"
        val sbn = createDummySbn(
            packageName = "com.whatsapp",
            title = "John Doe",
            text = sensitiveOtp
        )

        service.onNotificationPosted(sbn)

        val postedNotifications = shadowNotificationManager.allNotifications
        assertEquals("Should have posted exactly 1 masked notification", 1, postedNotifications.size)

        val masked = postedNotifications.first()
        val extras = masked.extras

        // Hide Content Only: Sender visible, text masked
        assertEquals("John Doe", extras.getCharSequence(Notification.EXTRA_TITLE)?.toString())
        assertEquals("WhatsApp • Protected", extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString())
        assertEquals("🔒 New message hidden", extras.getCharSequence(Notification.EXTRA_TEXT)?.toString())

        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
        assertTrue("BigText should mention sender", bigText.contains("New message from John Doe"))

        // Security check: sensitive text must be completely absent
        assertFalse("Sensitive OTP must not leak", extras.toString().contains(sensitiveOtp))
    }

    @Test
    fun testMultipleMessagesStackedCounter() {
        prefs.notificationPrivacyMode = LockPreferences.NOTIF_MODE_STRICT

        val sbn1 = createDummySbn("com.whatsapp", "Alice", "Message 1", id = 101)
        service.onNotificationPosted(sbn1)

        val notif1 = shadowNotificationManager.allNotifications.last()
        assertEquals("🔒 New message received", notif1.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString())

        val sbn2 = createDummySbn("com.whatsapp", "Bob", "Message 2", id = 102)
        service.onNotificationPosted(sbn2)

        val notif2 = shadowNotificationManager.allNotifications.last()
        assertEquals("🔒 2 new messages received", notif2.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString())

        val sbn3 = createDummySbn("com.whatsapp", "Charlie", "Message 3", id = 103)
        service.onNotificationPosted(sbn3)

        val notif3 = shadowNotificationManager.allNotifications.last()
        assertEquals("🔒 3 new messages received", notif3.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString())
    }

    @Test
    fun testStealthModeSuppressesNotification() {
        prefs.notificationPrivacyMode = LockPreferences.NOTIF_MODE_BLOCK

        val sbn = createDummySbn("com.whatsapp", "Boss", "Urgent meeting")
        service.onNotificationPosted(sbn)

        val postedNotifications = shadowNotificationManager.allNotifications
        assertEquals("Stealth mode should post zero notifications", 0, postedNotifications.size)
    }

    @Test
    fun testUnlockedAppAllowsNotificationsThroughWithoutMasking() {
        // App is unlocked in active session
        AppLockSession.unlockApp("com.whatsapp")

        val sbn = createDummySbn("com.whatsapp", "Friend", "Active chat message")
        service.onNotificationPosted(sbn)

        val postedNotifications = shadowNotificationManager.allNotifications
        assertEquals("Active unlocked app should not be masked", 0, postedNotifications.size)
    }

    @Test
    fun testOnAppUnlockedClearsMaskedNotifications() {
        prefs.notificationPrivacyMode = LockPreferences.NOTIF_MODE_STRICT

        val sbn = createDummySbn("com.whatsapp", "Someone", "Hello")
        service.onNotificationPosted(sbn)
        assertEquals(1, shadowNotificationManager.allNotifications.size)

        // User unlocks WhatsApp
        AppLockNotificationListenerService.onAppUnlocked(context, "com.whatsapp")

        assertEquals("Masked notification must be dismissed on unlock", 0, shadowNotificationManager.allNotifications.size)
        assertEquals("Notification count must be reset to 0", 0, AppLockNotificationListenerService.getNotificationCount("com.whatsapp"))
    }
}
