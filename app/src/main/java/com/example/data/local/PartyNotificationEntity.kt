package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.PartyNotification
import com.example.data.model.TeamSide

@Entity(tableName = "party_notifications")
data class PartyNotificationEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val teamSide: String,
    val title: String,
    val message: String,
    val pointsDelta: Int,
    val checkpointName: String?,
    val checkpointId: Int? = null,
    val photoProofUri: String? = null,
    val isRivalAlert: Boolean,
    val emoji: String
) {
    fun toDomain(): PartyNotification {
        val team = try {
            TeamSide.valueOf(teamSide)
        } catch (e: Exception) {
            TeamSide.AITOR
        }
        return PartyNotification(
            id = id,
            timestamp = timestamp,
            teamSide = team,
            title = title,
            message = message,
            pointsDelta = pointsDelta,
            checkpointName = checkpointName,
            checkpointId = checkpointId,
            photoProofUri = photoProofUri,
            isRivalAlert = isRivalAlert,
            emoji = emoji
        )
    }

    companion object {
        fun fromDomain(n: PartyNotification): PartyNotificationEntity {
            return PartyNotificationEntity(
                id = n.id,
                timestamp = n.timestamp,
                teamSide = n.teamSide.name,
                title = n.title,
                message = n.message,
                pointsDelta = n.pointsDelta,
                checkpointName = n.checkpointName,
                checkpointId = n.checkpointId,
                photoProofUri = n.photoProofUri,
                isRivalAlert = n.isRivalAlert,
                emoji = n.emoji
            )
        }
    }
}

