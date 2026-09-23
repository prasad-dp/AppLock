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
    fun isGenuineAppEntry(targetPackage: String, previousApp: String? = currentForegroundApp): Boolean {
        if (previousApp == null) return true
        if (previousApp == targetPackage && isUnlocked(targetPackage)) {
            return false // User was already inside this app and it is unlocked
        }
        return true
    }

    fun setCurrentForeground(packageName: String?) {
        if (!packageName.isNullOrEmpty() && packageName != "com.example.applocker") {
            currentForegroundApp = packageName
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
            currentForegroundApp = null
            _activeUnlockingPackage = null
        }
    }

    fun isExitingToHome(packageName: String? = null): Boolean {
        synchronized(unlockedApps) {
            if (lastHomeTransitionTime == 0L) return false
            val elapsed = System.currentTimeMillis() - lastHomeTransitionTime
            if (elapsed > 600L) {
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

    // Track the package we are actively unlocking with a 2-second timeout to prevent deadlocks
    @Volatile
    private var _activeUnlockingPackage: String? = null
    @Volatile
    private var _activeUnlockingTime: Long = 0L

    var activeUnlockingPackage: String?
        get() {
            if (System.currentTimeMillis() - _activeUnlockingTime > 2000L) {
                _activeUnlockingPackage = null
            }
            return _activeUnlockingPackage
        }
        set(value) {
            _activeUnlockingPackage = value
            _activeUnlockingTime = if (value != null) System.currentTimeMillis() else 0L
        }

    fun isUnlockingNow(pkg: String): Boolean {
        if (System.currentTimeMillis() - _activeUnlockingTime > 2000L) {
            _activeUnlockingPackage = null
            return false
        }
        return _activeUnlockingPackage == pkg
    }

    // Track state of locker service
    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning

    fun setServiceRunning(running: Boolean) {
        _isServiceRunning.value = running
    }

    fun isUnlocked(packageName: String): Boolean {
        synchronized(unlockedApps) {
            if (packageName in unlockedApps) return true
            val family = com.example.util.AppLockPackageHelper.getPackageFamily(packageName)
            return family.any { it in unlockedApps }
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
            val family = com.example.util.AppLockPackageHelper.getPackageFamily(packageName)
            val now = System.currentTimeMillis()
            for (pkg in family) {
                if (pkg in unlockedApps) {
                    lastActiveTimes[pkg] = now
                }
            }
        }
    }

    fun unlockApp(packageName: String) {
        val now = System.currentTimeMillis()
        synchronized(unlockedApps) {
            val family = com.example.util.AppLockPackageHelper.getPackageFamily(packageName)
            for (pkg in family) {
                unlockedApps.add(pkg)
                unlockTimes[pkg] = now
                lastUnlockTimes[pkg] = now
                lastActiveTimes[pkg] = now
            }
        }
        if (activeUnlockingPackage == packageName) {
            activeUnlockingPackage = null
        }
    }

    fun isRecentlyUnlocked(packageName: String, gracePeriodMs: Long = 2500L): Boolean {
        synchronized(unlockedApps) {
            val family = com.example.util.AppLockPackageHelper.getPackageFamily(packageName)
            for (pkg in family) {
                if (pkg in unlockedApps) {
                    val unlockTime = lastUnlockTimes[pkg] ?: unlockTimes[pkg] ?: continue
                    if ((System.currentTimeMillis() - unlockTime) < gracePeriodMs) {
                        return true
                    }
                }
            }
            return false
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
            val family = com.example.util.AppLockPackageHelper.getPackageFamily(packageName)
            for (pkg in family) {
                unlockedApps.remove(pkg)
                unlockTimes.remove(pkg)
                lastUnlockTimes.remove(pkg)
                lastActiveTimes.remove(pkg)
            }
        }
    }

    // Circuit breaker: track recent unlock activity launches to prevent rapid looping
    private val launchHistory = mutableMapOf<String, MutableList<Long>>()
    private val loopCooldownUntil = mutableMapOf<String, Long>()

    /**
     * Circuit breaker check to prevent duplicate lock screen launches.
     * Returns true if the app is unlocked or under circuit breaker cooldown.
     */
    fun shouldThrottleLaunch(packageName: String): Boolean {
        val now = System.currentTimeMillis()
        synchronized(unlockedApps) {
            // 1. If the app is currently unlocked, NEVER launch unlock screen
            if (packageName in unlockedApps) {
                return true
            }

            // 2. If the app is under an active circuit breaker cooldown
            val cooldown = loopCooldownUntil[packageName] ?: 0L
            if (now < cooldown) {
                return true
            }

            return false
        }
    }

    /**
     * Records an actual lock screen launch event.
     * Trips circuit breaker for 1.5s only if launched more than 3 times in 2 seconds.
     */
    fun recordLaunchAttempt(packageName: String) {
        val now = System.currentTimeMillis()
        synchronized(unlockedApps) {
            val timestamps = launchHistory.getOrPut(packageName) { mutableListOf() }
            timestamps.removeAll { now - it > 2000L }
            timestamps.add(now)

            if (timestamps.size >= 3) {
                loopCooldownUntil[packageName] = now + 1500L
                timestamps.clear()
            }
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
