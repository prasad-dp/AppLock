package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import android.view.inputmethod.InputMethodManager

object AppLockPackageHelper {

    private val CHROME_PACKAGE_FAMILY = setOf(
        "com.android.chrome",
        "org.chromium.chrome",
        "com.chrome.beta",
        "com.chrome.canary",
        "com.chrome.dev"
    )

    private val CAMERA_PACKAGE_FAMILY = setOf(
        "com.android.camera",
        "com.android.camera2",
        "com.google.android.GoogleCamera",
        "com.sec.android.app.camera",
        "com.samsung.android.app.camera",
        "com.oppo.camera",
        "com.vivo.camera",
        "com.motorola.cameraone",
        "com.asus.camera",
        "com.oneplus.camera",
        "com.huawei.camera",
        "com.xiaomi.camera",
        "com.transsion.camera",
        "com.realme.camera"
    )

    private val GALLERY_PACKAGE_FAMILY = setOf(
        "com.google.android.apps.photos",
        "com.sec.android.gallery3d",
        "com.miui.gallery",
        "com.coloros.gallery",
        "com.vivo.gallery",
        "com.huawei.photos",
        "com.android.gallery3d"
    )

    /**
     * Given a target package, returns all known package aliases in the same app family.
     */
    fun getPackageFamily(packageName: String): Set<String> {
        if (packageName.isEmpty()) return emptySet()
        val lower = packageName.lowercase()
        if (CHROME_PACKAGE_FAMILY.contains(lower) || lower.contains("chrome")) {
            return CHROME_PACKAGE_FAMILY + packageName
        }
        if (CAMERA_PACKAGE_FAMILY.contains(lower) || lower.contains("camera")) {
            return CAMERA_PACKAGE_FAMILY + packageName
        }
        if (GALLERY_PACKAGE_FAMILY.contains(lower) || lower.contains("gallery") || lower.contains("photos")) {
            return GALLERY_PACKAGE_FAMILY + packageName
        }
        return setOf(packageName)
    }

    /**
     * Checks if a package or any of its family aliases is contained in the set of locked packages.
     */
    fun isPackageLocked(packageName: String, lockedPackages: Set<String>): Boolean {
        if (packageName.isEmpty()) return false
        if (lockedPackages.contains(packageName)) return true
        val family = getPackageFamily(packageName)
        return family.any { lockedPackages.contains(it) }
    }

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

    private val DOMAIN_PREFIXES = setOf(
        "com", "org", "net", "edu", "gov", "mil", "int", "in", "us", "uk", "co", "io", "ai",
        "app", "dev", "de", "fr", "jp", "cn", "ru", "br", "au", "ca", "ch", "it", "nl", "se",
        "no", "es", "kr", "za", "sg", "my", "nz", "ae", "sa"
    )

    private val GENERIC_WRAPPER_TOKENS = setOf(
        "android", "app", "apps", "application", "action", "actions", "client", "clients",
        "mobile", "mob", "service", "services", "mshop", "shopping", "shop", "store", "user",
        "customer", "main", "ui", "view", "core", "base", "internal", "media", "mediaclient",
        "frontpage", "consumer", "passenger", "driver", "partner", "pro", "lite", "free",
        "beta", "alpha", "dev", "prod", "production", "release", "debug", "test", "v1", "v2", "v3",
        "system", "systemui", "software", "corp", "inc", "ltd", "tech", "technology"
    )

    private val KNOWN_BRAND_MAP = mapOf(
        "abhibus" to "AbhiBus",
        "amazon" to "Amazon",
        "whatsapp" to "WhatsApp",
        "instagram" to "Instagram",
        "telegram" to "Telegram",
        "facebook" to "Facebook",
        "katana" to "Facebook",
        "snapchat" to "Snapchat",
        "youtube" to "YouTube",
        "phonepe" to "PhonePe",
        "paytm" to "Paytm",
        "flipkart" to "Flipkart",
        "swiggy" to "Swiggy",
        "zomato" to "Zomato",
        "ubercab" to "Uber",
        "uber" to "Uber",
        "olacabs" to "Ola",
        "redbus" to "redBus",
        "hotstar" to "Hotstar",
        "netflix" to "Netflix",
        "spotify" to "Spotify",
        "truecaller" to "Truecaller",
        "twitter" to "Twitter",
        "linkedin" to "LinkedIn",
        "pinterest" to "Pinterest",
        "reddit" to "Reddit",
        "discord" to "Discord",
        "zepto" to "Zepto",
        "zeptonow" to "Zepto",
        "blinkit" to "Blinkit",
        "grofers" to "Blinkit",
        "dunzo" to "Dunzo",
        "myntra" to "Myntra",
        "ajio" to "AJIO",
        "nykaa" to "Nykaa",
        "meesho" to "Meesho",
        "rapido" to "Rapido",
        "makemytrip" to "MakeMyTrip",
        "goibibo" to "Goibibo",
        "irctc" to "IRCTC",
        "cleartrip" to "Cleartrip",
        "ixigo" to "Ixigo",
        "yatra" to "Yatra",
        "tataneu" to "Tata Neu",
        "airtel" to "Airtel",
        "myjio" to "Jio",
        "dream11" to "Dream11",
        "cred" to "CRED",
        "zerodha" to "Zerodha Kite",
        "kite" to "Zerodha Kite",
        "groww" to "Groww",
        "upstox" to "Upstox",
        "angelone" to "Angel One",
        "gmail" to "Gmail",
        "chrome" to "Chrome",
        "tiktok" to "TikTok",
        "viber" to "Viber",
        "signal" to "Signal",
        "slack" to "Slack",
        "teams" to "Microsoft Teams",
        "outlook" to "Microsoft Outlook",
        "zoom" to "Zoom"
    )

