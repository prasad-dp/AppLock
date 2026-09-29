# App Locker Pro — Long-Term Maintenance & Routine Inspection Guide

## 1. Overview
This document outlines critical engineering checks, platform policy compliance items, OEM-specific behaviors, and operational procedures to ensure App Locker Pro remains reliable, secure, and compliant across future Android releases.

---

## 2. Frequent Inspection Checklist (What to Check Frequently)

### 2.1 Before Every App Release
- [ ] **Run the Automated Test Suite**:
  ```powershell
  $env:JAVA_HOME="C:\Program Files\Java\jdk-17"; ./gradlew testDebugUnitTest
  ```
  Ensure all unit tests in `AppLockSessionTest`, `NotificationPrivacyTest`, and `BiometricLockoutTest` pass without regressions.
- [ ] **Verify Re-Lock on Home Screen**:
  Open a locked app -> unlock -> tap device Back button to return to launcher -> re-open app. Confirm that `UnlockActivity` immediately prompts for credentials.
- [ ] **Verify Notification Privacy Tap-to-Unlock**:
  Post/receive a notification from a locked app -> tap the masked notification -> enter passcode -> confirm target application opens smoothly.
- [ ] **Verify Intruder Photo Snapshot & 'X' Dismiss**:
  Trigger 3 incorrect passcodes -> open Intruder tab -> view snapshot -> confirm 'X' button dismisses the dialog immediately.
- [ ] **Google Play In-App Billing**:
  Verify purchase flow on internal test track. Confirm purchase restoration (`viewModel.refreshPurchases()`) on fresh installs.
- [ ] **AdMob Ads**:
  Ensure test ads show properly in debug builds and live ad units are linked in release builds. Verify `app-ads.txt` is crawlable on the developer website.

---

## 3. Platform & Google Play Policy Compliance

### 3.1 Accessibility Service Policy
- **Requirement**: Google Play strictly audits applications using `AccessibilityService`.
- **Audit Rules**:
  - The in-app Accessibility Disclosure Modal (`AccessibilityDisclosureModal.kt`) **MUST** be shown before redirecting the user to system settings.
  - The disclosure must clearly state that Accessibility is used *solely* to detect the foreground application window for app locking, and that **no personal data, text, or keystrokes are collected, stored, or transmitted**.
  - On the Google Play Console Data Safety and App Content declarations, check "App Functionality" under Accessibility API usage.

### 3.2 Foreground Service & Android 14+ Restrictions
- **Foreground Service Type**: Declared as `specialUse` in `AndroidManifest.xml` with property `android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE` set to `"App protection services"`.
- **Background Activity Launch (BAL)**:
  - Starting with Android 14 (API 34) and Android 15/16, calling `PendingIntent.send()` from background components requires explicit authorization:
    ```kotlin
    val options = ActivityOptions.makeBasic().apply {
        setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
    }.toBundle()
    ```
  - Any new notification launch or service intent must include this option or fallback to `context.startActivity(launchIntent)`.

---

## 4. OEM Battery Optimization & Background Killers

Android OEMs enforce aggressive background process killing that can pause background services:

| OEM | System Settings to Verify / Direct User to |
| :--- | :--- |
| **Xiaomi / Redmi / POCO** (MIUI / HyperOS) | 1. **Autostart**: Security -> Permissions -> Autostart -> Enable for App Locker.<br>2. **Display pop-up windows while in the background**: Enable in App info permissions.<br>3. **Battery Saver**: Set to "No restrictions". |
| **Samsung** (One UI) | 1. Settings -> Battery -> Background usage limits -> Ensure App Locker is in **Never sleeping apps**.<br>2. Disable "Put unused apps to sleep". |
| **OPPO / Realme** (ColorOS / Realme UI) | 1. App info -> Battery usage -> Enable "Allow background activity" & "Allow auto-launch". |
| **Vivo / iQOO** (FuntouchOS / OriginOS) | 1. Settings -> Battery -> High background power consumption -> Allow. |
| **OnePlus** (OxygenOS) | 1. Settings -> Apps -> App management -> App Locker -> Battery usage -> Allow background activity. |

---

## 5. Database & Cryptographic Migration Protocols

### 5.1 Room Database Schema Upgrades
- Database: `AppDatabase.kt` (current version: 2).
- When adding or altering tables (`locked_apps`, `intruder_alerts`):
  1. Increment `@Database(version = X)`.
  2. Implement an explicit `Migration(oldVersion, newVersion)`.
  3. Never use `fallbackToDestructiveMigration()` in production builds to prevent wiping user lock configurations and encrypted intruder snapshots.

### 5.2 Android KeyStore Integrity
- Alias: `"IntruderPhotoKey"`.
- Never modify the alias or key algorithm (`AES/GCM/NoPadding`) without a migration strategy, as existing `.enc` files on disk will become permanently undecryptable if the key is regenerated.
