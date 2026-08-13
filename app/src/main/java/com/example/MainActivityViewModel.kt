package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.LockedApp
import com.example.data.LockPreferences
import com.example.data.IntruderAlert
import com.example.util.AlphanumericComparator
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppFilterMode {
    ALL,
    LOCKED,
    UNLOCKED
}

@Immutable
data class GridAppInfo(
    val packageName: String,
    val appName: String,
    val isLocked: Boolean
)

sealed interface SetupState {
    object WelcomePatternRequired : SetupState
    object SelectLockType : SetupState
    data class SetFirstPattern(val errorMessage: String? = null) : SetupState
    data class ConfirmPattern(
        val firstAttempt: List<Int>,
        val errorMessage: String? = null,
        val attemptId: Int = 0
    ) : SetupState
    data class SetFirstPin(val errorMessage: String? = null) : SetupState
    data class ConfirmPin(
        val firstAttempt: String,
        val errorMessage: String? = null,
        val attemptId: Int = 0
    ) : SetupState
    data class SetFirstPassword(val errorMessage: String? = null) : SetupState
    data class ConfirmPassword(
        val firstAttempt: String,
        val errorMessage: String? = null,
        val attemptId: Int = 0
    ) : SetupState
    object SetupSuccess : SetupState
    object SetupFinished : SetupState
}

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val repository: AppRepository
    private val prefs = LockPreferences(context)

    // State flows
    private val _installedApps = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isLoadingApps = MutableStateFlow(true)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps

    // Setup Wizard state flow
    private val _setupState = MutableStateFlow<SetupState>(
        if (prefs.hasPatternSet()) SetupState.SetupFinished else SetupState.WelcomePatternRequired
    )
    val setupState: StateFlow<SetupState> = _setupState

    // Track locked apps from Room DB
    val lockedAppsFlow: Flow<List<LockedApp>>

    private val _filterMode = MutableStateFlow(AppFilterMode.ALL)
    val filterMode: StateFlow<AppFilterMode> = _filterMode

    // Combined stream of installed + search query + lock status + filter mode
    val appGridState: StateFlow<List<GridAppInfo>>

    init {
        val database = AppDatabase.getInstance(context)
        repository = AppRepository(database.lockedAppDao(), database.intruderAlertDao())
        lockedAppsFlow = repository.allLockedAppsStateFlow

        appGridState = combine(_installedApps, lockedAppsFlow, _searchQuery, _filterMode) { installed, lockedList, query, filter ->
            val lockedSet = HashSet<String>(lockedList.size).apply {
                for (item in lockedList) add(item.packageName)
            }
            
            val mapped = ArrayList<GridAppInfo>(installed.size + lockedList.size)
            val installedSet = HashSet<String>(installed.size)

            for ((packageName, appName) in installed) {
                installedSet.add(packageName)
                mapped.add(
                    GridAppInfo(
                        packageName = packageName,
                        appName = appName,
                        isLocked = lockedSet.contains(packageName)
                    )
                )
            }

            for (lockedApp in lockedList) {
                if (!installedSet.contains(lockedApp.packageName)) {
                    mapped.add(
                        GridAppInfo(
                            packageName = lockedApp.packageName,
                            appName = lockedApp.appName,
                            isLocked = true
                        )
                    )
                }
            }

            val trimmedQuery = query.trim()
            val hasQuery = trimmedQuery.isNotEmpty()

            val filtered = ArrayList<GridAppInfo>(mapped.size)
            for (item in mapped) {
                if (hasQuery && !item.appName.contains(trimmedQuery, ignoreCase = true)) {
                    continue
                }
                val matchesFilter = when (filter) {
                    AppFilterMode.ALL -> true
                    AppFilterMode.LOCKED -> item.isLocked
                    AppFilterMode.UNLOCKED -> !item.isLocked
                }
                if (matchesFilter) {
                    filtered.add(item)
                }
            }
            filtered.sortedWith(AlphanumericComparator.GRID_APP_COMPARATOR)
        }.flowOn(Dispatchers.Default).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoadingApps.value = true
            val appsList = withContext(Dispatchers.IO) {
                val pm = context.packageManager
                val intent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = pm.queryIntentActivities(intent, 0)
                resolveInfos.mapNotNull { resolveInfo ->
                    val packageName = resolveInfo.activityInfo.packageName
                    if (packageName == context.packageName) return@mapNotNull null // Don't lock ourselves
                    try {
                        val appLabel = resolveInfo.loadLabel(pm).toString()
                        Pair(packageName, appLabel)
                    } catch (e: Exception) {
                        null
                    }
                }.distinctBy { it.first }.sortedWith { a, b -> AlphanumericComparator.compareNames(a.second, b.second) }
            }
            _installedApps.value = appsList
            _isLoadingApps.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterMode(mode: AppFilterMode) {
        _filterMode.value = mode
    }

    fun lockRecommendedApps() {
        viewModelScope.launch {
            val sensitiveKeywords = listOf(
                "settings", "gallery", "photo", "photos", "whatsapp", "instagram",
                "facebook", "messenger", "pay", "bank", "wallet", "mail", "gmail",
                "contact", "contacts", "message", "messages", "drive", "chrome",
                "browser", "file", "files", "camera"
            )
            _installedApps.value.forEach { (packageName, appName) ->
                val lowerName = appName.lowercase()
                val lowerPkg = packageName.lowercase()
                if (sensitiveKeywords.any { lowerName.contains(it) || lowerPkg.contains(it) }) {
                    repository.lockApp(packageName, appName)
                }
            }
        }
    }

    fun unlockAllApps() {
        viewModelScope.launch {
            _installedApps.value.forEach { (packageName, _) ->
                repository.unlockApp(packageName)
            }
        }
    }

    fun toggleAppLock(packageName: String, appName: String, shouldLock: Boolean) {
        viewModelScope.launch {
            if (shouldLock) {
                repository.lockApp(packageName, appName)
            } else {
                repository.unlockApp(packageName)
            }
        }
    }

    // Pattern / PIN / Password wizard control
    fun resetSetupWizard() {
        prefs.clearPattern()
        _setupState.value = SetupState.SelectLockType
    }

    fun startWizard() {
        _setupState.value = SetupState.SelectLockType
    }

    fun selectLockType(type: String) {
        when (type) {
            "pattern" -> _setupState.value = SetupState.SetFirstPattern()
            "pin" -> _setupState.value = SetupState.SetFirstPin()
            "password" -> _setupState.value = SetupState.SetFirstPassword()
        }
    }

    fun getLockType(): String {
        return prefs.lockType
    }

    fun handlePatternDrawn(pattern: List<Int>) {
        val current = _setupState.value
        if (pattern.size < 4) {
            return
        }

        when (current) {
            is SetupState.SetFirstPattern -> {
                _setupState.value = SetupState.ConfirmPattern(pattern)
            }
            is SetupState.ConfirmPattern -> {
                if (current.firstAttempt == pattern) {
                    // Success! Store pattern sequence
                    val patternString = pattern.joinToString(",")
                    prefs.savedPattern = com.example.util.SecurityUtils.hashSecret(patternString)
                    prefs.lockType = "pattern"
                    _setupState.value = SetupState.SetupSuccess
                } else {
                    // Redirect to initial lock setup screen with error prompt when confirmation fails
                    _setupState.value = SetupState.SetFirstPattern(
                        errorMessage = "Patterns do not match! Draw initial pattern again."
                    )
                }
            }
            else -> {}
        }
    }

    fun handlePinEntered(pin: String) {
        val current = _setupState.value
        when (current) {
            is SetupState.SetFirstPin -> {
                _setupState.value = SetupState.ConfirmPin(pin)
            }
            is SetupState.ConfirmPin -> {
                if (current.firstAttempt == pin) {
                    prefs.savedPasscode = com.example.util.SecurityUtils.hashSecret(pin)
                    prefs.lockType = "pin"
                    _setupState.value = SetupState.SetupSuccess
                } else {
                    // Redirect to initial lock setup screen with error prompt when confirmation fails
                    _setupState.value = SetupState.SetFirstPin(
                        errorMessage = "PINs do not match! Enter initial PIN again."
                    )
                }
            }
            else -> {}
        }
    }

    fun handlePasswordEntered(password: String) {
        val current = _setupState.value
        when (current) {
            is SetupState.SetFirstPassword -> {
                if (password.length >= 8) {
                    _setupState.value = SetupState.ConfirmPassword(password)
                }
            }
            is SetupState.ConfirmPassword -> {
                if (current.firstAttempt == password) {
                    prefs.savedPasscode = com.example.util.SecurityUtils.hashSecret(password)
                    prefs.lockType = "password"
                    _setupState.value = SetupState.SetupSuccess
                } else {
                    // Redirect to initial lock setup screen with error prompt when confirmation fails
                    _setupState.value = SetupState.SetFirstPassword(
                        errorMessage = "Passwords do not match! Enter initial password again."
                    )
                }
            }
            else -> {}
        }
    }

    fun restartCurrentTypeSetup() {
        when (_setupState.value) {
            is SetupState.SetFirstPattern, is SetupState.ConfirmPattern -> _setupState.value = SetupState.SetFirstPattern()
            is SetupState.SetFirstPin, is SetupState.ConfirmPin -> _setupState.value = SetupState.SetFirstPin()
            is SetupState.SetFirstPassword, is SetupState.ConfirmPassword -> _setupState.value = SetupState.SetFirstPassword()
            else -> {}
        }
    }

    fun completeWizard() {
        _setupState.value = SetupState.SetupFinished
    }

    fun isServiceActive(): Boolean {
        return prefs.isServiceActive
    }

    fun setServiceActive(active: Boolean) {
        prefs.isServiceActive = active
    }

    fun isBiometricEnabled(): Boolean {
        return prefs.isBiometricEnabled
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.isBiometricEnabled = enabled
    }

    fun isIntruderDetectionEnabled(): Boolean {
        return prefs.isIntruderDetectionEnabled
    }

    fun setIntruderDetectionEnabled(enabled: Boolean) {
        prefs.isIntruderDetectionEnabled = enabled
    }

    fun isAutoCleanupEnabled(): Boolean {
        return prefs.isAutoCleanupEnabled
    }

    fun setAutoCleanupEnabled(enabled: Boolean) {
        prefs.isAutoCleanupEnabled = enabled
        if (enabled) {
            performAutoCleanupIfNeeded()
        }
    }

    fun performAutoCleanupIfNeeded() {
        if (!prefs.isAutoCleanupEnabled) return
        viewModelScope.launch {
            val alerts = intruderAlertsFlow.value
            val cutoffTimestamp = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000L)
            val expiredAlerts = alerts.filter { it.timestamp < cutoffTimestamp }
            expiredAlerts.forEach { repository.deleteIntruderAlert(it) }
        }
    }

    val intruderAlertsFlow: StateFlow<List<IntruderAlert>> = repository.allIntruderAlertsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteIntruderAlert(alert: IntruderAlert) {
        viewModelScope.launch {
            repository.deleteIntruderAlert(alert)
        }
    }

    fun clearAllIntruderAlerts() {
        viewModelScope.launch {
            repository.deleteAllIntruderAlerts()
        }
    }
}
