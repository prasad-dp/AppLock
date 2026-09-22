package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import android.view.inputmethod.InputMethodManager

object AppLockPackageHelper {

    @Volatile
    private var cachedImePackages: Set<String> = emptySet()
    @Volatile
    private var lastImeCacheTime = 0L

    @Volatile
    private var cachedLauncherPackages: Set<String> = emptySet()
    @Volatile
    private var lastLauncherCacheTime = 0L

    /**
     * Checks if the package represents an Input Method (Soft Keyboard).
     * Combines dynamic query of enabled input methods with extensive OEM keyboard signatures.
     */
    fun isInputMethodPackage(context: Context, pkg: String?): Boolean {
        if (pkg.isNullOrEmpty()) return false
        val now = System.currentTimeMillis()

        // Cache enabled IMEs for 15 seconds to avoid expensive IPC on every event
        if (now - lastImeCacheTime > 15_000L || cachedImePackages.isEmpty()) {
            try {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                val imes = imm?.enabledInputMethodList ?: imm?.inputMethodList
                if (imes != null) {
                    cachedImePackages = imes.map { it.packageName }.toSet()
                    lastImeCacheTime = now
                }
            } catch (_: Exception) {}
        }

        if (cachedImePackages.contains(pkg)) {
            return true
        }

        val lower = pkg.lowercase()
        return lower.contains("honeyboard") ||      // Samsung OneUI Keyboard
                lower.contains("swiftkey") ||       // Microsoft SwiftKey
                lower.contains("touchtype") ||      // SwiftKey package signature
                lower.contains("inputmethod") ||    // Standard IME naming
                lower.contains("keyboard") ||       // Standard keyboard naming
                lower.contains(".ime") ||           // IME package suffix
                lower.endsWith("ime") ||
                lower.contains("fleksy") ||         // Fleksy
                lower.contains("swype") ||          // Swype
                lower.contains("facemoji") ||       // Facemoji
                lower.contains("kika") ||           // Kika
                lower.contains("sogou") ||          // Sogou
                lower.contains("baidu.input") ||    // Baidu OEM keyboards
                lower.contains("chamelephon") ||
                lower.contains("board")             // Generic board keyboards
    }

    /**
     * Checks if the package represents a device Launcher / Home Screen.
     */
    fun isLauncherPackage(context: Context, pkg: String?): Boolean {
        if (pkg.isNullOrEmpty()) return false
        val now = System.currentTimeMillis()

        if (now - lastLauncherCacheTime > 30_000L || cachedLauncherPackages.isEmpty()) {
            try {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                }
                val resolveInfos = context.packageManager.queryIntentActivities(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                cachedLauncherPackages = resolveInfos.mapNotNull { it.activityInfo?.packageName }.toSet()
                lastLauncherCacheTime = now
            } catch (_: Exception) {}
        }

        if (cachedLauncherPackages.contains(pkg)) {
            return true
        }

        val lower = pkg.lowercase()
        return lower.contains("launcher") ||
                lower.contains("home") ||
                lower == "com.sec.android.app.launcher" ||
                lower == "com.google.android.apps.nexuslauncher" ||
                lower == "com.miui.home" ||
                lower == "com.huawei.android.launcher" ||
                lower == "com.oppo.launcher" ||
                lower == "com.bbk.launcher2" ||
                lower == "com.transsion.hilauncher"
    }

    /**
     * Identifies transient windows, system overlays, soft keyboards, or system dialogs
     * that should NOT count as leaving the current protected application.
     */
    fun isSystemOrTransientPackage(context: Context, pkg: String?): Boolean {
        if (pkg.isNullOrEmpty()) return true
        if (pkg == context.packageName) return true

        // First check if it is a soft keyboard
        if (isInputMethodPackage(context, pkg)) {
            return true
        }

        val lower = pkg.lowercase()
        return lower == "android" ||
                lower == "com.android.systemui" ||
                lower.endsWith(".systemui") ||
                lower.contains("systemui") ||
                lower == "com.google.android.gms" ||
                lower == "com.google.android.packageinstaller" ||
                lower == "com.android.packageinstaller" ||
                lower == "com.android.vending" || // Google Play Store in-app billing / review dialogs
                lower.contains("permissioncontroller") ||
                lower.contains("intentresolver") || // Android Sharesheet
                lower.contains("documentsui") || // Storage / document picker
                lower.contains("media.module") || // Android photo picker
                lower.contains("printspooler") ||
                lower.contains("biometric") ||
                lower.contains("fingerprint") ||
                lower.contains("faceunlock") ||
                lower.contains("keyguard") ||
                lower.contains("credentials") ||
                lower.contains("confirm_device_credential") ||
                lower.contains("autofill") ||
                lower.contains("passkey") ||
                lower.contains("passwordmanager") ||
                lower.contains("onepassword") ||
                lower.contains("bitwarden") ||
                lower.contains("lastpass") ||
                lower.contains("dashlane") ||
                lower.contains("clipboard")
    }

    /**
     * Inspects the accessibility event and its window type to determine if it is
     * an input method, system window, or accessibility overlay.
     */
    fun isTransientAccessibilityWindow(event: AccessibilityEvent?): Boolean {
        if (event == null) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                val window = event.source?.window
                if (window != null) {
                    val windowType = window.type
                    if (windowType == AccessibilityWindowInfo.TYPE_INPUT_METHOD ||
                        windowType == AccessibilityWindowInfo.TYPE_SYSTEM ||
                        windowType == AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY
                    ) {
                        return true
                    }
                }
            } catch (_: Exception) {}
        }
        return false
    }

    /**
     * Verifies whether the target package is ACTUALLY the active foreground application window.
     * Prevents lock screen popups during Recents swiping, app destruction, snapshot removal, or background cleanup.
     */
    fun isAppTargetInActiveForeground(
        service: android.accessibilityservice.AccessibilityService,
        targetPackage: String
    ): Boolean {
        if (targetPackage.isEmpty()) return false
        if (targetPackage == service.packageName) return false

        // 1. Check active root node package
        val activeRootPkg = try {
            service.rootInActiveWindow?.packageName?.toString()
        } catch (_: Exception) { null }

        if (!activeRootPkg.isNullOrEmpty()) {
            if (activeRootPkg == targetPackage) {
                return true
            }
            if (isLauncherPackage(service, activeRootPkg) || isSystemOrTransientPackage(service, activeRootPkg)) {
                // Active foreground window is Launcher, SystemUI, or Recents -> target app is NOT in foreground!
                return false
            }
            // Active window belongs to a different app entirely
            if (activeRootPkg != service.packageName && activeRootPkg != targetPackage) {
                return false
            }
        }

        // 2. Inspect active interactive windows
        try {
            val windows = service.windows
            if (!windows.isNullOrEmpty()) {
                for (window in windows) {
                    if (window.isFocused || window.isActive) {
                        val windowPkg = window.root?.packageName?.toString() ?: continue
                        if (windowPkg == targetPackage) {
                            return true
                        }
                        if (isLauncherPackage(service, windowPkg) || isSystemOrTransientPackage(service, windowPkg)) {
                            return false
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        return activeRootPkg == null || activeRootPkg == targetPackage
    }
}
