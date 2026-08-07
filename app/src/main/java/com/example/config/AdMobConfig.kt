package com.example.config

/**
 * AdMob Configuration & Test Ad Unit IDs.
 * 
 * Instructions for Production Release:
 * Replace these Google Test Ad Unit IDs with your official AdMob IDs from your AdMob Dashboard:
 * https://admob.google.com/
 */
object AdMobConfig {

    // Official Google AdMob Sample App ID for Testing
    // (In AndroidManifest.xml: com.google.android.gms.ads.APPLICATION_ID)
    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"

    // 1. Banner Ad Unit ID
    // Standard 320x50 / Adaptive Banner shown at screen bottoms
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    // 2. Interstitial Ad Unit ID
    // Full-screen transition ads (e.g. after clearing logs or changing security settings)
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    // 3. Rewarded Video Ad Unit ID
    // Rewarded ads for unlocking intruder photo snapshots
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    // 4. Rewarded Interstitial Ad Unit ID
    // High-engagement video ads with auto-reward triggers
    const val REWARDED_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/5354046379"

    // 5. Native Advanced Ad Unit ID
    // Native cards blended directly into app lists (In-Feed)
    const val NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

    // 6. App Open Ad Unit ID
    // Displayed when bringing the app to foreground
    const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"
}
