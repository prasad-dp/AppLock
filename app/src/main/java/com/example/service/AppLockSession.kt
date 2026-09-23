package com.example.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AppLockSession {
    // Stores currently unlocked app package names during the current session
    private val unlockedApps = mutableSetOf<String>()
    private val unlockTimes = mutableMapOf<String, Long>()
    private val lastUnlockTimes = mutableMapOf<String, Long>()
    private val lastActiveTimes = mutableMapOf<String, Long>()
    
    // Track the package we are actively unlocking so we do not launch multiple overlay activities
    @Volatile
    var activeUnlockingPackage: String? = null

    // Track active foreground package verified by system window events
    @Volatile
    var currentForegroundPackage: String? = null
        private set

    @Volatile
    var lastForegroundUpdateTime: Long = 0L
        private set

    fun updateForegroundPackage(packageName: String?) {
        currentForegroundPackage = packageName
        lastForegroundUpdateTime = System.currentTimeMillis()
    }

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
            if (now - unlockTime < 3000L) {
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
        }
        activeUnlockingPackage = null
        currentForegroundPackage = null
        lastForegroundUpdateTime = 0L
    }

    fun getUnlockedAppsCopy(): List<String> {
        synchronized(unlockedApps) {
            if (unlockedApps.isEmpty()) return emptyList()
            return unlockedApps.toList()
        }
    }
}
