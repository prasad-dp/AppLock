# 🛡️ App Locker & Intruder Security Vault

An enterprise-grade, privacy-focused Android application designed to secure sensitive apps, detect unauthorized unlock attempts, capture silent intruder camera snapshots, and protect security logs using hardware-backed **AES-256 GCM Encryption** and **Biometric Vault Verification**.

---

## 📑 Table of Contents
1. [Overview & Key Purpose](#-overview--key-purpose)
2. [Comprehensive Feature Breakdown](#-comprehensive-feature-breakdown)
3. [Technology Stack](#-technology-stack)
4. [System Architecture & Project Structure](#-system-architecture--project-structure)
5. [Deep-Dive Implementation Details](#-deep-dive-implementation-details)
   - [App Locking Mechanism & UsageStats Monitoring](#1-app-locking-mechanism--usagestats-monitoring)
   - [Hardware-Backed AES-256 GCM Encryption](#2-hardware-backed-aes-256-gcm-encryption)
   - [CameraX Silent Snapshot Capture](#3-camerax-silent-snapshot-capture)
   - ["Tap to Reveal" Biometric Vault Shield](#4-tap-to-reveal-biometric-vault-shield)
   - [Multi-Pass Secure File Shredding](#5-multi-pass-secure-file-shredding)
   - [In-Memory LruCache & Database Optimizations](#6-in-memory-lrucache--database-optimizations)
6. [Monetization & AdMob Integration](#-monetization--admob-integration)
7. [Developer & Build Guide](#-developer--build-guide)

---

## 🔒 Overview & Key Purpose

Modern smartphones hold banking data, personal photos, private chats, and confidential business apps. Standard Android lock screens protect the entire device, but once unlocked and handed to a friend, child, or colleague, individual apps remain exposed.

**App Locker & Intruder Security Vault** solves this by providing:
- **Per-App Protection**: Lock individual applications (WhatsApp, Photos, Banking, Settings) behind a distinct PIN, Pattern, or Biometric prompt.
- **Intruder Detection**: Silently capture a front-camera snapshot when anyone inputs an incorrect PIN or Pattern.
- **Hardware-Level Encryption**: Encrypt captured intruder photos immediately using hardware keys inside the **Android KeyStore** so photos never sit unencrypted on disk.
- **Vault Shielding**: Keep intruder records blurred and shielded until verified via Fingerprint or Face Unlock.
- **Data Shredding**: Overwrite disk sectors with random noise and zeros prior to unlinking files when records are deleted.

---

## 🌟 Comprehensive Feature Breakdown

### 1. 🔐 Smart App Locking
- **Supported Lock Types**: 4-Digit / 6-Digit PIN, Custom Pattern Lock, and System Biometrics (Fingerprint / Face Unlock / Device Credential).
- **Foreground Monitoring**: Operates via a low-overhead foreground background service (`AppLockService`) that detects target package launches in real time and enforces a high-priority lock overlay window.
- **System App Protection**: Pre-categorizes system settings, Play Store, and package installer to prevent unauthorized uninstallation or permission tampering.

### 2. 📸 CameraX Silent Intruder Detector
- **Silent Front-Camera Capture**: Utilizes Google CameraX bound to the application lifecycle to take a low-latency front-camera photo without firing a camera flash or play shutter sounds.
- **Custom Failed-Attempt Threshold**: Configure intruder snapshot triggers after **1, 2, 3, or 5 failed unlock attempts**.
- **Intruder Metadata Logs**: Logs precise timestamp, target application package name, and encrypted photo reference.

### 3. 🛡️ Hardware-Backed AES-256 Photo Encryption
- **Zero Raw Disk Storage**: Photos are encrypted immediately in memory prior to being written to storage as `.enc` files.
- **KeyStore Integrity**: Cryptographic keys are generated inside the device's hardware-backed **Android KeyStore** (`KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT`).
- **RAM-Only Decryption**: Photos are decrypted directly into RAM bitmaps when viewed in the application. Unencrypted raw `.jpg` files are never written to physical disk.

### 4. 👁️ "Tap to Reveal" Biometric Vault Lock
- **Shielded Log Preview**: All intruder snapshot cards in the logs tab start in a blurred, lock-badged state.
- **Biometric Prompt Verification**: Tapping a photo invokes Android's native `BiometricPrompt` (Fingerprint, Face Unlock, or Device PIN). Upon successful verification, the image is decrypted and displayed.
- **Rewarded Ad Fallback**: Non-premium users without biometric hardware or configured PINs can watch a short rewarded ad to temporarily unblur the photo.

### 5. 🗑️ Multi-Pass Secure File Shredding
- **Storage Sanitization**: Deleting an intruder record executes a 3-pass sanitization sequence using `RandomAccessFile`:
  1. Overwrite all file bytes with cryptographically secure random bytes.
  2. Overwrite all file bytes with binary zeros (`0x00`).
  3. Flush file buffer to physical media (`raf.fd.sync()`) and call `file.delete()`.
- Prevents forensic data recovery tools from restoring deleted intruder photos.

### 6. ⚡ Battery & Performance Optimization Engine
- **Adaptive Service Polling Loop**:
  - `50ms` delay during active app launch switching or locked package interaction.
  - `180ms` delay when sitting stably in an unlocked application.
  - `1200ms` delay when screen is turned off (`PowerManager.isInteractive == false`).
- **In-Memory LruCache**: 15MB budget memory cache for decrypted thumbnails to prevent redundant AES decryption CPU usage during list scrolling.
- **Room Database Indexing**: Indexed timestamps (`@Entity(indices = [Index(value = ["timestamp"])]))`) for fast logging queries and automated 30-day log purges.
- **Compose Recomposition Skipping**: `@Immutable` annotations on core data models (`IntruderAlert`, `LockedApp`, `GridAppInfo`).

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
| **Async / State** | Kotlin Coroutines & `StateFlow` | Thread management & reactive UI updates |
| **Monetization** | Google Mobile Ads SDK (AdMob) | Banner, Native, and Rewarded Video Ads |

---

## 🏗️ System Architecture & Project Structure

```
app/src/main/java/com/example/
├── MainActivity.kt               # Main entry point, navigation tabs, UI views, Vault Auth
├── MainActivityViewModel.kt      # ViewModel managing state, app lists, setup flow
├── UnlockActivity.kt             # Full-screen lock overlay activity triggered on app launch
│
├── config/
│   └── AdMobConfig.kt            # AdMob unit IDs and configuration
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
│   ├── AppLockService.kt         # Foreground monitoring service with adaptive polling
│   └── AppLockSession.kt         # Temporary unlock grace period state manager
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

### 1. App Locking Mechanism & UsageStats Monitoring
The application utilizes `UsageStatsManager.queryEvents()` inside `AppLockService`:
1. `AppLockService` runs as a foreground service with a persistent notification.
2. It maintains an in-memory `synchronizedSet` of locked package names populated directly from Room DB (`LockedAppDao`).
3. Every poll tick, it checks the top foreground activity package name.
4. If a target package is detected and is not currently in an active grace period (`AppLockSession`), `UnlockActivity` is immediately launched with `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP`.

### 2. Hardware-Backed AES-256 GCM Encryption
All intruder photos are encrypted using `AES/GCM/NoPadding`:
- **Key Generation**: A 256-bit AES secret key is generated inside `AndroidKeyStore` with `AES/GCM/NoPadding` cipher transformations.
- **Initialization Vector (IV)**: A unique 12-byte random IV is generated for every photo and prepended to the encrypted file payload.
- **Decryption**: When decoding, the 12-byte IV is extracted from the head of the file, fed into `GCMParameterSpec(128, iv)`, and decrypted into a byte array in memory before being passed to `BitmapFactory.decodeByteArray()`.

### 3. CameraX Silent Snapshot Capture
`IntruderCameraHelper` manages photo capture:
- Binds `ProcessCameraProvider` to the application process lifecycle using `CameraSelector.LENS_FACING_FRONT`.
- Configures `ImageCapture.BUILDER` with `CAPTURE_MODE_MINIMIZE_LATENCY` and `FLASH_MODE_OFF`.
- Captures output directly into a private app directory as an encrypted `.enc` file via `EncryptedFileManager.encryptBytesToFile()`.

### 4. "Tap to Reveal" Biometric Vault Shield
When a user views intruder logs in `MainActivity`:
1. Intruder photos are rendered inside a container with high-radius Gaussian blur and a lock icon overlay.
2. Tapping the item triggers `triggerVaultBiometricAuth()`:
   - Queries `BiometricManager.from(activity)` for `BIOMETRIC_STRONG | BIOMETRIC_WEAK | DEVICE_CREDENTIAL`.
   - On successful callback, the record's timestamp is added to `revealedPhotoAlertTimestamps` state set.
   - The card recomposes to decode and render the unblurred bitmap.

### 5. Multi-Pass Secure File Shredding
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

### 6. In-Memory LruCache & Database Optimizations
- **Bitmap LruCache**: Decrypted bitmaps are stored in a 15MB `LruCache<String, Bitmap>` keyed by `${filePath}_${maxDimension}`. Subsequent re-renders during scrolling fetch the decoded bitmap in **O(1)** time without hitting the cipher engine.
- **Parallel Batch Shredding**: `AppRepository.deleteAllIntruderAlerts()` wraps deletion tasks in `async(Dispatchers.IO)` coroutines to execute multi-file shredding concurrently.

---

## 💰 Monetization & AdMob Integration

The application integrates Google Mobile Ads SDK (AdMob) with strict Play Store policy compliance:
- **Banner Ads**: Non-intrusive bottom banner anchored in `MainActivity`.
- **Native Cards**: Native ad placements seamlessly integrated within list items in the app lock manager and intruder log feeds.
- **Rewarded Video Ads**: Option for non-premium users without biometric hardware to watch a short video ad to unblur intruder photos.
- **AdMob Config**: Centralized in `com.example.config.AdMobConfig` using standard test unit IDs for safety during development.

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

### Latest Release Updates
- **Confirmation Wizard Redirection**: Added automatic reset redirection to initial lock creation screens when pattern/PIN/password confirmations do not match, complete with dynamic button labels (*"Redraw Pattern"*, *"Re-enter PIN"*, *"Re-enter Password"*).
- **Unblockable Pattern Gesture Input**: Optimized touch gesture handling in `PatternLockView` to allow continuous drawing without touch state lockouts.
- **Performance & Memory Tuning**: Optimized `AppLockService` background polling loops, state flow memory allocations, and zero-allocation in-place alphanumeric app sorting.

All release artifacts (including `app-release.aab` and `PRIVACY_POLICY.html`) are located in the `store_assets/` folder ready for Google Play Console submission.
