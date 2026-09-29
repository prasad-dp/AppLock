# App Locker Pro — Feature Catalog & Specifications

## 1. Feature Matrix Overview

| Feature Module | Free Tier | Premium Pro Tier |
| :--- | :---: | :---: |
| **0ms Instant App Lock** | ✅ | ✅ |
| **PIN, Pattern & Password Authentication** | ✅ | ✅ |
| **Biometric (Fingerprint / Face Unlock)** | ✅ | ✅ |
| **System Apps Protection (Settings, Play Store)** | ✅ | ✅ |
| **Intruder Selfie Capture (CameraX)** | ✅ | ✅ |
| **AES-256 GCM Hardware Photo Encryption** | ✅ | ✅ |
| **Multi-Pass File Shredder** | ✅ | ✅ |
| **Notification Privacy Shield (Strict / Hide Content)** | ✅ | ✅ |
| **Global Re-Lock Policy** | Immediately / 30s | All Timers |
| **Per-App Granular Re-Lock Policies** | ❌ | ✅ |
| **Unrestricted Intruder Vault Photo Unblur** | Rewarded Video Ad | Instant Biometric / Ad-Free |
| **Ad-Free User Experience** | Ads Enabled | 100% Ad-Free |

---

## 2. Detailed Feature Breakdown

### 2.1 Smart App Locking
- **Authentication Types**:
  - **4-Digit PIN**: Numeric pad with randomized or standard layouts.
  - **3x3 Pattern**: Fluid geometric touch connector with directional line rendering.
  - **Alphanumeric Password**: Full alphanumeric keyboard input with visibility toggle.
  - **Biometrics**: Native Android `BiometricPrompt` supporting strong/weak biometrics and device credentials.
- **Security Lockout & Cooldown**:
  - Failed attempt counter is tracked per-package and globally in `LockPreferences`.
  - At **3 failed attempts**, a silent front-camera intruder photo is captured.
  - At **3 failed attempts**, a mandatory **30-second security cooldown** timer is enforced with live countdown.
  - Successful authentication immediately clears all lockouts via `clearAllLockouts(packageName)`.

### 2.2 Re-Lock Timeout Policies
- **Global Default**: Applies to all locked applications without a custom policy.
  - Available settings: `immediately` (0 seconds upon app departure/home), `15_sec`, `30_sec`, `1_min`, `5_min`.
- **Per-App Granular Re-Lock**:
  - Allows sensitive apps (e.g. Banking, WhatsApp) to re-lock immediately while casual apps (e.g. Gallery) stay unlocked for 1 minute.
- **Home Screen Auto-Relock**:
  - Navigating to the home launcher triggers immediate session revocation for apps set to `immediately`.

### 2.3 Intruder Detection & Vault
- **Silent Capture via CameraX**:
  - Front camera (`CameraSelector.LENS_FACING_FRONT`) with `FLASH_MODE_OFF`.
  - Zero shutter sound or preview window.
- **Hardware AES-256 Encryption**:
  - Image bytes encrypted in-memory with KeyStore AES-256 GCM key before saving to storage.
  - Raw unencrypted images never touch physical disk.
- **Photo Detail Dialog**:
  - Fullscreen high-resolution preview decrypted strictly in memory.
  - Dual 'X' dismiss controls (top-bar button and dedicated circular 'X' below the photo).
  - Multi-pass file shredder permanently scrubs deleted snapshots.
  - Secure photo sharing via Android FileProvider.

### 2.4 Notification Privacy Shield
- **Intercepts incoming notifications** for locked apps via `AppLockNotificationListenerService`.
- **Protection Modes**:
  1. **Strict Shield (`strict`)**: Conceals both sender and content (e.g. `WhatsApp • 1 new message`).
  2. **Hide Content Only (`hide_content`)**: Displays sender/headline, conceals sensitive message body and OTPs (e.g. `John • Notification details hidden`).
  3. **Stealth Mode (`block`)**: Completely cancels notifications until the app is opened.
- **Tap-to-Unlock Navigation**:
  - Tapping a masked notification opens `UnlockActivity`.
  - Upon credential verification, the masked notification is dismissed, and the user is routed into the target conversation or app.
  - Fully compatible with Android 14+ Background Activity Launch (BAL) restrictions.

### 2.5 In-App Purchases & Monetization
- **Google Play Billing**: One-time in-app purchase for lifetime Premium Pro access.
- **Dynamic Localization**: Automatic currency formatting and local pricing display.
- **AdMob Integration**:
  - Non-intrusive bottom banners on Dashboard and Security tabs.
  - Rewarded video ads for non-premium intruder photo reveals.
  - Interstitial ads on administrative operations (e.g. updating re-lock policies, purging logs).
