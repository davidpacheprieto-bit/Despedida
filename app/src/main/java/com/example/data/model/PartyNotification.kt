package com.example.data.model

data class PartyNotification(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val teamSide: TeamSide,
    val title: String,
    val message: String,
    val pointsDelta: Int = 0,
    val checkpointName: String? = null,
    val checkpointId: Int? = null,
    val photoProofUri: String? = null,
    val isRivalAlert: Boolean = false,
    val emoji: String = "🎉"
)
