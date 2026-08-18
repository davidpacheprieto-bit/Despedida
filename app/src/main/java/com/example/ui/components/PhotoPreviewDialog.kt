package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.TeamSide
import com.example.service.PhotoHelper
import com.example.ui.theme.BoldThemeBorder
import com.example.ui.theme.BoldThemeSurface
import com.example.ui.theme.BoldThemeSurfaceVariant
import com.example.ui.theme.BoldThemeTextMuted
import com.example.ui.theme.BoldThemeTextPrimary
import com.example.ui.theme.PurpleDeep
import com.example.ui.theme.PurpleLightContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.RoseDark
import com.example.ui.theme.RoseLightContainer
import com.example.ui.theme.RosePrimary
import java.io.File

@Composable
fun PhotoPreviewDialog(
    title: String,
    subtitle: String,
    photoPathOrUri: String?,
    teamSide: TeamSide,
    onDismiss: () -> Unit,
    onSaveToGallery: () -> Unit
) {
    val isAitor = teamSide == TeamSide.AITOR
    val teamColor = if (isAitor) PurplePrimary else RosePrimary
    val teamContainer = if (isAitor) PurpleLightContainer else RoseLightContainer
    val teamTextColor = if (isAitor) PurpleDeep else RoseDark

    val file = remember(photoPathOrUri) {
        if (!photoPathOrUri.isNullOrBlank()) File(photoPathOrUri) else null
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp)
                .testTag("photo_preview_modal_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = BoldThemeSurface),
            border = BorderStroke(1.5.dp, BoldThemeBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = teamContainer
                            ) {
                                Text(
                                    text = if (isAitor) "🦁 TEAM AITOR" else "👑 TEAM AMAIA",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = teamTextColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = "FOTO DE PRUEBA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BoldThemeTextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = BoldThemeTextPrimary
                        )
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = teamColor
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("close_photo_preview_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = BoldThemeTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Photo Display Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (file != null && file.exists()) {
                        AsyncImage(
                            model = file,
                            contentDescription = "Foto en $title",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else if (!photoPathOrUri.isNullOrBlank() && photoPathOrUri.startsWith("content://")) {
                        AsyncImage(
                            model = photoPathOrUri,
                            contentDescription = "Foto en $title",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        // Fallback festive generated snapshot
                        val placeholder = remember(title, teamSide) {
                            PhotoHelper.createFestivePlaceholderBitmap(
                                landmarkName = title,
                                teamName = teamSide.displayName,
                                teamEmoji = teamSide.emoji,
                                stickerText = "📸 Prueba Superada"
                            )
                        }
                        Image(
                            bitmap = placeholder.asImageBitmap(),
                            contentDescription = "Foto de prueba",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = BoldThemeSurfaceVariant)
                    ) {
                        Text("Cerrar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BoldThemeTextPrimary)
                    }

                    Button(
                        onClick = onSaveToGallery,
                        modifier = Modifier
                            .weight(1.6f)
                            .height(46.dp)
                            .testTag("save_preview_photo_to_gallery_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = teamColor)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GUARDAR EN GALERÍA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}
