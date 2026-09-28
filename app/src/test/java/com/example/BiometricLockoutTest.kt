package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.LockPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BiometricLockoutTest {

    private lateinit var context: Context
    private lateinit var prefs: LockPreferences

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("app_lock_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        prefs = LockPreferences(context)
    }

    @Test
    fun `test lockout on one app does not affect other apps in preferences`() {
        val ajioPkg = "com.ril.ajio"
        val whatsappPkg = "com.whatsapp"

        // Simulate 3 wrong attempts on Ajio triggering lockout
        prefs.setFailedAttempts(ajioPkg, 3)
        val lockoutEnd = System.currentTimeMillis() + 30_000L
        prefs.setLockoutEndTimestamp(ajioPkg, lockoutEnd)

        // Verify Ajio is locked out
        assertEquals(3, prefs.getFailedAttempts(ajioPkg))
        assertEquals(lockoutEnd, prefs.getLockoutEndTimestamp(ajioPkg))

        // Verify WhatsApp is NOT locked out
        assertEquals(0, prefs.getFailedAttempts(whatsappPkg))
        assertEquals(0L, prefs.getLockoutEndTimestamp(whatsappPkg))
    }

    @Test
    fun `test clearAllLockouts cleans both package and global state`() {
        val ajioPkg = "com.ril.ajio"

        prefs.setFailedAttempts(ajioPkg, 3)
        prefs.setLockoutEndTimestamp(ajioPkg, System.currentTimeMillis() + 30_000L)
        prefs.setFailedAttempts("", 2)
        prefs.setLockoutEndTimestamp("", System.currentTimeMillis() + 15_000L)

        // Clear upon successful unlock
        prefs.clearAllLockouts(ajioPkg)

        assertEquals(0, prefs.getFailedAttempts(ajioPkg))
        assertEquals(0L, prefs.getLockoutEndTimestamp(ajioPkg))
        assertEquals(0, prefs.getFailedAttempts(""))
        assertEquals(0L, prefs.getLockoutEndTimestamp(""))
    }

    @Test
    fun `test biometric lockout timestamp tracking`() {
        assertEquals(0L, prefs.biometricLockoutEndTimestamp)

        val cooldownEnd = System.currentTimeMillis() + 30_000L
        prefs.biometricLockoutEndTimestamp = cooldownEnd
        assertEquals(cooldownEnd, prefs.biometricLockoutEndTimestamp)

        // Reset cooldown
        prefs.biometricLockoutEndTimestamp = 0L
        assertEquals(0L, prefs.biometricLockoutEndTimestamp)
    }
}
