package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.GoldCelebration
import com.example.ui.theme.LeonPurpleLight
import com.example.ui.theme.TeamAitorPrimary
import com.example.ui.theme.TeamAmaiaPrimary
import kotlin.random.Random

private data class ConfettiParticle(
    val initialX: Float,
    val initialY: Float,
    val speedX: Float,
    val speedY: Float,
    val size: Float,
    val color: Color,
    val rotationSpeed: Float
)

@Composable
fun ConfettiEffect(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    var progress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 3500, easing = LinearEasing),
        label = "confetti_fall"
    )

    LaunchedEffect(isVisible) {
        progress = 1f
    }

    val particles = remember {
        val colors = listOf(
            GoldCelebration,
            LeonPurpleLight,
            TeamAitorPrimary,
            TeamAmaiaPrimary,
            Color(0xFF00E676),
            Color(0xFFFFEA00),
            Color(0xFFFF4081)
        )
        List(70) {
            ConfettiParticle(
                initialX = Random.nextFloat(),
                initialY = Random.nextFloat() * 0.2f - 0.1f,
                speedX = (Random.nextFloat() - 0.5f) * 0.4f,
                speedY = 0.6f + Random.nextFloat() * 0.6f,
                size = 12f + Random.nextFloat() * 18f,
                color = colors[Random.nextInt(colors.size)],
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        particles.forEach { p ->
            val curX = (p.initialX + p.speedX * animatedProgress) * canvasWidth
            val curY = (p.initialY + p.speedY * animatedProgress) * canvasHeight

            if (curY < canvasHeight) {
                drawRect(
                    color = p.color,
                    topLeft = Offset(curX, curY),
                    size = Size(p.size, p.size * 0.6f)
                )
            }
        }
    }
}
