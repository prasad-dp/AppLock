package com.example.util

import com.example.GridAppInfo
import com.example.data.LockPreferences

data class DoubleLockRiskItem(
    val appInfo: GridAppInfo,
    val currentPolicy: String,
    val riskReason: String,
    val suggestedPolicy: String = "15_sec"
)

data class DoubleLockRecommendationReport(
    val highRiskApps: List<DoubleLockRiskItem>,
    val totalConflictCount: Int,
    val hasImmediateGlobalSetting: Boolean
)

object DoubleLockRecommendationEngine {

    fun analyzeDoubleLockRisks(
        allApps: List<GridAppInfo>,
        prefs: LockPreferences
    ): DoubleLockRecommendationReport {
        val lockedApps = allApps.filter { it.isLocked }
        val globalTimeout = prefs.reLockTimeout
        val highRiskApps = mutableListOf<DoubleLockRiskItem>()

        for (app in lockedApps) {
            val perAppTimeout = prefs.getPerAppRelockTimeout(app.packageName)
            val effectiveTimeout = perAppTimeout ?: globalTimeout

            // Check if app is vulnerable to double locking or infinite re-lock loops
            val isProne = DoubleLockDetector.isDoubleLockProne(app.packageName, app.appName)

            if (isProne && effectiveTimeout == "immediately") {
                val reason = DoubleLockDetector.getAppLockCategoryReason(app.packageName, app.appName) +
                        " With 'Immediate' re-lock, switching screens or passing internal biometrics can trigger an infinite unlock loop."

                highRiskApps.add(
                    DoubleLockRiskItem(
                        appInfo = app,
                        currentPolicy = effectiveTimeout,
                        riskReason = reason,
                        suggestedPolicy = "15_sec"
                    )
                )
            }
        }

        return DoubleLockRecommendationReport(
            highRiskApps = highRiskApps,
            totalConflictCount = highRiskApps.size,
            hasImmediateGlobalSetting = (globalTimeout == "immediately" && highRiskApps.isNotEmpty())
        )
    }
}
