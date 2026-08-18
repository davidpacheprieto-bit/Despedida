package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ChallengeType
import com.example.data.model.Checkpoint
import com.example.data.model.TeamSide
import com.example.data.model.TriviaQuestion

@Entity(tableName = "checkpoints")
data class CheckpointEntity(
    @PrimaryKey val id: Int,
    val orderIndex: Int,
    val title: String,
    val landmarkName: String,
    val latitude: Double,
    val longitude: Double,
    val clueDescription: String,
    val challengeTitle: String,
    val challengeDescription: String,
    val challengeType: String,
    val pointsReward: Int,
    val isUnlocked: Boolean,
    val isCompleted: Boolean,
    val completedByTeam: String?,
    val photoProofUri: String?,
    val completedTimestamp: Long?,
    val triviaQuestionText: String?,
    val triviaOptions: String?, // comma separated or pipe separated
    val triviaCorrectIndex: Int?,
    val triviaSpicyFact: String?,
    val secretCode: String?
) {
    fun toDomain(): Checkpoint {
        val trivia = if (triviaQuestionText != null && triviaOptions != null && triviaCorrectIndex != null) {
            TriviaQuestion(
                question = triviaQuestionText,
                options = triviaOptions.split("||"),
                correctIndex = triviaCorrectIndex,
                spicyFunFact = triviaSpicyFact ?: ""
            )
        } else null

        val type = try {
            ChallengeType.valueOf(challengeType)
        } catch (e: Exception) {
            ChallengeType.STREET_CHALLENGE
        }

        val team = completedByTeam?.let {
            try { TeamSide.valueOf(it) } catch (e: Exception) { null }
        }

        return Checkpoint(
            id = id,
            orderIndex = orderIndex,
            title = title,
            landmarkName = landmarkName,
            latitude = latitude,
            longitude = longitude,
            clueDescription = clueDescription,
            challengeTitle = challengeTitle,
            challengeDescription = challengeDescription,
            challengeType = type,
            pointsReward = pointsReward,
            isUnlocked = isUnlocked,
            isCompleted = isCompleted,
            completedByTeam = team,
            photoProofUri = photoProofUri,
            completedTimestamp = completedTimestamp,
            triviaQuestion = trivia,
            secretCode = secretCode
        )
    }

    companion object {
        fun fromDomain(c: Checkpoint): CheckpointEntity {
            return CheckpointEntity(
                id = c.id,
                orderIndex = c.orderIndex,
                title = c.title,
                landmarkName = c.landmarkName,
                latitude = c.latitude,
                longitude = c.longitude,
                clueDescription = c.clueDescription,
                challengeTitle = c.challengeTitle,
                challengeDescription = c.challengeDescription,
                challengeType = c.challengeType.name,
                pointsReward = c.pointsReward,
                isUnlocked = c.isUnlocked,
                isCompleted = c.isCompleted,
                completedByTeam = c.completedByTeam?.name,
                photoProofUri = c.photoProofUri,
                completedTimestamp = c.completedTimestamp,
                triviaQuestionText = c.triviaQuestion?.question,
                triviaOptions = c.triviaQuestion?.options?.joinToString("||"),
                triviaCorrectIndex = c.triviaQuestion?.correctIndex,
                triviaSpicyFact = c.triviaQuestion?.spicyFunFact,
                secretCode = c.secretCode
            )
        }
    }
}
