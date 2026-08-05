package com.example.util

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
     * Supports both SHA-256 hashes and backward-compatible direct match.
     */
    fun verifySecret(input: String, saved: String?): Boolean {
        if (saved.isNullOrEmpty()) return false
        if (input == saved) return true
        return hashSecret(input) == saved
    }
}
