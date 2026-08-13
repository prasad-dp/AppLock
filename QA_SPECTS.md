# 🧪 App Locker Pro — Complete QA Testing & Feature Specification Document (`QA_SPECTS.md`)

This document provides an end-to-end testing specification for **App Locker Pro**, designed for QA engineers, automation testers, and security auditors.

---

## 1. 🔍 System & Architecture Overview

- **Application Name**: App Locker Pro
- **Package / Application ID**: `com.example` / `com.aistudio.applocker.kxmpzq`
- **UI Framework**: Jetpack Compose (100% declarative UI with Material 3 design system)
- **Architecture Pattern**: MVVM + Clean Architecture
- **Local Database Engine**: Room Persistence Library (SQLite) with KSP
- **Asynchronous Processing**: Kotlin Coroutines & Flow streams
- **Testing Stack**: Robolectric (Local JVM Unit & CUJ testing)

---

## 2. 🔑 System Permissions Matrix

| Permission Name | System API Action / Manifest Identifier | Purpose | Behavior when Missing |
|---|---|---|---|
| **Usage Access** | `Settings.ACTION_USAGE_ACCESS_SETTINGS` | Required to detect foreground application launches | Reddish warning banner shown on Apps & Security screens; lock screen cannot detect app launches |
| **Display Overlay** | `Settings.ACTION_MANAGE_OVERLAY_PERMISSION` | Required to draw secure lock screen over target apps | Reddish warning banner shown on Apps & Security screens; lock screen cannot draw on top of apps |
| **Camera Access** | `android.permission.CAMERA` | Required for silent front-camera intruder photo captures | Intruder Selfie disabled; prompt shown to grant camera access when arming Intruder Detection |

---

## 3. 📢 AdMob Monetization & Ad Triggers Matrix

| Ad Format | Component / Screen Location | Exact Trigger Condition | Test Verification Criteria |
|---|---|---|---|
| **Banner Ad** | Home Navigation Bar Bottom Anchor | Always visible at the bottom of main screens (`AdMobBanner`) | Verify container renders cleanly above navigation bar without obscuring touch targets |
| **Native Card Ad** | Inline in Apps Tab & Security Tab | Scrollable item in list views (`AdMobNativeCard`) | Verify native card renders sponsored badge and test ad content |
| **Interstitial Ad** | Intruder Photo Deletion | **Triggered automatically after every 3 intruder photo deletions** | Delete 1st & 2nd photo -> No ad. Delete 3rd photo -> Interstitial ad pops up immediately |
| **Interstitial Ad** | Turning OFF Intruder Detection Switch | **Triggered when user toggles OFF Intruder Selfie switch (fires on both "Keep On" and "Turn Off" choices)** | Toggle switch OFF -> Warning popup appears -> Select either choice -> Interstitial ad pops up |
| **Interstitial Ad** | Arming / Security Toggle Actions | Triggered during key security state toggles (Biometric, Settings Shield) | Toggle Biometric or Settings Shield -> Interstitial ad dialog displays |
| **Rewarded Video Ad** | Intruder Vault Photo Detail Preview | Triggered when user taps "Unlock Full Photo" | Photo is blurred by default -> Tap unlock -> Watch rewarded video -> Photo unblurs |

---

## 4. 🧪 Pinpointed QA Test Cases

### 4.1 Test Case TC-01: System Permission Banner Synchronization
- **Pre-conditions**: Revoke Usage Access or Display Overlay permission in System Settings.
- **Test Steps**:
  1. Open App Locker Pro.
  2. Navigate to **Apps Tab**. Verify reddish "System Permission Required" warning card is visible at top.
  3. Navigate to **Security Tab**. Verify the exact same warning card is visible at top.
  4. Tap "Grant Usage Access Permission" or "Grant Display Overlay Permission" and grant permission in Android Settings.
  5. Return to App Locker Pro.
- **Expected Result**: Warning banner disappears synchronously on both Apps Tab and Security Tab.

---

### 4.2 Test Case TC-02: App Locking & Foreground Interception
- **Pre-conditions**: Usage Access and Display Overlay permissions granted; Background Shield active.
- **Test Steps**:
  1. Open App Locker Pro -> **Apps Tab**.
  2. Search for target application (e.g., Chrome).
  3. Toggle lock switch to **ON**.
  4. Press Home button and launch Chrome.
- **Expected Result**: App Locker Pro lock screen overlay immediately intercepts Chrome. Chrome is blocked until correct PIN/Pattern is supplied.

---

