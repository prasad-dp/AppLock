package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.AppRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: AppRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AppRepository(db.lockedAppDao(), db.intruderAlertDao())
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `test initial setup pin mismatch stays on confirm pin screen`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = MainActivityViewModel(app)
        viewModel.startWizard()
        viewModel.selectLockType("pin")

        assertTrue(viewModel.setupState.value is SetupState.SetFirstPin)

        // Enter initial PIN
        viewModel.handlePinEntered("1234")
        val stateAfterFirst = viewModel.setupState.value
        assertTrue(stateAfterFirst is SetupState.ConfirmPin)
        assertEquals("1234", (stateAfterFirst as SetupState.ConfirmPin).firstAttempt)

        // Enter wrong confirmation PIN
        viewModel.handlePinEntered("5678")
        val stateAfterMismatch = viewModel.setupState.value
        assertTrue(stateAfterMismatch is SetupState.ConfirmPin)
        val confirmState = stateAfterMismatch as SetupState.ConfirmPin
        assertEquals("1234", confirmState.firstAttempt)
        assertEquals("PINs do not match! Enter PIN again to confirm.", confirmState.errorMessage)

        // Enter correct confirmation PIN
        viewModel.handlePinEntered("1234")
        assertTrue(viewModel.setupState.value is SetupState.SetupSuccess)
    }

    @Test
    fun `test restart current type setup redirects to fresh first pattern screen`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = MainActivityViewModel(app)
        viewModel.startWizard()
        viewModel.selectLockType("pattern")

        assertTrue(viewModel.setupState.value is SetupState.SetFirstPattern)

        viewModel.restartCurrentTypeSetup()
        assertTrue(viewModel.setupState.value is SetupState.SetFirstPattern)
    }

    @Test
    fun `test triggerAdMobInterstitial shows interstitial dialog`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = MainActivityViewModel(app)
        assertFalse(viewModel.showAdMobInterstitialDialog.value)

        viewModel.triggerAdMobInterstitial()
        assertTrue(viewModel.showAdMobInterstitialDialog.value)

        viewModel.dismissAdMobInterstitialDialog()
        assertFalse(viewModel.showAdMobInterstitialDialog.value)
    }

    @Test
    fun `test individual app lock and unlock lifecycle`() = runBlocking {
        // 1. Verify app is not locked initially
        assertFalse(repository.isAppLocked("com.instagram.android"))

        // 2. Lock the app
        repository.lockApp("com.instagram.android", "Instagram")

        // 3. Verify it is locked now
        assertTrue(repository.isAppLocked("com.instagram.android"))

        // 4. Trace the list of locked apps
        val lockedList = repository.allLockedAppsStateFlow.first()
        assertEquals(1, lockedList.size)
        assertEquals("com.instagram.android", lockedList[0].packageName)
        assertEquals("Instagram", lockedList[0].appName)

        // 5. Unlock the app
        repository.unlockApp("com.instagram.android")

        // 6. Verify it is unlocked
        assertFalse(repository.isAppLocked("com.instagram.android"))
    }
}
