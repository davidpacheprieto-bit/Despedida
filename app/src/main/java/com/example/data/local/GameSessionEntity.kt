package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_session")
data class GameSessionEntity(
    @PrimaryKey val id: Int = 1,
    val selectedTeam: String = "AITOR", // AITOR or AMAIA
    val patrolName: String = "Los Cazurros de León",
    val aitorScore: Int = 0,
    val amaiaScore: Int = 0,
    val isGameStarted: Boolean = false,
    val isGameFinished: Boolean = false,
    val userLatitude: Double = 42.5985,
    val userLongitude: Double = -5.5700,
    val isGpsSimulationActive: Boolean = false
)
