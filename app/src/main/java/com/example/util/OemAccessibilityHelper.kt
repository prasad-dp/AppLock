package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast

/**
 * OEM Brand Identification & Navigation Helper for Accessibility Settings.
 * Provides custom breadcrumb paths tailored to major Android skins (One UI, OxygenOS, MIUI/HyperOS, Funtouch OS, etc.)
 * and handles Android 13+ (API 33+) Restricted Settings guidance for Play Store compliance and smooth UX.
 */
enum class OemBrand(
    val displayName: String,
    val skinName: String,
    val steps: List<String>,
    val specialNote: String? = null
) {
    SAMSUNG(
        displayName = "Samsung",
        skinName = "One UI",
        steps = listOf(
            "Open Settings",
            "Tap 'Accessibility'",
            "Tap 'Installed apps' (or 'Downloaded services')",
            "Find 'App Locker' and toggle it ON"
        )
    ),
    ONEPLUS_OPPO_REALME(
        displayName = "OnePlus / OPPO / Realme",
        skinName = "OxygenOS / ColorOS / Realme UI",
        steps = listOf(
            "Open Settings",
            "Tap 'Additional settings' (or 'System settings')",
            "Tap 'Accessibility'",
            "Tap 'Downloaded apps' -> 'App Locker'",
            "Toggle Switch to ON"
        )
    ),
    XIAOMI_REDMI_POCO(
        displayName = "Xiaomi / Redmi / POCO",
        skinName = "MIUI / HyperOS",
        steps = listOf(
            "Open Settings",
            "Tap 'Additional settings'",
            "Tap 'Accessibility'",
            "Tap 'Downloaded apps' -> 'App Locker'",
            "Turn switch ON & acknowledge 10s security prompt"
        ),
        specialNote = "MIUI/HyperOS requires confirming a 10-second security countdown prompt."
    ),
    VIVO_IQOO(
        displayName = "Vivo / iQOO",
        skinName = "Funtouch OS / OriginOS",
        steps = listOf(
            "Open Settings",
            "Tap 'Shortcuts & accessibility'",
            "Tap 'Accessibility'",
            "Tap 'Downloaded apps' (or 'Installed services')",
            "Select 'App Locker' and toggle ON"
        )
    ),
    MOTOROLA(
        displayName = "Motorola",
        skinName = "My UX / Hello UI",
        steps = listOf(
            "Open Settings",
            "Tap 'Accessibility'",
            "Tap 'Downloaded apps' (or 'Installed services')",
            "Select 'App Locker' and turn switch ON"
        )
    ),
    HUAWEI_HONOR(
        displayName = "Huawei / Honor",
        skinName = "EMUI / MagicOS",
        steps = listOf(
            "Open Settings",
            "Tap 'Accessibility features' -> 'Accessibility'",
            "Tap 'Installed services' -> 'App Locker'",
            "Turn switch ON"
        )
    ),
    PIXEL_STOCK(
        displayName = "Google Pixel / Stock Android",
        skinName = "Android",
        steps = listOf(
            "Open Settings",
            "Tap 'Accessibility'",
            "Tap 'App Locker' (under Downloaded apps)",
            "Turn 'Use App Locker' ON"
        )
    );

    companion object {
        /**
         * Automatically detects the OEM brand based on Build constants.
         */
        fun detectCurrentDevice(): OemBrand {
            val manufacturer = Build.MANUFACTURER?.lowercase() ?: ""
            val brand = Build.BRAND?.lowercase() ?: ""
            val fingerprint = Build.FINGERPRINT?.lowercase() ?: ""

            return when {
                manufacturer.contains("samsung") || brand.contains("samsung") -> SAMSUNG
                manufacturer.contains("oneplus") || brand.contains("oneplus") ||
                        manufacturer.contains("oppo") || brand.contains("oppo") ||
                        manufacturer.contains("realme") || brand.contains("realme") -> ONEPLUS_OPPO_REALME
                manufacturer.contains("xiaomi") || brand.contains("xiaomi") ||
                        manufacturer.contains("redmi") || brand.contains("redmi") ||
                        manufacturer.contains("poco") || brand.contains("poco") -> XIAOMI_REDMI_POCO
                manufacturer.contains("vivo") || brand.contains("vivo") ||
                        manufacturer.contains("iqoo") || brand.contains("iqoo") -> VIVO_IQOO
                manufacturer.contains("motorola") || brand.contains("motorola") ||
                        manufacturer.contains("moto") || brand.contains("moto") -> MOTOROLA
                manufacturer.contains("huawei") || brand.contains("huawei") ||
                        manufacturer.contains("honor") || brand.contains("honor") -> HUAWEI_HONOR
                manufacturer.contains("google") || brand.contains("google") -> PIXEL_STOCK
                else -> PIXEL_STOCK
            }
        }
    }
}

object OemAccessibilityHelper {

    /**
     * Safely opens the system Accessibility Settings screen with automatic fallbacks.
     */
    fun openAccessibilitySettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
                true
            } catch (ex: Exception) {
                Toast.makeText(context, "Please open device Settings -> Accessibility", Toast.LENGTH_LONG).show()
                false
            }
        }
    }

    /**
     * Opens the App Info details page (used to unblock "Restricted settings" on Android 13+).
     */
    fun openAppDetailsSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open App Info. Open Settings -> Apps -> App Locker", Toast.LENGTH_LONG).show()
            false
        }
    }

    /**
     * Checks if the device is running Android 13+ where "Restricted settings" might apply for sideloaded builds.
     */
    fun isAndroid13OrHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }
}
