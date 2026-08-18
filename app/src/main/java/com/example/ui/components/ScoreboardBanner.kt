package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TeamSide
import com.example.ui.theme.PurpleDeep
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleScoreCard
import com.example.ui.theme.RoseDark
import com.example.ui.theme.RoseScoreCard

@Composable
fun ScoreboardBanner(
    aitorScore: Int,
    amaiaScore: Int,
    userTeam: TeamSide,
    patrolName: String,
    modifier: Modifier = Modifier
) {
    val total = (aitorScore + amaiaScore).coerceAtLeast(1)
    val aitorRatio = (aitorScore.toFloat() / total.toFloat()).coerceIn(0.10f, 0.90f)
    val amaiaRatio = (amaiaScore.toFloat() / total.toFloat()).coerceIn(0.10f, 0.90f)

    val animatedAitorRatio by animateFloatAsState(targetValue = aitorRatio, label = "aitor_ratio")
    val animatedAmaiaRatio by animateFloatAsState(targetValue = amaiaRatio, label = "amaia_ratio")

    val scoreDiff = aitorScore - amaiaScore
    val leaderText = when {
        scoreDiff > 0 -> "🦁 Team Aitor lidera (+${scoreDiff} pts)"
        scoreDiff < 0 -> "👑 Team Amaia lidera (+${-scoreDiff} pts)"
        else -> "⚖️ ¡Empate a $aitorScore puntos!"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("scoreboard_banner")
    ) {
        // Dual Grid Cards from Bold Typography Design
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Team Aitor Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("score_card_aitor"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PurpleScoreCard
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AITOR SCORE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = PurpleDeep.copy(alpha = 0.7f)
                        )
                        Text("🦁", fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "%,d".format(aitorScore),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = PurpleDeep,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(PurpleDeep.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedAitorRatio)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(PurpleDeep)
                        )
                    }
                }
            }

            // Team Amaia Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("score_card_amaia"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = RoseScoreCard
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AMAIA SCORE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = RoseDark.copy(alpha = 0.7f)
                        )
                        Text("👑", fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "%,d".format(amaiaScore),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = RoseDark,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(RoseDark.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedAmaiaRatio)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(RoseDark)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Patrol & Leader status line
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📍 $patrolName (${userTeam.displayName})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = leaderText,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = userTeam.primaryColor
            )
        }
    }
}

