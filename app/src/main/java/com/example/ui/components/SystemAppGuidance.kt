package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * System App Locking Security Guidance Component.
 * Recommends users lock system apps ("com.android.settings", "com.google.android.packageinstaller", etc.)
 * to prevent intruders from disabling App Locker or revoking permissions.
 */
@Composable
fun SystemAppLockingGuidanceCard(
    isSettingsLocked: Boolean,
    onLockSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSettingsLocked) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("system_app_guidance_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isSettingsLocked) Icons.Default.Shield else Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = if (isSettingsLocked) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "System Security Guidance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSettingsLocked) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = if (isSettingsLocked) "System Settings Protected" else "Recommended Security Policy",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSettingsLocked) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "To prevent unauthorized users or thieves from force-stopping App Locker or revoking permissions, always lock System Settings ('com.android.settings') and Package Installer.",
                style = MaterialTheme.typography.bodySmall,
                color = if (isSettingsLocked) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f)
            )

            if (!isSettingsLocked) {
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onLockSettingsClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lock_system_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Protect System Settings Now",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