    /**
     * Sanitizes application labels so that no phone linkifiers, SMS gateways, or chat apps
     * (WhatsApp, Telegram, Signal, iMessage) can ever detect a URL or create a clickable hyperlink.
     * Strips web domains (.com, .in, etc.), delimiters, colons, slashes, and removes all dots.
     */
    fun sanitizeAppLabel(input: String): String {
        var cleaned = input
        // Remove web domain suffixes e.g. "Booking.com" -> "Booking", "Amazon.in" -> "Amazon"
        cleaned = cleaned.replace(Regex("""\.(com|in|org|net|io|co|ai|app|gov|edu|me|tv|info)\b""", RegexOption.IGNORE_CASE), "")
        // Strip subtitle / descriptor segments (e.g. "AbhiBus - Bus Tickets", "Amazon: Shop Now")
        val delimiterRegex = Regex("""[\-–—|:•(\[]""")
        val parts = cleaned.split(delimiterRegex)
        if (parts.isNotEmpty() && parts[0].trim().isNotBlank()) {
            cleaned = parts[0].trim()
        }
        // Replace all dots, slashes, colons, at-signs with spaces so it can never be linkified
        cleaned = cleaned.replace('.', ' ')
            .replace('/', ' ')
            .replace('\\', ' ')
            .replace(':', ' ')
            .replace('@', ' ')
            .replace('_', ' ')
        // Collapse whitespace
        cleaned = cleaned.replace(Regex("""\s+"""), " ").trim()
        return cleaned.ifBlank { "App Locker" }
    }

    private fun formatCandidateName(token: String): String {
        val withSpaces = token.replace(Regex("(?<=[a-z0-9])(?=[A-Z])"), " ")
            .replace('_', ' ')
            .replace('-', ' ')
        val words = withSpaces.split(Regex("""\s+""")).filter { it.isNotBlank() }
        if (words.isEmpty()) return token
        return words.joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    /**
     * Resolves a clean, brand-accurate, human-friendly application name
     * (e.g. "AbhiBus", "Amazon", "WhatsApp", "PhonePe") from any package name regardless of complexity,
     * guaranteeing no dots or URL-like patterns so messaging apps and phone linkifiers will never hyperlink it.
     */
    fun getAppLabel(context: Context, packageName: String?): String {
        if (packageName.isNullOrBlank()) return "App Locker"

        // 1. Try PackageManager to get official installed application label
        try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            val label = pm.getApplicationLabel(info)?.toString()
            if (!label.isNullOrBlank() && label != packageName) {
                return sanitizeAppLabel(label)
            }
        } catch (_: Exception) {}

        val lower = packageName.lowercase()

        // 2. High-precision compound signatures
        if (lower.contains("facebook.orca")) return "Messenger"
        if (lower.contains("whatsapp.w4b") || (lower.contains("whatsapp") && lower.contains("business"))) return "WhatsApp Business"
        if (lower.contains("paisa") || lower.contains("googlepay") || lower.contains("gpay")) return "Google Pay"
        if (lower.contains("google.android.gm")) return "Gmail"
        if (lower.contains("google.android.apps.photos")) return "Google Photos"
        if (lower.contains("google.android.apps.messaging")) return "Messages"
        if (lower.contains("googlequicksearchbox")) return "Google"

        // 3. Known brand registry matching
        for ((brandKey, brandTitle) in KNOWN_BRAND_MAP) {
            if (lower.contains(brandKey)) {
                return brandTitle
            }
        }

        // 4. Universal heuristic parser for arbitrary / complex package names
        val rawSegments = packageName.split('.')
        // Drop leading domain prefixes (e.g. "com", "in", "org", "net", "de", "uk", etc.)
        val nonDomainSegments = rawSegments.dropWhile { DOMAIN_PREFIXES.contains(it.lowercase()) }

        // Filter out generic wrapper/module tokens (e.g. "android", "app", "action", "shopping", "mshop", "client", "mobile")
        val brandCandidates = nonDomainSegments.filter {
            val segLower = it.lowercase()
            !DOMAIN_PREFIXES.contains(segLower) && !GENERIC_WRAPPER_TOKENS.contains(segLower)
        }

        val chosenCandidate = when {
            brandCandidates.isNotEmpty() -> {
                // In reverse domain notation (e.g. "com.abhibus.action", "in.amazon.mShop.android.shopping"),
                // the first non-generic token is the distinct company / brand name
                brandCandidates.first()
            }
            nonDomainSegments.isNotEmpty() -> {
                // If all tokens were deemed generic (e.g. "com.android.calculator2"), take the last non-domain segment
                nonDomainSegments.last()
            }
            else -> {
                rawSegments.lastOrNull() ?: packageName
            }
        }

        val formatted = formatCandidateName(chosenCandidate)
        return sanitizeAppLabel(formatted)
    }
}
