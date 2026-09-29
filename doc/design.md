# App Locker Pro — UI/UX Design System & Specifications

## 1. Overview
App Locker Pro is built on modern Android Material Design 3 (Material You) principles using **100% Jetpack Compose**. The design aesthetic emphasizes a high-trust, privacy-first, secure feel with deep contrasts, modern rounded geometry, and clear visual feedback.

---

## 2. Color Palette & Theming

The application supports dynamic Dark and Light modes with curated color tokens that emphasize security and clarity:

### 2.1 Dark Mode Palette (Default)
- **Primary / Accent**: `#3B82F6` (Electric Blue) — Signifies security, authentication, and verified actions.
- **Primary Container**: `#1E3A8A` / `#2563EB` — Background for primary badges, selected pills, and active toggles.
- **Background**: `#0F172A` (Slate 900) — Deep, distraction-free backdrop.
- **Surface / Card**: `#1E293B` (Slate 800) — Elevated cards with subtle 1dp border (`outlineVariant`).
- **Surface Variant**: `#334155` (Slate 700) — Chip containers, secondary inputs, and inactive controls.
- **Error / Intruder**: `#EF4444` (Vibrant Crimson) — Intruder alerts, wrong passcode shakes, lockout notices.
- **Success / Shield**: `#10B981` (Emerald Green) — Armed shield status, correct credential confirmation.
- **Warning**: `#F59E0B` (Amber) — Rewarded ad indicators, system permission warnings.

### 2.2 Light Mode Palette
- **Primary**: `#2563EB` (Royal Blue)
- **Background**: `#F8FAFC` (Slate 50)
- **Surface**: `#FFFFFF` (Pure White)
- **Surface Variant**: `#F1F5F9` (Slate 100)
- **Outline Variant**: `#E2E8F0` (Slate 200)

---

## 3. Typography & Hierarchy

The app leverages system Roboto / sans-serif with strong typographic weighting to direct attention:

| Style Token | Weight | Size / Line Height | Usage |
| :--- | :--- | :--- | :--- |
| `displaySmall` | Bold (700) | 32sp / 38sp | Main dashboard header stats |
| `titleLarge` | Bold (700) | 22sp / 28sp | Screen titles, Section headers |
| `titleMedium` | SemiBold (600) | 16sp / 24sp | App card names, Dialog titles |
| `bodyLarge` | Normal (400) | 16sp / 24sp | Prompts, descriptive body text |
| `bodyMedium` | Normal (400) | 14sp / 20sp | Subtitles, credential guides |
| `labelSmall` | Medium (500) | 11sp / 16sp | Badges, category pills, timestamps |

---

## 4. Key Screen Layouts & Component Hierarchy

### 4.1 Dashboard & App Protection List (`MainActivity`)
- **Top Bar**: Search bar with real-time text query filtering, Lock All / Unlock All quick actions, and Settings entry.
- **Filter Pills**: Compact, horizontal scrollable category chips (`All`, `Locked`, `Social`, `Finance`, `System`, `Others`) with dynamic item count badges.
- **App List Rows (`AppRowItem`)**:
  - Application icon (cached in-memory via `AppIconCache` / `LruCache`).
  - App label & package summary.
  - Quick-action toggle switch or lock icon for one-tap protection.
  - Per-app re-lock timeout badge (Pro feature indicator).

### 4.2 Intruder Logs & Snapshot Vault
- **Shield Status Banner**: Green check badge when no intruders are recorded, warning crimson badge when unauthorized break-ins are detected.
- **Intruder Cards (`IntruderAlertItem`)**:
  - Encrypted snapshot thumbnail with biometric/ad reveal overlay.
  - Target application label and auth method attempted (`PIN`, `PATTERN`, `PASSWORD`).
  - Formatted timestamp (`MMM dd, yyyy - hh:mm a`).
  - Quick share and multi-pass shred/delete actions.
- **Fullscreen Photo Dialog (`zoomPhotoAlert`)**:
  - Edge-to-edge backdrop with `statusBarsPadding()` and `navigationBarsPadding()`.
  - Full-size decrypted snapshot bitmap (RAM-only).
  - Dedicated circular **'X' close button** centered beneath the photo.
  - Action row containing **Close** and **Share Snapshot** buttons.
  - Tap-to-dismiss background support.

### 4.3 Shield Overlay (`UnlockActivity` & `LockVerifyScreen`)
- **Independent Task**: Isolated in task affinity `com.example.unlock` with `singleInstance` launch mode.
- **Interactive Controls**:
  - Custom 3x3 touch-drawn `PatternLockView` with animated node connections.
  - 4-Digit or custom PIN keyboard with randomized or standard layouts.
  - Alphanumeric secure input field for password mode.
  - Biometric quick-action button with haptic feedback.
- **Feedback Animations**: Horizontal shake animation and vibrator pulses on invalid credential entry.

---

## 5. Micro-Interactions & Motion Design

1. **Window Transition Animations**:
   - Zero-animation transitions (`overridePendingTransition(0, 0)`) when popping the lock screen to prevent visual lag or glimpses.
2. **Error State Haptics**:
   - Distinct vibration waveform `[0, 120, 80, 120]` on failed credentials.
3. **Card Elevating & State Toggling**:
   - Spring animations on lock state toggles (`spring(stiffness = Spring.StiffnessMedium)`).
4. **Touch Target Accessibility**:
   - Minimum 48x48dp interactive touch bounding box on all buttons, 'X' dismiss icons, and toggles.
