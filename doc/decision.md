# App Locker Pro — Architectural Decision Records (ADRs)

## Index of Decisions
- [ADR 001: Dual-Engine Interception Architecture](#adr-001-dual-engine-interception-architecture)
- [ADR 002: Hardware-Backed AES-256 GCM Photo Encryption](#adr-002-hardware-backed-aes-256-gcm-photo-encryption)
- [ADR 003: Isolated SingleInstance Task for UnlockActivity](#adr-003-isolated-singleinstance-task-for-unlockactivity)
- [ADR 004: In-Memory Session State Machine (AppLockSession)](#adr-004-in-memory-session-state-machine-applocksession)
- [ADR 005: Notification Privacy Masking via NotificationListenerService](#adr-005-notification-privacy-masking-via-notificationlistenerservice)
- [ADR 006: Android 14+ Background Activity Launch (BAL) Handling](#adr-006-android-14-background-activity-launch-bal-handling)
- [ADR 007: Immediate Session Revocation on Home Launcher Return](#adr-007-immediate-session-revocation-on-home-launcher-return)

---

## ADR 001: Dual-Engine Interception Architecture
- **Status**: Accepted
- **Context**: Relying exclusively on `UsageStatsManager` introduces a 50–180ms delay, allowing users to briefly glimpse protected app content. Conversely, requiring Accessibility permission upfront creates significant onboarding drop-off and faces restrictions on some devices.
- **Decision**: Implement a **Dual-Engine** strategy:
  - **Primary**: `AppLockAccessibilityService` provides instant **0ms** window state interception before the app draws its first frame.
  - **Fallback**: `AppLockService` uses adaptive `UsageStatsManager` polling when Accessibility permission is withheld.
- **Consequences**: Zero screen flicker for users with Accessibility enabled, with fully functional fallback protection for users relying solely on Usage Stats.

---

## ADR 002: Hardware-Backed AES-256 GCM Photo Encryption
- **Status**: Accepted
- **Context**: Storing captured intruder photos as plain JPEG files in app storage or external cache exposes sensitive imagery to file managers, forensic tools, or backup tools.
- **Decision**: Use the Android KeyStore provider to generate an `AES/GCM/NoPadding` key. Front-camera photos are captured directly into memory buffers, encrypted using AES-256 GCM, and written as `.enc` files. Unencrypted images are never saved to disk.
- **Consequences**: Guarantees local-first zero-knowledge security. Decryption occurs purely in memory when authorized via biometrics or ad verification.

---

## ADR 003: Isolated SingleInstance Task for UnlockActivity
- **Status**: Accepted
- **Context**: If the unlock screen runs within the target app's task stack or the main app's stack, pressing back or switching apps can compromise the lock screen or leave the main app in an inconsistent state.
- **Decision**: Declare `UnlockActivity` with `android:launchMode="singleInstance"` and a custom task affinity (`android:taskAffinity="com.example.unlock"`), excluded from recents (`android:excludeFromRecents="true"`).
- **Consequences**: The unlock overlay runs in total isolation. Back button presses can be intercepted and redirected to the Home launcher (`CATEGORY_HOME`) without leaking the underlying app.

---

## ADR 004: In-Memory Session State Machine (AppLockSession)
- **Status**: Accepted
- **Context**: Storing unlocked app tokens on disk or in SharedPreferences causes excessive I/O overhead during rapid app switching and risks leaving apps unlocked across reboots or crashes.
- **Decision**: Maintain unlocked packages strictly in an in-memory thread-safe singleton (`AppLockSession`).
- **Consequences**: Instantaneous O(1) unlock checks, zero disk I/O during window transitions, and automatic relocking upon process termination or reboot.

---

## ADR 005: Notification Privacy Masking via NotificationListenerService
- **Status**: Accepted
- **Context**: Locking an app prevents direct access to its UI, but incoming notifications (messages, OTPs, emails) continue to preview sensitive content on the lock screen and notification shade.
- **Decision**: Implement `AppLockNotificationListenerService` to intercept notifications belonging to locked apps. Sensitive notifications are cancelled and replaced with privacy-safe placeholders displaying authentic app icons and generic text.
- **Consequences**: Sensitive OTPs and messages are concealed. Original `contentIntent` references are stored in memory to allow seamless direct navigation upon user unlock.

---

## ADR 006: Android 14+ Background Activity Launch (BAL) Handling
- **Status**: Accepted
- **Context**: Starting with Android 14 (API 34), calling `PendingIntent.send()` from background services or secondary activities is blocked by Android OS unless background activity launch permissions are explicitly attached.
- **Decision**: Attach `ActivityOptions.makeBasic().setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)` when dispatching notification pending intents, coupled with a guaranteed fallback to `packageManager.getLaunchIntentForPackage`.
- **Consequences**: Guarantees that tapping a masked notification smoothly opens the target application across all modern Android versions.

---

## ADR 007: Immediate Session Revocation on Home Launcher Return
- **Status**: Accepted
- **Context**: When the global re-lock policy is set to "Immediately", exiting an app to the home screen must immediately require authentication upon re-entry, without being bypassed by recent unlock grace periods.
- **Decision**: Detect home launcher packages (`isLauncherPackage`) in both services and immediately invoke `lockApp(unlockedApp, force = true)` while clearing exit transition flags (`clearGoToHome()`).
- **Consequences**: Fixes the issue where returning to home and immediately reopening an app bypassed the lock screen.
