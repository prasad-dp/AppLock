# 🚀 Google Play Store Listing & Launch Metadata

**Application:** App Locker Pro & Intruder Security Vault  
**Package Name:** `com.aistudio.applocker.kyzqpz`  
**Version:** `1.0.1` (`versionCode`: `2`)  
**Target SDK:** Android 16 (API Level 36, `compileSdk: 36`) | **Min SDK:** Android 7.0 (API Level 24)

---

## 1. 📝 Store Listing Text & ASO (App Store Optimization)

### App Title (Max 30 Characters)
```text
App Locker Pro: Intruder Vault
```

### Short Description (Max 80 Characters)
```text
Lock apps instantly, capture silent intruder selfies & encrypt security logs.
```

### Full Description (Formatted for Google Play Store)
```text
🛡️ Protect your privacy, confidential chats, banking apps, and personal photos with App Locker Pro — the enterprise-grade app lock and silent intruder security vault for Android.

⚡ 0ms ZERO-DELAY INSTANT INTERCEPTION
Unlike standard app lockers that suffer from 1-2 second screen lag, App Locker Pro intercepts protected app launches at the window creation layer via Accessibility Services. Zero screen glimpses. Zero delay.

📸 SILENT FRONT-CAMERA INTRUDER DETECTOR
Catch anyone snooping on your phone! When an unauthorized user enters an incorrect PIN, Pattern, or Password, the front camera silently captures a high-resolution snapshot without flashing or playing shutter sounds.

🔐 HARDWARE-BACKED AES-256 GCM ENCRYPTION
Your security logs never sit unencrypted on disk. All intruder photos are encrypted in memory with 256-bit AES GCM using keys securely stored in the hardware Android KeyStore.

👁️ "TAP TO REVEAL" BIOMETRIC VAULT SHIELD
Intruder snapshot previews are blurred and shielded in the logs tab. Only you can unblur and inspect intruder evidence using your Fingerprint, Face Recognition, or Master Passcode.

🔄 SMART DOUBLE-LOCK ADVISOR
Avoid annoying double-authentication loops! App Locker Pro automatically detects apps that have native biometric security (such as WhatsApp, Google Wallet, and Banking apps) and optimizes lock timing.

🗑️ 3-PASS FORENSIC FILE SHREDDER
When you delete an intruder alert, physical storage sectors are overwritten with cryptographic random noise and zeros before unlinking, preventing forensic data recovery.

═════════════════════════════════════════════
✨ KEY FEATURES AT A GLANCE
═════════════════════════════════════════════
• Multiple Lock Styles: 4-Digit PIN, 6-Digit PIN, 3x3 Pattern, & Alphanumeric Password.
• Biometric Fingerprint & Face Unlock support.
• Customizable Failed Attempt Thresholds (1, 2, 3, or 5 attempts).
• Per-App Re-Lock Rules: Lock Immediately, after 15s, 30s, 1m, or 5m.
• System Settings & Google Play Store Protection to prevent unauthorized uninstallation.
• 100% On-Device Privacy: Zero photos or passwords uploaded to external servers.
• Fluid Material Design 3 Dynamic Dark & Light themes.

═════════════════════════════════════════════
🔒 PERMISSIONS & ACCESSIBILITY DISCLOSURE
═════════════════════════════════════════════
App Locker Pro uses the Android Accessibility Service (BIND_ACCESSIBILITY_SERVICE) exclusively to detect window state transitions (TYPE_WINDOW_STATE_CHANGED) when a protected app is launched, enabling 0ms instant locking. App Locker Pro NEVER reads screen content, monitors keystrokes, tracks user inputs, or transmits personal information.
```

---

## 2. 🎨 Graphic Assets Specification

