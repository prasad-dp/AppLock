# 🧪 App Locker Pro — Complete QA Testing & Feature Specification Document (`QA_SPECTS.md`)

This document provides a comprehensive, end-to-end testing specification for **App Locker Pro**, designed for QA engineers, automation testers, and security auditors. It covers architecture, permission matrices, monetization mechanics, functional feature breakdowns, test case scenarios, and UI test tags across the entire application.

---

## 1. 🔍 System & Architecture Overview

- **Application Name**: App Locker Pro
- **Package Name (Namespace)**: `com.example`
- **Application ID**: `com.aistudio.applocker.kxmpzq`
- **UI Framework**: Jetpack Compose (100% declarative UI with Material Design 3)
- **Architecture Pattern**: MVVM + Clean Architecture + Repository Pattern
- **Local Persistence Engine**: Room Database Library (SQLite) with KSP
- **Preferences Security**: `EncryptedSharedPreferences` (AES-256 GCM encryption via Android KeyStore)
- **Asynchronous Execution**: Kotlin Coroutines & Reactive `StateFlow` / `SharedFlow`
- **Background Engine**: Android Foreground Service (`AppLockService`) with `specialUse` subtype
- **Camera Integration**: CameraX Library for silent front-camera intruder photo capture
- **Biometric Integration**: AndroidX `BiometricManager` & `BiometricPrompt`
- **Testing Stack**: Robolectric (JVM Unit & CUJ integration) + Roborazzi (Screenshot Testing)

---

## 2. 🛡️ Security Architecture & Privacy Safeguards

1. **Window Security Flag (`FLAG_SECURE`)**: Enforced on `MainActivity` and `UnlockActivity` decor windows to prevent system screenshot capture and screen recording, as well as hiding preview thumbnails in recent apps list.
2. **Touch Filtering (`filterTouchesWhenObscured`)**: Toggled on all secure windows to block tapjacking and overlay spoofing attacks from untrusted background apps.
3. **Session Invalidation**:
   - **Screen Off Broadcast Receiver**: Automatically revokes all active app unlock sessions when the device screen turns OFF or locks.
   - **Session Tracking (`AppLockSession`)**: Thread-safe in-memory session manager tracking unlocked packages and timestamp expiration.
4. **Encrypted Persistence (`LockPreferences`)**: MasterKey AES256_GCM hardware-backed key encryption for PIN, Pattern hashes, and security settings. Automatic zero-loss migration from legacy unencrypted preferences.

---

## 3. 🔑 System Permissions Matrix

| Permission Name | System API Identifier | Functional Purpose | Behavior when Missing / Revoked |
|---|---|---|---|
| **Usage Access** | `android.permission.PACKAGE_USAGE_STATS` | Detects real-time foreground activity changes to intercept locked apps | Red warning card on Apps & Security tabs; foreground lock screen fails to detect target launches |
| **Display Overlay** | `android.permission.SYSTEM_ALERT_WINDOW` | Draws secure lock screen overlay over protected target apps | Red warning card on Apps & Security tabs; overlay cannot draw above other apps |
| **Camera Access** | `android.permission.CAMERA` | Captures silent front-camera intruder photo upon 3 consecutive failed unlock attempts | Intruder Selfie disabled; runtime camera permission prompt shown when arming feature |
| **Biometric Access** | `android.permission.USE_BIOMETRIC` & `USE_FINGERPRINT` | Authenticates user via device fingerprint or facial recognition | Biometric prompt icon disabled; falls back to primary PIN/Pattern/Password |
| **Foreground Service** | `android.permission.FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_SPECIAL_USE` | Keeps `AppLockService` active 24/7 in background | System terminates background monitoring loop under memory pressure |
| **Notifications** | `android.permission.POST_NOTIFICATIONS` | Displays required persistent foreground notification ("App Locker Active") | Ongoing service notification hidden on Android 13+ |
| **Haptic Feedback** | `android.permission.VIBRATE` | Tactile vibration feedback on wrong unlock attempts | Tactile error feedback omitted |

---

## 4. 📢 AdMob Monetization & Ad Triggers Matrix

