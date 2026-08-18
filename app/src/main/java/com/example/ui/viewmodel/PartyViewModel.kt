package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.GameSessionEntity
import com.example.data.local.PartyDatabase
import com.example.data.model.Checkpoint
import com.example.data.model.PartyNotification
import com.example.data.model.TeamSide
import com.example.data.repository.PartyRepository
import com.example.service.LocationHelper
import com.example.service.NotificationHelper
import com.example.service.UserCoordinates
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PartyUiState(
    val isInitialized: Boolean = false,
    val selectedTeam: TeamSide = TeamSide.AITOR,
    val patrolName: String = "Los Cazurros de Aitor",
    val aitorScore: Int = 0,
    val amaiaScore: Int = 0,
    val isGameStarted: Boolean = false,
    val isGameFinished: Boolean = false,
    val userCoordinates: UserCoordinates = UserCoordinates(42.5985, -5.5700, isSimulated = true),
    val distanceToActiveMeters: Double = 0.0,
    val bearingToActiveDegrees: Float = 0f,
    val isNearActiveCheckpoint: Boolean = false,
    val activeCheckpoint: Checkpoint? = null,
    val activeChallengeModalCheckpoint: Checkpoint? = null,
    val isShowingConfetti: Boolean = false,
    val latestPushBanner: PartyNotification? = null,
    val viewingPhotoNotification: PartyNotification? = null,
    val viewingPhotoCheckpoint: Checkpoint? = null,
    val showFinaleDialog: Boolean = false
)

class PartyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PartyRepository
    private var bannerDismissJob: Job? = null

    private val _uiState = MutableStateFlow(PartyUiState())
    val uiState: StateFlow<PartyUiState> = _uiState.asStateFlow()

    val checkpoints: StateFlow<List<Checkpoint>>
    val notifications: StateFlow<List<PartyNotification>>

    init {
        val db = PartyDatabase.getDatabase(application)
        repository = PartyRepository(db.partyDao())

        NotificationHelper.createNotificationChannel(application)

        checkpoints = repository.checkpointsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        notifications = repository.notificationsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.initializeDefaultGameDataIfEmpty()
        }

        // Listen for session updates
        viewModelScope.launch {
            repository.gameSessionFlow.collect { session ->
                if (session != null) {
                    val team = try { TeamSide.valueOf(session.selectedTeam) } catch (e: Exception) { TeamSide.AITOR }
                    _uiState.value = _uiState.value.copy(
                        isInitialized = true,
                        selectedTeam = team,
                        patrolName = session.patrolName,
                        aitorScore = session.aitorScore,
                        amaiaScore = session.amaiaScore,
                        isGameStarted = session.isGameStarted,
                        isGameFinished = session.isGameFinished,
                        userCoordinates = UserCoordinates(
                            latitude = session.userLatitude,
                            longitude = session.userLongitude,
                            isSimulated = session.isGpsSimulationActive
                        )
                    )
                    recalculateDistanceAndBearing()
                }
            }
        }

        // Listen for checkpoints changes to determine active target
        viewModelScope.launch {
            checkpoints.collect { list ->
                if (list.isNotEmpty()) {
                    val active = list.firstOrNull { it.isUnlocked && !it.isCompleted } 
                        ?: list.lastOrNull()
                    
                    val allCompleted = list.all { it.isCompleted }
                    _uiState.value = _uiState.value.copy(
                        activeCheckpoint = active,
                        showFinaleDialog = allCompleted || _uiState.value.isGameFinished
                    )
                    recalculateDistanceAndBearing()
                }
            }
        }
    }

    fun selectTeamAndStart(team: TeamSide, patrolName: String) {
        viewModelScope.launch {
            val session = GameSessionEntity(
                id = 1,
                selectedTeam = team.name,
                patrolName = patrolName.ifBlank { if (team == TeamSide.AITOR) "Cazurros de Aitor" else "Reinas de Amaia" },
                aitorScore = _uiState.value.aitorScore,
                amaiaScore = _uiState.value.amaiaScore,
                isGameStarted = true,
                isGameFinished = false,
                userLatitude = _uiState.value.userCoordinates.latitude,
                userLongitude = _uiState.value.userCoordinates.longitude,
                isGpsSimulationActive = _uiState.value.userCoordinates.isSimulated
            )
            repository.updateSession(session)

            triggerPushBanner(
                PartyNotification(
                    teamSide = team,
                    title = "🚀 ¡${team.displayName} entra en acción!",
                    message = "Patrulla '${session.patrolName}' lista para conquistar León.",
                    emoji = team.emoji
                )
            )
        }
    }

    fun openChallengeModal(checkpoint: Checkpoint) {
        _uiState.value = _uiState.value.copy(activeChallengeModalCheckpoint = checkpoint)
    }

    fun closeChallengeModal() {
        _uiState.value = _uiState.value.copy(activeChallengeModalCheckpoint = null)
    }

    fun completeActiveChallenge(
        checkpoint: Checkpoint,
        earnedPoints: Int,
        photoUri: String? = null
    ) {
        viewModelScope.launch {
            val team = _uiState.value.selectedTeam
            repository.completeCheckpoint(
                checkpointId = checkpoint.id,
                teamSide = team,
                earnedPoints = earnedPoints,
                photoProofUri = photoUri
            )

            // Trigger celebratory effects
            vibrateCelebration()
            _uiState.value = _uiState.value.copy(
                isShowingConfetti = true,
                activeChallengeModalCheckpoint = null
            )

            val hasPhoto = !photoUri.isNullOrBlank()
            val photoNote = if (hasPhoto) " 📸 [Foto de prueba subida - Toca para ver]" else ""

            triggerPushBanner(
                PartyNotification(
                    teamSide = team,
                    title = "🎉 ¡Reto superado por ${team.displayName}!",
                    message = "¡+$earnedPoints puntos en ${checkpoint.landmarkName}!$photoNote",
                    pointsDelta = earnedPoints,
                    checkpointName = checkpoint.landmarkName,
                    checkpointId = checkpoint.id,
                    photoProofUri = photoUri,
                    emoji = team.emoji
                )
            )

            // Stop confetti after 4 seconds
            delay(4000)
            _uiState.value = _uiState.value.copy(isShowingConfetti = false)
        }
    }

    fun openPhotoForNotification(notification: PartyNotification) {
        _uiState.value = _uiState.value.copy(
            viewingPhotoNotification = notification,
            viewingPhotoCheckpoint = null
        )
    }

    fun openPhotoForCheckpoint(checkpoint: Checkpoint) {
        _uiState.value = _uiState.value.copy(
            viewingPhotoCheckpoint = checkpoint,
            viewingPhotoNotification = null
        )
    }

    fun closePhotoViewer() {
        _uiState.value = _uiState.value.copy(
            viewingPhotoNotification = null,
            viewingPhotoCheckpoint = null
        )
    }

    fun updateRealGpsLocation(coords: UserCoordinates) {
        _uiState.value = _uiState.value.copy(
            userCoordinates = coords
        )
        recalculateDistanceAndBearing()

        viewModelScope.launch {
            repository.updateUserCoordinates(
                lat = coords.latitude,
                lng = coords.longitude,
                isSim = false
            )
        }
    }

    fun updateUserLocation(lat: Double, lng: Double, isSimulated: Boolean = false) {
        _uiState.value = _uiState.value.copy(
            userCoordinates = UserCoordinates(
                latitude = lat,
                longitude = lng,
                isSimulated = isSimulated,
                hasRealGpsFix = !isSimulated
            )
        )
        recalculateDistanceAndBearing()

        viewModelScope.launch {
            repository.updateUserCoordinates(
                lat = lat,
                lng = lng,
                isSim = isSimulated
            )
        }
    }

    fun teleportToActiveCheckpoint() {
        val target = _uiState.value.activeCheckpoint ?: return
        updateUserLocation(target.latitude, target.longitude, isSimulated = true)
        vibrateGentle()
    }

    private fun recalculateDistanceAndBearing() {
        val active = _uiState.value.activeCheckpoint ?: return
        val user = _uiState.value.userCoordinates

        val distance = LocationHelper.calculateDistanceMeters(
            user.latitude, user.longitude,
            active.latitude, active.longitude
        )
        val bearing = LocationHelper.calculateBearing(
            user.latitude, user.longitude,
            active.latitude, active.longitude
        )

        val isNear = distance <= 45.0 // within 45 meters means arrived at checkpoint

        _uiState.value = _uiState.value.copy(
            distanceToActiveMeters = distance,
            bearingToActiveDegrees = bearing,
            isNearActiveCheckpoint = isNear
        )
    }

    fun triggerPushBanner(notification: PartyNotification) {
        bannerDismissJob?.cancel()
        _uiState.value = _uiState.value.copy(latestPushBanner = notification)

        // Post system notification as well
        NotificationHelper.showPartyNotification(
            getApplication(),
            title = notification.title,
            message = notification.message
        )

        bannerDismissJob = viewModelScope.launch {
            delay(5000)
            _uiState.value = _uiState.value.copy(latestPushBanner = null)
        }
    }

    fun dismissPushBanner() {
        bannerDismissJob?.cancel()
        _uiState.value = _uiState.value.copy(latestPushBanner = null)
    }

    fun resetWholeGame() {
        viewModelScope.launch {
            repository.resetGame(_uiState.value.selectedTeam, _uiState.value.patrolName)
        }
    }

    fun vibrateCelebration() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 250, 100, 300), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 250, 100, 300), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(500)
                }
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    fun vibrateGentle() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(80)
                }
            }
        } catch (e: Exception) {
            // ignore
        }
    }
}