| Asset Type | Dimension | Format | File Path in Repo |
|---|---|---|---|
| **App Icon** | 512 x 512 px | 32-bit PNG | `store_assets/play_store_512x512_icon.png` |
| **Feature Graphic** | 1024 x 500 px | 32-bit PNG | `store_assets/play_store_1024x500_feature_graphic.png` |
| **Screenshot 1 (0ms Interception)** | 1080 x 1920 px (9:16) | 32-bit PNG | `store_assets/screenshots/playstore_phone_screenshot_1_instant_lock.png` |
| **Screenshot 2 (Intruder Camera)** | 1080 x 1920 px (9:16) | 32-bit PNG | `store_assets/screenshots/playstore_phone_screenshot_2_intruder_selfie.png` |
| **Screenshot 3 (Biometric Vault)** | 1080 x 1920 px (9:16) | 32-bit PNG | `store_assets/screenshots/playstore_phone_screenshot_3_biometric_vault.png` |
| **Screenshot 4 (AES-256 KeyStore)** | 1080 x 1920 px (9:16) | 32-bit PNG | `store_assets/screenshots/playstore_phone_screenshot_4_hardware_encryption.png` |
| **Screenshot 5 (PIN/Pattern/Pass)** | 1080 x 1920 px (9:16) | 32-bit PNG | `store_assets/screenshots/playstore_phone_screenshot_5_lock_modes.png` |
| **Screenshot 6 (Double-Lock Advisor)**| 1080 x 1920 px (9:16) | 32-bit PNG | `store_assets/screenshots/playstore_phone_screenshot_6_double_lock_advisor.png` |
| **Screenshot 7 (Forensic Shredder)** | 1080 x 1920 px (9:16) | 32-bit PNG | `store_assets/screenshots/playstore_phone_screenshot_7_forensic_shredder.png` |
| **Screenshot 8 (Command Center Hub)** | 1080 x 1920 px (9:16) | 32-bit PNG | `store_assets/screenshots/playstore_phone_screenshot_8_security_hub.png` |
| **Privacy Policy HTML** | Public Web Page | HTML | `store_assets/PRIVACY_POLICY.html` & `index.html` |
| **Monetization & Ads Config** | IAB Standard | TXT | `store_assets/monetization.txt` & `app-ads.txt` |
| **Release Artifact** | Signed Bundle | `.aab` | `store_assets/app-release.aab` |

---

## 3. 🛡️ Google Play Data Safety Questionnaire Answers

When completing the **Data Safety** section in Google Play Console, use the following declarations:

1. **Does your app collect or share any user data?**
   - Select: **Yes** (solely due to Google AdMob Advertising ID collection).
2. **Data Types Collected**:
   - **Device or other IDs**:
     - *Advertising ID* (Collected by Google AdMob for ads and fraud prevention).
     - *Encrypted in transit?* **Yes** (HTTPS).
     - *Can users request deletion?* **Yes**.
     - *Purpose:* Advertising or marketing, Analytics.
   - **Photos & Videos**:
     - *Is user photo collected/shared off device?* **No**. Captured intruder photos are processed and stored strictly locally inside sandboxed, hardware-encrypted app storage.
   - **App info and performance**:
     - *Diagnostics / Crash logs* (Optional AdMob crash metrics).
3. **Data Security Practices**:
   - Data is encrypted in transit via standard HTTPS.
   - All sensitive on-device data is encrypted with hardware KeyStore AES-256 GCM.

---

## 4. 📋 Accessibility Service Declaration (For Reviewers)

When asked to explain the usage of `BIND_ACCESSIBILITY_SERVICE` in Google Play Console:

```text
App Locker Pro utilizes BIND_ACCESSIBILITY_SERVICE strictly as an app locking mechanism to detect TYPE_WINDOW_STATE_CHANGED events when protected target applications are opened. This allows the app to display a secure lock screen overlay with zero latency (0ms), preventing private screen content from being glimpsed. The service does not read user interface text, monitor user keystrokes, track user actions, or transmit any data off the device.
```

---

## 5. 🏷️ Category, Tags & Content Rating

- **Primary Category:** Tools
- **Secondary Category (optional):** Productivity
- **Tags:** App Lock, Privacy, Security, Fingerprint Lock, Vault, Anti Theft
- **Target Audience:** 18+ (Ages 18 and over)
- **Content Rating:** Everyone (ESRB) / PEGI 3 / USK 0 (No violence, no user-generated content, no unfiltered internet access).
- **Contains Ads:** Select **"Yes, my app contains ads"** (Google AdMob integrated).

---

## 6. 💳 In-App Products & Subscriptions SKU Summary

| SKU | Type | Price (USD) | Description |
|---|---|---|---|
| `applocker_pro_monthly` | Auto-renewing Subscription | $1.99 / mo | Monthly Pro Access |
| `applocker_pro_yearly` | Auto-renewing Subscription | $11.99 / yr | Annual Pro (7-Day Free Trial) |
| `applocker_pro_lifetime` | Non-Consumable In-App Product | $24.99 | Lifetime Pro License |
