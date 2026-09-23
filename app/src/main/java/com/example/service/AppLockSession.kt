package com.example.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AppLockSession {
    // Stores currently unlocked app package names during the current session
    private val unlockedApps = mutableSetOf<String>()
    private val unlockTimes = mutableMapOf<String, Long>()
    private val lastUnlockTimes = mutableMapOf<String, Long>()
    private val lastActiveTimes = mutableMapOf<String, Long>()
    
    // Track current active foreground app to distinguish genuine new entries from internal navigation/exits
    @Volatile
    var currentForegroundApp: String? = null

    /**
     * Checks whether transitioning to targetPackage is a genuine NEW launch from outside.
     * Returns true ONLY if the previous foreground app was NOT targetPackage.
     * If the user was already inside targetPackage, returns false to prevent re-triggering
     * lock overlays during internal activity navigation, back-button presses, or activity destruction.
     */
    fun isGenuineAppEntry(targetPackage: String, previousApp: String? = null): Boolean {
        synchronized(unlockedApps) {
            val prev = previousApp ?: currentForegroundApp
            if (prev == null) return true
            if (prev == targetPackage) {
                return false // User was already inside this app
            }
            return true
        }
    }

    fun setCurrentForeground(packageName: String?) {
        synchronized(unlockedApps) {
            if (!packageName.isNullOrEmpty() && packageName != "com.example.applocker" && !packageName.startsWith("com.aistudio.applocker")) {
                currentForegroundApp = packageName
            }
        }
    }

    // Track Home navigation transitions to prevent duplicate lock screen popups during app exit
    @Volatile
    private var lastHomeTransitionTime = 0L
    @Volatile
    private var lastHomeExitedPackage: String? = null

    fun markGoToHome(packageName: String?) {
        synchronized(unlockedApps) {
            lastHomeTransitionTime = System.currentTimeMillis()
            lastHomeExitedPackage = packageName
        }
    }

    fun isExitingToHome(packageName: String? = null): Boolean {
        synchronized(unlockedApps) {
            if (lastHomeTransitionTime == 0L) return false
            val elapsed = System.currentTimeMillis() - lastHomeTransitionTime
            if (elapsed > 2500L) {
                lastHomeTransitionTime = 0L
                lastHomeExitedPackage = null
                return false
            }
            if (packageName != null && lastHomeExitedPackage != null && packageName != lastHomeExitedPackage) {
                return false
            }
            return true
        }
    }

    fun clearGoToHome() {
        synchronized(unlockedApps) {
            lastHomeTransitionTime = 0L
            lastHomeExitedPackage = null
        }
    }

    // Track the package we are actively unlocking so we do not launch multiple overlay activities
    @Volatile
    var activeUnlockingPackage: String? = null

    // Track state of locker service
    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning

    fun setServiceRunning(running: Boolean) {
        _isServiceRunning.value = running
    }

    fun isUnlocked(packageName: String): Boolean {
        synchronized(unlockedApps) {
            return packageName in unlockedApps
        }
    }

    fun getUnlockTime(packageName: String): Long {
        synchronized(unlockedApps) {
            return lastUnlockTimes[packageName] ?: unlockTimes[packageName] ?: 0L
        }
    }

    fun getLastActiveTime(packageName: String): Long {
        synchronized(unlockedApps) {
            return lastActiveTimes[packageName] ?: unlockTimes[packageName] ?: 0L
        }
    }

    fun updateActiveTime(packageName: String) {
        synchronized(unlockedApps) {
            if (packageName in unlockedApps) {
                lastActiveTimes[packageName] = System.currentTimeMillis()
            }
        }
    }

    fun unlockApp(packageName: String) {
        val now = System.currentTimeMillis()
        synchronized(unlockedApps) {
            unlockedApps.add(packageName)
            unlockTimes[packageName] = now
            lastUnlockTimes[packageName] = now
            lastActiveTimes[packageName] = now
        }
        if (activeUnlockingPackage == packageName) {
            activeUnlockingPackage = null
        }
    }

    fun isRecentlyUnlocked(packageName: String, gracePeriodMs: Long = 2500L): Boolean {
        synchronized(unlockedApps) {
            val unlockTime = lastUnlockTimes[packageName] ?: unlockTimes[packageName] ?: return false
            return (System.currentTimeMillis() - unlockTime) < gracePeriodMs
        }
    }

    /**
     * Locks the specified app by revoking its session token.
     * When force is false, respects the post-unlock grace period to prevent transition race conditions.
     */
    fun lockApp(packageName: String, force: Boolean = false) {
        synchronized(unlockedApps) {
            if (!force && isRecentlyUnlocked(packageName)) {
                return
            }
            unlockedApps.remove(packageName)
            unlockTimes.remove(packageName)
            lastActiveTimes.remove(packageName)
        }
    }

    // Circuit breaker: track recent unlock activity launches to prevent rapid looping
    private val launchHistory = mutableMapOf<String, MutableList<Long>>()
    private val loopCooldownUntil = mutableMapOf<String, Long>()

    /**
     * Circuit breaker to prevent rapid duplicate lock screen loops.
     * Throttles launches if the app is already unlocked, recently unlocked, or launched repeatedly.
     */
    fun shouldThrottleLaunch(packageName: String): Boolean {
        val now = System.currentTimeMillis()
        synchronized(unlockedApps) {
            // 1. If the app is currently unlocked, NEVER launch unlock screen
            if (packageName in unlockedApps) {
                return true
            }

            // 2. If app was unlocked within the last 3000ms, NEVER launch unlock screen
            val unlockTime = lastUnlockTimes[packageName] ?: unlockTimes[packageName] ?: 0L
            if (unlockTime > 0L && (now - unlockTime < 3000L)) {
                return true
            }

            // 3. If the app is under an active circuit breaker cooldown
            val cooldown = loopCooldownUntil[packageName] ?: 0L
            if (now < cooldown) {
                return true
            }

            // 4. Check launch frequency in a rolling 4-second window
            val timestamps = launchHistory.getOrPut(packageName) { mutableListOf() }
            timestamps.removeAll { now - it > 4000L }

            // If launched more than 2 times in the last 4 seconds, trip the circuit breaker for 3 seconds
            if (timestamps.size >= 2) {
                loopCooldownUntil[packageName] = now + 3000L
                timestamps.clear()
                return true
            }

            timestamps.add(now)
            return false
        }
    }

    fun clearSession() {
        synchronized(unlockedApps) {
            unlockedApps.clear()
            unlockTimes.clear()
            lastUnlockTimes.clear()
            lastActiveTimes.clear()
            launchHistory.clear()
            loopCooldownUntil.clear()
            lastHomeTransitionTime = 0L
            lastHomeExitedPackage = null
            currentForegroundApp = null
        }
        activeUnlockingPackage = null
    }

    fun getUnlockedAppsCopy(): List<String> {
        synchronized(unlockedApps) {
            if (unlockedApps.isEmpty()) return emptyList()
            return unlockedApps.toList()
        }
    }
}
