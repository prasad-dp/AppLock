# AppLock Architecture & Edge Case Analysis Notes (`thoughts.md`)

## 1. Overview & Recent Fixes (Commit vs Current Refactor)

### A. App Lock Delay & Failure to Relock on Background / Recents Clearance
* **Problem**: Protected apps took several seconds to lock or bypassed locking completely after being cleared/swiped from background or recents.
* **Root Causes**:
  1. **Premature Departure State Deletion in `lockApp()`**: `leftForegroundTimes[pkg]` was being erased when `lockApp()` was called. When the user re-launched the app from the launcher or recents, `isGenuineAppEntry()` found no departure record and misclassified the cold launch as a transient internal activity transition.
  2. **Launch Throttling & Exit Grace Delay**: In `AppLockAccessibilityService` and `AppLockService`, `isRecentlyExited(1500L)` and `shouldThrottleLaunch` dropped or delayed launch events immediately following an exit or recents clear.
  3. **Target Active Foreground Inspection Race**: On cold launches from launcher, `rootInActiveWindow` in `AppLockPackageHelper.isAppTargetInActiveForeground` could briefly remain on the launcher during early transition frames. The check rejected the launch because the root window package was still the launcher.
* **Solution Implemented**:
  * Departure records are preserved until `markAppInForeground()` confirms authentication and entry.
  * Cold launches from launchers bypass exit grace throttles.
  * Fallback restored in `isAppTargetInActiveForeground`: allows overlay engagement if the active window is null or in transition.

---

### B. WhatsApp Video Call Floating Window (PiP Mode) & Maximization
* **Problem**: Answering a WhatsApp video call and placing it in a floating/PiP window triggered the lock screen. Unlocking it prevented maximizing the call.
* **Root Causes**:
  1. **Home Launcher Auto-Relock Collision**: When minimizing the call to PiP and landing on the home launcher, `AppLockAccessibilityService` and `AppLockService`'s polling loop saw `isLauncher == true` with policy `immediately` and forcibly locked WhatsApp.
  2. **Missing PiP Window Inspection**: Neither accessibility service nor the session tracker checked if the target package was in picture-in-picture mode (`AccessibilityWindowInfo.isInPictureInPictureMode`).
  3. **Accessibility XML Config Barrier**: In `accessibility_service_config.xml`, `android:canRetrieveWindowContent="false"` was set and `flagRetrieveInteractiveWindows` was missing. On physical OEM devices, Android prohibits `AccessibilityService.getWindows()` from returning window structures if `canRetrieveWindowContent` is `false`.
* **Solution Implemented**:
  * Set `canRetrieveWindowContent="true"` and `flagRetrieveInteractiveWindows` in `accessibility_service_config.xml`.
  * Added `AppLockPackageHelper.isPackageInPipMode(service, packageName)` checking `window.isInPictureInPictureMode`.
  * Added `AppLockSession.setPipMode()` and `AppLockSession.isAppInActiveCallOrPip()`.
  * Guarded auto-relock loops in both `AppLockAccessibilityService` and `AppLockService` against locking active PiP/call packages.

---

### C. Snapchat Message Count Mismatch in Notification Privacy
* **Problem**: In WhatsApp, notification privacy grouped and accurately reported message counts (e.g., `🔒 3 new messages`), whereas Snapchat reported mismatched counts or counted transient typing indicators.
* **Root Causes**:
  1. **Typing Indicators Counted as Messages**: Snapchat posts `"Alice is typing a snap..."` / `"typing..."` notifications. App Locker counted each typing update as an additional unread message.
  2. **Bundled Notifications & Missing Group Key Lifecycle**: When Snapchat bundles messages or updates a summary, `notification.number` might be omitted or differ from standard WhatsApp `MessagingStyle`.
  3. **Notification Removal Leak**: When reading chats, `onNotificationRemoved` did not decrement counts per individual key, leaving ghost message counts on the lock banner.