- **AdMob Application ID**: `ca-app-pub-4572028109031472~9939103146`

| Ad Format | Component / Screen Location | Exact Trigger Condition | Test Verification Criteria |
|---|---|---|---|
| **Banner Ad** | Home Navigation Bar Bottom Anchor | Persistent at bottom anchor of main screens (`AdMobBanner`) | Verify container renders cleanly above navigation bar without obscuring touch targets |
| **Native Card Ad** | Apps Tab & Security Tab | Scrollable item embedded in list views (`AdMobNativeCard`) | Verify native card renders sponsored badge and test ad banner content |
| **Interstitial Ad** | Intruder Photo Deletion | **Fires automatically after every 3 intruder photo deletions** | Deletions 1 & 2 -> No ad. Deletion 3 -> `InterstitialAdDialog` pops up immediately |
| **Interstitial Ad** | Disarming Intruder Selfie Switch | **Fires when toggling OFF Intruder Selfie switch (both "Keep On" and "Turn Off" choices)** | Toggle switch OFF -> Warning popup appears -> Select either choice -> Interstitial ad pops up |
| **Interstitial Ad** | Security Settings Toggles | Fires during key security state modifications (Biometric toggle, Re-Lock policy selection) | Change Re-Lock policy or toggle Biometric -> Interstitial ad overlay displays |
| **Rewarded Video Ad** | Intruder Vault Photo Preview | Triggered when tapping **"Unlock Full Photo"** on blurred intruder image | Photo is blurred by default -> Tap unlock -> Watch rewarded video -> Photo unblurs |
| **Rewarded Video Ad** | Share Intruder Snapshot | Triggered when tapping **"Share Snapshot"** in Intruder Vault | Tap share -> Watch rewarded video -> System share intent opens |

---

## 5. 🧩 Complete Feature Breakdown & Functional Scope

### 5.1 Credential Setup & Lock Type Wizard (`SetupState`)
- **Initial Setup Flow**: Welcome -> Choose Lock Type -> First Entry -> Confirmation Entry -> Setup Success -> Main Dashboard.
- **Lock Type 1: Pattern Lock**: 3x3 dot matrix gesture. Requires connecting at least 4 dots. Has animated swipe feedback, red error indication, and confirmation re-draw.
- **Lock Type 2: Numeric PIN**: 4-digit numeric code. Features spring-animated bullet indicators and structured 3-column keypad with haptic feedback.
- **Lock Type 3: Password Lock**: Alphanumeric password with minimum 8 characters required. Includes password visibility toggle (`Visibility`/`VisibilityOff`).
- **Reset Wizard Flow**: Password/PIN/Pattern reset gated behind existing credential verification (`LockVerifyScreen`).

### 5.2 App Protection & Locking Engine (Apps Tab)
- **App Discovery**: Dynamically queries launcher applications via `PackageManager`. Excludes self-package to prevent deadlocks.
- **Search & Filtering**: Real-time alphanumeric search filter (`searchQuery`) and category filter tabs (`ALL`, `LOCKED`, `UNLOCKED`).
- **Single App Locking**: Instant lock/unlock switch per app. Updates Room DB state asynchronously.
- **Bulk Operations**:
  - **Lock All Apps (`bulk_lock_all_button`)**: Instant bulk lock for all launcher applications.
  - **Unlock All Apps (`bulk_unlock_all_button`)**: Prompts verification dialog before unprotecting all apps.
- **Sensitive Apps Auto-Lock**: One-tap smart action (`lockRecommendedApps`) targeting settings, gallery, messaging, financial, and social apps.
- **LRU Memory Cache (`AppIconCache`)**: High-performance bitmap drawable caching preventing list scroll lag.
- **System App Guidance (`SystemAppGuidance`)**: Guidance banners urging users to lock sensitive system components (System Settings, Play Store, Package Installer).

