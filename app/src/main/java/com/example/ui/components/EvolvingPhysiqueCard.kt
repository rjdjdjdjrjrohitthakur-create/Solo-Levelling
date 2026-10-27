package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.LegendaryGold
import com.example.ui.theme.NeonCyan

@Composable
fun EvolvingPhysiqueCard(
    playerLevel: Int,
    modifier: Modifier = Modifier
) {
    var showPreviewModal by remember { mutableStateOf(false) }
    val currentTier = remember(playerLevel) { getPhysiqueTier(playerLevel) }

    CyberCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("evolving_physique_card"),
        borderColor = currentTier.color.copy(alpha = 0.5f),
        borderWidth = 1.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            Color(0xFF0C1324)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = currentTier.color,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PHYSIQUE TRANSFORMATION",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = currentTier.color,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = currentTier.color.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, currentTier.color.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clickable { showPreviewModal = true }
                        .testTag("preview_physique_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Preview",
                            tint = currentTier.color,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PREVIEW",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = currentTier.color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body Figure and Stats Column
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Humanoid Avatar Canvas
                Box(
                    modifier = Modifier
                        .size(130.dp, 175.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF070B16))
                        .border(BorderStroke(1.dp, CyberBorder), RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EvolvingPhysiqueAvatar(
                        level = playerLevel,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Progression Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TIER ${currentTier.stage}/5",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = currentTier.color,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = currentTier.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = currentTier.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dynamic Physique Note
                    val description = when (currentTier.stage) {
                        1 -> "Lean and slender rookie frame. As you complete real-life workouts, habits, and tasks, your avatar will build real muscle, broader shoulders, and cybernetic veins!"
                        2 -> "Toned athletic build. Shoulders have broadened and kinetic energy veins are activating."
                        3 -> "Vanguard muscular hypertrophy. Defined deltoids, chest plates, and bicep volume."
                        4 -> "Chiseled Titan frame. Deep V-taper, sculpted abs, and glowing plasma veins."
                        else -> "Ascended Sovereign. Supreme godlike physique with celestial halo and maxed power."
                    }

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }

    if (showPreviewModal) {
        PhysiquePreviewDialog(
            currentLevel = playerLevel,
            onDismiss = { showPreviewModal = false }
        )
    }
}

@Composable
fun PhysiquePreviewDialog(
    currentLevel: Int,
    onDismiss: () -> Unit
) {
    var previewLevel by remember { mutableFloatStateOf(currentLevel.toFloat()) }
    val simulatedTier = remember(previewLevel) { getPhysiqueTier(previewLevel.toInt()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PHYSIQUE EVOLUTION CHAMBER",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = NeonCyan,
                    letterSpacing = 1.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Simulate how your avatar transforms as you level up in real life:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .size(150.dp, 200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF070B16))
                        .border(BorderStroke(1.5.dp, simulatedTier.color), RoundedCornerShape(16.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EvolvingPhysiqueAvatar(
                        level = previewLevel.toInt(),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "SIMULATED LEVEL ${previewLevel.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = simulatedTier.color
                )

                Text(
                    text = "${simulatedTier.title} • Tier ${simulatedTier.stage}/5",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = previewLevel,
                    onValueChange = { previewLevel = it },
                    valueRange = 1f..50f,
                    steps = 48,
                    colors = SliderDefaults.colors(
                        thumbColor = simulatedTier.color,
                        activeTrackColor = simulatedTier.color,
                        inactiveTrackColor = CyberBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("preview_level_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Lvl 1 (Frail)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("Lvl 15 (Muscular)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("Lvl 50 (God)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("CLOSE", fontWeight = FontWeight.Bold, color = Color(0xFF030712))
            }
        }
    )
}
