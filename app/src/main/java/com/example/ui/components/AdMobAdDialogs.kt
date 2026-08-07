package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.config.AdMobConfig
import kotlinx.coroutines.delay

/**
 * Rewarded Video Ad Dialog for high-value features (e.g. Unlocking Intruder Photo Snapshot).
 */
@Composable
fun RewardedAdDialog(
    adTitle: String = "Reveal Intruder Snapshot",
    onRewardGranted: () -> Unit,
    onDismiss: () -> Unit
) {
    var timerSeconds by remember { mutableIntStateOf(5) }
    var isFinished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (timerSeconds > 0) {
            delay(1000L)
            timerSeconds--
        }
        isFinished = true
    }

    Dialog(
        onDismissRequest = {
            if (isFinished) {
                onRewardGranted()
            } else {
                onDismiss()
            }
        },
        properties = DialogProperties(dismissOnBackPress = isFinished, dismissOnClickOutside = isFinished)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("rewarded_ad_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFF57C00),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "REWARDED VIDEO AD",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (isFinished) {
                        IconButton(onClick = {
                            onRewardGranted()
                        }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close Ad")
                        }
                    } else {
                        Text(
                            text = "Skip in ${timerSeconds}s",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Text(
                    text = adTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Watching this video unlocks full snapshot. Test Ad Unit: ${AdMobConfig.REWARDED_AD_UNIT_ID}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                if (!isFinished) {
                    LinearProgressIndicator(
                        progress = { (5 - timerSeconds) / 5f },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                } else {
                    Button(
                        onClick = { onRewardGranted() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("claim_reward_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Claim Reward & View Photo", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Interstitial Full-Screen Ad Dialog for Transition & Administration Actions (e.g. Clearing Logs).
 */
@Composable
fun InterstitialAdDialog(
    actionTitle: String = "Administrative Action Completed",
    onAdDismissed: () -> Unit
) {
    var timerSeconds by remember { mutableIntStateOf(3) }
    var canClose by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (timerSeconds > 0) {
            delay(1000L)
            timerSeconds--
        }
        canClose = true
    }

    Dialog(
        onDismissRequest = {
            if (canClose) onAdDismissed()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("interstitial_ad_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "INTERSTITIAL AD",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Button(
                        onClick = onAdDismissed,
                        enabled = canClose,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = if (canClose) "Close Ad X" else "Wait ${timerSeconds}s",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(72.dp)
                    )

                    Text(
                        text = actionTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Thank you for keeping your device secure! Test Ad Unit: ${AdMobConfig.INTERSTITIAL_AD_UNIT_ID}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Upgrade to Premium to permanently disable all ads.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
