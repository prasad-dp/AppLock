package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.service.AppLockSession
import com.example.util.AppLockPackageHelper
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppLockSessionTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        AppLockSession.clearSession()
    }

    @After
    fun tearDown() {
        AppLockSession.clearSession()
    }

    @Test
    fun testUnlockAndGracePeriodProtection() {
        val testPkg = "com.snapchat.android"

        assertFalse(AppLockSession.isUnlocked(testPkg))

        // Unlock Snapchat
        AppLockSession.unlockApp(testPkg)
        assertTrue(AppLockSession.isUnlocked(testPkg))
        assertTrue(AppLockSession.isRecentlyUnlocked(testPkg, gracePeriodMs = 2500L))

        // Non-forced lockApp during grace period should be ignored
        AppLockSession.lockApp(testPkg, force = false)
        assertTrue("App should remain unlocked during post-unlock transition grace period", AppLockSession.isUnlocked(testPkg))

        // Forced lockApp should immediately lock
        AppLockSession.lockApp(testPkg, force = true)
        assertFalse("Forced lockApp should revoke unlock session", AppLockSession.isUnlocked(testPkg))
    }

    @Test
    fun testKeyboardDetection() {
        // Test Samsung OneUI Honeyboard
        assertTrue(
            "Samsung Honeyboard must be identified as Input Method",
            AppLockPackageHelper.isInputMethodPackage(context, "com.samsung.android.honeyboard")
        )

        // Test Microsoft SwiftKey
        assertTrue(
            "SwiftKey must be identified as Input Method",
            AppLockPackageHelper.isInputMethodPackage(context, "com.touchtype.swiftkey")
        )

        // Test Gboard
        assertTrue(
            "Gboard must be identified as Input Method",
            AppLockPackageHelper.isInputMethodPackage(context, "com.google.android.inputmethod.latin")
        )

        // Test user application should NOT be identified as Input Method
        assertFalse(
            "Snapchat must not be identified as Input Method",
            AppLockPackageHelper.isInputMethodPackage(context, "com.snapchat.android")
        )
    }

    @Test
    fun testSystemAndTransientPackageDetection() {
        // System UI
        assertTrue(AppLockPackageHelper.isSystemOrTransientPackage(context, "com.android.systemui"))
        assertTrue(AppLockPackageHelper.isSystemOrTransientPackage(context, "android"))

        // Google Play Services / Permission controller
        assertTrue(AppLockPackageHelper.isSystemOrTransientPackage(context, "com.google.android.gms"))
        assertTrue(AppLockPackageHelper.isSystemOrTransientPackage(context, "com.google.android.permissioncontroller"))

        // Keyboards through transient check
        assertTrue(AppLockPackageHelper.isSystemOrTransientPackage(context, "com.samsung.android.honeyboard"))
        assertTrue(AppLockPackageHelper.isSystemOrTransientPackage(context, "com.touchtype.swiftkey"))

        // Media pickers and Document UI
        assertTrue(AppLockPackageHelper.isSystemOrTransientPackage(context, "com.android.documentsui"))
        assertTrue(AppLockPackageHelper.isSystemOrTransientPackage(context, "com.google.android.providers.media.module"))
        assertTrue(AppLockPackageHelper.isSystemOrTransientPackage(context, "com.android.vending"))

        // Standard user app should NOT be transient
        assertFalse(AppLockPackageHelper.isSystemOrTransientPackage(context, "com.snapchat.android"))
    }

    @Test
    fun testCircuitBreakerLoopProtection() {
        val testPkg = "com.snapchat.android"

        // 1. When app is unlocked, shouldThrottleLaunch must return true
        AppLockSession.unlockApp(testPkg)
        assertTrue(
            "Launch must be throttled if app is currently unlocked",
            AppLockSession.shouldThrottleLaunch(testPkg)
        )

        // 2. Lock app with force
        AppLockSession.lockApp(testPkg, force = true)

        // Since it was just unlocked < 3000ms ago, shouldThrottleLaunch must still protect it from rapid re-trigger
        assertTrue(
            "Launch must be throttled during post-unlock cooldown",
            AppLockSession.shouldThrottleLaunch(testPkg)
        )

        // Clear session completely
        AppLockSession.clearSession()

        // 3. Clean initial launch should be allowed
        assertFalse(
            "Initial launch should not be throttled",
            AppLockSession.shouldThrottleLaunch(testPkg)
        )

        // Second launch in rapid succession
        assertFalse(
            "Second launch allowed under normal threshold",
            AppLockSession.shouldThrottleLaunch(testPkg)
        )

        // Third launch within 4 seconds trips the circuit breaker!
        assertTrue(
            "Third rapid launch must trip the circuit breaker and be throttled",
            AppLockSession.shouldThrottleLaunch(testPkg)
        )
    }

    @Test
