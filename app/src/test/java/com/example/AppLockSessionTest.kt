package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.service.AppLockSession
import com.example.util.AppLockPackageHelper
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
}
