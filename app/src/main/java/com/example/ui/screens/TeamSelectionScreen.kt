package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TeamSide
import com.example.ui.theme.BoldThemeBackground
import com.example.ui.theme.BoldThemeBorder
import com.example.ui.theme.BoldThemeSurface
import com.example.ui.theme.BoldThemeSurfaceVariant
import com.example.ui.theme.BoldThemeTextMuted
import com.example.ui.theme.BoldThemeTextPrimary
import com.example.ui.theme.PurpleDeep
import com.example.ui.theme.PurpleLightContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleScoreCard
import com.example.ui.theme.RoseDark
import com.example.ui.theme.RoseLightContainer
import com.example.ui.theme.RosePrimary
import com.example.ui.theme.RoseScoreCard

@Composable
fun TeamSelectionScreen(
    onTeamSelected: (team: TeamSide, patrolName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTeam by remember { mutableStateOf(TeamSide.AITOR) }
    var patrolName by remember { mutableStateOf("Los Cazurros de Aitor") }

    val suggestedAitorNames = listOf(
        "Los Cazurros de Aitor",
        "Comando Cecina León",
        "Los Solteros de Oro",
        "Leones del Húmedo"
    )

    val suggestedAmaiaNames = listOf(
        "Las Reinas de Amaia",
        "El Escuadrón Nupcial",
        "Damas del Barrio Húmedo",
        "Team Novia al Poder"
    )

    val currentSuggestions = if (selectedTeam == TeamSide.AITOR) suggestedAitorNames else suggestedAmaiaNames

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BoldThemeBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Date & Location Pill
        Surface(
            shape = CircleShape,
            color = PurpleLightContainer
        ) {
            Text(
                text = "LEÓN, CASTILLA Y LEÓN",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                color = PurplePrimary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "EL DESAFÍO FINAL",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = BoldThemeTextPrimary,
            letterSpacing = (-0.5).sp
        )

        Text(
            text = "Aitor 🦁 & Amaia 👑",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = PurplePrimary
        )

        Text(
            text = "Gymkana interactiva de retos por el Casco Histórico con GPS y puntuación en vivo",
            style = MaterialTheme.typography.bodyMedium,
            color = BoldThemeTextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Team Selection Title
        Text(
            text = "¿DE QUÉ PARTE ESTÁS?",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = BoldThemeTextPrimary,
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Team Aitor Card
            TeamCard(
                team = TeamSide.AITOR,
                isSelected = selectedTeam == TeamSide.AITOR,
                containerColor = PurpleScoreCard,
                textColor = PurpleDeep,
                onSelect = {
                    selectedTeam = TeamSide.AITOR
                    if (patrolName.contains("Amaia") || patrolName.contains("Reinas") || patrolName.contains("Damas")) {
                        patrolName = "Los Cazurros de Aitor"
                    }
                },
                modifier = Modifier.weight(1f).testTag("team_aitor_select_card")
            )

            // Team Amaia Card
            TeamCard(
                team = TeamSide.AMAIA,
                isSelected = selectedTeam == TeamSide.AMAIA,
                containerColor = RoseScoreCard,
                textColor = RoseDark,
                onSelect = {
                    selectedTeam = TeamSide.AMAIA
                    if (patrolName.contains("Aitor") || patrolName.contains("Cazurros") || patrolName.contains("Solteros")) {
                        patrolName = "Las Reinas de Amaia"
                    }
                },
                modifier = Modifier.weight(1f).testTag("team_amaia_select_card")
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Patrol Name Input & Quick Suggestions
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = BoldThemeSurface
            ),
            border = BorderStroke(1.dp, BoldThemeBorder.copy(alpha = 0.6f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "NOMBRE DE LA PATRULLA",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = BoldThemeTextMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = patrolName,
                    onValueChange = { patrolName = it },
                    placeholder = { Text("Ej: Los Cazurros del Novio") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = PurplePrimary)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("patrol_name_input"),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "IDEAS RÁPIDAS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp,
                    color = BoldThemeTextMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(currentSuggestions) { suggestion ->
                        Surface(
                            shape = CircleShape,
                            color = if (patrolName == suggestion) PurplePrimary else BoldThemeSurfaceVariant,
                            modifier = Modifier.clickable { patrolName = suggestion }
                        ) {
                            Text(
                                text = suggestion,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (patrolName == suggestion) Color.White else BoldThemeTextPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Rules & How It Works Briefing
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = BoldThemeSurfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "REGLAS DE LA BATALLA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = BoldThemeTextPrimary
                )

                RuleItem(
                    icon = "📍",
                    title = "Ruta GPS secuencial",
                    desc = "Puntos clave de León (Botines, Catedral, Húmedo...). Cada reto desbloquea el siguiente."
                )
                RuleItem(
                    icon = "🎯",
                    title = "Retos y Pruebas Reales",
                    desc = "Trivia salseante de Aitor y Amaia, fotos de grupo con stickers y retos con leoneses."
                )
                RuleItem(
                    icon = "⚡",
                    title = "Marcador en Vivo & Push",
                    desc = "Notificaciones instantáneas de los progresos del bando contrincante."
                )
                RuleItem(
                    icon = "🍽️",
                    title = "Gran Meta en el Restaurante",
                    desc = "Clasificación final y entrega de trofeo en el banquete."
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Start Game Button (Bold Pill Button)
        Button(
            onClick = {
                onTeamSelected(selectedTeam, patrolName.ifBlank { selectedTeam.displayName })
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("start_gymkana_button"),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = PurplePrimary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(imageVector = Icons.Default.Celebration, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "¡ENTRAR AL ${selectedTeam.displayName.uppercase()}!",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TeamCard(
    team: TeamSide,
    isSelected: Boolean,
    containerColor: Color,
    textColor: Color,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderWidth by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        label = "border_w"
    )

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onSelect() }
            .border(borderWidth, textColor, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(team.emoji, fontSize = 32.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = team.displayName.uppercase(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                color = textColor
            )

            Text(
                text = team.captainName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isSelected) {
                Surface(
                    shape = CircleShape,
                    color = textColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Text("SELECCIONADO", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleItem(
    icon: String,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = icon, fontSize = 18.sp)
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BoldThemeTextPrimary
            )
            Text(
                text = desc,
                fontSize = 11.sp,
                color = BoldThemeTextMuted
            )
        }
    }
}

