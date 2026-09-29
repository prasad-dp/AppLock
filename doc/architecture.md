# App Locker Pro — Architecture Blueprint

## 1. Architectural Style & Design Principles
App Locker Pro follows **Clean Architecture** combined with **MVVM (Model-View-ViewModel)** and **Unidirectional Data Flow (UDF)**. 

```
┌────────────────────────────────────────────────────────┐
│                   UI Layer (Jetpack Compose)           │
│         MainActivity, UnlockActivity, PatternLockView   │
└───────────────────────────▲────────────────────────────┘
                            │ StateFlow / Events
┌───────────────────────────┴────────────────────────────┐
│                   ViewModel Layer                      │
│             MainActivityViewModel, Repository          │
└───────────────────────────▲────────────────────────────┘
                            │ Coroutines / Flow
┌───────────────────────────┴────────────────────────────┐
│                    Service & Core Layer                │
│  AppLockAccessibilityService | AppLockService          │
│  AppLockNotificationListenerService | AppLockSession   │
└───────────────────────────▲────────────────────────────┘
                            │ Room / KeyStore / SharedPreferences
┌───────────────────────────┴────────────────────────────┐
│                     Data Layer                         │
│  AppDatabase (Room) | EncryptedSharedPreferences      │
│  EncryptedFileManager (AES-256 GCM) | CameraHelper     │
└────────────────────────────────────────────────────────┘
```

---

## 2. Dual-Engine Interception Pipeline

To achieve true **0ms instant locking** without screen flashing or glimpses while supporting devices where accessibility permissions are restricted, the app uses a dual-engine architecture:

### 2.1 Primary Engine: `AppLockAccessibilityService` (0ms Instant)
- **Mechanism**: Listens to system window transitions via `AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED`.
- **Latency**: **0 ms**. The event is triggered by the Android Window Manager before the target application draws its first frame.
- **Verification**:
  - Validates `rootInActiveWindow` and `windows` hierarchy via `AppLockPackageHelper.isAppTargetInActiveForeground`.
  - Discards transient system overlays (soft keyboards, autofill, permission dialogues, biometric prompts).
  - Detects Home launcher packages (`isLauncherPackage`) and instantly triggers auto-relock policies.
- **Action**: Launches `UnlockActivity` with `FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_SINGLE_TOP`.

### 2.2 Fallback Engine: `AppLockService` (`UsageStatsManager`)
- **Mechanism**: Long-running Foreground Service (Type: `specialUse`) that continuously polls `UsageStatsManager.queryEvents()`.
- **Adaptive Polling Delay**:
  - Pauses (`1200ms`) when screen is powered off (`PowerManager.isInteractive == false`).
  - Low-frequency (`250ms`) when Accessibility Service is active.
  - High-frequency (`15ms - 40ms`) when Accessibility is unavailable and locked apps are active.

---

## 3. Session State Machine (`AppLockSession`)

`AppLockSession` is a thread-safe, in-memory singleton that acts as the single source of truth for unlocked apps:

- **`unlockedApps: MutableSet<String>`**: Active package tokens that are temporarily permitted to run.
- **`unlockTimes` & `lastActiveTimes`**: Real-time timestamps used for auto-relock evaluation.
- **`currentForegroundApp`**: Tracks active package transitions to distinguish internal app navigation from genuine new launches.
- **`lockApp(packageName, force)`**:
  - `force = false`: Protects against transient double-locks while an app displays internal biometric prompts.
  - `force = true`: Instantly revokes session tokens upon home screen return or switch.
- **`clearGoToHome()`**: Clears exit transition flags once the user has landed on the home launcher.

---

## 4. Overlay Architecture: `UnlockActivity`

- **Process & Task Isolation**: Configured with `launchMode="singleInstance"` and a dedicated task affinity `com.example.unlock`.
- **Security Defenses**:
  - `filterTouchesWhenObscured = true`: Prevents tapjacking and overlay attacks.
  - `onBackPressedDispatcher`: Re-routes back navigation to Android Launcher Home (`CATEGORY_HOME`), ensuring the locked app cannot be glimpsed via back-press.
  - `onUserLeaveHint()`: Automatically dismisses overlay if the user deliberately navigates away.
- **Notification Handling**: When opened from a notification (`EXTRA_FROM_NOTIFICATION = true`), successful authentication dismisses masked notifications and seamlessly resumes the target app or pending intent.

---

## 5. Security & Cryptographic Storage Architecture

### 5.1 Android KeyStore & AES-256 GCM
- Cryptographic keys are generated inside the hardware-backed **Android KeyStore** (`KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT`).
- Block mode: `GCM`, Padding: `NoPadding`.
- Captured intruder photos are encrypted **in memory** and written as `.enc` files. Raw unencrypted `.jpg` files are never persisted to disk.

### 5.2 Multi-Pass Secure File Shredding
When an intruder photo is deleted:
1. File length is queried via `RandomAccessFile`.
2. Sector bytes are overwritten with cryptographically secure random bytes (`SecureRandom`).
3. Sector bytes are overwritten with zeros (`0x00`).
4. Buffers are synced to hardware (`fd.sync()`) before file deletion.

### 5.3 EncryptedSharedPreferences
Master credentials, lock types, and sensitivity preferences are persisted in `app_locker_preferences_encrypted` using AES-256 SIV for keys and AES-256 GCM for values.
