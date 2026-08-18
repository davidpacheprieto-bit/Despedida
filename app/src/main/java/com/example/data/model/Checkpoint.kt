package com.example.data.model

data class Checkpoint(
    val id: Int,
    val orderIndex: Int,
    val title: String,
    val landmarkName: String,
    val latitude: Double,
    val longitude: Double,
    val clueDescription: String,
    val challengeTitle: String,
    val challengeDescription: String,
    val challengeType: ChallengeType,
    val pointsReward: Int,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false,
    val completedByTeam: TeamSide? = null,
    val photoProofUri: String? = null,
    val completedTimestamp: Long? = null,
    val triviaQuestion: TriviaQuestion? = null,
    val secretCode: String? = null
)
