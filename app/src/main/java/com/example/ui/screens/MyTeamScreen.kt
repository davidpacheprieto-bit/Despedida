package com.example.ui.screens

import android.content.Context
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.Checkpoint
import com.example.data.model.TeamSide
import com.example.service.PhotoHelper
import com.example.ui.theme.BoldThemeBackground
import com.example.ui.theme.BoldThemeBorder
import com.example.ui.theme.BoldThemeSurface
import com.example.ui.theme.BoldThemeSurfaceVariant
import com.example.ui.theme.BoldThemeTextMuted
import com.example.ui.theme.BoldThemeTextPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PurpleDeep
import com.example.ui.theme.PurpleLightContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.RoseDark
import com.example.ui.theme.RoseLightContainer
import com.example.ui.theme.RosePrimary
import com.example.ui.viewmodel.PartyUiState
import java.io.File

@Composable
fun MyTeamScreen(
    uiState: PartyUiState,
    checkpoints: List<Checkpoint>,
    onChangeTeamClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAitor = uiState.selectedTeam == TeamSide.AITOR
    val teamColor = if (isAitor) PurplePrimary else RosePrimary
    val teamContainer = if (isAitor) PurpleLightContainer else RoseLightContainer
    val teamTextColor = if (isAitor) PurpleDeep else RoseDark
    val teamEmoji = if (isAitor) "🦁" else "👑"
    val teamTitle = if (isAitor) "TEAM AITOR (CAZURROS)" else "TEAM AMAIA (REINAS)"
    val teamScore = if (isAitor) uiState.aitorScore else uiState.amaiaScore
    val rivalScore = if (isAitor) uiState.amaiaScore else uiState.aitorScore
    val completedByThisTeam = checkpoints.count { it.isCompleted && it.completedByTeam == uiState.selectedTeam }

    // State for viewing full-screen photo
    var selectedPhotoCheckpoint by remember { mutableStateOf<Checkpoint?>(null) }

    if (selectedPhotoCheckpoint != null) {
        val cp = selectedPhotoCheckpoint!!
        PhotoDetailViewerDialog(
            checkpoint = cp,
            teamSide = uiState.selectedTeam,
            onDismiss = { selectedPhotoCheckpoint = null },
            onSaveToGallery = {
                val path = cp.photoProofUri ?: ""
                PhotoHelper.savePhotoToSystemGallery(context, path, cp.landmarkName)
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("my_team_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            // Main Team Identity Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = teamContainer),
                border = BorderStroke(1.5.dp, teamColor.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(teamEmoji, fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = "MIEMBRO OFICIAL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = teamTextColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = teamTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = teamTextColor,
                        letterSpacing = (-0.4).sp
                    )
                    Text(
                        text = "Patrulla: ${uiState.patrolName}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BoldThemeTextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Score Card Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("PUNTOS DEL EQUIPO", fontSize = 9.sp, fontWeight = FontWeight.Black, color = teamTextColor)
                                Text("$teamScore", fontSize = 24.sp, fontWeight = FontWeight.Black, color = teamColor)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("VENTAJA / DIFERENCIA", fontSize = 9.sp, fontWeight = FontWeight.Black, color = BoldThemeTextMuted)
                                val diff = teamScore - rivalScore
                                val diffText = if (diff > 0) "+$diff pts 🥇" else if (diff < 0) "$diff pts" else "Empate 🤝"
                                Text(diffText, fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (diff >= 0) EmeraldSuccess else RosePrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Switch Team Button
                    Button(
                        onClick = onChangeTeamClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("change_team_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = teamTextColor
                        ),
                        border = BorderStroke(1.dp, teamColor.copy(alpha = 0.3f))
                    ) {
                        Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CAMBIAR AL OTRO EQUIPO (${if (isAitor) "AMAIA" else "AITOR"})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Section Title: Photo Album of the Team
        val teamPhotos = checkpoints.filter { it.isCompleted && it.completedByTeam == uiState.selectedTeam && !it.photoProofUri.isNullOrBlank() }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = teamColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "ÁLBUM DE FOTOS DEL EQUIPO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        color = BoldThemeTextPrimary
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = teamContainer
                ) {
                    Text(
                        text = "${teamPhotos.size} FOTOS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = teamTextColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        if (teamPhotos.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BoldThemeSurface),
                    border = BorderStroke(1.dp, BoldThemeBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📸", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Sin fotos guardadas todavía",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BoldThemeTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Al validar retos con foto desde el Radar o los detalles del reto, aparecerán aquí para verlas y descargarlas a tu galería.",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = BoldThemeTextMuted
                        )
                    }
                }
            }
        } else {
            // Photo Cards Grid / List
            items(teamPhotos) { checkpoint ->
                TeamPhotoCard(
                    checkpoint = checkpoint,
                    teamColor = teamColor,
                    onPhotoClick = { selectedPhotoCheckpoint = checkpoint },
                    onSaveClick = {
                        val path = checkpoint.photoProofUri ?: ""
                        PhotoHelper.savePhotoToSystemGallery(context, path, checkpoint.landmarkName)
                    }
                )
            }
        }

        // Team Progress Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BoldThemeSurface),
                border = BorderStroke(1.dp, BoldThemeBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PROGRESO EN LA RUTA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = BoldThemeTextPrimary
                        )
                        Text(
                            text = "$completedByThisTeam de ${checkpoints.size} superados",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = teamColor
                        )
                    }

                    val progress = if (checkpoints.isNotEmpty()) {
                        completedByThisTeam.toFloat() / checkpoints.size.toFloat()
                    } else 0f

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = teamColor,
                        trackColor = BoldThemeSurfaceVariant
                    )
                }
            }
        }

        // Section Title: Checkpoints Completed by Team
        item {
            Text(
                text = "HISTORIAL DE RETOS COMPLETADOS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = BoldThemeTextMuted
            )
        }

        val myTeamCheckpoints = checkpoints.filter { it.isCompleted && it.completedByTeam == uiState.selectedTeam }

        if (myTeamCheckpoints.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = BoldThemeSurfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎯", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Aún no habéis completado retos",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BoldThemeTextPrimary
                        )
                        Text(
                            text = "Id a la pestaña 'RADAR' para superar la primera parada en Casa Botines.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = BoldThemeTextMuted
                        )
                    }
                }
            }
        } else {
            items(myTeamCheckpoints) { cp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = BoldThemeSurface),
                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(cp.challengeType.iconEmoji, fontSize = 22.sp)
                            Column {
                                Text(
                                    text = cp.landmarkName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BoldThemeTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = cp.challengeTitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BoldThemeTextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = EmeraldSuccess
                        ) {
                            Text(
                                text = "+${cp.pointsReward} PTS",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun TeamPhotoCard(
    checkpoint: Checkpoint,
    teamColor: Color,
    onPhotoClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("team_photo_card_${checkpoint.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = BoldThemeSurface),
        border = BorderStroke(1.dp, BoldThemeBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Photo Preview Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1B24))
                    .clickable { onPhotoClick() }
            ) {
                val photoPath = checkpoint.photoProofUri ?: ""
                val file = remember(photoPath) { File(photoPath) }

                if (file.exists()) {
                    AsyncImage(
                        model = file,
                        contentDescription = "Foto en ${checkpoint.landmarkName}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback generated preview
                    val placeholder = remember(checkpoint.landmarkName, checkpoint.completedByTeam) {
                        PhotoHelper.createFestivePlaceholderBitmap(
                            landmarkName = checkpoint.landmarkName,
                            teamName = checkpoint.completedByTeam?.displayName ?: "Equipo",
                            teamEmoji = checkpoint.completedByTeam?.emoji ?: "🦁",
                            stickerText = "🏆 Reto Superado"
                        )
                    }
                    Image(
                        bitmap = placeholder.asImageBitmap(),
                        contentDescription = "Foto festiva",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Top left badge
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Text(
                            text = "PARADA ${checkpoint.orderIndex}",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Top right zoom button
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    IconButton(
                        onClick = onPhotoClick,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Ver pantalla completa",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metadata & Download to Gallery action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = checkpoint.landmarkName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = BoldThemeTextPrimary
                    )
                    Text(
                        text = checkpoint.challengeTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = BoldThemeTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onSaveClick,
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("download_photo_button_${checkpoint.id}"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = teamColor),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Guardar",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoDetailViewerDialog(
    checkpoint: Checkpoint,
    teamSide: TeamSide,
    onDismiss: () -> Unit,
    onSaveToGallery: () -> Unit
) {
    val photoPath = checkpoint.photoProofUri ?: ""
    val file = remember(photoPath) { File(photoPath) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 16.dp)
                .testTag("photo_detail_dialog"),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = BoldThemeSurface),
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
                        Text(
                            text = checkpoint.landmarkName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = BoldThemeTextPrimary
                        )
                        Text(
                            text = "Recuerdo Oficial • ${teamSide.displayName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PurplePrimary
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = BoldThemeTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // High-res Image View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (file.exists()) {
                        AsyncImage(
                            model = file,
                            contentDescription = "Foto completa en ${checkpoint.landmarkName}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        val placeholder = remember(checkpoint.landmarkName, checkpoint.completedByTeam) {
                            PhotoHelper.createFestivePlaceholderBitmap(
                                landmarkName = checkpoint.landmarkName,
                                teamName = checkpoint.completedByTeam?.displayName ?: "Equipo",
                                teamEmoji = checkpoint.completedByTeam?.emoji ?: "🦁",
                                stickerText = "🏆 Reto Superado"
                            )
                        }
                        Image(
                            bitmap = placeholder.asImageBitmap(),
                            contentDescription = "Foto festiva",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: Save to Gallery and Close
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
                            .weight(1.5f)
                            .height(46.dp)
                            .testTag("dialog_download_to_gallery_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DESCARGAR A GALERÍA",
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