### 5.3 Double-Lock Protection & Smart Advisor
- **Double-Lock Detector (`DoubleLockDetector`)**: Identifies apps with built-in native security or biometrics (WhatsApp, Telegram, Signal, Google Wallet, PhonePe, Paytm, PayPal, Revolut, Cash App, Venmo, 1Password, Bitwarden, Dashlane, LastPass, Authenticator, Knox Secure Folder, MIUI Security Center, Banking apps).
- **Double-Lock Recommendation Engine (`DoubleLockRecommendationEngine`)**: Detects conflicts between "Immediate" re-lock policy and native app biometrics that cause infinite re-lock loops.
- **Smart Advisor Dialog (`DoubleLockAdvisorDialog`)**: Suggests 15-second or 30-second re-lock delay when locking double-lock prone apps. Includes "Don't warn again" per-app suppression (`suppressDoubleLockWarning`).
- **Double Unlock Tip Banner**: Educates users on optimizing re-lock settings for banking and messaging apps.

### 5.4 Re-Lock Timeout Policies & Granular Customization
- **Global Re-Lock Timeout Options**:
  - `immediately`: Re-lock as soon as target app leaves foreground (1.5 sec grace).
  - `15_sec`: Re-lock after 15 seconds out of foreground.
  - `30_sec`: Re-lock after 30 seconds out of foreground (**Default**).
  - `1_min`: Re-lock after 1 minute out of foreground.
  - `5_min`: Re-lock after 5 minutes out of foreground.
- **Per-App Custom Re-Lock Timeout (`PerAppRelockDialog`)**: Granular policy override allowing distinct timeouts per application (e.g., 15 sec for WhatsApp, Immediate for Banking apps).

### 5.5 Intruder Selfie Vault & Security Cooldown
- **Failed Attempt Tracking**: Counter tracked per package and globally (`getFailedAttempts`).
- **3-Attempt Break-in Trigger**:
  1. On 3rd consecutive failed unlock attempt, triggers CameraX front camera to silently capture an intruder selfie.
  2. Saves photo to encrypted local storage and creates an `IntruderAlert` Room record (photo path, attempted package, lock type, timestamp).
  3. Enforces a **30-second Security Cooldown Block** (`SecurityLockoutScreen`) with a real-time countdown timer.
  4. Triggers dual-pulse error vibration.
- **Intruder Vault Tab**:
  - Displays chronological break-in log with app icon, app label, lock type used, and timestamp.
  - **Blurred Photo Preview**: Photos are blurred by default for privacy. Tapping "Unlock Full Photo" opens `RewardedAdDialog`.
  - **Auto-Cleanup Engine**: Automatically purges intruder logs older than 30 days when enabled.
  - **Photo Deletion & Ad Counter**: Every 3 photo deletions triggers an Interstitial Ad.
  - **Clear All Logs**: Bulk wipe of intruder records.

### 5.6 Security Settings & System Controls (Security Tab)
- **Background Shield Switch (`service_active_switch`)**: Enables/Disables `AppLockService`. Disabling prompts high-severity warning confirmation dialog (`disable_shield_confirmation_dialog`).
- **Biometric Unlock Switch (`biometric_active_switch`)**: Enables device fingerprint / face unlock prompt on lock overlay screen.
- **Intruder Detection Switch (`intruder_detection_active_switch`)**: Toggles camera selfie capture. Disarming prompts confirmation dialog (`turn_off_intruder_confirm_dialog`) and fires Interstitial Ad on any decision.
- **Auto-Cleanup Switch**: Toggles 30-day auto-purge of old logs.
- **Theme Mode Toggle (`FancyThemeToggle`)**: Dark Mode / Light Mode with spring animation and glow effects.
- **Go Premium Sheet (`GoPremiumDialog` / `GoPremiumSheet`)**: Unlocks per-app re-lock policies, ad-free experience, and unlimited vault photo unlocks.

---

## 6. 🧪 Comprehensive QA Test Cases Matrix

### 6.1 Test Case TC-01: System Permission Banner Synchronization
- **Pre-conditions**: Revoke Usage Access or Display Overlay permission in System Settings.
- **Test Steps**:
  1. Launch App Locker Pro.
  2. Navigate to **Apps Tab**. Observe reddish "System Permission Required" warning card at top.
  3. Navigate to **Security Tab**. Observe exact same warning card.
  4. Tap "Grant Permission" and enable permission in System Settings.
  5. Return to App Locker Pro.
