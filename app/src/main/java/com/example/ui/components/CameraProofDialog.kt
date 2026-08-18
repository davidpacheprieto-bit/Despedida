package com.example.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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

@Composable
fun CameraProofDialog(
    checkpoint: Checkpoint,
    userTeam: TeamSide,
    onPhotoConfirmed: (savedPhotoPath: String, photoNote: String, selectedSticker: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isFestivePlaceholder by remember { mutableStateOf(false) }

    var selectedSticker by remember {
        mutableStateOf(
            if (userTeam == TeamSide.AITOR) "🦁 ¡Team Aitor lo borda!" else "👑 ¡Reinas del Húmedo!"
        )
    }
    var customCaption by remember { mutableStateOf("") }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            selectedImageUri = null
            isFestivePlaceholder = false
        }
    }

    // Gallery Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            capturedBitmap = null
            isFestivePlaceholder = false
        }
    }

    val hasPhoto = capturedBitmap != null || selectedImageUri != null || isFestivePlaceholder

    val stickers = listOf(
        if (userTeam == TeamSide.AITOR) "🦁 ¡Team Aitor lo borda!" else "👑 ¡Reinas del Húmedo!",
        "💍 ¡Vivan Aitor y Amaia!",
        "🍻 ¡Tapa Cazurra en León!",
        "🏰 Muralla Conquistada",
        "📸 Gaudí Approved",
        "🏆 ¡Reto Superado +50P!"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp)
                .testTag("camera_proof_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = BoldThemeSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
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
                            text = "FOTO PRUEBA DE EQUIPO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = BoldThemeTextPrimary
                        )
                        Text(
                            text = "📍 ${checkpoint.landmarkName}",
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

                // Viewfinder / Polaroid Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (hasPhoto) Color.Black else Color(0xFF1E1B24))
                        .border(1.5.dp, BoldThemeBorder, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        capturedBitmap != null -> {
                            Image(
                                bitmap = capturedBitmap!!.asImageBitmap(),
                                contentDescription = "Foto capturada",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        selectedImageUri != null -> {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Foto de galería",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        isFestivePlaceholder -> {
                            val placeholder = remember(checkpoint.landmarkName, userTeam, selectedSticker) {
                                PhotoHelper.createFestivePlaceholderBitmap(
                                    landmarkName = checkpoint.landmarkName,
                                    teamName = userTeam.displayName,
                                    teamEmoji = userTeam.emoji,
                                    stickerText = selectedSticker
                                )
                            }
                            Image(
                                bitmap = placeholder.asImageBitmap(),
                                contentDescription = "Foto festiva de prueba",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = PurpleLightContainer,
                                    modifier = Modifier.size(54.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = PurplePrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Captura el momento con tu grupo",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Frente a ${checkpoint.landmarkName} con todo el equipo",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Floating sticker overlay on top-left if photo present
                    if (hasPhoto && !isFestivePlaceholder) {
                        Surface(
                            shape = CircleShape,
                            color = PurplePrimary,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = selectedSticker,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons to take photo / pick from gallery / instant test
                if (!hasPhoto) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { cameraLauncher.launch(null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("snap_camera_button"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ABRIR CÁMARA DE FOTOS", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("pick_gallery_button"),
                                shape = CircleShape
                            ) {
                                Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Galería", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    isFestivePlaceholder = true
                                    capturedBitmap = null
                                    selectedImageUri = null
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("instant_photo_button"),
                                shape = CircleShape
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Foto Rápida", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Photo customization: Sticker picker & caption
                    Text(
                        text = "ELIGE TU STICKER DE EQUIPO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        color = BoldThemeTextMuted,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(stickers) { sticker ->
                            val isSelected = selectedSticker == sticker
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) PurplePrimary else BoldThemeSurfaceVariant,
                                modifier = Modifier.clickable { selectedSticker = sticker }
                            ) {
                                Text(
                                    text = sticker,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                    color = if (isSelected) Color.White else BoldThemeTextPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customCaption,
                        onValueChange = { customCaption = it },
                        placeholder = { Text("Añade un mensaje o anécdota (ej: ¡Ronda en El Rebote!)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                capturedBitmap = null
                                selectedImageUri = null
                                isFestivePlaceholder = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = BoldThemeSurfaceVariant)
                        ) {
                            Text("Cambiar", fontSize = 11.sp, color = BoldThemeTextPrimary, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val savedPath: String = when {
                                    capturedBitmap != null -> {
                                        PhotoHelper.saveBitmapToInternalStorage(
                                            context = context,
                                            bitmap = capturedBitmap!!,
                                            stickerText = selectedSticker,
                                            captionText = customCaption.ifBlank { null },
                                            checkpointId = checkpoint.id
                                        )
                                    }
                                    selectedImageUri != null -> {
                                        PhotoHelper.saveUriToInternalStorage(
                                            context = context,
                                            uri = selectedImageUri!!,
                                            stickerText = selectedSticker,
                                            captionText = customCaption.ifBlank { null },
                                            checkpointId = checkpoint.id
                                        ) ?: ""
                                    }
                                    else -> {
                                        val placeholder = PhotoHelper.createFestivePlaceholderBitmap(
                                            landmarkName = checkpoint.landmarkName,
                                            teamName = userTeam.displayName,
                                            teamEmoji = userTeam.emoji,
                                            stickerText = selectedSticker
                                        )
                                        PhotoHelper.saveBitmapToInternalStorage(
                                            context = context,
                                            bitmap = placeholder,
                                            stickerText = null,
                                            captionText = customCaption.ifBlank { null },
                                            checkpointId = checkpoint.id
                                        )
                                    }
                                }

                                onPhotoConfirmed(savedPath, customCaption, selectedSticker)
                            },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(46.dp)
                                .testTag("confirm_photo_proof_button"),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ENVIAR FOTO (+50P)", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                        }
                    }
                }
            }
        }
    }
}
