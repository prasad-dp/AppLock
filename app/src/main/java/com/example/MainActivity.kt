package com.example

import com.example.util.AlphanumericComparator
import com.example.util.findActivity

import android.app.AppOpsManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.IntruderAlert
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.LockPreferences
import com.example.service.AppLockService
import com.example.service.AppLockSession
import androidx.compose.material.icons.automirrored.filled.Launch
import com.example.ui.pattern.PatternLockView
import com.example.ui.pattern.PatternState
import com.example.ui.pattern.PinPadView
import com.example.ui.pattern.PasswordUnlockView
import com.example.ui.pattern.LockVerifyScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

object AppIconCache {
    private val maxMemoryKb = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSizeKb = (maxMemoryKb / 8).coerceAtLeast(2048) // allocate 1/8th of max heap in KB

    private val cache = object : android.util.LruCache<String, android.graphics.drawable.Drawable>(cacheSizeKb) {
        override fun sizeOf(key: String, value: android.graphics.drawable.Drawable): Int {
            return if (value is android.graphics.drawable.BitmapDrawable && !value.bitmap.isRecycled) {
                (value.bitmap.byteCount / 1024).coerceAtLeast(1)
            } else {
                64 // Default size estimate in KB for vector or custom drawables
            }
        }
    }

    @Synchronized
    fun get(packageName: String): android.graphics.drawable.Drawable? = cache.get(packageName)

    @Synchronized
    fun put(packageName: String, drawable: android.graphics.drawable.Drawable) {
        cache.put(packageName, drawable)
    }