- **Expected Result**: Warning banner disappears synchronously on both Apps Tab and Security Tab.

### 6.2 Test Case TC-02: App Locking & Foreground Interception
- **Pre-conditions**: Usage Access and Display Overlay permissions granted; Background Shield active.
- **Test Steps**:
  1. Navigate to **Apps Tab**.
  2. Search for a target app (e.g., Chrome).
  3. Toggle lock switch to **ON**.
  4. Press Home button and launch Chrome.
- **Expected Result**: `UnlockActivity` overlay immediately intercepts Chrome. Chrome is blocked until valid credential is provided.

### 6.3 Test Case TC-03: Security Lockout Cooldown (3 Wrong Attempts)
- **Pre-conditions**: App Locker Pro is locking a target app.
- **Test Steps**:
  1. Open locked app to bring up lock overlay.
  2. Enter **1st wrong PIN/Pattern**. Observe error prompt & vibration.
  3. Enter **2nd wrong PIN/Pattern**. Observe error prompt & vibration.
  4. Enter **3rd wrong PIN/Pattern**.
- **Expected Result**: 
  - Silent front camera photo is captured.
  - `SecurityLockoutScreen` appears displaying "Security Lockout Active" and a 30-second countdown timer.
  - Standard entry controls are disabled until timer reaches 0.

### 6.4 Test Case TC-04: Intruder Vault Photo Deletion Ad Counter (Every 3 Deletions)
- **Pre-conditions**: At least 3 intruder logs exist in Intruder Vault.
- **Test Steps**:
  1. Navigate to **Intruder Vault Tab**.
  2. Delete **1st intruder log**. Observe: No ad displays.
  3. Delete **2nd intruder log**. Observe: No ad displays.
  4. Delete **3rd intruder log**.
- **Expected Result**: Upon completing 3rd deletion, `InterstitialAdDialog` immediately displays on screen.

### 6.5 Test Case TC-05: Intruder Toggle Disarm Warning & Ad Display
- **Pre-conditions**: Intruder Camera Detection switch is **ON**.
- **Test Steps**:
  1. Navigate to **Security Tab**.
  2. Tap **Intruder Camera Detection** switch to turn it **OFF**.
  3. Observe confirmation dialog (`turn_off_intruder_confirm_dialog`) appears.
  4. **Sub-Test A (Keep On)**: Tap "Keep On".
     - Intruder Selfie stays **ON**.
     - Interstitial Ad dialog displays.
  5. Tap switch OFF again.
  6. **Sub-Test B (Turn Off)**: Tap "Turn Off".
     - Intruder Selfie turns **OFF**.
     - Toast "Intruder Detection disarmed" displays.
     - Interstitial Ad dialog displays.
- **Expected Result**: Warning popup appears before disarming, and Interstitial Ad displays on both user decisions.

### 6.6 Test Case TC-06: Rewarded Video Ad Photo Unblur
- **Pre-conditions**: Intruder Vault contains captured photo logs.
- **Test Steps**:
  1. Open **Intruder Vault Tab** and select an entry.
  2. Observe image is blurred by default.
  3. Tap **"Unlock Full Photo"** button.
  4. Complete rewarded video simulation (`RewardedAdDialog`).
  5. Tap "Claim Reward".
- **Expected Result**: Ad dialog closes, photo unblurs completely, and photo zoom preview opens.

### 6.7 Test Case TC-07: Double-Lock Smart Advisor Workflow
- **Pre-conditions**: Target app is double-lock prone (e.g., WhatsApp, Banking app). Global re-lock policy set to "immediately".
- **Test Steps**:
  1. Search for WhatsApp on **Apps Tab**.
  2. Toggle lock switch to **ON**.
- **Expected Result**: `DoubleLockAdvisorDialog` pops up warning about potential double-unlock conflict and recommending a 15-second re-lock delay.

### 6.8 Test Case TC-08: Granular Per-App Re-Lock Policy
- **Pre-conditions**: Premium active or testing per-app timeout dialog.
- **Test Steps**:
  1. Long-press or tap timer icon on a locked app in **Apps Tab**.
  2. Select "15 Seconds" in `PerAppRelockDialog`.
  3. Tap "Save".
