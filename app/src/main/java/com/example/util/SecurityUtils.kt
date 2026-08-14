package com.example.util

import android.content.Context
import android.content.ContextWrapper
import androidx.fragment.app.FragmentActivity
import java.security.MessageDigest

object SecurityUtils {
    /**
     * Computes a standard SHA-256 hash of the sensitive passcode, PIN, or pattern string.
     */
    fun hashSecret(input: String): String {
        if (input.isEmpty()) return ""
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies the user input against the stored secret in SharedPreferences.
     * Uses constant-time byte comparison (MessageDigest.isEqual) to prevent timing attacks.
     * Supports both SHA-256 hashes and backward-compatible direct match.
     */
    fun verifySecret(input: String, saved: String?): Boolean {
        if (saved.isNullOrEmpty() || input.isEmpty()) return false
        val inputBytes = input.toByteArray(Charsets.UTF_8)
        val savedBytes = saved.toByteArray(Charsets.UTF_8)
        if (MessageDigest.isEqual(inputBytes, savedBytes)) return true

        val hashedInput = hashSecret(input).toByteArray(Charsets.UTF_8)
        return MessageDigest.isEqual(hashedInput, savedBytes)
    }
}

fun Context.findActivity(): FragmentActivity? {
    var cur = this
    while (cur is ContextWrapper) {
        if (cur is FragmentActivity) {
            return cur
        }
        cur = cur.baseContext
    }
    return null
}
