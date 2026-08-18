package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.TeamAitorPrimary
import com.example.ui.theme.TeamAitorSecondary
import com.example.ui.theme.TeamAmaiaPrimary
import com.example.ui.theme.TeamAmaiaSecondary

enum class TeamSide(
    val displayName: String,
    val captainName: String,
    val teamMotto: String,
    val emoji: String,
    val primaryColor: Color,
    val secondaryColor: Color
) {
    AITOR(
        displayName = "Team Aitor",
        captainName = "Aitor (El Novio)",
        teamMotto = "¡Por el novio y por la fiesta en León!",
        emoji = "🦁",
        primaryColor = TeamAitorPrimary,
        secondaryColor = TeamAitorSecondary
    ),
    AMAIA(
        displayName = "Team Amaia",
        captainName = "Amaia (La Novia)",
        teamMotto = "¡La novia manda y nos llevamos el trofeo!",
        emoji = "👑",
        primaryColor = TeamAmaiaPrimary,
        secondaryColor = TeamAmaiaSecondary
    )
}