### 4.3 Test Case TC-03: Intruder Photo Deletion Ad Counter (Every 3 Deletions)
- **Pre-conditions**: At least 3 intruder logs exist in Intruder Vault.
- **Test Steps**:
  1. Navigate to **Intruder Vault Tab**.
  2. Delete **1st intruder photo log**. Observe: No ad displays.
  3. Delete **2nd intruder photo log**. Observe: No ad displays.
  4. Delete **3rd intruder photo log**.
- **Expected Result**: Upon completing 3rd deletion, `InterstitialAdDialog` immediately displays on screen.

---

### 4.4 Test Case TC-04: Intruder Toggle Disarm Warning & Ad Display
- **Pre-conditions**: Intruder Camera Detection switch is currently **ON**.
- **Test Steps**:
  1. Navigate to **Security Tab**.
  2. Tap **Intruder Camera Detection** switch to turn it **OFF**.
  3. Verify warning dialog (`turn_off_intruder_confirm_dialog`) appears stating:
     *"By turning off Intruder Selfie, intruder detection won't work and photos will no longer be captured on unauthorized unlock attempts."*
  4. **Sub-Test A (Keep On)**: Tap "Keep On".
     - Verify Intruder Selfie stays **ON**.
     - Verify Interstitial Ad dialog displays.
  5. Tap switch OFF again.
  6. **Sub-Test B (Turn Off)**: Tap "Turn Off".
     - Verify Intruder Selfie turns **OFF**.
     - Verify Toast "Intruder Detection disarmed" displays.
     - Verify Interstitial Ad dialog displays.
- **Expected Result**: Warning popup is shown prior to disarming, and Interstitial Ad displays on any user decision.

---

### 4.5 Test Case TC-05: Rewarded Video Ad Photo Unblur
- **Pre-conditions**: Intruder Vault contains at least 1 captured intruder photo.
- **Test Steps**:
  1. Open **Intruder Vault Tab** and tap on an intruder photo entry.
  2. Observe photo preview image is blurred.
  3. Tap **"Unlock Full Photo"** button.
  4. Observe `AdMobRewardedAdDialog` opens with a timer simulation.
  5. Wait for video timer completion and tap "Claim Reward".
- **Expected Result**: Ad dialog closes, intruder photo image is unblurred, and toast "Intruder Selfie Feature Unlocked!" appears.

---

### 4.6 Test Case TC-06: Dynamic Ad Dialog Screen Bounds & Responsive Layout
- **Pre-conditions**: Device or emulator running in standard / landscape / tablet orientation.
- **Test Steps**:
  1. Trigger an Interstitial or Rewarded Ad dialog.
  2. Observe overlay card scaling (`95% width`, `85% height`).
  3. Verify action controls ("Close Ad", "Claim Reward", "Watch Video") are fully contained inside viewport without vertical or horizontal clipping.
- **Expected Result**: Ad overlays adjust dynamically across all viewport aspect ratios.

---

### 4.7 Test Case TC-07: Bulk Lock & Unlock All Apps
- **Pre-conditions**: App Locker Pro is on Apps Tab.
- **Test Steps**:
  1. Tap **"Lock All"** button in header (`bulk_lock_all_button`).
  2. Verify all apps show locked status.
  3. Tap **"Unlock All"** button (`bulk_unlock_all_button`).
  4. Confirm unlock action in dialog.
- **Expected Result**: All apps update locked state accordingly.

---

## 5. 🏷️ Compose UI TestTag Reference Table

| Compose `testTag` Identifier | UI Component Description | Screen / View Location |
|---|---|---|
| `grant_permission_button` | Usage Access Settings Action Button | Apps / Security Permission Card |
| `grant_overlay_permission_button` | Display Overlay Settings Action Button | Apps / Security Permission Card |
| `intruder_detection_active_switch` | Intruder Detection Camera Switch | Security Tab |
| `turn_off_intruder_confirm_dialog` | Turn OFF Intruder Confirmation Dialog | Security Tab |
| `service_active_switch` | Background App Shield Switch | Security Tab |
| `biometric_active_switch` | Biometric / Fingerprint Unlock Switch | Security Tab |
| `bulk_lock_all_button` | Lock All Installed Apps Button | Apps Tab Header |
| `bulk_unlock_all_button` | Unlock All Installed Apps Button | Apps Tab Header |
| `close_ad_button` | Close / Dismiss Interstitial Ad Button | Interstitial Ad Dialog Overlay |
| `claim_reward_button` | Claim Reward Button | Rewarded Ad Dialog Overlay |

---

## 6. ⚙️ Automated Test Suite Execution Command

Run the complete JVM unit and Robolectric test suite locally via Gradle:

```bash
gradle :app:testDebugUnitTest
```
