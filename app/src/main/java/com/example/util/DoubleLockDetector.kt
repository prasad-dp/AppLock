package com.example.util

object DoubleLockDetector {

    private val KNOWN_BUILTIN_LOCK_PACKAGES = setOf(
        // WhatsApp & Messaging
        "com.whatsapp",
        "com.whatsapp.w4b",
        "org.telegram.messenger",
        "org.thoughtcrime.securesms",

        // Payments & Digital Wallets
        "com.google.android.apps.walletnfcrel",
        "com.phonepe.app",
        "net.one97.paytm",
        "com.paypal.android.p2pmobile",
        "com.revolut.revolut",
        "com.squareup.cash",
        "com.venmo",

        // Password Managers & Security Vaults
        "com.onepassword.android",
        "com.x8bit.bitwarden",
        "com.dashlane",
        "com.lastpass.lpandroid",
        "com.google.android.apps.authenticator2",
        "com.authy.authy",

        // System Settings & Knox Secure Folder
        "com.android.settings",
        "com.samsung.knox.securefolder",
        "com.miui.securitycenter"
    )

    private val KEYWORD_TRIGGERS = listOf(
        "bank", "pay", "wallet", "crypto", "finance", "vault", "pass", "authenticator", "secure"
    )

    fun isDoubleLockProne(packageName: String, appName: String): Boolean {
        if (KNOWN_BUILTIN_LOCK_PACKAGES.contains(packageName.lowercase())) {
            return true
        }

        val lowerPackage = packageName.lowercase()
        val lowerName = appName.lowercase()

        return KEYWORD_TRIGGERS.any { keyword ->
            lowerPackage.contains(keyword) || lowerName.contains(keyword)
        }
    }

    fun getAppLockCategoryReason(packageName: String, appName: String): String {
        val lowerPkg = packageName.lowercase()
        val lowerName = appName.lowercase()

        return when {
            lowerPkg.contains("whatsapp") || lowerPkg.contains("telegram") || lowerPkg.contains("securesms") ->
                "Messaging apps like $appName often have built-in fingerprint lock enabled."
            lowerPkg.contains("settings") || lowerPkg.contains("securefolder") || lowerPkg.contains("securitycenter") ->
                "System security tools have native Android protection enabled."
            lowerPkg.contains("bank") || lowerPkg.contains("pay") || lowerPkg.contains("wallet") || lowerPkg.contains("paypal") || lowerName.contains("bank") || lowerName.contains("pay") ->
                "Financial and payment apps usually enforce biometrics or PIN lock natively."
            lowerPkg.contains("onepassword") || lowerPkg.contains("bitwarden") || lowerPkg.contains("dashlane") || lowerPkg.contains("lastpass") ->
                "Password managers lock automatically using native biometrics."
            else ->
                "$appName appears to be a sensitive app that may enforce native biometrics or system lock."
        }
    }
}
