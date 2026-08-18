package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ChallengeType
import com.example.data.model.Checkpoint
import com.example.data.model.TeamSide
import com.example.ui.components.CameraProofDialog
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
fun ChallengeDetailDialog(
    checkpoint: Checkpoint,
    userTeam: TeamSide,
    onCompleteChallenge: (earnedPoints: Int, photoUri: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showCameraDialog by remember { mutableStateOf(false) }
    var selectedTriviaOption by remember { mutableStateOf<Int?>(null) }
    var triviaAnswered by remember { mutableStateOf(false) }
    var isTriviaCorrect by remember { mutableStateOf(false) }

    if (showCameraDialog) {
        CameraProofDialog(
            checkpoint = checkpoint,
            userTeam = userTeam,
            onPhotoConfirmed = { savedPhotoPath, note, sticker ->
                showCameraDialog = false
                val points = checkpoint.pointsReward + 50
                onCompleteChallenge(points, savedPhotoPath.ifBlank { "saved_proof_${checkpoint.id}" })
            },
            onDismiss = { showCameraDialog = false }
        )
        return
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("challenge_detail_dialog"),
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
                // Top header bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PurpleLightContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(checkpoint.challengeType.iconEmoji, fontSize = 13.sp)
                            Text(
                                text = checkpoint.challengeType.label.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = PurplePrimary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = BoldThemeTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Landmark & Challenge Title
                Text(
                    text = checkpoint.challengeTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    color = BoldThemeTextPrimary,
                    letterSpacing = (-0.4).sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📍 ${checkpoint.landmarkName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RosePrimary
                    )

                    Surface(
                        shape = CircleShape,
                        color = PurpleLightContainer,
                        modifier = Modifier.clickable {
                            try {
                                val uri = Uri.parse("google.navigation:q=${checkpoint.latitude},${checkpoint.longitude}&mode=w")
                                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                } else {
                                    val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${checkpoint.latitude},${checkpoint.longitude}&travelmode=walking")
                                    context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                }
                            } catch (e: Exception) {
                                val fallback = Uri.parse("https://www.google.com/maps/search/?api=1&query=${checkpoint.latitude},${checkpoint.longitude}")
                                context.startActivity(Intent(Intent.ACTION_VIEW, fallback))
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Navigation, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(11.dp))
                            Text("Google Maps", fontSize = 10.sp, fontWeight = FontWeight.Black, color = PurplePrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Points Reward Badge
                Surface(
                    shape = CircleShape,
                    color = PurpleLightContainer,
                    border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "+${checkpoint.pointsReward} PUNTOS EN JUEGO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        color = PurpleDeep,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Challenge Description Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BoldThemeSurfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "MISIÓN DEL RETO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = PurplePrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = checkpoint.challengeDescription,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 17.sp,
                            color = BoldThemeTextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Conditional Challenge Content based on Type
                when (checkpoint.challengeType) {
                    ChallengeType.TRIVIA -> {
                        val trivia = checkpoint.triviaQuestion
                        if (trivia != null) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = trivia.question,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BoldThemeTextPrimary
                                )

                                trivia.options.forEachIndexed { index, option ->
                                    val isSelected = selectedTriviaOption == index
                                    val isCorrect = index == trivia.correctIndex
                                    val btnBg = when {
                                        !triviaAnswered && isSelected -> PurpleLightContainer
                                        triviaAnswered && isCorrect -> EmeraldSuccess.copy(alpha = 0.2f)
                                        triviaAnswered && isSelected && !isCorrect -> RoseLightContainer
                                        else -> BoldThemeSurfaceVariant
                                    }
                                    val borderCol = when {
                                        !triviaAnswered && isSelected -> PurplePrimary
                                        triviaAnswered && isCorrect -> EmeraldSuccess
                                        triviaAnswered && isSelected && !isCorrect -> RosePrimary
                                        else -> Color.Transparent
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = btnBg,
                                        border = BorderStroke(1.5.dp, borderCol),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = !triviaAnswered) {
                                                selectedTriviaOption = index
                                                triviaAnswered = true
                                                isTriviaCorrect = isCorrect
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(
                                                text = "${'A' + index}.",
                                                fontWeight = FontWeight.Black,
                                                color = PurplePrimary,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = option,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected || (triviaAnswered && isCorrect)) FontWeight.Bold else FontWeight.Medium,
                                                color = BoldThemeTextPrimary,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (triviaAnswered) {
                                                if (isCorrect) {
                                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess)
                                                } else if (isSelected) {
                                                    Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = RosePrimary)
                                                }
                                            }
                                        }
                                    }
                                }

                                if (triviaAnswered) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isTriviaCorrect) EmeraldSuccess.copy(alpha = 0.12f) else PurpleLightContainer
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = PurplePrimary)
                                            Text(
                                                text = trivia.spicyFunFact,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = BoldThemeTextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    ChallengeType.SINGING_DANCE -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = PurpleLightContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🎤 HIMNO CAZURRO", fontWeight = FontWeight.Black, letterSpacing = 1.sp, color = PurplePrimary, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "«¡Viva León, viva mi pueblo!\n¡Vivan Aitor y Amaia que hoy se casarán!\nSi te vas al Húmedo no bebas agua,\nque la cecina rica te alegrará...»",
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    textAlign = TextAlign.Center,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 17.sp,
                                    color = BoldThemeTextPrimary
                                )
                            }
                        }
                    }

                    ChallengeType.DRINK_TOAST -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = RoseLightContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("🍻", fontSize = 28.sp)
                                Column {
                                    Text("BRINDIS TRADICIONAL", fontWeight = FontWeight.Black, letterSpacing = 0.8.sp, fontSize = 10.sp, color = RoseDark)
                                    Text(
                                        "«¡Arriba, abajo, al centro y pa' dentro! Por los novios y por León.»",
                                        fontSize = 11.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = BoldThemeTextPrimary
                                    )
                                }
                            }
                        }
                    }

                    else -> {}
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Action Buttons (Bold Rounded-Full)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showCameraDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("open_camera_proof_button"),
                        shape = CircleShape
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Foto (+50p)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val finalScore = if (checkpoint.challengeType == ChallengeType.TRIVIA && !isTriviaCorrect && triviaAnswered) {
                                (checkpoint.pointsReward * 0.7).toInt()
                            } else {
                                checkpoint.pointsReward
                            }
                            onCompleteChallenge(finalScore, null)
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp)
                            .testTag("validate_challenge_completed_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "COMPLETAR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