- **Expected Result**: Custom per-app timeout is saved in `LockPreferences` (`per_app_relock_timeout_<package>`) and overrides global policy for that specific app.

### 6.9 Test Case TC-09: Bulk Lock & Unlock All Apps
- **Pre-conditions**: Apps Tab active.
- **Test Steps**:
  1. Tap **"Lock All"** (`bulk_lock_all_button`). Verify all listed apps show locked status.
  2. Tap **"Unlock All"** (`bulk_unlock_all_button`).
  3. Verify credential confirmation dialog appears.
  4. Confirm unlock.
- **Expected Result**: All apps update locked state accordingly.

### 6.10 Test Case TC-10: Background Shield Disable Confirmation
- **Pre-conditions**: Background Shield active.
- **Test Steps**:
  1. Navigate to **Security Tab**.
  2. Toggle **Background App Shield** switch to **OFF**.
- **Expected Result**: High-severity warning dialog (`disable_shield_confirmation_dialog`) appears detailing security risks before stopping `AppLockService`.

---

## 7. 🏷️ Complete Compose UI TestTag Reference Table

| Compose `testTag` Identifier | UI Component Description | Screen / View Location |
|---|---|---|
| `grant_permission_button` | Usage Access Settings Action Button | Apps / Security Permission Card |
| `grant_overlay_permission_button` | Display Overlay Settings Action Button | Apps / Security Permission Card |
| `start_wizard_button` | Begin Setup Button | Pattern Wizard Intro Screen |
| `select_lock_type_Pattern Lock` | Select Pattern Lock Card | Lock Type Selection Screen |
| `select_lock_type_PIN Lock` | Select PIN Lock Card | Lock Type Selection Screen |
| `select_lock_type_Password Lock` | Select Password Lock Card | Lock Type Selection Screen |
| `pin_key_<1-9,0,⌫,FP,X>` | Numeric PIN Pad Key Buttons | PIN Lock Entry / Lock Overlay |
| `password_input_field` | Alphanumeric Password Text Field | Password Lock Entry Screen |
| `submit_password_button` | Submit Password Button | Password Lock Entry Screen |
| `finish_wizard_button` | Complete Wizard Setup Button | Setup Success Screen |
| `reset_wizard` | Redraw / Re-enter Credential Button | Setup Confirmation Screen |
| `fancy_theme_toggle` | Dark / Light Theme Mode Toggle Button | Top Header Bar |
| `bulk_lock_all_button` | Lock All Installed Apps Button | Apps Tab Header |
| `bulk_unlock_all_button` | Unlock All Installed Apps Button | Apps Tab Header |
| `service_active_switch` | Background App Shield Switch | Security Tab |
| `disable_shield_confirmation_dialog` | Disable Shield Warning Dialog | Security Tab |
| `biometric_active_switch` | Biometric / Fingerprint Unlock Switch | Security Tab |
| `intruder_detection_active_switch` | Intruder Detection Camera Switch | Security Tab |
| `turn_off_intruder_confirm_dialog` | Turn OFF Intruder Confirmation Dialog | Security Tab |
| `apply_double_lock_recommendation_button` | Apply Double-Lock Recommendation Button | Smart Advisor Dialog |
| `face_scanning_banner` | Biometric Face Scan Bottom Sheet | Lock Overlay Screen |
| `alt_auth_button` | Use PIN / Alternative Auth Button | Biometric Scan Sheet |
| `close_ad_button` | Close / Dismiss Interstitial Ad Button | Interstitial Ad Dialog Overlay |
| `claim_reward_button` | Claim Reward Button | Rewarded Ad Dialog Overlay |

---

## 8. ⚙️ Automated Test Suite Execution Command

Run the JVM unit, Robolectric, and Roborazzi screenshot test suite via Gradle:

```bash
# Run all local JVM unit & Robolectric CUJ tests
gradle :app:testDebugUnitTest

# Verify screenshot test visual regressions
gradle :app:verifyRoborazziDebug

# Record new reference screenshot baselines
gradle :app:recordRoborazziDebug
```
