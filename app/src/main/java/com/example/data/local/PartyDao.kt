package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyDao {

    @Query("SELECT * FROM checkpoints ORDER BY orderIndex ASC")
    fun getAllCheckpoints(): Flow<List<CheckpointEntity>>

    @Query("SELECT * FROM checkpoints WHERE id = :id")
    suspend fun getCheckpointById(id: Int): CheckpointEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckpoints(checkpoints: List<CheckpointEntity>)

    @Update
    suspend fun updateCheckpoint(checkpoint: CheckpointEntity)

    @Query("SELECT * FROM party_notifications ORDER BY timestamp DESC LIMIT 50")
    fun getAllNotifications(): Flow<List<PartyNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: PartyNotificationEntity)

    @Query("DELETE FROM party_notifications")
    suspend fun clearAllNotifications()

    @Query("SELECT * FROM game_session WHERE id = 1")
    fun getGameSession(): Flow<GameSessionEntity?>

    @Query("SELECT * FROM game_session WHERE id = 1")
    suspend fun getGameSessionOnce(): GameSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGameSession(session: GameSessionEntity)

    @Query("SELECT COUNT(*) FROM checkpoints")
    suspend fun getCheckpointsCount(): Int

    @Query("SELECT COUNT(*) FROM party_notifications")
    suspend fun getNotificationsCount(): Int

    @Query("UPDATE game_session SET userLatitude = :lat, userLongitude = :lng, isGpsSimulationActive = :isSim WHERE id = 1")
    suspend fun updateUserLocationOnly(lat: Double, lng: Double, isSim: Boolean)

    @Query("DELETE FROM checkpoints")
    suspend fun clearCheckpoints()
}
