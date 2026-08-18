package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PartyNotification
import com.example.data.model.TeamSide
import com.example.service.PhotoHelper
import com.example.ui.theme.BoldThemeBackground
import com.example.ui.theme.BoldThemeBorder
import com.example.ui.theme.BoldThemeSurface
import com.example.ui.theme.BoldThemeSurfaceVariant
import com.example.ui.theme.BoldThemeTextMuted
import com.example.ui.theme.BoldThemeTextPrimary
import com.example.ui.theme.GoldCelebration
import com.example.ui.theme.PurpleDeep
import com.example.ui.theme.PurpleLightContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.RoseDark
import com.example.ui.theme.RoseLightContainer
import com.example.ui.theme.RosePrimary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LiveFeedScreen(
    notifications: List<PartyNotification>,
    userTeam: TeamSide,
    onNotificationClick: (PartyNotification) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("live_feed_screen")
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Header Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = PurpleLightContainer
            ),
            border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PurplePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "CANAL EN DIRECTO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = PurpleDeep
                    )
                    Text(
                        text = "Notificaciones reales y fotos subidas al completar cada reto",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = BoldThemeTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no hay avisos. ¡Comenzad la ruta para ver los retos!",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = BoldThemeTextMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(notifications, key = { it.id }) { item ->
                    NotificationFeedItem(
                        notification = item,
                        userTeam = userTeam,
                        onClick = { onNotificationClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationFeedItem(
    notification: PartyNotification,
    userTeam: TeamSide,
    onClick: () -> Unit
) {
    val isMyTeam = notification.teamSide == userTeam
    val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val formattedTime = timeFormatter.format(Date(notification.timestamp))

    val isRival = notification.isRivalAlert && !isMyTeam
    val cardBg = if (isRival) RoseLightContainer else BoldThemeSurface
    val borderCol = if (isRival) RosePrimary.copy(alpha = 0.3f) else BoldThemeBorder
    val hasPhoto = !notification.photoProofUri.isNullOrBlank()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("notification_feed_item_${notification.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        border = BorderStroke(1.dp, borderCol),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isRival) RosePrimary.copy(alpha = 0.15f) else PurpleLightContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(notification.emoji, fontSize = 20.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = notification.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.2).sp,
                                color = if (isRival) RoseDark else PurplePrimary
                            )

                            if (!isMyTeam && notification.isRivalAlert) {
                                Surface(
                                    shape = CircleShape,
                                    color = RosePrimary
                                ) {
                                    Text(
                                        text = "RIVAL",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = formattedTime,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BoldThemeTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = notification.message,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp,
                        color = BoldThemeTextPrimary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (notification.pointsDelta > 0) {
                            Surface(
                                shape = CircleShape,
                                color = PurpleLightContainer
                            ) {
                                Text(
                                    text = "+${notification.pointsDelta} PUNTOS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = PurpleDeep,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        if (hasPhoto) {
                            Surface(
                                shape = CircleShape,
                                color = GoldCelebration
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "VER FOTO",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // If notification has photo, show image thumbnail preview as well
            if (hasPhoto) {
                Spacer(modifier = Modifier.height(10.dp))
                val photoPath = notification.photoProofUri ?: ""
                val file = remember(photoPath) { File(photoPath) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black)
                        .clickable { onClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (file.exists()) {
                        AsyncImage(
                            model = file,
                            contentDescription = "Foto subida",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val placeholder = remember(notification.checkpointName, notification.teamSide) {
                            PhotoHelper.createFestivePlaceholderBitmap(
                                landmarkName = notification.checkpointName ?: "León",
                                teamName = notification.teamSide.displayName,
                                teamEmoji = notification.teamSide.emoji,
                                stickerText = "📸 Foto de Reto"
                            )
                        }
                        Image(
                            bitmap = placeholder.asImageBitmap(),
                            contentDescription = "Foto de prueba",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Overlay pill
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "🔍 Toca para ampliar",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}


