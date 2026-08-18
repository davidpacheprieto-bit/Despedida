package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Checkpoint
import com.example.data.model.TeamSide
import com.example.service.LocationHelper
import com.example.service.UserCoordinates
import com.example.ui.theme.BoldThemeBorder
import com.example.ui.theme.BoldThemeSurface
import com.example.ui.theme.BoldThemeSurfaceVariant
import com.example.ui.theme.BoldThemeTextMuted
import com.example.ui.theme.BoldThemeTextPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldCelebration
import com.example.ui.theme.PurpleDeep
import com.example.ui.theme.PurpleLightContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.RosePrimary

enum class MapDisplayMode {
    GOOGLE_MAPS_REAL,
    RADAR_TACTICO
}

@Composable
fun LeonGpsMapRadar(
    checkpoints: List<Checkpoint>,
    activeCheckpoint: Checkpoint?,
    userCoordinates: UserCoordinates,
    distanceMeters: Double,
    bearingDegrees: Float,
    isNearActive: Boolean,
    userTeam: TeamSide,
    onCheckpointSelected: (Checkpoint) -> Unit,
    onSimulateWalk: () -> Unit,
    onOpenActiveChallenge: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mapMode by remember { mutableStateOf(MapDisplayMode.GOOGLE_MAPS_REAL) }

    val infiniteTransition = rememberInfiniteTransition(label = "radar_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    fun launchGoogleMapsNavigation(lat: Double, lng: Double, label: String) {
        try {
            val uri = Uri.parse("google.navigation:q=$lat,$lng&mode=w")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng&travelmode=walking")
                context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
            }
        } catch (e: Exception) {
            val fallbackUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
            context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("leon_gps_map_card"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Mode Selector: Real Google Map of León vs Radar Táctico
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = BoldThemeSurfaceVariant.copy(alpha = 0.7f),
            border = BorderStroke(1.dp, BoldThemeBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Real Map Tab
                val isRealMap = mapMode == MapDisplayMode.GOOGLE_MAPS_REAL
                Surface(
                    modifier = Modifier
                        .weight(1.2f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { mapMode = MapDisplayMode.GOOGLE_MAPS_REAL }
                        .testTag("tab_real_google_map"),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isRealMap) PurplePrimary else Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = if (isRealMap) Color.White else BoldThemeTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "MAPA REAL LEÓN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = if (isRealMap) Color.White else BoldThemeTextPrimary
                        )
                    }
                }

                // Radar Tab
                val isRadar = mapMode == MapDisplayMode.RADAR_TACTICO
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { mapMode = MapDisplayMode.RADAR_TACTICO }
                        .testTag("tab_radar_tactico"),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isRadar) PurplePrimary else Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = null,
                            tint = if (isRadar) Color.White else BoldThemeTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RADAR GPS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = if (isRadar) Color.White else BoldThemeTextPrimary
                        )
                    }
                }
            }
        }

        // Map View Container
        if (mapMode == MapDisplayMode.GOOGLE_MAPS_REAL) {
            LeonRealMapView(
                checkpoints = checkpoints,
                activeCheckpoint = activeCheckpoint,
                userCoordinates = userCoordinates,
                userTeam = userTeam,
                onCheckpointSelected = onCheckpointSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )
        } else {
            // Interactive 2D Radar Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(PurpleLightContainer.copy(alpha = 0.35f))
                    .border(1.dp, BoldThemeBorder.copy(alpha = 0.5f), RoundedCornerShape(26.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(checkpoints) {
                            detectTapGestures { offset ->
                                val w = size.width.toFloat()
                                val h = size.height.toFloat()
                                val tapped = checkpoints.minByOrNull { cp ->
                                    val (cx, cy) = computeNormalizedCoords(cp.latitude, cp.longitude, w, h)
                                    val dx = cx - offset.x
                                    val dy = cy - offset.y
                                    dx * dx + dy * dy
                                }
                                if (tapped != null) {
                                    onCheckpointSelected(tapped)
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    // Map Grid & Historical Roman Streets (León)
                    val gridColor = Color(0xFF6750A4).copy(alpha = 0.12f)
                    val wallColor = Color(0xFF6750A4).copy(alpha = 0.28f)

                    // Roman Wall Boundary
                    drawLine(wallColor, Offset(w * 0.15f, h * 0.18f), Offset(w * 0.85f, h * 0.18f), strokeWidth = 3f, cap = StrokeCap.Round)
                    drawLine(wallColor, Offset(w * 0.85f, h * 0.18f), Offset(w * 0.85f, h * 0.75f), strokeWidth = 3f, cap = StrokeCap.Round)
                    drawLine(wallColor, Offset(w * 0.85f, h * 0.75f), Offset(w * 0.15f, h * 0.75f), strokeWidth = 3f, cap = StrokeCap.Round)
                    drawLine(wallColor, Offset(w * 0.15f, h * 0.75f), Offset(w * 0.15f, h * 0.18f), strokeWidth = 3f, cap = StrokeCap.Round)

                    // Street Arteries (Calle Ancha, Plaza Mayor, Húmedo)
                    drawLine(gridColor, Offset(w * 0.25f, h * 0.35f), Offset(w * 0.75f, h * 0.35f), strokeWidth = 2f)
                    drawLine(gridColor, Offset(w * 0.40f, h * 0.18f), Offset(w * 0.40f, h * 0.82f), strokeWidth = 2f)
                    drawLine(gridColor, Offset(w * 0.60f, h * 0.25f), Offset(w * 0.60f, h * 0.75f), strokeWidth = 2f)

                    // Route trajectory path
                    val points = checkpoints.map { cp ->
                        computeNormalizedCoords(cp.latitude, cp.longitude, w, h)
                    }

                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val cp1 = checkpoints[i]
                        val isSegmentDone = cp1.isCompleted

                        drawLine(
                            color = if (isSegmentDone) EmeraldSuccess else PurplePrimary.copy(alpha = 0.7f),
                            start = Offset(p1.first, p1.second),
                            end = Offset(p2.first, p2.second),
                            strokeWidth = if (isSegmentDone) 4f else 2.5f,
                            pathEffect = if (isSegmentDone) null else PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f),
                            cap = StrokeCap.Round
                        )
                    }

                    // Checkpoint Nodes
                    checkpoints.forEach { cp ->
                        val (cx, cy) = computeNormalizedCoords(cp.latitude, cp.longitude, w, h)
                        val isTarget = cp.id == activeCheckpoint?.id
                        val isDone = cp.isCompleted

                        val nodeColor = when {
                            isDone -> EmeraldSuccess
                            isTarget -> RosePrimary
                            cp.isUnlocked -> PurplePrimary
                            else -> Color(0xFF9E9E9E)
                        }

                        if (isTarget) {
                            drawCircle(
                                color = RosePrimary.copy(alpha = pulseAlpha * 0.5f),
                                radius = 22.dp.toPx() * pulseRadius,
                                center = Offset(cx, cy)
                            )
                        }

                        // Node Outer
                        drawCircle(
                            color = nodeColor,
                            radius = if (isTarget) 12.dp.toPx() else 8.dp.toPx(),
                            center = Offset(cx, cy),
                            style = Stroke(width = 2.5.dp.toPx())
                        )

                        // Node Inner
                        drawCircle(
                            color = if (isDone) EmeraldSuccess else if (isTarget) RosePrimary else Color.White,
                            radius = if (isTarget) 7.dp.toPx() else 5.dp.toPx(),
                            center = Offset(cx, cy)
                        )
                    }

                    // User Location Dot
                    val (ux, uy) = computeNormalizedCoords(userCoordinates.latitude, userCoordinates.longitude, w, h)
                    drawCircle(
                        color = PurplePrimary.copy(alpha = 0.25f),
                        radius = 16.dp.toPx(),
                        center = Offset(ux, uy)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 7.dp.toPx(),
                        center = Offset(ux, uy)
                    )
                    drawCircle(
                        color = PurplePrimary,
                        radius = 5.dp.toPx(),
                        center = Offset(ux, uy)
                    )
                }

                // Top GPS Status Pill HUD
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.92f),
                        shadowElevation = 2.dp,
                        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSuccess)
                            )
                            Text(
                                text = "GPS: LEÓN ACTIVO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = BoldThemeTextPrimary
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.92f),
                        shadowElevation = 2.dp,
                        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "📍 ${activeCheckpoint?.landmarkName ?: "León"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RosePrimary
                            )
                        }
                    }
                }
            }
        }

        // Active Checkpoint / Floating Card with Google Maps Direct Directions
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(24.dp))
                .testTag("active_checkpoint_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header: Badge + Point Count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Surface(
                            shape = CircleShape,
                            color = PurpleLightContainer
                        ) {
                            Text(
                                text = "PRUEBA ACTUAL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = PurplePrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = activeCheckpoint?.title ?: "Desafío de León",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = BoldThemeTextPrimary,
                            letterSpacing = (-0.3).sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "PUNTO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BoldThemeTextMuted
                        )
                        Text(
                            text = "${activeCheckpoint?.orderIndex ?: 1} / ${checkpoints.size}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = BoldThemeTextPrimary
                        )
                    }
                }

                // Challenge Description
                Text(
                    text = activeCheckpoint?.challengeDescription ?: "Completa la prueba para desbloquear el siguiente punto de la ruta.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = BoldThemeTextMuted,
                    lineHeight = 17.sp
                )

                // Distance / Proximity Banner
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = null,
                        tint = if (isNearActive) EmeraldSuccess else PurplePrimary,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(bearingDegrees)
                    )
                    Text(
                        text = if (isNearActive) "🎯 ¡Estás dentro del radio del reto (<45m)!" else "Distancia al objetivo: ${LocationHelper.formatDistance(distanceMeters)}",
                        fontSize = 11.sp,
                        fontWeight = if (isNearActive) FontWeight.ExtraBold else FontWeight.Bold,
                        color = if (isNearActive) EmeraldSuccess else PurplePrimary
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenActiveChallenge,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                            .testTag("open_challenge_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isNearActive) EmeraldSuccess else PurplePrimary
                        )
                    ) {
                        Text(
                            text = if (isNearActive) "COMPLETAR RETO (+${activeCheckpoint?.pointsReward ?: 150}P)" else "VER / HACER RETO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = Color.White
                        )
                    }

                    // Direct Google Maps walking navigation button
                    Button(
                        onClick = {
                            if (activeCheckpoint != null) {
                                launchGoogleMapsNavigation(
                                    activeCheckpoint.latitude,
                                    activeCheckpoint.longitude,
                                    activeCheckpoint.landmarkName
                                )
                            }
                        },
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("open_gmaps_directions_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BoldThemeSurfaceVariant,
                            contentColor = BoldThemeTextPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Ruta en Google Maps",
                            modifier = Modifier.size(16.dp),
                            tint = PurplePrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Maps",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onSimulateWalk,
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("simulate_walk_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BoldThemeSurfaceVariant,
                            contentColor = BoldThemeTextPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Demo",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Convert real León coordinates to 2D Canvas relative coordinates
private fun computeNormalizedCoords(lat: Double, lng: Double, width: Float, height: Float): Pair<Float, Float> {
    val minLat = 42.5940
    val maxLat = 42.6015
    val minLng = -5.5735
    val maxLng = -5.5655

    val normX = ((lng - minLng) / (maxLng - minLng)).coerceIn(0.05, 0.95).toFloat()
    val normY = (1.0f - ((lat - minLat) / (maxLat - minLat)).coerceIn(0.05, 0.95)).toFloat()

    return Pair(normX * width, normY * height)
}