* **Solution Implemented**:
  * Added `isTypingIndicator(notification)` to filter out transient typing status before counting.
  * Added tiered count extractor `extractNotificationMessageCount(sbn)` evaluating `notification.number`, `Notification.EXTRA_MESSAGES`, `Notification.EXTRA_TEXT_LINES`, and regex extractors (`(\d+)\s*(?:new\s*)?(?:messages?|chats?|snaps?)`).
  * Added `notificationItemCounts` map and active notification key tracking in `onNotificationRemoved` to decrement counts and cancel masked notifications once all messages are dismissed.

---

## 2. Floating Windows & PiP Resolution Across ALL Apps

### A. Native Picture-in-Picture (PiP) Mode
* **Mechanism**:
  * `AppLockPackageHelper.isPackageInPipMode(service, targetPackage)` iterates over `service.windows` and inspects `window.isInPictureInPictureMode`.
  * This is an Android Framework API (`android.view.accessibility.AccessibilityWindowInfo`) available since Android 8.0 (API 26).
* **Coverage**:
  * **Completely generic for ALL applications**: Works out of the box for YouTube, Google Maps, Zoom, Google Meet, Netflix, Twitch, VLC, Telegram, and any app utilizing `Activity.enterPictureInPictureMode()`.
  * When any app is in PiP mode, `AppLockSession.setPipMode(pkg, true)` prevents auto-relocking when the user navigates the home screen or other apps.
  * Maximizing the PiP window does not trigger repeated lock loops because the app remains in the unlocked session state.

### B. OEM Freeform / Pop-up Floating Windows (Samsung One UI, Xiaomi HyperOS, OnePlus FlexDrop)
* **Distinction**:
  * OEM Freeform Floating Windows are **not** standard Android PiP windows. They run in Freeform Multi-Window mode (`WINDOWING_MODE_FREEFORM`).
  * `window.isInPictureInPictureMode` returns `false` for freeform windows.
* **Behavior with App Locker**:
  * **Initial Launch**: When a locked app is launched in a freeform pop-up window, `TYPE_WINDOW_STATE_CHANGED` fires. `UnlockActivity` opens as a secure overlay, prompting the user for authentication. Once authenticated, `UnlockActivity.finish()` reveals the floating app window unlocked.
  * **Multitasking & Focus Shifts**:
    * If the user taps outside the floating window (onto the background app or launcher), input focus leaves the floating window.
    * If the app's relock policy is set to **`"immediately"`**, App Locker will lock the app as soon as touch focus leaves it. Returning to the floating window will require unlocking again.
    * If the relock policy is set to **`"15_sec"`**, **`"30_sec"`**, or **`"1_min"`**, the floating window remains unlocked during multitasking until the timeout expires.

---

## 3. Foldables & Dual-Screen Devices Analysis

### A. Foldable Devices (Galaxy Z Fold, Galaxy Z Flip, Pixel Fold, OnePlus Open)

#### 1. Screen Continuity (Fold / Unfold Transitions)
* **Mechanism**:
  * When folding or unfolding (transitioning between cover screen and inner unfolded screen), Android fires runtime configuration changes (`smallestScreenWidthDp`, density, aspect ratio, orientation).
* **Impact on App Locker**:
  * **Unlocked Session State**: `AppLockSession` stores unlock states in an in-memory singleton. Configuration changes do not reset or clear this state. Unlocked apps **stay unlocked seamlessly**.
  * **Lock Screen (`UnlockActivity`)**: Built with Jetpack Compose (`fillMaxSize()`), it automatically recomposes and scales to the new display dimensions.
* **Edge Case - Manifest Orientation Lock**:
  * `UnlockActivity` specifies `android:screenOrientation="portrait"` in `AndroidManifest.xml`.
  * On book-style foldables (e.g. Pixel Fold, OnePlus Open) where the unfolded inner screen is landscape-first (wider than tall), forcing portrait can trigger OEM letterboxing/pillarboxing (black bars on the sides) or force the screen orientation.
  * *Security*: Security is 100% maintained, but UI presentation may be letterboxed on wide screens.

