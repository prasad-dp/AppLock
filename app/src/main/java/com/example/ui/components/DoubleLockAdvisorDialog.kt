package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.GridAppInfo
import com.example.util.DoubleLockDetector

@Composable
fun DoubleLockAdvisorDialog(
    appInfo: GridAppInfo,
    onDismiss: () -> Unit,
    onApplyRecommendation: (timeoutChoice: String, dontShowAgain: Boolean) -> Unit
) {
    val reasonText = remember(appInfo.packageName, appInfo.appName) {
        DoubleLockDetector.getAppLockCategoryReason(appInfo.packageName, appInfo.appName)
    }

    var selectedOption by remember { mutableStateOf("15_sec") }
    var dontShowAgain by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "SMART ADVISOR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Text(
                        text = "Double-Lock Notice",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = appInfo.appName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = reasonText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "If this app already asks for a fingerprint or PIN, locking it with 'Immediate' re-lock can force you to unlock twice in rapid succession. Choose a re-lock recommendation:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                val recommendationOptions = listOf(
                    "15_sec" to "Set 15 Seconds Re-Lock (Recommended)",
                    "1_min" to "Set 1 Minute Re-Lock",
                    "immediately" to "Keep Immediate Re-Lock"
                )

                recommendationOptions.forEach { (key, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = key }
                            .padding(vertical = 6.dp)
                    ) {
                        RadioButton(
                            selected = (selectedOption == key),
                            onClick = { selectedOption = key }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (selectedOption == key) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedOption == key) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dontShowAgain = !dontShowAgain }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = dontShowAgain,
                        onCheckedChange = { dontShowAgain = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Don't warn again for this app",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApplyRecommendation(selectedOption, dontShowAgain) },
                modifier = Modifier.testTag("apply_double_lock_recommendation_button")
            ) {
                Text("Apply & Lock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
