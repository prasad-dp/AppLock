package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class LockPreferences(context: Context) {
    private val prefs: SharedPreferences = createEncryptedPreferences(context)

    companion object {
        private const val KEY_PATTERN = "lock_pattern_passcode"
        private const val KEY_LOCK_TYPE = "lock_type" // "pattern", "pin", "password"
        private const val KEY_PASSCODE = "lock_passcode" // Store PIN/Password string
        private const val KEY_BIOMETRIC_ENABLED = "biometric_auth_enabled"
        private const val KEY_SERVICE_ACTIVE = "locker_service_active"
        private const val KEY_INTRUDER_DETECTION_ENABLED = "intruder_detection"

        private fun createEncryptedPreferences(context: Context): SharedPreferences {
            val oldPrefs = context.getSharedPreferences("app_locker_preferences", Context.MODE_PRIVATE)
            return try {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                val encryptedPrefs = EncryptedSharedPreferences.create(
                    context,
                    "app_locker_preferences_encrypted",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )

                // Migrate old preferences if present
                if (oldPrefs.all.isNotEmpty()) {
                    val editor = encryptedPrefs.edit()
                    for ((key, value) in oldPrefs.all) {
                        if (!encryptedPrefs.contains(key)) {
                            when (value) {
                                is String -> editor.putString(key, value)
                                is Boolean -> editor.putBoolean(key, value)
                                is Int -> editor.putInt(key, value)
                                is Long -> editor.putLong(key, value)
                                is Float -> editor.putFloat(key, value)
                            }
                        }
                    }
                    editor.apply()
                    oldPrefs.edit().clear().apply()
                }

                encryptedPrefs
            } catch (e: Exception) {
                oldPrefs
            }
        }
    }

    var isIntruderDetectionEnabled: Boolean
        get() = prefs.getBoolean(KEY_INTRUDER_DETECTION_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_INTRUDER_DETECTION_ENABLED, value).apply()

    var isAutoCleanupEnabled: Boolean
        get() = prefs.getBoolean("auto_cleanup_logs_enabled", false)
        set(value) = prefs.edit().putBoolean("auto_cleanup_logs_enabled", value).apply()

    var reLockTimeout: String
        get() = prefs.getString("relock_timeout_policy", "1_min") ?: "1_min"
        set(value) = prefs.edit().putString("relock_timeout_policy", value).apply()

    var lockType: String
        get() = prefs.getString(KEY_LOCK_TYPE, "pattern") ?: "pattern"
        set(value) = prefs.edit().putString(KEY_LOCK_TYPE, value).apply()

    var savedPattern: String?
        get() = prefs.getString(KEY_PATTERN, null)
        set(value) = prefs.edit().putString(KEY_PATTERN, value).apply()

    var savedPasscode: String?
        get() = prefs.getString(KEY_PASSCODE, null)
        set(value) = prefs.edit().putString(KEY_PASSCODE, value).apply()

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()

    var lockoutEndTimestamp: Long
        get() = prefs.getLong("lockout_end_timestamp", 0L)
        set(value) = prefs.edit().putLong("lockout_end_timestamp", value).apply()

    fun getLockoutEndTimestamp(packageName: String): Long {
        if (packageName.isEmpty()) {
            return lockoutEndTimestamp
        }
        return prefs.getLong("lockout_end_timestamp_$packageName", 0L)
    }

    fun setLockoutEndTimestamp(packageName: String, value: Long) {
        if (packageName.isEmpty()) {
            lockoutEndTimestamp = value
        } else {
            prefs.edit().putLong("lockout_end_timestamp_$packageName", value).apply()
        }
    }

    fun getFailedAttempts(packageName: String): Int {
        if (packageName.isEmpty()) {
            return prefs.getInt("failed_attempts_count", 0)
        }
        return prefs.getInt("failed_attempts_count_$packageName", 0)
    }

    fun setFailedAttempts(packageName: String, count: Int) {
        if (packageName.isEmpty()) {
            prefs.edit().putInt("failed_attempts_count", count).apply()
        } else {
            prefs.edit().putInt("failed_attempts_count_$packageName", count).apply()
        }
    }

    var isServiceActive: Boolean
        get() = prefs.getBoolean(KEY_SERVICE_ACTIVE, true)
        set(value) = prefs.edit().putBoolean(KEY_SERVICE_ACTIVE, value).apply()

    var isDarkMode: Boolean
        get() = prefs.getBoolean("dark_mode_enabled", true)
        set(value) = prefs.edit().putBoolean("dark_mode_enabled", value).apply()

    var isPremiumUser: Boolean
        get() = prefs.getBoolean("is_premium_user", false)
        set(value) = prefs.edit().putBoolean("is_premium_user", value).apply()

    fun isDoubleLockWarningSuppressed(packageName: String): Boolean {
        return prefs.getBoolean("suppress_double_lock_warning_$packageName", false)
    }

    fun suppressDoubleLockWarning(packageName: String, suppress: Boolean) {
        prefs.edit().putBoolean("suppress_double_lock_warning_$packageName", suppress).apply()
    }

    fun getPerAppRelockTimeout(packageName: String): String? {
        return prefs.getString("per_app_relock_timeout_$packageName", null)
    }

    fun setPerAppRelockTimeout(packageName: String, value: String?) {
        if (value.isNullOrEmpty() || value == "global") {
            prefs.edit().remove("per_app_relock_timeout_$packageName").apply()
        } else {
            prefs.edit().putString("per_app_relock_timeout_$packageName", value).apply()
        }
    }

    fun hasPatternSet(): Boolean {
        return when (lockType) {
            "pattern" -> !savedPattern.isNullOrEmpty()
            "pin", "password" -> !savedPasscode.isNullOrEmpty()
            else -> false
        }
    }

    fun clearPattern() {
        prefs.edit().remove(KEY_PATTERN).remove(KEY_PASSCODE).apply()
    }
}
