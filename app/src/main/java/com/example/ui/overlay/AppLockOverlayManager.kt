package com.example.ui.overlay

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.data.LockPreferences
import com.example.service.AppLockSession
import com.example.ui.pattern.LockVerifyScreen
import com.example.ui.theme.MyApplicationTheme

object AppLockOverlayManager {
    private const val TAG = "AppLockOverlayManager"
    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var overlayView: ComposeView? = null
    @Volatile
    private var overlayLifecycleOwner: OverlayLifecycleOwner? = null
    @Volatile
    private var activePackage: String? = null

    val isOverlayShowing: Boolean
        get() = overlayView != null

    fun showOverlay(
        context: Context,
        packageName: String,
        onSuccess: () -> Unit = {},
        onCancel: () -> Unit = {}
    ) {
        mainHandler.post {
            try {
                if (activePackage == packageName && overlayView != null) {
                    return@post
                }
                
                // Dismiss any existing overlay first
                dismissOverlay(context)

                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                if (windowManager == null) {
                    Log.e(TAG, "WindowManager unavailable")
                    return@post
                }

                if (!com.example.util.PermissionUtils.hasOverlayPermission(context)) {
                    Log.w(TAG, "Overlay permission not granted! Falling back to activity launch.")
                    launchFallbackActivity(context, packageName)
                    return@post
                }

                val prefs = LockPreferences(context)
                if (!prefs.hasPatternSet()) {
                    prefs.savedPasscode = "1234"
                    prefs.lockType = "pin"
                }

                val lifecycleOwner = OverlayLifecycleOwner().apply {
                    onCreate()
                    onResume()
                }
                overlayLifecycleOwner = lifecycleOwner

                val composeView = ComposeView(context).apply {
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
                    setViewTreeLifecycleOwner(lifecycleOwner)
                    setViewTreeViewModelStoreOwner(lifecycleOwner)
                    setViewTreeSavedStateRegistryOwner(lifecycleOwner)

                    setContent {
                        MyApplicationTheme(darkTheme = prefs.isDarkMode) {
                            LockVerifyScreen(
                                prefs = prefs,
                                packageName = packageName,
                                onSuccess = {
                                    AppLockSession.unlockApp(packageName)
                                    dismissOverlay(context)
                                    onSuccess()
                                },
                                onCancel = {
                                    AppLockSession.markGoToHome(packageName)
                                    dismissOverlay(context)
                                    try {
                                        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                            addCategory(Intent.CATEGORY_HOME)
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        }
                                        context.startActivity(homeIntent)
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Failed to launch home intent from overlay", e)
                                    }
                                    onCancel()
                                }
                            )
                        }
                    }
                }

                val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    layoutType,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_FULLSCREEN or
                            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.CENTER
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                    }
                }

                windowManager.addView(composeView, params)
                overlayView = composeView
                activePackage = packageName
                AppLockSession.activeUnlockingPackage = packageName
                AppLockSession.recordLaunchAttempt(packageName)
                Log.d(TAG, "Successfully attached WindowManager overlay for $packageName")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to attach WindowManager overlay, attempting activity fallback", e)
                launchFallbackActivity(context, packageName)
            }
        }
    }

    fun dismissOverlay(context: Context? = null) {
        mainHandler.post {
            try {
                val viewToDismiss = overlayView
                if (viewToDismiss != null) {
                    val ctx = context ?: viewToDismiss.context
                    val windowManager = ctx.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                    windowManager?.removeViewImmediate(viewToDismiss)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error dismissing WindowManager overlay", e)
            } finally {
                overlayLifecycleOwner?.onDestroy()
                overlayLifecycleOwner = null
                overlayView = null
                val pkg = activePackage
                activePackage = null
                if (pkg != null && AppLockSession.activeUnlockingPackage == pkg) {
                    AppLockSession.activeUnlockingPackage = null
                }
            }
        }
    }

    private fun launchFallbackActivity(context: Context, packageName: String) {
        try {
            val intent = Intent(context, com.example.UnlockActivity::class.java).apply {
                putExtra("EXTRA_PACKAGE_NAME", packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Fallback activity launch failed", e)
        }
    }
}
