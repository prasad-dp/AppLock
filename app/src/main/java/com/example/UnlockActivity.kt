package com.example

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.data.LockPreferences
import com.example.service.AppLockSession
import com.example.ui.pattern.PatternLockView
import com.example.ui.pattern.PatternState
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UnlockActivity : FragmentActivity() {
    private val targetPackageState = mutableStateOf<String?>(null)
    private lateinit var prefs: LockPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.filterTouchesWhenObscured = true
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
        enableEdgeToEdge()

        val initialPackage = intent.getStringExtra("EXTRA_PACKAGE_NAME")
        targetPackageState.value = initialPackage
        prefs = LockPreferences(this)

        // Security check: if pattern is not configured, unlock session immediately to prevent lockers soft-locks
        if (!prefs.hasPatternSet()) {
            initialPackage?.let { AppLockSession.unlockApp(it) }
            finishAndRemoveTask()
            return
        }

        // Loop Prevention: If the app is already unlocked or was recently unlocked, dismiss immediately
        if (initialPackage != null && (AppLockSession.isUnlocked(initialPackage) || AppLockSession.isRecentlyUnlocked(initialPackage, 3000L))) {
            finishAndRemoveTask()
            return
        }

        // Modern callback mechanism for intercepting physical back key press and back swipe gestures
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goToHome()
            }
        })

        setContent {
            MyApplicationTheme(darkTheme = prefs.isDarkMode) {
                Scaffold { innerPadding ->
                    com.example.ui.pattern.LockVerifyScreen(
                        prefs = prefs,
                        packageName = targetPackageState.value ?: "",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        onSuccess = {
                            targetPackageState.value?.let { AppLockSession.unlockApp(it) }
                            Toast.makeText(this, "Application Unlocked", Toast.LENGTH_SHORT).show()
                            finishAndRemoveTask()
                            @Suppress("DEPRECATION")
                            overridePendingTransition(0, 0)
                        },
                        onCancel = {
                            goToHome()
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val packageArg = intent.getStringExtra("EXTRA_PACKAGE_NAME")
        targetPackageState.value = packageArg

        if (packageArg != null && !prefs.hasPatternSet()) {
            AppLockSession.unlockApp(packageArg)
            finishAndRemoveTask()
            return
        }

        // Loop Prevention: If the app is already unlocked or was recently unlocked, dismiss immediately
        if (packageArg != null && (AppLockSession.isUnlocked(packageArg) || AppLockSession.isRecentlyUnlocked(packageArg, 3000L))) {
            finishAndRemoveTask()
            return
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val pkg = targetPackageState.value
        AppLockSession.markGoToHome(pkg)
        if (pkg != null && !AppLockSession.isUnlocked(pkg)) {
            AppLockSession.activeUnlockingPackage = null
        }
        if (!isFinishing) {
            finishAndRemoveTask()
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

    override fun onStop() {
        super.onStop()
        // Do NOT finish on onStop. When the screen turns off or sleeps, UnlockActivity
        // must remain intact in front of the locked app so that when the device is unlocked again,
        // the app is still shielded. Deliberate navigation away is handled by onUserLeaveHint() and onBackPressed().
    }

    override fun onDestroy() {
        super.onDestroy()
        val pkg = targetPackageState.value
        if (pkg != null && !AppLockSession.isUnlocked(pkg)) {
            if (AppLockSession.activeUnlockingPackage == pkg) {
                AppLockSession.activeUnlockingPackage = null
            }
        }
    }

    @android.annotation.SuppressLint("MissingSuperCall", "GestureBackNavigation")
    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        // Prevent bypassing the lock activity via back button. Close locked app and redirect to launcher home.
        goToHome()
    }

    private fun goToHome() {
        val pkg = targetPackageState.value
        AppLockSession.markGoToHome(pkg)
        AppLockSession.activeUnlockingPackage = null
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finishAndRemoveTask()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }
}


