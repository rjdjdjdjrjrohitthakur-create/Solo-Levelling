package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.LegendaryGold
import com.example.ui.theme.NeonCyan

data class PhysiqueTier(
    val stage: Int,
    val title: String,
    val subtitle: String,
    val color: Color
)

fun getPhysiqueTier(level: Int): PhysiqueTier {
    return when {
        level >= 40 -> PhysiqueTier(5, "Ascended Sovereign", "Godlike Cyber Apex", LegendaryGold)
        level >= 25 -> PhysiqueTier(4, "Shredded Titan", "Colossal V-Taper & Cyber Veins", NeonCyan)
        level >= 15 -> PhysiqueTier(3, "Muscular Vanguard", "Dense Hypertrophy & Deltoid Armor", CyberPurple)
        level >= 5 -> PhysiqueTier(2, "Athletic Operative", "Toned Frame & Lean Mass", Color(0xFF38BDF8))
        else -> PhysiqueTier(1, "Novice Silhouette", "Lean & Frail (Awakening)", Color(0xFF94A3B8))
    }
}

@Composable
fun EvolvingPhysiqueAvatar(
    level: Int,
    modifier: Modifier = Modifier
) {
    val tier = getPhysiqueTier(level)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_aura")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val coreGlowRadius by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_glow"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val centerX = canvasW / 2f
            val topY = canvasH * 0.08f

            // Evolution factor from 0f (Level 1) to 1f (Level 40+)
            val evolutionFactor = ((level - 1) / 39f).coerceIn(0f, 1f)

            // Dynamic proportions based on level
            // 1. Shoulders widen from 36% of canvas to 82% of canvas
            val shoulderHalfWidth = canvasW * (0.18f + (0.23f * evolutionFactor))

            // 2. Torso V-Taper
            val chestHalfWidth = canvasW * (0.13f + (0.17f * evolutionFactor))
            val waistHalfWidth = canvasW * (0.10f + (0.07f * evolutionFactor))

            // 3. Muscle thickness
            val armThickness = 10f + (22f * evolutionFactor)
            val legThickness = 12f + (20f * evolutionFactor)
            val neckThickness = 12f + (14f * evolutionFactor)

            // Primary color shifts from muted steel/faint cyan to blazing electric neon & gold
            val primaryColor = when (tier.stage) {
                5 -> LegendaryGold
                4 -> NeonCyan
                3 -> CyberPurple
                2 -> Color(0xFF38BDF8)
                else -> Color(0xFF64748B)
            }

            val glowColor = primaryColor.copy(alpha = pulseAlpha)

            // GOD TIER / ASCENDED: Celestial Floating Halo
            if (tier.stage >= 5) {
                drawCircle(
                    color = LegendaryGold.copy(alpha = 0.25f * pulseAlpha),
                    radius = canvasW * 0.42f,
                    center = Offset(centerX, topY + canvasH * 0.22f),
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(LegendaryGold.copy(alpha = 0.3f), Color.Transparent),
                        center = Offset(centerX, topY + canvasH * 0.22f),
                        radius = canvasW * 0.45f
                    ),
                    radius = canvasW * 0.45f,
                    center = Offset(centerX, topY + canvasH * 0.22f)
                )
            }

            // 1. HEAD & CYBER VISOR
            val headRadius = canvasH * 0.07f
            val headCenter = Offset(centerX, topY + headRadius)

            // Head outline
            drawCircle(
                color = Color(0xFF0F172A),
                radius = headRadius,
                center = headCenter
            )
            drawCircle(
                color = primaryColor,
                radius = headRadius,
                center = headCenter,
                style = Stroke(width = 2.dp.toPx())
            )

            // Glowing Visor / Cyber Eye
            val visorY = headCenter.y - (headRadius * 0.1f)
            val visorW = headRadius * (0.7f + (0.3f * evolutionFactor))
            drawLine(
                color = glowColor,
                start = Offset(centerX - visorW, visorY),
                end = Offset(centerX + visorW, visorY),
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 2. NECK & TRAPEZIUS
            val neckStartY = headCenter.y + headRadius
            val neckEndY = neckStartY + (canvasH * 0.05f)

            // Trapezius muscles (grow prominently with level)
            if (evolutionFactor > 0.15f) {
                val trapPath = Path().apply {
                    moveTo(centerX - (neckThickness * 0.5f), neckStartY + 5f)
                    lineTo(centerX - shoulderHalfWidth * 0.6f, neckEndY + 10f)
                    lineTo(centerX + shoulderHalfWidth * 0.6f, neckEndY + 10f)
                    lineTo(centerX + (neckThickness * 0.5f), neckStartY + 5f)
                    close()
                }
                drawPath(
                    path = trapPath,
                    color = primaryColor.copy(alpha = 0.3f)
                )
            }

            // Neck line
            drawLine(
                color = primaryColor,
                start = Offset(centerX, neckStartY),
                end = Offset(centerX, neckEndY),
                strokeWidth = neckThickness,
                cap = StrokeCap.Round
            )

            // 3. TORSO & CHEST
            val torsoTopY = neckEndY
            val torsoBottomY = torsoTopY + (canvasH * 0.28f)
            val chestY = torsoTopY + (canvasH * 0.10f)

            val torsoPath = Path().apply {
                moveTo(centerX - shoulderHalfWidth, torsoTopY)
                lineTo(centerX + shoulderHalfWidth, torsoTopY)
                lineTo(centerX + chestHalfWidth, chestY)
                lineTo(centerX + waistHalfWidth, torsoBottomY)
                lineTo(centerX - waistHalfWidth, torsoBottomY)
                lineTo(centerX - chestHalfWidth, chestY)
                close()
            }

            // Torso Fill & Armor
            drawPath(
                path = torsoPath,
                color = Color(0xFF131D33)
            )
            drawPath(
                path = torsoPath,
                color = primaryColor,
                style = Stroke(width = 2.dp.toPx())
            )

            // Chest Pectoral & Abdominal Plates (more defined as level increases)
            if (evolutionFactor > 0.1f) {
                // Midline sternum
                drawLine(
                    color = primaryColor.copy(alpha = 0.6f),
                    start = Offset(centerX, torsoTopY + 8f),
                    end = Offset(centerX, torsoBottomY - 10f),
                    strokeWidth = 1.8.dp.toPx()
                )

                // Pec dividers
                drawLine(
                    color = primaryColor.copy(alpha = 0.5f),
                    start = Offset(centerX - (chestHalfWidth * 0.7f), chestY),
                    end = Offset(centerX + (chestHalfWidth * 0.7f), chestY),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Abdominal horizontal grid for Muscular/Titan tiers
                if (evolutionFactor > 0.35f) {
                    val abSpacing = (torsoBottomY - chestY) / 3.5f
                    for (i in 1..2) {
                        val abY = chestY + (i * abSpacing)
                        val abW = waistHalfWidth * 0.65f
                        drawLine(
                            color = primaryColor.copy(alpha = 0.45f),
                            start = Offset(centerX - abW, abY),
                            end = Offset(centerX + abW, abY),
                            strokeWidth = 1.2.dp.toPx()
                        )
                    }
                }
            }

            // Glowing Arc Reactor / Cybernetic Core (center of chest)
            val coreCenter = Offset(centerX, torsoTopY + (canvasH * 0.08f))
            drawCircle(
                color = primaryColor.copy(alpha = 0.2f),
                radius = coreGlowRadius * (1f + (evolutionFactor * 1.5f)),
                center = coreCenter
            )
            drawCircle(
                color = glowColor,
                radius = 4.dp.toPx() + (evolutionFactor * 3.dp.toPx()),
                center = coreCenter
            )

            // 4. SHOULDERS & ARMS
            val leftShoulder = Offset(centerX - shoulderHalfWidth, torsoTopY + 6f)
            val rightShoulder = Offset(centerX + shoulderHalfWidth, torsoTopY + 6f)

            // Deltoid shoulder pads
            val shoulderRadius = armThickness * 0.9f
            drawCircle(color = Color(0xFF1E293B), radius = shoulderRadius, center = leftShoulder)
            drawCircle(color = primaryColor, radius = shoulderRadius, center = leftShoulder, style = Stroke(1.5.dp.toPx()))

            drawCircle(color = Color(0xFF1E293B), radius = shoulderRadius, center = rightShoulder)
            drawCircle(color = primaryColor, radius = shoulderRadius, center = rightShoulder, style = Stroke(1.5.dp.toPx()))

            val elbowDrop = canvasH * 0.16f
            val handDrop = canvasH * 0.30f
            val armSpread = canvasW * (0.04f + (0.04f * evolutionFactor))

            val leftElbow = Offset(leftShoulder.x - armSpread, leftShoulder.y + elbowDrop)
            val rightElbow = Offset(rightShoulder.x + armSpread, rightShoulder.y + elbowDrop)

            val leftHand = Offset(leftElbow.x - (armSpread * 0.5f), leftShoulder.y + handDrop)
            val rightHand = Offset(rightElbow.x + (armSpread * 0.5f), rightShoulder.y + handDrop)

            // Biceps / Upper arms
            drawLine(
                color = primaryColor,
                start = leftShoulder,
                end = leftElbow,
                strokeWidth = armThickness,
                cap = StrokeCap.Round
            )
            drawLine(
                color = primaryColor,
                start = rightShoulder,
                end = rightElbow,
                strokeWidth = armThickness,
                cap = StrokeCap.Round
            )

            // Forearms
            val forearmThickness = armThickness * (0.75f + (0.15f * evolutionFactor))
            drawLine(
                color = primaryColor,
                start = leftElbow,
                end = leftHand,
                strokeWidth = forearmThickness,
                cap = StrokeCap.Round
            )
            drawLine(
                color = primaryColor,
                start = rightElbow,
                end = rightHand,
                strokeWidth = forearmThickness,
                cap = StrokeCap.Round
            )

            // Fist knuckles
            drawCircle(color = glowColor, radius = forearmThickness * 0.65f, center = leftHand)
            drawCircle(color = glowColor, radius = forearmThickness * 0.65f, center = rightHand)

            // 5. PELVIS & LEGS
            val hipWidth = waistHalfWidth * 0.85f
            val leftHip = Offset(centerX - hipWidth, torsoBottomY)
            val rightHip = Offset(centerX + hipWidth, torsoBottomY)

            val kneeDrop = canvasH * 0.18f
            val footDrop = canvasH * 0.34f
            val stanceSpread = canvasW * (0.08f + (0.06f * evolutionFactor))

            val leftKnee = Offset(leftHip.x - (stanceSpread * 0.4f), leftHip.y + kneeDrop)
            val rightKnee = Offset(rightHip.x + (stanceSpread * 0.4f), rightHip.y + kneeDrop)

            val leftFoot = Offset(leftHip.x - stanceSpread, leftHip.y + footDrop)
            val rightFoot = Offset(rightHip.x + stanceSpread, rightHip.y + footDrop)

            // Thighs (Quads)
            drawLine(
                color = primaryColor,
                start = leftHip,
                end = leftKnee,
                strokeWidth = legThickness,
                cap = StrokeCap.Round
            )
            drawLine(
                color = primaryColor,
                start = rightHip,
                end = rightKnee,
                strokeWidth = legThickness,
                cap = StrokeCap.Round
            )

            // Knees
            drawCircle(color = Color(0xFF1E293B), radius = legThickness * 0.65f, center = leftKnee)
            drawCircle(color = primaryColor, radius = legThickness * 0.65f, center = leftKnee, style = Stroke(1.5.dp.toPx()))

            drawCircle(color = Color(0xFF1E293B), radius = legThickness * 0.65f, center = rightKnee)
            drawCircle(color = primaryColor, radius = legThickness * 0.65f, center = rightKnee, style = Stroke(1.5.dp.toPx()))

            // Calves & Boots
            val calfThickness = legThickness * 0.85f
            drawLine(
                color = primaryColor,
                start = leftKnee,
                end = leftFoot,
                strokeWidth = calfThickness,
                cap = StrokeCap.Round
            )
            drawLine(
                color = primaryColor,
                start = rightKnee,
                end = rightFoot,
                strokeWidth = calfThickness,
                cap = StrokeCap.Round
            )

            // Ground base cyber pulse
            drawLine(
                color = primaryColor.copy(alpha = 0.5f),
                start = Offset(leftFoot.x - 14f, leftFoot.y + 4f),
                end = Offset(leftFoot.x + 14f, leftFoot.y + 4f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = primaryColor.copy(alpha = 0.5f),
                start = Offset(rightFoot.x - 14f, rightFoot.y + 4f),
                end = Offset(rightFoot.x + 14f, rightFoot.y + 4f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
