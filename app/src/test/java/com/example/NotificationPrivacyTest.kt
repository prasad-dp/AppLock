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
        AppLockNotificationListenerService.resetAllCounts()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        shadowNotificationManager = shadowOf(notificationManager)
        notificationManager.cancelAll()

        service = Robolectric.buildService(AppLockNotificationListenerService::class.java).create().get()
        service.setLockedPackageForTesting("com.whatsapp")
    }

    @After
    fun tearDown() {
        AppLockSession.clearSession()
        AppLockNotificationListenerService.resetAllCounts()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    private fun createDummySbn(
        packageName: String,
        title: String,
        text: String,
        id: Int = 1001,
        key: String = "dummy_key_1",
        category: String? = null,
        isGroupSummary: Boolean = false,
        number: Int = 0
    ): StatusBarNotification {
        val extras = Bundle().apply {
            putCharSequence(Notification.EXTRA_TITLE, title)
            putCharSequence(Notification.EXTRA_TEXT, text)
        }
        val notifBuilder = NotificationCompat.Builder(context, "test_channel")
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .addExtras(extras)

        if (category != null) {
            notifBuilder.setCategory(category)
        }
        if (isGroupSummary) {
            notifBuilder.setGroup("dummy_group").setGroupSummary(true)
        }
        if (number > 0) {
            notifBuilder.setNumber(number)
        }
        val notif = notifBuilder.build()

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

    @Test
    fun testGenericAppInStrictShieldMode() {
        service.setLockedPackageForTesting("com.application.zomato")
        prefs.notificationPrivacyMode = LockPreferences.NOTIF_MODE_STRICT

        val sbn = createDummySbn(
            packageName = "com.application.zomato",
            title = "Special 50% Off Biryani",
            text = "Order now from Paradise Biryani and save big!"
        )

        service.onNotificationPosted(sbn)

        val postedNotifications = shadowNotificationManager.allNotifications
        assertEquals(1, postedNotifications.size)

        val masked = postedNotifications.first()
        val extras = masked.extras

        // For non-messaging apps, should show "🔒 New notification received"
        assertEquals("🔒 New notification received", extras.getCharSequence(Notification.EXTRA_TEXT)?.toString())
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
        assertTrue(bigText.contains("Notification details hidden for your privacy"))
        assertFalse(extras.toString().contains("Biryani"))
    }

    @Test
    fun testGenericAppInHideContentOnlyMode() {
        service.setLockedPackageForTesting("com.rapido.passenger")
        prefs.notificationPrivacyMode = LockPreferences.NOTIF_MODE_HIDE_CONTENT

        val sbn = createDummySbn(
            packageName = "com.rapido.passenger",
            title = "Captain Assigned",
            text = "Rahul (KA-01-AB-1234) is arriving in 3 mins. OTP: 4421"
        )

        service.onNotificationPosted(sbn)

        val postedNotifications = shadowNotificationManager.allNotifications
        assertEquals(1, postedNotifications.size)

        val masked = postedNotifications.first()
        val extras = masked.extras

        // In Mode 2 for Rapido, show headline ("Captain Assigned"), mask sensitive text/OTP
        assertEquals("Captain Assigned", extras.getCharSequence(Notification.EXTRA_TITLE)?.toString())
        assertEquals("🔒 Notification details hidden", extras.getCharSequence(Notification.EXTRA_TEXT)?.toString())
        assertFalse("Sensitive OTP or vehicle plate must not leak", extras.toString().contains("4421"))
    }

    @Test
    fun testGroupSummaryIgnoredForMessageCount() {
        prefs.notificationPrivacyMode = LockPreferences.NOTIF_MODE_STRICT

        // Message 1 from WhatsApp
        val sbn1 = createDummySbn("com.whatsapp", "Alice", "Hello", id = 101, key = "whatsapp_msg_101")
        service.onNotificationPosted(sbn1)

        val notif1 = shadowNotificationManager.allNotifications.last()
        assertEquals("🔒 New message received", notif1.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString())
        assertEquals(1, AppLockNotificationListenerService.getNotificationCount("com.whatsapp"))

        // Group summary posted by WhatsApp for the same 1 message
        val summarySbn = createDummySbn("com.whatsapp", "WhatsApp", "1 new message", id = 999, key = "whatsapp_summary", isGroupSummary = true)
        service.onNotificationPosted(summarySbn)

        // Count should STILL be 1, NOT 2!
        assertEquals("Group summary must not increment notification count", 1, AppLockNotificationListenerService.getNotificationCount("com.whatsapp"))

        // Message 2 from WhatsApp
        val sbn2 = createDummySbn("com.whatsapp", "Bob", "How are you", id = 102, key = "whatsapp_msg_102")
        service.onNotificationPosted(sbn2)

        val notif2 = shadowNotificationManager.allNotifications.last()
        assertEquals("🔒 2 new messages received", notif2.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString())
        assertEquals(2, AppLockNotificationListenerService.getNotificationCount("com.whatsapp"))
    }

    @Test
    fun testSnapchatBundledCountParsed() {
        prefs.notificationPrivacyMode = LockPreferences.NOTIF_MODE_STRICT
        service.setLockedPackageForTesting("com.snapchat.android")

        // Snapchat posts single notification saying "3 new Snaps"
        val snapSbn = createDummySbn(
            packageName = "com.snapchat.android",
            title = "Snapchat",
            text = "3 new Snaps",
            id = 501,
            key = "snap_501"
        )
        service.onNotificationPosted(snapSbn)

        val notif = shadowNotificationManager.allNotifications.last()
        assertEquals("🔒 3 new messages received", notif.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString())
        assertEquals(3, AppLockNotificationListenerService.getNotificationCount("com.snapchat.android"))
    }

    @Test
    fun testSnapchatTypingNotificationIgnored() {
        prefs.notificationPrivacyMode = LockPreferences.NOTIF_MODE_STRICT
        service.setLockedPackageForTesting("com.snapchat.android")

        // 1. Typing notification
        val typingSbn = createDummySbn(
            packageName = "com.snapchat.android",
            title = "Alice",
            text = "is typing...",
            id = 502,
            key = "snap_typing_502"
        )
        service.onNotificationPosted(typingSbn)

        // Typing notification should NOT create a masked notification or increment count
        assertEquals(0, AppLockNotificationListenerService.getNotificationCount("com.snapchat.android"))

        // 2. Genuine message arrives
        val msgSbn = createDummySbn(
            packageName = "com.snapchat.android",
            title = "Alice",
            text = "Hey there!",
            id = 503,
            key = "snap_msg_503"
        )
        service.onNotificationPosted(msgSbn)

        val notif = shadowNotificationManager.allNotifications.last()
        assertEquals("🔒 New message received", notif.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString())
        assertEquals(1, AppLockNotificationListenerService.getNotificationCount("com.snapchat.android"))
    }

    @Test
    fun testNotificationRemovedDecrementsCountAndCancels() {
        prefs.notificationPrivacyMode = LockPreferences.NOTIF_MODE_STRICT

        val sbn1 = createDummySbn("com.whatsapp", "Alice", "Hello", id = 101, key = "msg_1")
        val sbn2 = createDummySbn("com.whatsapp", "Bob", "Hi", id = 102, key = "msg_2")

        service.onNotificationPosted(sbn1)
        service.onNotificationPosted(sbn2)
        assertEquals(2, AppLockNotificationListenerService.getNotificationCount("com.whatsapp"))

        // Removing 1 notification decrements count to 1
        service.onNotificationRemoved(sbn1)
        assertEquals(1, AppLockNotificationListenerService.getNotificationCount("com.whatsapp"))

        // Removing the final notification cancels the masked notification and resets count to 0
        service.onNotificationRemoved(sbn2)
        assertEquals(0, AppLockNotificationListenerService.getNotificationCount("com.whatsapp"))
    }
}