#### 2. Split-Screen & Multi-Window Multitasking (Side-by-Side 50/50 or 70/30)
* **Mechanism**:
  * In Android 10+ Multi-Resume, both split-screen apps are visible simultaneously.
  * However, accessibility focus follows the pane currently receiving user input.
* **How Locking Operates**:
  * When the user taps a locked app in a split-screen pane, `TYPE_WINDOW_STATE_CHANGED` fires and `UnlockActivity` prompts the user.
  * Once unlocked, the app is usable in its split-screen pane.
* **Flow Break under `"immediately"` Policy**:
  * When the user taps from App A (locked app, left pane) to App B (right pane), `onAccessibilityEvent` receives `pkgName = App B`.
  * In `AppLockAccessibilityService`, the relock loop marks App A as out of foreground.
  * If App A's relock policy is `"immediately"`, App A gets **locked while still visible on screen in the split-screen pane**. Tapping back into App A triggers the lock screen again.
  * *Recommendation*: A non-zero relock timeout (`15s` or `30s`) allows smooth side-by-side split-screen usage without repeated lock prompts.

---

### B. Dual-Screen Devices (Microsoft Surface Duo, LG Dual Screen)

#### 1. Multi-Display Architecture
* **Mechanism**:
  * Dual-screen devices expose separate `Display` instances (`Display.DEFAULT_DISPLAY = 0` and `Display 1`).
  * `service.windows` enumerates windows with their corresponding `displayId`.
* **Potential Display Target Mismatch**:
  * `launchUnlockScreen()` currently invokes `startActivity(intent)` with `FLAG_ACTIVITY_NEW_TASK` without specifying `ActivityOptions.setLaunchDisplayId(displayId)`.
  * On certain dual-screen OEM firmware, launching an activity from a background service without a target display ID defaults to `Display.DEFAULT_DISPLAY` (Display 0).
  * If a locked app is interacted with on Display 1, the lock screen could appear on Display 0 on some hardware setups.
  * *Future Enhancement*: Use `ActivityOptions.setLaunchDisplayId(window.displayId)` when `Build.VERSION.SDK_INT >= Build.VERSION_CODES.O`.

#### 2. Back Key / Dismissal Security
* In `UnlockActivity.goToHome()`, pressing back sends `Intent.CATEGORY_HOME`, which minimizes the task or returns to launcher home on the relevant display, preventing any data leak.

---

## 4. Summary Matrix

| Device / Scenario | Current Behavior | Works Fine? | Key Architectural Notes |
|---|---|---|---|
| **Standard PiP across ALL apps** (YouTube, Maps, Zoom, Meet, etc.) | Evaluates `window.isInPictureInPictureMode`; suppresses auto-relock | **YES** | Generic across all apps using Android PiP API. |
| **OEM Freeform Pop-up Windows** (Samsung, Xiaomi) | Intercepts as standard app window; prompts unlock on touch | **YES** | Re-locks on focus loss if policy is "immediately"; works smoothly with 15s/30s timeout. |
| **Fold / Unfold Transitions** (Galaxy Fold, Pixel Fold) | Session preserved in memory; app remains unlocked | **YES** | Zero disruption to session state across folds/unfolds. |
| **Foldable Inner Screen UI** | Manifest locks `screenOrientation="portrait"` | **YES (with minor letterbox)** | Works securely, but may show pillarboxing on landscape-first foldables. |
| **Split-Screen Multitasking** (Two apps side-by-side) | Locks target on focus; re-locks if policy is "immediately" and focus shifts | **YES (with timeout)** | Recommend 15s/30s timeout for split-screen users so focus switching doesn't re-prompt. |
| **Dual Physical Displays** (Surface Duo) | Launches lock screen on default display | **PARTIALLY** | On specific firmware, lock screen may appear on Display 0 even if target app is on Display 1. |
