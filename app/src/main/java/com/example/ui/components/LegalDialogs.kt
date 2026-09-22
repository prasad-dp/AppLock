package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("privacy_policy_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Policy,
                            contentDescription = "Privacy Policy",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Privacy Policy",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_privacy_policy")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    LegalHighlightCard(
                        title = "🔒 100% On-Device Privacy Architecture",
                        description = "This application strictly operates on a local-first security architecture. All passcodes, patterns, app configurations, and captured intruder photos are encrypted with AES-256 GCM in the Android KeyStore and NEVER leave your device. We do not operate remote servers or collect personal data."
                    )

                    LegalSectionHeader(title = "1. Global Legal Compliance (GDPR, CCPA, DPDP)")
                    LegalBodyText(
                        text = "• GDPR (European Union / UK): In compliance with Articles 5, 6, and 17, data processing is strictly limited to on-device owner security. Users hold an absolute Right to Erasure.\n" +
                                "• CCPA / CPRA (California, USA): We do NOT sell, rent, trade, or share user personal data with third parties for cross-context behavioral advertising.\n" +
                                "• DPDP Act (India, 2023): All processing occurs locally with explicit user consent for physical device protection and security purposes.\n" +
                                "• COPPA (USA): This application does not knowingly target, solicit, or collect information from children under 13 years of age."
                    )

                    LegalSectionHeader(title = "2. Intruder Selfie & Camera Processing")
                    LegalBodyText(
                        text = "• Purpose: The camera permission is used solely to silently capture front-camera security snapshots when consecutive unauthorized unlock attempts occur.\n" +
                                "• Security: Images are encrypted instantly in RAM with AES-256 GCM and stored as .enc files in sandboxed app storage. Raw unencrypted photos are never written to disk.\n" +
                                "• Consent & Transparency: The feature is strictly optional and can be toggled on/off at any time in Settings."
                    )

                    LegalSectionHeader(title = "3. Android Accessibility API Disclosure")
                    LegalBodyText(
                        text = "• Purpose: The Accessibility Service (BIND_ACCESSIBILITY_SERVICE) is utilized exclusively to provide the 0ms Instant Lock Engine by detecting TYPE_WINDOW_STATE_CHANGED events to intercept locked applications.\n" +
                                "• Privacy Commitment: The service NEVER collects, reads, inspects, or logs screen content, keystrokes, personal messages, credit card numbers, or passwords."
                    )

                    LegalSectionHeader(title = "4. Usage Stats & Overlay Permissions")
                    LegalBodyText(
                        text = "• PACKAGE_USAGE_STATS: Detects foreground package changes to trigger lock screens.\n" +
                                "• SYSTEM_ALERT_WINDOW: Draws the security passcode/pattern overlay above protected apps."
                    )

                    LegalSectionHeader(title = "5. Advertising & Third-Party SDKs (AdMob)")
                    LegalBodyText(
                        text = "• Free Tier: The free version displays banner, interstitial, and rewarded ads delivered via Google AdMob SDK.\n" +
                                "• Zero Personal Tracking: We do not collect or transmit personal identifiers. Ad delivery adheres to Google Play Families and privacy guidelines.\n" +
                                "• Ad-Free Experience: Upgrading to Premium Pro permanently removes all advertising and unlocks all security features."
                    )

                    LegalSectionHeader(title = "6. Biometric Data & Authentication")
                    LegalBodyText(
                        text = "• Biometric verification uses Android's official BiometricPrompt API.\n" +
                                "• The app never accesses, records, or stores raw biometric or fingerprint templates."
                    )

                    LegalSectionHeader(title = "7. Data Deletion & Right to Erasure")
                    LegalBodyText(
                        text = "You can immediately wipe all data at any time from Settings > 'Clear Data (Purge & Shred Storage)'. Deleting logs runs a 3-pass cryptographic shredder. Uninstalling the app permanently erases all sandbox data."
                    )

                    LegalSectionHeader(title = "8. Policy Updates & Inquiries")
                    LegalBodyText(
                        text = "Last updated: September 2026. For any privacy questions or regulatory inquiries, contact our Data Privacy team at: dptechsupport10@gmail.com."
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dismiss_privacy_policy_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("I Understand & Agree", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TermsOfServiceDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("terms_of_service_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = "Terms of Service",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Terms of Service",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_terms_of_service")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    LegalHighlightCard(
                        title = "⚖️ Authorized Device Owner Use",
                        description = "By using this application, you confirm that you are the lawful owner or authorized custodian of this device. App Locker is an anti-theft and privacy protection tool."
                    )

                    LegalSectionHeader(title = "1. Acceptance of Terms")
                    LegalBodyText(
                        text = "By installing, accessing, or using App Locker, you agree to be bound by these Terms of Service. If you do not agree to these terms, please uninstall the application."
                    )

                    LegalSectionHeader(title = "2. Lawful Security & Personal Privacy")
                    LegalBodyText(
                        text = "• The intruder selfie and app-locking features are provided exclusively for personal device security and unauthorized access prevention.\n" +
                                "• You agree not to use this software in violation of any applicable local, national, or international surveillance or privacy laws."
                    )

                    LegalSectionHeader(title = "3. Google Play Purchases & Billing")
                    LegalBodyText(
                        text = "• Premium Pro is an optional upgrade processed securely via Google Play In-App Billing.\n" +
                                "• Purchasing Premium Pro grants lifetime access to ad-free protection, advanced re-lock timers, and full intruder photo vault capabilities.\n" +
                                "• Purchases are bound to your Google Play account and can be restored at any time via the 'Restore Purchases' button in Settings.\n" +
                                "• Refund requests are handled in accordance with Google Play's standard refund policies."
                    )

                    LegalSectionHeader(title = "4. Free Tier & Advertising")
                    LegalBodyText(
                        text = "• The free version of App Locker is supported by Google AdMob advertising.\n" +
                                "• Ad delivery does not interfere with the core offline security functions of the application."
                    )

                    LegalSectionHeader(title = "5. Limitation of Liability")
                    LegalBodyText(
                        text = "The application is provided 'AS IS' without warranty of any kind. To the maximum extent permitted by applicable law, the developers shall not be liable for any indirect, incidental, or consequential damages resulting from device malfunction, forgotten master passwords, or third-party OS battery-saver interference."
                    )

                    LegalSectionHeader(title = "6. Modifications & Updates")
                    LegalBodyText(
                        text = "We reserve the right to update these terms to reflect changes in app functionality or regulatory requirements. Continued use of the application indicates acceptance of any revised terms."
                    )

                    LegalSectionHeader(title = "7. Contact")
                    LegalBodyText(
                        text = "Last updated: September 2026. For questions regarding these Terms, contact: dptechsupport10@gmail.com."
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dismiss_terms_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Accept Terms", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LegalHighlightCard(title: String, description: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun LegalSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun LegalBodyText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.25f
    )
}