fun testForegroundPackageTracking() {
        assertNull(AppLockSession.currentForegroundPackage)
        assertEquals(0L, AppLockSession.lastForegroundUpdateTime)

        AppLockSession.updateForegroundPackage("com.snapchat.android")
        assertEquals("com.snapchat.android", AppLockSession.currentForegroundPackage)
        assertTrue(AppLockSession.lastForegroundUpdateTime > 0L)

        AppLockSession.clearSession()
        assertNull(AppLockSession.currentForegroundPackage)
        assertEquals(0L, AppLockSession.lastForegroundUpdateTime)
    }

    @Test
    fun testLauncherPackageDetection() {
        assertTrue(AppLockPackageHelper.isLauncherPackage(context, "com.google.android.apps.nexuslauncher"))
        assertTrue(AppLockPackageHelper.isLauncherPackage(context, "com.sec.android.app.launcher"))
        assertTrue(AppLockPackageHelper.isLauncherPackage(context, "com.miui.home"))
        assertFalse(AppLockPackageHelper.isLauncherPackage(context, "com.snapchat.android"))
        assertFalse(AppLockPackageHelper.isLauncherPackage(context, "com.grofers.customerapp"))
    }

    @Test
    fun testPerAppRelockPreferencesMap() {
        val prefs = com.example.data.LockPreferences(context)
        val testPkg = "com.android.chrome"

        prefs.setPerAppRelockTimeout(testPkg, "30_sec")
        assertEquals("30_sec", prefs.getPerAppRelockTimeout(testPkg))

        val allTimeouts = prefs.getAllPerAppRelockTimeouts()
        assertEquals("30_sec", allTimeouts[testPkg])

        // Setting to null or global clears it
        prefs.setPerAppRelockTimeout(testPkg, null)
        assertNull(prefs.getPerAppRelockTimeout(testPkg))
        assertNull(prefs.getAllPerAppRelockTimeouts()[testPkg])
    }

    @Test
    fun testGoToHomeTransitionProtection() {
        val testPkg = "com.snapchat.android"

        // Initially not exiting to home
        assertFalse(AppLockSession.isExitingToHome(testPkg))

        // Mark go to home from Snapchat
        AppLockSession.markGoToHome(testPkg)
        assertTrue("Should be exiting to home for Snapchat", AppLockSession.isExitingToHome(testPkg))
        assertTrue("Should also be exiting to home generally", AppLockSession.isExitingToHome(null))

        // Different package check
        assertFalse("Should not match exiting to home for a different package", AppLockSession.isExitingToHome("com.instagram.android"))

        // Clear go to home when landing on Launcher
        AppLockSession.clearGoToHome()
        assertFalse("Should no longer be exiting to home after landing on launcher", AppLockSession.isExitingToHome(testPkg))
    }

    @Test
    fun testGenuineAppEntryNavigation() {
        val launcherPkg = "com.sec.android.app.launcher"
        val whatsAppPkg = "com.whatsapp"

        // 1. Initial transition from Launcher into WhatsApp is a GENUINE entry
        AppLockSession.setCurrentForeground(launcherPkg)
        assertTrue(
            "Transitioning from Launcher into WhatsApp must be detected as genuine app entry",
            AppLockSession.isGenuineAppEntry(whatsAppPkg)
        )

        // 2. Set current foreground to WhatsApp
        AppLockSession.setCurrentForeground(whatsAppPkg)

        // 3. Navigation inside WhatsApp (activity changes, back presses) must NOT be genuine new entry
        assertFalse(
            "Internal activity changes or back press inside WhatsApp must NOT be flagged as genuine new entry",
            AppLockSession.isGenuineAppEntry(whatsAppPkg)
        )

        // 4. Pressing back to exit to Launcher
        AppLockSession.setCurrentForeground(launcherPkg)

        // 5. Re-entering WhatsApp from Launcher is again a genuine new entry
        assertTrue(
            "Re-entering WhatsApp from Launcher after exiting must be detected as genuine entry",
            AppLockSession.isGenuineAppEntry(whatsAppPkg)
        )
    }

    @Test
    fun testGetAppLabel() {
        assertEquals("App Locker", AppLockPackageHelper.getAppLabel(context, null))
        assertEquals("App Locker", AppLockPackageHelper.getAppLabel(context, ""))

        // High profile / Indian brand resolution
        val abhibusLabel = AppLockPackageHelper.getAppLabel(context, "com.abhibus.action")
        assertFalse("App label should not contain dots causing URL hyperlinks", abhibusLabel.contains("."))
        assertEquals("AbhiBus", abhibusLabel)

        val amazonLabel = AppLockPackageHelper.getAppLabel(context, "in.amazon.mShop.android.shopping")
        assertFalse("App label should not contain dots causing URL hyperlinks", amazonLabel.contains("."))
        assertEquals("Amazon", amazonLabel)

        val phonePeLabel = AppLockPackageHelper.getAppLabel(context, "com.phonepe.app")
        assertFalse("App label should not contain dots causing URL hyperlinks", phonePeLabel.contains("."))
        assertEquals("PhonePe", phonePeLabel)

        val whatsAppLabel = AppLockPackageHelper.getAppLabel(context, "com.whatsapp")
        assertEquals("WhatsApp", whatsAppLabel)

        // Complex unknown package: resolves brand name while discarding domain prefix ("com") and generic suffix ("action")
        val complexBrandLabel = AppLockPackageHelper.getAppLabel(context, "com.samplebrand.action")
        assertFalse("App label should not contain dots causing URL hyperlinks", complexBrandLabel.contains("."))
        assertEquals("Samplebrand", complexBrandLabel)

        // Multi-word camelCase unknown package with multiple generic suffixes
        val multiWordLabel = AppLockPackageHelper.getAppLabel(context, "in.smartMobility.client.shopping")
        assertFalse("App label should not contain dots causing URL hyperlinks", multiWordLabel.contains("."))
        assertEquals("Smart Mobility", multiWordLabel)

        // Label sanitization: strips domain extensions and guarantees dotless non-clickable text
        val sanitizedDomain = AppLockPackageHelper.sanitizeAppLabel("Booking.com - Hotel Bookings")
        assertFalse("Sanitized label must not contain dots", sanitizedDomain.contains("."))
        assertEquals("Booking", sanitizedDomain)
    }

    @Test
    fun testNotificationPrivacyPreferences() {
        val prefs = com.example.data.LockPreferences(context)

        // Default should be false
        prefs.isNotificationPrivacyEnabled = false
        assertFalse(prefs.isNotificationPrivacyEnabled)

        prefs.isNotificationPrivacyEnabled = true
        assertTrue(prefs.isNotificationPrivacyEnabled)

        // Test privacy modes
        assertEquals(com.example.data.LockPreferences.NOTIF_MODE_STRICT, prefs.notificationPrivacyMode)

        prefs.notificationPrivacyMode = com.example.data.LockPreferences.NOTIF_MODE_HIDE_CONTENT
        assertEquals(com.example.data.LockPreferences.NOTIF_MODE_HIDE_CONTENT, prefs.notificationPrivacyMode)

        prefs.notificationPrivacyMode = com.example.data.LockPreferences.NOTIF_MODE_BLOCK
        assertEquals(com.example.data.LockPreferences.NOTIF_MODE_BLOCK, prefs.notificationPrivacyMode)
    }

    @Test
    fun testNotificationListenerSettingsIntent() {
        val intent = com.example.util.PermissionUtils.getNotificationListenerSettingsIntent()
        assertEquals(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS, intent.action)
        assertTrue((intent.flags and android.content.Intent.FLAG_ACTIVITY_NEW_TASK) != 0)
    }
}