    @Synchronized
    fun clear() {
        cache.evictAll()
    }
}

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        window.decorView.filterTouchesWhenObscured = true
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val prefs = remember { LockPreferences(context) }
            var isDarkMode by remember { mutableStateOf(prefs.isDarkMode) }

            MyApplicationTheme(darkTheme = isDarkMode) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LockerMainScreen(
                        isDarkMode = isDarkMode,
                        onDarkModeChange = { value ->
                            prefs.isDarkMode = value
                            isDarkMode = value
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun LockerMainScreen(
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainActivityViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = remember(context) { context as? FragmentActivity }
    val setupState by viewModel.setupState.collectAsStateWithLifecycle()
    val prefs = remember { LockPreferences(context) }
    var isAppLockerUnlocked by remember { mutableStateOf(!prefs.hasPatternSet()) }

    // Keep app locker unlocked while navigating wizard or finishing setup
    LaunchedEffect(setupState) {
        if (setupState !is SetupState.SetupFinished || !prefs.hasPatternSet()) {
            isAppLockerUnlocked = true
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    if (prefs.hasPatternSet()) {
                        isAppLockerUnlocked = false
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (!isAppLockerUnlocked && setupState is SetupState.SetupFinished) {
        LockVerifyScreen(
            prefs = prefs,
            onSuccess = {
                isAppLockerUnlocked = true
            },
            modifier = modifier,
            title = "App Locker",
            subtitle = "Verify credential to manage settings"
        )
    } else {
        AnimatedContent(
            targetState = setupState,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "setup_flow"
        ) { state ->
            when (state) {
                is SetupState.WelcomePatternRequired,
                is SetupState.SelectLockType,
                is SetupState.SetFirstPattern,
                is SetupState.ConfirmPattern,
                is SetupState.SetFirstPin,
                is SetupState.ConfirmPin,
                is SetupState.SetFirstPassword,
                is SetupState.ConfirmPassword,
                is SetupState.SetupSuccess -> {
                    PatternWizardView(
                        state = state,
                        isDarkMode = isDarkMode,
                        onDarkModeChange = onDarkModeChange,
                        onPatternDrawn = { viewModel.handlePatternDrawn(it) },
                        onPinEntered = { viewModel.handlePinEntered(it) },
                        onPasswordEntered = { viewModel.handlePasswordEntered(it) },
                        onSelectLockType = { viewModel.selectLockType(it) },
                        onFinish = { 
                            isAppLockerUnlocked = true
                            viewModel.completeWizard() 
                        },
                        onStart = { viewModel.startWizard() }
                    )
                }
                is SetupState.SetupFinished -> {
                    DashboardView(
                        viewModel = viewModel,
                        isDarkMode = isDarkMode,
                        onDarkModeChange = onDarkModeChange,
                        modifier = modifier
                    )
                }
            }
        }
    }
}

// 1. SETUP PATTERN / PIN / PASSWORD WIZARD SCREEN
@Composable
fun LockTypeCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("select_lock_type_$title")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FancyThemeToggle(
    isDarkMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(targetState = isDarkMode, label = "theme_transition")
    
    val rotation by transition.animateFloat(
        transitionSpec = { spring(dampingRatio = 0.6f, stiffness = 120f) },
        label = "rotation"
    ) { state ->
        if (state) 0f else 180f
    }
    
    val scale by transition.animateFloat(
        transitionSpec = { spring(dampingRatio = 0.5f, stiffness = 150f) },
        label = "scale"
    ) { _ -> 1f }

    val iconTint by transition.animateColor(
        transitionSpec = { tween(200) },
        label = "tint"
    ) { state ->
        if (state) Color(0xFFFFD54F) else Color(0xFF3F51B5)
    }

    val glowColor = if (isDarkMode) Color(0xFFFFD54F).copy(alpha = 0.15f) else Color(0xFF3F51B5).copy(alpha = 0.10f)

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onToggle)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = CircleShape
            )
            .drawBehind {
                drawCircle(
                    color = glowColor,
                    radius = size.minDimension / 2f + 4.dp.toPx()
                )
            }
            .testTag("fancy_theme_toggle"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isDarkMode) Icons.Default.WbSunny else Icons.Default.NightsStay,
            contentDescription = "Toggle Theme Mode",
            tint = iconTint,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer(
                    rotationZ = rotation,
                    scaleX = scale,
                    scaleY = scale
                )
        )
    }
}

@Composable
fun PatternWizardView(
    state: SetupState,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onPatternDrawn: (List<Int>) -> Unit,
    onPinEntered: (String) -> Unit,
    onPasswordEntered: (String) -> Unit,
    onSelectLockType: (String) -> Unit,
    onFinish: () -> Unit,
    onStart: () -> Unit
) {
    var feedbackState by remember { mutableStateOf(PatternState.DRAWING) }
    var instructionText by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        feedbackState = PatternState.DRAWING
        when (state) {
            SetupState.WelcomePatternRequired -> {
                instructionText = "Protect your system! Choose Swipe Pattern, PIN or Password to lock access."
            }
            SetupState.SelectLockType -> {
                instructionText = "Select your preferred credential protection type"
            }
            SetupState.SetFirstPattern -> {
                instructionText = "Draw a pattern lock sequence (connect at least 4 dots)"
            }
            is SetupState.ConfirmPattern -> {
                instructionText = "Draw pattern again to confirm sequence"
            }
            SetupState.SetFirstPin -> {
                instructionText = "Create a 4-digit PIN"
            }
            is SetupState.ConfirmPin -> {
                instructionText = "Enter PIN again to confirm"
            }
            SetupState.SetFirstPassword -> {
                instructionText = "Create a security password"
            }
            is SetupState.ConfirmPassword -> {
                instructionText = "Type password again to confirm"
            }
            SetupState.SetupSuccess -> {
                feedbackState = PatternState.SUCCESS
                instructionText = "Credential Configured! Your applications are now protected."
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FancyThemeToggle(
                isDarkMode = isDarkMode,
                onToggle = { onDarkModeChange(!isDarkMode) }
            )
        }

        // Shield / Lock graphics
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (state == SetupState.SetupSuccess) Icons.Default.LockOpen else Icons.Default.Lock,
                contentDescription = "Lock",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = when (state) {
                    SetupState.WelcomePatternRequired -> "Welcome to App Locker"
                    SetupState.SelectLockType -> "Choose Lock Mode"
                    SetupState.SetFirstPattern -> "Create Unlock Pattern"
                    is SetupState.ConfirmPattern -> "Confirm Unlock Pattern"
                    SetupState.SetFirstPin -> "Create Numeric PIN"
                    is SetupState.ConfirmPin -> "Confirm Numeric PIN"
                    SetupState.SetFirstPassword -> "Create Password"
                    is SetupState.ConfirmPassword -> "Confirm Password"
                    SetupState.SetupSuccess -> "Setup Successful"
                    else -> ""
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = instructionText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Draw Pattern / PIN Keyboard / Setup Selection Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                SetupState.WelcomePatternRequired -> {
                    // Intro Button
                    Button(
                        onClick = onStart,
                        modifier = Modifier.testTag("start_wizard_button")
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Begin Setup")
                    }
                }
                SetupState.SelectLockType -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    ) {
                        LockTypeCard(
                            title = "Pattern Lock",
                            description = "Draw a visual connected swipe gesture",
                            icon = Icons.Default.Gesture,
                            onClick = { onSelectLockType("pattern") }
                        )
                        LockTypeCard(
                            title = "PIN Lock",
                            description = "Secure 4-digit code using numbers",
                            icon = Icons.Default.Dialpad,
                            onClick = { onSelectLockType("pin") }
                        )
                        LockTypeCard(
                            title = "Password Lock",
                            description = "Alphanumeric characters for security",
                            icon = Icons.Default.VpnKey,
                            onClick = { onSelectLockType("password") }
                        )
                    }
                }
                SetupState.SetFirstPattern, is SetupState.ConfirmPattern -> {
                    Box(modifier = Modifier.size(320.dp), contentAlignment = Alignment.Center) {
                        PatternLockView(
                            modifier = Modifier.fillMaxSize(),
                            state = feedbackState,
                            onPatternComplete = { list ->
                                if (list.size < 4) {
                                    instructionText = "Too short! Connect at least 4 dots."
                                    feedbackState = PatternState.ERROR
                                } else {
                                    onPatternDrawn(list)
                                }
                            }
                        )
                    }
                }
                SetupState.SetFirstPin, is SetupState.ConfirmPin -> {
                    PinPadView(
                        modifier = Modifier.fillMaxWidth(),
                        pinLength = 4,
                        onPinComplete = onPinEntered,
                        resetIdentifier = state
                    )
                }
                SetupState.SetFirstPassword, is SetupState.ConfirmPassword -> {
                    PasswordUnlockView(
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = if (state is SetupState.SetFirstPassword) "Create your password" else "Confirm your password",
                        onPasswordComplete = onPasswordEntered,
                        resetIdentifier = state
                    )
                }
                SetupState.SetupSuccess -> {
                    // Finished Confirm Button
                    Button(
                        onClick = onFinish,
                        modifier = Modifier.testTag("finish_wizard_button")
                    ) {
                        Text("Secure My Device")
                    }
                }
                else -> {}
            }
        }

        if (state is SetupState.ConfirmPattern || state is SetupState.ConfirmPin || state is SetupState.ConfirmPassword) {
            TextButton(
                onClick = onStart,
                modifier = Modifier.testTag("reset_wizard")
            ) {
                Text("Start Over")
            }
        } else {
            Spacer(modifier = Modifier.height(32.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// 2. MASTER DASHBOARD VIEW
@Composable
fun DashboardView(
    viewModel: MainActivityViewModel,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val activity = remember(context) { context as? FragmentActivity }
    val prefs = remember { LockPreferences(context) }
    var reLockTimeoutState by remember { mutableStateOf(prefs.reLockTimeout) }
    val appGridState by viewModel.appGridState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isLoadingApps by viewModel.isLoadingApps.collectAsStateWithLifecycle()
    val filterMode by viewModel.filterMode.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isServiceActiveState by remember { mutableStateOf(viewModel.isServiceActive()) }
    var showDisableShieldConfirmation by remember { mutableStateOf(false) }
    var isBiometricEnabledState by remember { mutableStateOf(viewModel.isBiometricEnabled()) }
    var isIntruderDetectionEnabledState by remember { mutableStateOf(viewModel.isIntruderDetectionEnabled()) }
    var isAutoCleanupEnabledState by remember { mutableStateOf(viewModel.isAutoCleanupEnabled()) }
    var isPremiumUser by remember { mutableStateOf(prefs.isPremiumUser) }
    var showGoPremiumDialog by remember { mutableStateOf(false) }
    var revealedPhotoAlertTimestamps by remember { mutableStateOf(setOf<Long>()) }
    var alertTargetForRewardedAd by remember { mutableStateOf<IntruderAlert?>(null) }
    var alertTargetForShareAd by remember { mutableStateOf<IntruderAlert?>(null) }
    var zoomPhotoAlert by remember { mutableStateOf<IntruderAlert?>(null) }
    var showTransitionInterstitial by remember { mutableStateOf<String?>(null) }
    var showUnlockAllConfirmDialog by remember { mutableStateOf(false) }
    var perAppRelockTargetApp by remember { mutableStateOf<GridAppInfo?>(null) }
    var showPerAppRelockDialog by remember { mutableStateOf(false) }
    val intruderAlerts by viewModel.intruderAlertsFlow.collectAsStateWithLifecycle()
    val allLockedApps by viewModel.lockedAppsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    val cameraPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setIntruderDetectionEnabled(true)
            isIntruderDetectionEnabledState = true
            Toast.makeText(context, "Intruder Detection Enabled with Camera", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.setIntruderDetectionEnabled(false)
            isIntruderDetectionEnabledState = false
            Toast.makeText(context, "Camera permission required for Intruder Selfie", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        // Request Camera permission on app opening so required permissions are achieved upfront
        val hasCameraPerm = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!hasCameraPerm) {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }

        val activityIntent = (context as? android.app.Activity)?.intent
        val selection = activityIntent?.getStringExtra("SELECTION")
        if (selection == "intruder_records") {
            selectedTabIndex = 2
            Toast.makeText(context, "Welcome to Security Intruder Logs", Toast.LENGTH_LONG).show()
            activityIntent.removeExtra("SELECTION")
        }
    }

    // Dynamic state of System Usage Stats Permission
    var hasUsagePermission by remember { mutableStateOf(hasUsageStatsPermission(context)) }
    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    var appToVerifyForUnlock by remember { mutableStateOf<GridAppInfo?>(null) }
    var isVerifyingToReset by remember { mutableStateOf(false) }
    var isVerifyingToUnlockAll by remember { mutableStateOf(false) }

    if (appToVerifyForUnlock != null) {
        val app = appToVerifyForUnlock!!
        Dialog(
            onDismissRequest = { appToVerifyForUnlock = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                LockVerifyScreen(
                    prefs = LockPreferences(context),
                    packageName = app.packageName,
                    onSuccess = {
                        viewModel.toggleAppLock(app.packageName, app.appName, false)
                        appToVerifyForUnlock = null
                        Toast.makeText(context, "${app.appName} Unprotected", Toast.LENGTH_SHORT).show()
                    },
                    onCancel = {
                        appToVerifyForUnlock = null
                    },
                    title = "Unprotect App",
                    subtitle = "Verify credential to unprotect this application"
                )
            }
        }
    }

    if (isVerifyingToReset) {
        Dialog(
            onDismissRequest = { isVerifyingToReset = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                LockVerifyScreen(
                    prefs = LockPreferences(context),
                    onSuccess = {
                        isVerifyingToReset = false
                        viewModel.resetSetupWizard()
                        Toast.makeText(context, "Old Lock Cleared. Resetting...", Toast.LENGTH_SHORT).show()
                    },
                    onCancel = {
                        isVerifyingToReset = false
                    },
                    title = "Confirm Reset",
                    subtitle = "Verify existing credential to clear screen lock"
                )
            }
        }
    }

    if (showGoPremiumDialog) {
        com.example.ui.components.GoPremiumDialog(
            prefs = prefs,
            onDismiss = { showGoPremiumDialog = false },
            onPremiumPurchased = {
                isPremiumUser = true
                showGoPremiumDialog = false
            }
        )
    }

    if (showPerAppRelockDialog && perAppRelockTargetApp != null) {
        val targetApp = perAppRelockTargetApp!!
        PerAppRelockDialog(
            appInfo = targetApp,
            globalTimeout = reLockTimeoutState,
            currentPerAppTimeout = prefs.getPerAppRelockTimeout(targetApp.packageName),
            onDismiss = {
                showPerAppRelockDialog = false
                perAppRelockTargetApp = null
            },
            onSave = { selectedTimeout ->
                prefs.setPerAppRelockTimeout(targetApp.packageName, selectedTimeout)
                showPerAppRelockDialog = false
                perAppRelockTargetApp = null
                val label = when (selectedTimeout) {
                    "immediately" -> "Immediately"
                    "15_sec" -> "15 Seconds"
                    "30_sec" -> "30 Seconds"
                    "1_min" -> "1 Minute"
                    "5_min" -> "5 Minutes"
                    else -> "Global Default"
                }
                Toast.makeText(context, "${targetApp.appName} custom re-lock: $label", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (alertTargetForRewardedAd != null) {
        com.example.ui.components.RewardedAdDialog(
            adTitle = "Reveal Intruder Snapshot",
            onRewardGranted = {
                val alert = alertTargetForRewardedAd
                if (alert != null) {
                    revealedPhotoAlertTimestamps = revealedPhotoAlertTimestamps + alert.timestamp
                }
                alertTargetForRewardedAd = null
                Toast.makeText(context, "Intruder photo unlocked!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { alertTargetForRewardedAd = null }
        )
    }

    if (alertTargetForShareAd != null) {
        com.example.ui.components.RewardedAdDialog(
            adTitle = "Share Intruder Snapshot",
            onRewardGranted = {
                val alert = alertTargetForShareAd
                if (alert != null) {
                    shareIntruderSnapshot(context, alert)
                }
                alertTargetForShareAd = null
            },
            onDismiss = { alertTargetForShareAd = null }
        )
    }

    if (showTransitionInterstitial != null) {
        com.example.ui.components.InterstitialAdDialog(
            actionTitle = showTransitionInterstitial!!,
            onAdDismissed = { showTransitionInterstitial = null }
        )
    }

    if (showUnlockAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showUnlockAllConfirmDialog = false },
            icon = { Icon(Icons.Default.LockOpen, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Unlock All Apps") },
            text = { Text("Do you want to unlock all protected apps?") },
            confirmButton = {
                Button(
                    onClick = {
                        showUnlockAllConfirmDialog = false
                        isVerifyingToUnlockAll = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Unlock All")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showUnlockAllConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDisableShieldConfirmation) {
        AlertDialog(
            onDismissRequest = { showDisableShieldConfirmation = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.ShieldMoon,
                    contentDescription = "Shield Protection Warning",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Turn Off Background Shield?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Turning off Background Shield exposes your device to security risks:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "• Paused Protection: Real-time app interception will be temporarily stopped.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "• Privacy Warning: Anyone can open locked apps without PIN or Pattern.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "• Disabled Snapshots: Intruder selfie capture & break-in alerts will be disabled.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    Text(
                        text = "Keep active for continuous 24/7 protection.",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDisableShieldConfirmation = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Keep Shield Active", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDisableShieldConfirmation = false
                        isServiceActiveState = false
                        viewModel.setServiceActive(false)
                        val intent = Intent(context, AppLockService::class.java)
                        context.stopService(intent)
                        Toast.makeText(context, "Locker service stopped", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(
                        text = "Turn Off Anyway",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.testTag("disable_shield_confirmation_dialog")
        )
    }

    if (isVerifyingToUnlockAll) {
        Dialog(
            onDismissRequest = { isVerifyingToUnlockAll = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                LockVerifyScreen(
                    prefs = LockPreferences(context),
                    onSuccess = {
                        isVerifyingToUnlockAll = false
                        viewModel.unlockAllApps()
                        Toast.makeText(context, "All applications unlocked", Toast.LENGTH_SHORT).show()
                    },
                    onCancel = {
                        isVerifyingToUnlockAll = false
                    },
                    title = "Unlock All Applications",
                    subtitle = "Verify PIN, Pattern or Biometrics to unlock all apps"
                )
            }
        }
    }

    // Fullscreen Intruder Photo Dialog
    if (zoomPhotoAlert != null) {
        val alert = zoomPhotoAlert!!
        Dialog(
            onDismissRequest = { zoomPhotoAlert = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Intruder Photo Detail",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { zoomPhotoAlert = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val photoBitmap = remember(alert.photoPath) {
                        if (alert.photoPath.isNotEmpty()) com.example.security.EncryptedFileManager.decryptFileToBitmap(java.io.File(alert.photoPath)) else null
                    }
                    if (photoBitmap != null) {
                        coil.compose.AsyncImage(
                            model = photoBitmap,
                            contentDescription = "Full Intruder Snapshot",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(360.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.DarkGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Snapshot image file not available", color = Color.LightGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Target: ${alert.attemptedPackage ?: "App Locker"}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Timestamp: ${java.text.SimpleDateFormat("MMM dd, yyyy - hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(alert.timestamp))}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Auth method attempted: ${alert.lockType.uppercase(java.util.Locale.US)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val currentAlert = zoomPhotoAlert
                                    if (currentAlert != null) {
                                        if (isPremiumUser) {
                                            shareIntruderSnapshot(context, currentAlert)
                                        } else {
                                            alertTargetForShareAd = currentAlert
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("dialog_share_snapshot_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Share Snapshot")
                            }
                        }
                    }
                }
            }
        }
    }

    // Ensure Service starts when permissions are available and service is active
    LaunchedEffect(hasUsagePermission, hasOverlayPermission, isServiceActiveState) {
        if (hasUsagePermission && hasOverlayPermission && isServiceActiveState) {
            val intent = Intent(context, AppLockService::class.java)
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Observe lifecycle events to verify permission when returning from System Android Settings
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasUsagePermission = hasUsageStatsPermission(context)
                hasOverlayPermission = Settings.canDrawOverlays(context)
                // Check if Service should be restarted if permission is now granted
                if (hasUsagePermission && hasOverlayPermission && isServiceActiveState) {
                    val intent = Intent(context, AppLockService::class.java)
                    try {
                        ContextCompat.startForegroundService(context, intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val lockedApps = remember(allLockedApps, searchQuery) {
        val trimmedQuery = searchQuery.trim()
        val filtered = if (trimmedQuery.isEmpty()) {
            allLockedApps
        } else {
            allLockedApps.filter { it.appName.contains(trimmedQuery, ignoreCase = true) }
        }
        filtered.sortedWith(AlphanumericComparator.LOCKED_APP_COMPARATOR)
    }

    val onLockToggledRemembered = remember(viewModel, context, hasUsagePermission, hasOverlayPermission, isServiceActiveState) {
        { appInfo: GridAppInfo, locked: Boolean ->
            if (!locked) {
                // Toggling off (Unprotecting) -> Requires credential validation!
                appToVerifyForUnlock = appInfo
            } else {
                // Toggling on (Protecting) does not require validation, lock right away!
                viewModel.toggleAppLock(appInfo.packageName, appInfo.appName, true)
                if (hasUsagePermission && hasOverlayPermission && isServiceActiveState) {
                    val intent = Intent(context, AppLockService::class.java)
                    try {
                        ContextCompat.startForegroundService(context, intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
            .imePadding()
    ) {
        // App Header Toolbar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isServiceActiveState && hasUsagePermission && hasOverlayPermission)
                                    Color(0xFFE8F5E9)
                                else Color(0xFFFFEBEE)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isServiceActiveState && hasUsagePermission && hasOverlayPermission)
                                Icons.Default.Shield
                            else Icons.Default.ShieldMoon,
                            contentDescription = "Shield State icon",
                            tint = if (isServiceActiveState && hasUsagePermission && hasOverlayPermission)
                                Color(0xFF2E7D32)
                            else Color(0xFFC62828),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "App Locker",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isServiceActiveState && hasUsagePermission && hasOverlayPermission) "Shield Active" else "Shield Sleeping",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isServiceActiveState && hasUsagePermission && hasOverlayPermission)
                                Color(0xFF2E7D32)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isPremiumUser) {
                        Surface(
                            onClick = { showGoPremiumDialog = true },
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.testTag("toolbar_go_premium_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = "Go Premium",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "PRO",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    FancyThemeToggle(
                        isDarkMode = isDarkMode,
                        onToggle = { onDarkModeChange(!isDarkMode) }
                    )

                    // Reset Action Button
                    IconButton(
                        onClick = { isVerifyingToReset = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.SettingsBackupRestore,
                            contentDescription = "Reset pattern lock"
                        )
                    }
                }
            }
        }

        // STATS OVERVIEW CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${lockedApps.size}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Locked",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(modifier = Modifier.width(1.dp).height(28.dp).background(MaterialTheme.colorScheme.outlineVariant))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isServiceActiveState && hasUsagePermission && hasOverlayPermission) "ACTIVE" else "OFF",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isServiceActiveState && hasUsagePermission && hasOverlayPermission) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                    Text(
                        text = "Protection",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(modifier = Modifier.width(1.dp).height(28.dp).background(MaterialTheme.colorScheme.outlineVariant))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${intruderAlerts.size}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (intruderAlerts.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Intruders",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // MATERIAL 3 TAB ROW NAVIGATION
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    selectedTabIndex = 0
                },
                text = { Text("Apps", fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal) },
                icon = { Icon(Icons.Default.Apps, contentDescription = "Apps tab") },
                modifier = Modifier.testTag("tab_apps")
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    selectedTabIndex = 1
                },
                text = { Text("Security", fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal) },
                icon = { Icon(Icons.Default.Security, contentDescription = "Security tab") },
                modifier = Modifier.testTag("tab_security")
            )
            Tab(
                selected = selectedTabIndex == 2,
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    selectedTabIndex = 2
                },
                text = {
                    if (intruderAlerts.isNotEmpty()) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = MaterialTheme.colorScheme.error) {
                                    Text("${intruderAlerts.size}")
                                }
                            }
                        ) {
                            Text("Logs", fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal)
                        }
                    } else {
                        Text("Logs", fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal)
                    }
                },
                icon = { Icon(Icons.Default.CameraAlt, contentDescription = "Intruder Logs tab") },
                modifier = Modifier.testTag("tab_intruder_logs")
            )
        }

        // TAB CONTENT SECTIONS
        when (selectedTabIndex) {
            0 -> {
                // TAB 0: APPS TAB
                Column(modifier = Modifier.fillMaxSize()) {
                    // System Permissions Banner on Apps Screen if permissions are missing
                    if (!hasUsagePermission || !hasOverlayPermission) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Warning",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "System Permission Required",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "App Locker needs system access to detect app launches and display the lock screen on protected apps.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (!hasUsagePermission) {
                                        Button(
                                            onClick = {
                                                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.onErrorContainer,
                                                contentColor = MaterialTheme.colorScheme.errorContainer
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("grant_usage_permission_apps_tab_button")
                                        ) {
                                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Grant Usage Access Permission")
                                        }
                                    }
                                    if (!hasOverlayPermission) {
                                        Button(
                                            onClick = {
                                                try {
                                                    val intent = Intent(
                                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                        Uri.parse("package:${context.packageName}")
                                                    ).apply {
                                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                    }
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                    }
                                                    context.startActivity(intent)
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.onErrorContainer,
                                                contentColor = MaterialTheme.colorScheme.errorContainer
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("grant_overlay_permission_apps_tab_button")
                                        ) {
                                            Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Grant Display Overlay Permission")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Quick Action Row & Search
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        if (lockedApps.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { showUnlockAllConfirmDialog = true },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Unlock All", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Search system applications...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        viewModel.updateSearchQuery("")
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_app_text_field"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        )

                        // FILTER CHIPS ROW
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = filterMode == AppFilterMode.ALL,
                                onClick = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    viewModel.setFilterMode(AppFilterMode.ALL)
                                },
                                label = { Text("All Apps") }
                            )
                            FilterChip(
                                selected = filterMode == AppFilterMode.LOCKED,
                                onClick = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    viewModel.setFilterMode(AppFilterMode.LOCKED)
                                },
                                label = { Text("Locked (${lockedApps.size})") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                            FilterChip(
                                selected = filterMode == AppFilterMode.UNLOCKED,
                                onClick = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    viewModel.setFilterMode(AppFilterMode.UNLOCKED)
                                },
                                label = { Text("Unlocked") }
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isLoadingApps) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        } else if (appGridState.isEmpty()) {
                            item {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SearchOff,
                                        contentDescription = "Search empty",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No Applications Found",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Try adjusting your search query or filter.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        } else {
                            itemsIndexed(appGridState, key = { _, item -> item.packageName }) { index, appInfo ->
                                if (!isPremiumUser && index > 0 && index % 8 == 0) {
                                    com.example.ui.components.AdMobNativeCard(
                                        isPremium = isPremiumUser,
                                        onGoPremiumClick = { showGoPremiumDialog = true },
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                                AppRowItem(
                                    appInfo = appInfo,
                                    isPremiumUser = isPremiumUser,
                                    onLockToggled = onLockToggledRemembered,
                                    onPerAppRelockClick = { targetApp ->
                                        if (!isPremiumUser) {
                                            showGoPremiumDialog = true
                                        } else {
                                            perAppRelockTargetApp = targetApp
                                            showPerAppRelockDialog = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // TAB 1: SECURITY & SETTINGS TAB
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // STEP 1: PERMISSION SHIELD CARD
                    if (!hasUsagePermission) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Warning",
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "System Access Required",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "App Locker requires 'Usage Access' permission to check which app is in the foreground and intercept it. Please grant it in system settings.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.onErrorContainer,
                                            contentColor = MaterialTheme.colorScheme.errorContainer
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("grant_permission_button")
                                    ) {
                                        Text("Grant Usage Permission")
                                    }
                                }
                            }
                        }
                    }

                    // STEP 1.5: DISPLAY LOCK OVERLAY ACCESS CARD
                    if (hasUsagePermission && !hasOverlayPermission) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Warning",
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Overlay Permission Required",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "App Locker requires the 'Display over other apps' system permission to draw the lock screen on top of locked applications when they launch.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            try {
                                                val intent = Intent(
                                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                    Uri.parse("package:${context.packageName}")
                                                ).apply {
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                context.startActivity(intent)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                            contentColor = MaterialTheme.colorScheme.tertiaryContainer
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("grant_overlay_permission_button")
                                    ) {
                                        Text("Grant Overlay Permission")
                                    }
                                }
                            }
                        }
                    }

                    // STEP 2: CONTROLS SETTINGS CARD
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Security Policies",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // ACTIVE SERVICE TOGGLE
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Background App Shield",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Auto-intercept launches of locked applications",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = isServiceActiveState,
                                        onCheckedChange = { active ->
                                            if (active) {
                                                isServiceActiveState = true
                                                viewModel.setServiceActive(true)
                                                val intent = Intent(context, AppLockService::class.java)
                                                if (hasUsagePermission && hasOverlayPermission) {
                                                    try {
                                                        ContextCompat.startForegroundService(context, intent)
                                                        Toast.makeText(context, "Locker service started", Toast.LENGTH_SHORT).show()
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                        Toast.makeText(context, "Failed to start service: ${e.message}", Toast.LENGTH_LONG).show()
                                                    }
                                                } else {
                                                    Toast.makeText(context, "Grant usage and overlay permissions first!", Toast.LENGTH_LONG).show()
                                                }
                                            } else {
                                                // Ask for confirmation before turning off Background App Shield
                                                showDisableShieldConfirmation = true
                                            }
                                        },
                                        modifier = Modifier.testTag("service_active_switch")
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Fingerprint Unlocking",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Allow unlocking applications using your fingerprint scanner",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = isBiometricEnabledState,
                                        onCheckedChange = { active ->
                                            isBiometricEnabledState = active
                                            viewModel.setBiometricEnabled(active)
                                            if (!isPremiumUser) {
                                                showTransitionInterstitial = "Biometric Lock Updated"
                                            }
                                            Toast.makeText(context, if (active) "Fingerprint unlock active" else "Fingerprint unlock deactivated", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.testTag("biometric_active_switch")
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Intruder Camera Detection",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Snap front-facing photo on 3 failed unlock attempts",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = isIntruderDetectionEnabledState,
                                        onCheckedChange = { active ->
                                            if (active) {
                                                val granted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                                if (granted) {
                                                    isIntruderDetectionEnabledState = true
                                                    viewModel.setIntruderDetectionEnabled(true)
                                                    if (!isPremiumUser) {
                                                        showTransitionInterstitial = "Intruder Detection Armed"
                                                    }
                                                    Toast.makeText(context, "Intruder Detection fully armed!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                                }
                                            } else {
                                                isIntruderDetectionEnabledState = false
                                                viewModel.setIntruderDetectionEnabled(false)
                                                if (!isPremiumUser) {
                                                    showTransitionInterstitial = "Intruder Detection Disarmed"
                                                }
                                                Toast.makeText(context, "Intruder Detection disarmed", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.testTag("intruder_detection_active_switch")
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Protect System Settings",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Prevent force-stopping or revoking app locker permissions",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    val isSettingsLocked = remember(allLockedApps) { allLockedApps.any { it.packageName == "com.android.settings" } }
                                    Switch(
                                        checked = isSettingsLocked,
                                        onCheckedChange = { active ->
                                            val settingsApp = appGridState.firstOrNull { it.packageName == "com.android.settings" }
                                            val appName = settingsApp?.appName ?: "Settings"
                                            viewModel.toggleAppLock("com.android.settings", appName, active)
                                            if (!isPremiumUser) {
                                                showTransitionInterstitial = "System Settings Protection"
                                            }
                                            Toast.makeText(
                                                context,
                                                if (active) "System Settings Protected!" else "System Settings Protection Removed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        modifier = Modifier.testTag("protect_settings_switch")
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Global Re-Lock Timeout",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Determines when unlocked apps lock again after going to the background. Applies globally to all apps.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    var dropdownExpanded by remember { mutableStateOf(false) }
                                    val relockOptions = listOf(
                                        "immediately" to "Immediately",
                                        "15_sec" to "15 Seconds",
                                        "30_sec" to "30 Seconds",
                                        "1_min" to "1 Minute (Default)",
                                        "5_min" to "5 Minutes"
                                    )
                                    val selectedOptionLabel = relockOptions.find { it.first == reLockTimeoutState }?.second ?: "1 Minute (Default)"

                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedCard(
                                            onClick = { dropdownExpanded = true },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("relock_timeout_dropdown")
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "Re-Lock Policy",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = selectedOptionLabel,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Default.ArrowDropDown,
                                                    contentDescription = "Select timeout dropdown",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = dropdownExpanded,
                                            onDismissRequest = { dropdownExpanded = false },
                                            modifier = Modifier
                                                .fillMaxWidth(0.85f)
                                                .background(MaterialTheme.colorScheme.surface)
                                        ) {
                                            relockOptions.forEach { (key, label) ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = label,
                                                            fontWeight = if (reLockTimeoutState == key) FontWeight.Bold else FontWeight.Normal,
                                                            color = if (reLockTimeoutState == key) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                        )
                                                    },
                                                    onClick = {
                                                        reLockTimeoutState = key
                                                        prefs.reLockTimeout = key
                                                        dropdownExpanded = false
                                                        Toast.makeText(context, "Re-Lock Timeout: $label", Toast.LENGTH_SHORT).show()
                                                    },
                                                    leadingIcon = {
                                                        if (reLockTimeoutState == key) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary
                                                            )
                                                        } else null
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ADMOB BANNER
                    if (!isPremiumUser) {
                        item {
                            com.example.ui.components.AdMobBanner(
                                isPremium = isPremiumUser,
                                onGoPremiumClick = { showGoPremiumDialog = true }
                            )
                        }
                    }


                }
            }

            2 -> {
                // TAB 2: INTRUDER LOGS TAB
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Camera",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Security Intruder Logs",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    if (intruderAlerts.isNotEmpty()) {
                                        TextButton(
                                            onClick = {
                                                viewModel.clearAllIntruderAlerts()
                                                Toast.makeText(context, "All security logs cleared", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Text("Clear All", color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auto_cleanup_card")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Auto Delete Logs (30 Days)",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "PAID",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "Automatically purge intruder snapshots older than 30 days",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Switch(
                                            checked = isAutoCleanupEnabledState && isPremiumUser,
                                            onCheckedChange = { active ->
                                                if (!isPremiumUser) {
                                                    showGoPremiumDialog = true
                                                    Toast.makeText(context, "Auto Delete after 30 days is a Paid Feature. Upgrade to Premium!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    isAutoCleanupEnabledState = active
                                                    viewModel.setAutoCleanupEnabled(active)
                                                    Toast.makeText(context, if (active) "Auto Delete (30 days) enabled" else "Auto Delete disabled", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.testTag("auto_cleanup_logs_switch")
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (!isIntruderDetectionEnabledState) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("intruder_detection_off_banner")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = "Warning",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Intruder Detection is OFF",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Intruder photos will not be captured on failed unlock attempts.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = {
                                                    val granted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                                    if (granted) {
                                                        isIntruderDetectionEnabledState = true
                                                        viewModel.setIntruderDetectionEnabled(true)
                                                        Toast.makeText(context, "Intruder Detection Enabled", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                modifier = Modifier.align(Alignment.End)
                                            ) {
                                                Text("Turn ON Intruder Detection", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                if (intruderAlerts.isEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Shield SECURE",
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "No intrusion attempts captured yet",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                if (!isPremiumUser) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    com.example.ui.components.AdMobBanner(
                                        isPremium = isPremiumUser,
                                        onGoPremiumClick = { showGoPremiumDialog = true }
                                    )
                                }
                            }
                        }
                    }

                    if (intruderAlerts.isNotEmpty()) {
                        items(intruderAlerts, key = { it.timestamp }) { alert ->
                            val isPhotoUnlocked = revealedPhotoAlertTimestamps.contains(alert.timestamp)
                            IntruderAlertItem(
                                alert = alert,
                                isPremiumUser = isPremiumUser,
                                isPhotoUnlocked = isPhotoUnlocked,
                                onZoomPhoto = { zoomPhotoAlert = it },
                                onWatchAdForPhoto = { targetAlert ->
                                    triggerVaultBiometricAuth(
                                        context = context,
                                        alert = targetAlert,
                                        onSuccess = {
                                            revealedPhotoAlertTimestamps = revealedPhotoAlertTimestamps + targetAlert.timestamp
                                        },
                                        onFallbackAd = {
                                            alertTargetForRewardedAd = targetAlert
                                        }
                                    )
                                },
                                onShare = {
                                    if (isPremiumUser) {
                                        shareIntruderSnapshot(context, alert)
                                    } else {
                                        alertTargetForShareAd = alert
                                    }
                                },
                                onDelete = { viewModel.deleteIntruderAlert(it) }
                            )
                        }
                    }

                }
            }
        }
    }
}

fun triggerVaultBiometricAuth(
    context: android.content.Context,
    alert: IntruderAlert,
    onSuccess: () -> Unit,
    onFallbackAd: () -> Unit
) {
    val fa = context.findActivity() ?: (context as? androidx.fragment.app.FragmentActivity)
    if (fa != null) {
        val biometricManager = androidx.biometric.BiometricManager.from(fa)
        val authenticators = androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK or
                androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL

        if (biometricManager.canAuthenticate(authenticators) == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) {
            val executor = androidx.core.content.ContextCompat.getMainExecutor(fa)
            val biometricPrompt = androidx.biometric.BiometricPrompt(
                fa,
                executor,
                object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        Toast.makeText(fa, "Vault Unlocked 🔓", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        if (errorCode == androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                            errorCode == androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED
                        ) {
                            onFallbackAd()
                        } else if (errorCode != androidx.biometric.BiometricPrompt.ERROR_CANCELED) {
                            Toast.makeText(fa, "Biometric error: $errString", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(fa, "Biometric verification failed", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                .setTitle("Intruder Vault Lock 🔒")
                .setSubtitle("Confirm Fingerprint, Face, or PIN to unblur photo")
                .setAllowedAuthenticators(authenticators)
                .build()

            try {
                biometricPrompt.authenticate(promptInfo)
            } catch (e: Exception) {
                onFallbackAd()
            }
        } else {
            onFallbackAd()
        }
    } else {
        onFallbackAd()
    }
}

fun shareIntruderSnapshot(context: android.content.Context, alert: IntruderAlert) {
    try {
        val dateStr = java.text.SimpleDateFormat("MMM dd, yyyy - hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(alert.timestamp))
        val appName = alert.attemptedPackage ?: "App Locker"
        val shareText = "🚨 Intruder Alert Snapshot!\nTarget App: $appName\nTime: $dateStr\nAuthentication Method: ${alert.lockType.uppercase(java.util.Locale.US)}"

        val photoFile = if (alert.photoPath.isNotEmpty()) java.io.File(alert.photoPath) else null
        val shareFile = if (photoFile != null && photoFile.exists()) {
            com.example.security.EncryptedFileManager.getDecryptedTempFileForShare(context, photoFile)
        } else null

        if (shareFile != null && shareFile.exists()) {
            val contentUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                shareFile
            )
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                clipData = android.content.ClipData.newRawUri("Intruder Photo", contentUri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooserIntent = android.content.Intent.createChooser(shareIntent, "Share Intruder Snapshot").apply {
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(chooserIntent)
        } else {
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Intruder Alert"))
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to share snapshot: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

class DrawablePainter(private val drawable: android.graphics.drawable.Drawable) : androidx.compose.ui.graphics.painter.Painter() {
    override val intrinsicSize: androidx.compose.ui.geometry.Size
        get() = androidx.compose.ui.geometry.Size(
            drawable.intrinsicWidth.toFloat().coerceAtLeast(1f),
            drawable.intrinsicHeight.toFloat().coerceAtLeast(1f)
        )

    override fun androidx.compose.ui.graphics.drawscope.DrawScope.onDraw() {
        drawIntoCanvas { canvas ->
            drawable.setBounds(0, 0, size.width.toInt(), size.height.toInt())
            drawable.draw(canvas.nativeCanvas)
        }
    }
}

@Composable
fun PerAppRelockDialog(
    appInfo: GridAppInfo,
    globalTimeout: String,
    currentPerAppTimeout: String?,
    onDismiss: () -> Unit,
    onSave: (String?) -> Unit
) {
    val globalLabel = when (globalTimeout) {
        "immediately" -> "Immediately"
        "15_sec" -> "15 Seconds"
        "30_sec" -> "30 Seconds"
        "1_min" -> "1 Minute"
        "5_min" -> "5 Minutes"
        else -> "1 Minute"
    }

    var selectedOption by remember { mutableStateOf(currentPerAppTimeout ?: "global") }

    val options = listOf(
        "global" to "Use Global Setting ($globalLabel)",
        "immediately" to "Immediately",
        "15_sec" to "15 Seconds",
        "30_sec" to "30 Seconds",
        "1_min" to "1 Minute (Default)",
        "5_min" to "5 Minutes"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "PRO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Text(text = "Custom Re-Lock", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = appInfo.appName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select custom re-lock timeout when leaving ${appInfo.appName}:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                options.forEach { (key, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = key }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = (selectedOption == key),
                            onClick = { selectedOption = key }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (selectedOption == key) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedOption == key) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(if (selectedOption == "global") null else selectedOption)
                },
                modifier = Modifier.testTag("save_per_app_relock_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AppRowItem(
    appInfo: GridAppInfo,
    isPremiumUser: Boolean,
    onLockToggled: (GridAppInfo, Boolean) -> Unit,
    onPerAppRelockClick: (GridAppInfo) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { LockPreferences(context) }
    var perAppTimeout by remember(appInfo.packageName, isPremiumUser) {
        mutableStateOf(if (isPremiumUser) prefs.getPerAppRelockTimeout(appInfo.packageName) else null)
    }
    var appIcon by remember(appInfo.packageName) { 
        mutableStateOf<android.graphics.drawable.Drawable?>(AppIconCache.get(appInfo.packageName)) 
    }

    LaunchedEffect(appInfo.packageName, isPremiumUser) {
        perAppTimeout = if (isPremiumUser) prefs.getPerAppRelockTimeout(appInfo.packageName) else null
        if (appIcon != null) return@LaunchedEffect
        
        val pm = context.packageManager
        val icon = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                pm.getApplicationIcon(appInfo.packageName)
            } catch (e: Exception) {
                null
            }
        }
        if (icon != null) {
            AppIconCache.put(appInfo.packageName, icon)
            appIcon = icon
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_row_${appInfo.packageName}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            // Render App System Icon directly for vector adaptive compatibility in Android
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                val currentIcon = appIcon
                if (currentIcon != null) {
                    Image(
                        painter = DrawablePainter(currentIcon),
                        contentDescription = "App Icon",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = "Standard Android icon",
                        tint = Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = appInfo.appName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (appInfo.isLocked) {
                    val badgeLabel = when (perAppTimeout) {
                        "immediately" -> "Immediately"
                        "15_sec" -> "15s custom"
                        "30_sec" -> "30s custom"
                        "1_min" -> "1m custom"
                        "5_min" -> "5m custom"
                        else -> "Global timeout"
                    }
                    Text(
                        text = badgeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (perAppTimeout != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            if (appInfo.isLocked) {
                Surface(
                    onClick = { onPerAppRelockClick(appInfo) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (perAppTimeout != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .testTag("per_app_relock_button_${appInfo.packageName}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Custom Relock Timer",
                            tint = if (perAppTimeout != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        if (!isPremiumUser) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "PRO Feature",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                        } else {
                            Text(
                                text = when (perAppTimeout) {
                                    "immediately" -> "0s"
                                    "15_sec" -> "15s"
                                    "30_sec" -> "30s"
                                    "1_min" -> "1m"
                                    "5_min" -> "5m"
                                    else -> "Global"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (perAppTimeout != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = { onLockToggled(appInfo, !appInfo.isLocked) },
                modifier = Modifier.testTag("lock_toggle_${appInfo.packageName}")
            ) {
                Icon(
                    imageVector = if (appInfo.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = if (appInfo.isLocked) "Lock applied" else "App is unlocked",
                    tint = if (appInfo.isLocked)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// System Checker programmatic permission validator
@Suppress("DEPRECATION")
private fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
    } else {
        @Suppress("DEPRECATION")
        appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

@Composable
fun IntruderAlertItem(
    alert: IntruderAlert,
    isPremiumUser: Boolean,
    isPhotoUnlocked: Boolean,
    onZoomPhoto: (IntruderAlert) -> Unit,
    onWatchAdForPhoto: (IntruderAlert) -> Unit,
    onShare: (IntruderAlert) -> Unit,
    onDelete: (IntruderAlert) -> Unit
) {
    val context = LocalContext.current
    var attemptedAppName by remember(alert.attemptedPackage) {
        mutableStateOf(alert.attemptedPackage ?: "App Locker Settings")
    }
    LaunchedEffect(alert.attemptedPackage) {
        if (!alert.attemptedPackage.isNullOrEmpty()) {
            val name = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val pm = context.packageManager
                    val info = pm.getApplicationInfo(alert.attemptedPackage, 0)
                    pm.getApplicationLabel(info).toString()
                } catch (e: Exception) {
                    alert.attemptedPackage
                }
            }
            attemptedAppName = name
        } else {
            attemptedAppName = "App Locker Settings"
        }
    }

    val dateStr = remember(alert.timestamp) {
        java.text.SimpleDateFormat("MMM dd, yyyy - hh:mm a", java.util.Locale.getDefault())
            .format(java.util.Date(alert.timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (alert.photoPath.isNotEmpty()) {
            val decryptedBitmap = remember(alert.photoPath) {
                com.example.security.EncryptedFileManager.decryptFileToBitmap(java.io.File(alert.photoPath))
            }
            if (isPhotoUnlocked && decryptedBitmap != null) {
                coil.compose.AsyncImage(
                    model = decryptedBitmap,
                    contentDescription = "Intruder snapshot",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .clickable { onZoomPhoto(alert) },
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Surface(
                    onClick = { onWatchAdForPhoto(alert) },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked photo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Watch Ad",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Photo,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Target: $attemptedAppName",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Cred type: ${alert.lockType.uppercase(java.util.Locale.US)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Light
            )
        }

        IconButton(
            onClick = { onShare(alert) },
            modifier = Modifier.testTag("share_snapshot_button")
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share snapshot",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        IconButton(
            onClick = { onDelete(alert) },
            modifier = Modifier.testTag("delete_snapshot_button")
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete record",
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
            )
        }
    }
}
