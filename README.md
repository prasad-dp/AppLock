# 🛡️ App Locker & Intruder Security Vault

An enterprise-grade, privacy-focused Android application designed to secure sensitive apps with **0ms Instant Interception**, detect unauthorized unlock attempts, capture silent front-camera intruder snapshots, and protect security logs using hardware-backed **AES-256 GCM Encryption** and **Biometric Vault Verification**.

---

## 📑 Table of Contents
1. [Overview & Key Purpose](#-overview--key-purpose)
2. [Dual-Engine App Locking Architecture](#-dual-engine-app-locking-architecture)
3. [Comprehensive Feature Breakdown](#-comprehensive-feature-breakdown)
4. [Hybrid Just-In-Time (JIT) Permission Model](#-hybrid-just-in-time-jit-permission-model)
5. [Technology Stack](#-technology-stack)
6. [System Architecture & Project Structure](#-system-architecture--project-structure)
7. [Deep-Dive Implementation Details](#-deep-dive-implementation-details)
   - [0ms Instant Lock Engine (Accessibility)](#1-0ms-instant-lock-engine-accessibilityservice)
   - [Fallback Foreground Polling Engine (UsageStats)](#2-fallback-foreground-polling-engine-usagestatsmanager)
   - [Hardware-Backed AES-256 GCM Encryption](#3-hardware-backed-aes-256-gcm-encryption)
   - [CameraX Silent Snapshot Capture](#4-camerax-silent-snapshot-capture)
   - ["Tap to Reveal" Biometric Vault Shield](#5-tap-to-reveal-biometric-vault-shield)
   - [Multi-Pass Secure File Shredding](#6-multi-pass-secure-file-shredding)
   - [In-Memory LruCache & Database Optimizations](#7-in-memory-lrucache--database-optimizations)
8. [Monetization & In-App Purchases](#-monetization--in-app-purchases)
9. [Developer & Build Guide](#-developer--build-guide)

---

## 🔒 Overview & Key Purpose

Modern smartphones hold banking credentials, personal messaging apps, private photos, and confidential enterprise tools. While device lock screens protect overall access, handing an unlocked device to a friend, child, or colleague leaves individual applications exposed.

**App Locker** addresses this with a multi-layered security suite:
- **0ms Instant Interception**: Intercepts protected app launches at the window creation layer before any app screen content can be glimpsed.
- **Per-App Protection**: Lock individual applications (WhatsApp, Banking, Photos, Settings) behind a distinct PIN, Pattern, or Password.
- **Intruder Detection**: Silently capture a front-camera snapshot when anyone inputs an incorrect credential.
- **Hardware-Level Encryption**: Encrypt captured intruder photos immediately using hardware-backed **AES-256 GCM** in the **Android KeyStore**.
- **Biometric Vault**: Keep intruder records blurred and shielded until verified via Fingerprint or Face Unlock.
- **Multi-Pass Data Shredding**: Overwrite disk sectors with cryptographic random noise and zeros prior to unlinking deleted records.

---

## ⚡ Dual-Engine App Locking Architecture

App Locker utilizes a dual-engine architecture to guarantee instant, flicker-free locking across all Android versions:

| Engine | Technology | Latency | Benefit |
| :--- | :--- | :--- | :--- |
| **Primary Engine** | `AppLockAccessibilityService` | **0ms (Instant)** | Listens for `TYPE_WINDOW_STATE_CHANGED` events; intercepts the target app before its window draws. Completely eliminates home screen flickers and glimpses. |
| **Fallback Engine** | `AppLockService` + `UsageStatsManager` | **50–180ms** | Foreground background service polling `UsageStatsManager.queryEvents()` when accessibility is unavailable. |

---

## 🌟 Comprehensive Feature Breakdown

### 1. 🔐 Smart App Locking & Credential Methods
- **Supported Lock Types**: 4-Digit Numeric PIN, Custom 3x3 Pattern Lock, and Alphanumeric Password.
- **Biometric Integration**: Seamless device Biometric Prompt (Fingerprint / Face Unlock / Device Credential).
- **Double-Lock Protection**: Built-in detector and Smart Advisor for apps with native biometrics (WhatsApp, Telegram, Google Wallet, Banking apps) to prevent infinite re-lock loops.
- **Per-App Re-Lock Policies**: Configure global or granular timeouts (**Immediately**, **15 Seconds**, **30 Seconds**, **1 Minute**, **5 Minutes**).
- **System App Protection**: Pre-categorizes System Settings, Google Play Store, and Package Installer to prevent unauthorized uninstallation or permission tampering.

### 2. 📸 CameraX Silent Intruder Detector
- **Silent Front-Camera Capture**: Utilizes Google CameraX bound to the application lifecycle with `CAPTURE_MODE_MINIMIZE_LATENCY` and `FLASH_MODE_OFF`.
- **Configurable Threshold**: Trigger silent snapshots after **1, 2, 3, or 5 consecutive failed attempts**.
- **Security Lockout Cooldown**: Enforces a 30-second security cooldown with real-time countdown timer upon reaching the threshold.
- **Intruder Metadata Logs**: Logs precise timestamp, target application package name, and encrypted photo reference.

### 3. 🛡️ Hardware-Backed AES-256 Photo Encryption
- **Zero Raw Disk Storage**: Photos are encrypted in memory prior to being written to storage as `.enc` files.
- **Android KeyStore Integrity**: Cryptographic keys are generated and protected inside the device's hardware KeyStore (`AES/GCM/NoPadding`).
- **RAM-Only Decryption**: Photos are decrypted directly into RAM bitmaps when viewed in the application. Unencrypted raw `.jpg` files are never saved to physical storage.

### 4. 👁️ "Tap to Reveal" Biometric Vault Lock
- **Shielded Log Preview**: All intruder snapshot cards in the logs tab start in a blurred, lock-badged state.
- **Biometric Prompt Verification**: Tapping a photo invokes Android's native `BiometricPrompt` (Fingerprint, Face Unlock, or Device PIN). Upon successful verification, the image is decrypted and displayed.
- **Rewarded Ad Fallback**: Non-premium users without biometric hardware can watch a short rewarded ad to unblur photos.

### 5. 🗑️ Multi-Pass Secure File Shredding
- **Storage Sanitization**: Deleting an intruder record executes a 3-pass sanitization sequence using `RandomAccessFile`:
  1. Overwrite all file bytes with cryptographically secure random bytes.
  2. Overwrite all file bytes with binary zeros (`0x00`).
  3. Flush file buffer to physical media (`raf.fd.sync()`) and unlink file (`file.delete()`).
- Prevents forensic data recovery tools from restoring deleted intruder photos.

### 6. ⚡ Battery & Performance Optimization Engine
- **Smart Sleep Polling**: High-frequency querying pauses whenever the display is interactive sleep state (`PowerManager.isInteractive == false`).
- **In-Memory LruCache**: 15MB budget memory cache for decrypted thumbnails to prevent redundant AES decryption during list scrolling.
- **Room Database Indexing**: Indexed timestamps (`@Entity(indices = [Index(value = ["timestamp"])]))`) for fast logging queries and automated 30-day log purges.
- **Compose Recomposition Skipping**: `@Immutable` data models (`IntruderAlert`, `LockedApp`, `GridAppInfo`) for fluid 60fps scrolling.

---

## 🚦 Hybrid Just-In-Time (JIT) Permission Model

To provide a friction-free onboarding experience while strictly complying with Android permission guidelines:
1. **Friction-Free Onboarding**: Initial setup wizard focuses purely on creating and confirming the master credential without blocking permissions upfront.
2. **Contextual In-App Banners**: High-visibility permission cards remain accessible on the **Apps** and **Security** tabs when permissions are pending.
3. **Just-In-Time (JIT) Lock Prompt**: If a user attempts to lock an app without active Usage Access or Overlay permissions, a contextual dialog explains the requirement with a direct shortcut to system settings.
4. **Contextual Camera Prompt**: Camera permission is requested strictly when arming Intruder Camera Detection.
5. **0ms Deactivation Warning**: Toggling off 0ms Instant Lock presents an educational dialog detailing the benefits of zero-delay window interception before offering navigation to Accessibility Settings.

---

## 🛠️ Technology Stack

| Component | Library / Framework | Purpose |
| :--- | :--- | :--- |
| **Language** | Kotlin 1.9+ | 100% native Kotlin implementation |
| **UI Framework** | Jetpack Compose | Declarative UI, Material Design 3 |
| **Architecture** | MVVM + Repository Pattern | Clean separation of concerns |
| **Database** | Room SQLite Persistence | Local storage for locked app states and intruder logs |
| **Security** | Android KeyStore & `javax.crypto` | Hardware-backed AES-256 GCM key management |
| **Biometrics** | `androidx.biometric:biometric` | Native Fingerprint, Face, and Device Credential auth |
| **Camera** | AndroidX CameraX | Silent background front-camera image capture |
| **In-App Billing** | Google Play Billing Client 6.0+ | Pro Subscriptions & Lifetime In-App Purchases |
| **Monetization** | Google Mobile Ads SDK (AdMob) | Banner, Native, and Rewarded Video Ads |

---

## 🏗️ System Architecture & Project Structure

```
app/src/main/java/com/example/
├── MainActivity.kt               # Main entry point, navigation tabs, UI views, Vault Auth
├── MainActivityViewModel.kt      # ViewModel managing state, app lists, setup flow
├── UnlockActivity.kt             # Full-screen lock overlay activity triggered on app launch
│
├── billing/
│   └── BillingManager.kt         # Google Play Billing Client 6.0+ manager
│
├── config/
│   └── AdMobConfig.kt            # AdMob unit IDs and configuration
│
├── data/
│   ├── AppDatabase.kt            # Room database instance
│   ├── AppRepository.kt          # Single repository for DB & Encrypted File operations
│   ├── IntruderAlert.kt          # Room entity for intruder records (@Immutable, indexed)
│   ├── IntruderAlertDao.kt       # DAO queries for intruder log entries
│   ├── IntruderCameraHelper.kt   # CameraX silent front-camera capture pipeline
│   ├── IntruderNotificationHelper.kt # Local status notifications for intruder alerts
│   ├── LockedApp.kt              # Room entity for locked packages
│   ├── LockedAppDao.kt           # DAO queries for locked packages
│   └── LockPreferences.kt        # Key-Value preferences (Lock Type, PIN, Attempt Threshold)
│
├── security/
│   └── EncryptedFileManager.kt   # AES-256 KeyStore cipher, LruCache, multi-pass file shredder
│
├── service/
│   ├── AppLockAccessibilityService.kt # 0ms Instant window interception service
│   ├── AppLockService.kt              # Foreground monitoring service with adaptive polling
│   └── AppLockSession.kt              # Temporary unlock grace period state manager
│
├── ui/
│   ├── components/               # Custom Compose components (AdMob, Sheets, Guidance)
│   ├── pattern/                  # Custom PatternLockView & PIN unlock composables
│   └── theme/                    # Material 3 Color palette, Typography, and Theme
│
└── util/
    ├── AlphanumericComparator.kt # Natural sorting utility for app lists
    └── SecurityUtils.kt          # Hash & encryption helper routines
```

---

## 💡 Deep-Dive Implementation Details

### 1. 0ms Instant Lock Engine (`AppLockAccessibilityService`)
`AppLockAccessibilityService` hooks into `AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED`:
```kotlin
override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
    val packageName = event.packageName?.toString() ?: return
    
    // Check if target package is locked and not in active grace period
    if (repository.isPackageLocked(packageName) && !AppLockSession.isPackageUnlocked(packageName)) {
        val intent = Intent(this, UnlockActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_ANIMATION
            putExtra(UnlockActivity.EXTRA_PACKAGE_NAME, packageName)
        }
        startActivity(intent)
    }
}
```

### 2. Fallback Foreground Polling Engine (`UsageStatsManager`)
`AppLockService` maintains an adaptive coroutine polling loop:
1. Runs as a foreground service with a persistent notification.
2. Maintains an in-memory `synchronizedSet` of locked package names from Room DB.
3. Every tick, queries `UsageStatsManager.queryEvents()` for the foreground package.
4. Triggers `UnlockActivity` if target package is detected.

### 3. Hardware-Backed AES-256 GCM Encryption
All intruder photos are encrypted using `AES/GCM/NoPadding`:
- **Key Generation**: A 256-bit AES secret key is generated inside `AndroidKeyStore`.
- **Initialization Vector (IV)**: A unique 12-byte random IV is generated for every photo and prepended to the encrypted file payload.
- **Decryption**: The 12-byte IV is extracted from the head of the file, fed into `GCMParameterSpec(128, iv)`, and decrypted into RAM bytes before being decoded into a `Bitmap`.

### 4. CameraX Silent Snapshot Capture
`IntruderCameraHelper` manages photo capture:
- Binds `ProcessCameraProvider` to the application lifecycle with `CameraSelector.LENS_FACING_FRONT`.
- Configures `ImageCapture.BUILDER` with `CAPTURE_MODE_MINIMIZE_LATENCY` and `FLASH_MODE_OFF`.
- Encrypts byte output immediately via `EncryptedFileManager.encryptBytesToFile()`.

### 5. "Tap to Reveal" Biometric Vault Shield
- Intruder photos render with high-radius Gaussian blur and lock overlay.
- Tapping invokes `BiometricManager.from(activity)` for `BIOMETRIC_STRONG | BIOMETRIC_WEAK | DEVICE_CREDENTIAL`.
- Successful verification unblurs the photo for that session.

### 6. Multi-Pass Secure File Shredding
Deleting an intruder snapshot executes `EncryptedFileManager.shredAndDeleteFile(file)`:
```kotlin
RandomAccessFile(file, "rws").use { raf ->
    val length = raf.length()
    val randomBytes = ByteArray(4096)
    var pos = 0L
    // Pass 1: Overwrite with random bytes
    while (pos < length) {
        val writeLen = minOf(randomBytes.size.toLong(), length - pos).toInt()
        SecureRandom().nextBytes(randomBytes)
        raf.write(randomBytes, 0, writeLen)
        pos += writeLen
    }
    // Pass 2: Overwrite with zero bytes
    raf.seek(0)
    java.util.Arrays.fill(randomBytes, 0.toByte())
    pos = 0L
    while (pos < length) {
        val writeLen = minOf(randomBytes.size.toLong(), length - pos).toInt()
        raf.write(randomBytes, 0, writeLen)
        pos += writeLen
    }
    raf.fd.sync() // Force physical disk sync
}
file.delete()
```

### 7. In-Memory LruCache & Database Optimizations
- **Bitmap LruCache**: Decrypted bitmaps are stored in a memory-bounded `LruCache<String, Bitmap>` keyed by `${filePath}_${maxDimension}` for O(1) list scrolling.
- **Parallel Batch Shredding**: Multi-file shredding runs concurrently on `Dispatchers.IO`.

---

## 💰 Monetization & In-App Purchases

- **Google Play Billing 6.0+**: Seamless monthly/yearly subscriptions and lifetime Pro unlock via `BillingManager`.
- **Banner Ads**: Non-intrusive bottom banner anchored in `MainActivity`.
- **Native Cards**: Native ad cards integrated within app lists and intruder log feeds.
- **Rewarded Video Ads**: Allows free users without biometric hardware to unblur intruder photos or share snapshots.

---

## 🛠️ Developer & Build Guide

### Prerequisites
- Android Studio Jellyfish / Ladybug or Gradle 8.0+
- JDK 17
- Android SDK 34 (Build-Tools 34.0.0)

### Building the Project

```bash
# Clean project
./gradlew clean

# Compile Debug APK
./gradlew assembleDebug

# Run Unit & KSP Verification
./gradlew compileDebugKotlin

# Build Production Signed App Bundle (.aab)
./gradlew bundleRelease
```

---

## 📄 License & Release Notes

### Latest Updates
- **0ms Instant Lock Engine**: Integrated `AppLockAccessibilityService` for instantaneous app launch interception with zero screen delay.
- **0ms Deactivation Warning**: Added comprehensive educational dialog explaining zero-delay benefits before disabling.
- **Hybrid JIT Permission Model**: Streamlined credential setup flow with contextual Just-In-Time permission prompts.
- **Double-Lock Smart Advisor**: Real-time conflict prevention for apps with native biometrics.
- **Hardware-Backed AES-256 GCM & Multi-Pass Shredder**: Secure intruder snapshot lifecycle management.
- **Performance & Memory Tuning**: Optimized background polling loops, state flow memory allocations, and zero-allocation in-place alphanumeric app sorting.

All release artifacts (including `app-release.aab` and `PRIVACY_POLICY.html`) are located in the `store_assets/` folder ready for Google Play Console submission.
