package com.example.data.repository

import com.example.data.local.CheckpointEntity
import com.example.data.local.GameSessionEntity
import com.example.data.local.PartyDao
import com.example.data.local.PartyNotificationEntity
import com.example.data.model.ChallengeType
import com.example.data.model.Checkpoint
import com.example.data.model.PartyNotification
import com.example.data.model.TeamSide
import com.example.data.model.TriviaQuestion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PartyRepository(private val dao: PartyDao) {

    val checkpointsFlow: Flow<List<Checkpoint>> = dao.getAllCheckpoints().map { list ->
        list.map { it.toDomain() }
    }

    val notificationsFlow: Flow<List<PartyNotification>> = dao.getAllNotifications().map { list ->
        list.map { it.toDomain() }
    }

    val gameSessionFlow: Flow<GameSessionEntity?> = dao.getGameSession()

    suspend fun initializeDefaultGameDataIfEmpty() {
        val session = dao.getGameSessionOnce()
        if (session == null) {
            dao.saveGameSession(
                GameSessionEntity(
                    id = 1,
                    selectedTeam = "AITOR",
                    patrolName = "Comando Cazurro",
                    aitorScore = 0,
                    amaiaScore = 0,
                    isGameStarted = false,
                    isGameFinished = false,
                    userLatitude = 42.5985,
                    userLongitude = -5.5700,
                    isGpsSimulationActive = true
                )
            )
        }

        // Initialize default route checkpoints ONLY if table is empty
        if (dao.getCheckpointsCount() == 0) {
            val defaultCheckpoints = getDefaultLeonRoute()
            dao.insertCheckpoints(defaultCheckpoints.map { CheckpointEntity.fromDomain(it) })
        }

        // Initial welcome push notifications ONLY if empty
        if (dao.getNotificationsCount() == 0) {
            dao.insertNotification(
                PartyNotificationEntity.fromDomain(
                    PartyNotification(
                        teamSide = TeamSide.AITOR,
                        title = "🦁 ¡Comienza la Gymkana de León!",
                        message = "Aitor y sus cazurros listos para recorrer las calles históricas.",
                        pointsDelta = 0,
                        emoji = "🦁"
                    )
                )
            )
            dao.insertNotification(
                PartyNotificationEntity.fromDomain(
                    PartyNotification(
                        teamSide = TeamSide.AMAIA,
                        title = "👑 ¡Team Amaia a la carga!",
                        message = "Las reinas de la novia van a por todas en el Barrio Húmedo.",
                        pointsDelta = 0,
                        emoji = "👑"
                    )
                )
            )
        }
    }

    suspend fun updateSession(session: GameSessionEntity) {
        dao.saveGameSession(session)
    }

    suspend fun updateUserCoordinates(lat: Double, lng: Double, isSim: Boolean) {
        dao.updateUserLocationOnly(lat, lng, isSim)
    }

    suspend fun completeCheckpoint(
        checkpointId: Int,
        teamSide: TeamSide,
        earnedPoints: Int,
        photoProofUri: String? = null
    ) {
        val current = dao.getCheckpointById(checkpointId) ?: return
        val updated = current.copy(
            isCompleted = true,
            completedByTeam = teamSide.name,
            completedTimestamp = System.currentTimeMillis(),
            photoProofUri = photoProofUri ?: current.photoProofUri
        )
        dao.updateCheckpoint(updated)

        // Unlock next sequential checkpoint
        val nextId = checkpointId + 1
        val nextCheckpoint = dao.getCheckpointById(nextId)
        if (nextCheckpoint != null) {
            dao.updateCheckpoint(nextCheckpoint.copy(isUnlocked = true))
        }

        // Update team score
        val currentSession = dao.getGameSessionOnce() ?: GameSessionEntity()
        val newAitorScore = if (teamSide == TeamSide.AITOR) currentSession.aitorScore + earnedPoints else currentSession.aitorScore
        val newAmaiaScore = if (teamSide == TeamSide.AMAIA) currentSession.amaiaScore + earnedPoints else currentSession.amaiaScore
        
        val isFinal = checkpointId >= 8
        dao.saveGameSession(
            currentSession.copy(
                aitorScore = newAitorScore,
                amaiaScore = newAmaiaScore,
                isGameFinished = isFinal || currentSession.isGameFinished
            )
        )

        // Broadcast celebration notification / Rivalry alert
        val notificationTitle = if (teamSide == TeamSide.AITOR) {
            "⚡ ¡Team Aitor superó ${current.landmarkName}!"
        } else {
            "✨ ¡Team Amaia superó ${current.landmarkName}!"
        }

        val hasPhoto = !photoProofUri.isNullOrBlank() || !current.photoProofUri.isNullOrBlank()
        val effectivePhotoUri = photoProofUri ?: current.photoProofUri
        val photoText = if (hasPhoto) " 📸 [Foto de prueba subida - Toca para ver]" else ""

        dao.insertNotification(
            PartyNotificationEntity.fromDomain(
                PartyNotification(
                    teamSide = teamSide,
                    title = notificationTitle,
                    message = "Sumaron +$earnedPoints pts con el reto '${current.challengeTitle}'.$photoText",
                    pointsDelta = earnedPoints,
                    checkpointName = current.landmarkName,
                    checkpointId = checkpointId,
                    photoProofUri = effectivePhotoUri,
                    isRivalAlert = true,
                    emoji = if (teamSide == TeamSide.AITOR) "🦁" else "👑"
                )
            )
        )
    }

    suspend fun addNotification(notification: PartyNotification) {
        dao.insertNotification(PartyNotificationEntity.fromDomain(notification))
    }

    suspend fun resetGame(selectedTeam: TeamSide, patrolName: String) {
        dao.clearCheckpoints()
        dao.clearAllNotifications()
        dao.saveGameSession(
            GameSessionEntity(
                id = 1,
                selectedTeam = selectedTeam.name,
                patrolName = patrolName,
                aitorScore = 0,
                amaiaScore = 0,
                isGameStarted = true,
                isGameFinished = false,
                userLatitude = 42.5985,
                userLongitude = -5.5700,
                isGpsSimulationActive = true
            )
        )
        val defaultCheckpoints = getDefaultLeonRoute()
        dao.insertCheckpoints(defaultCheckpoints.map { CheckpointEntity.fromDomain(it) })
    }

    companion object {
        fun getDefaultLeonRoute(): List<Checkpoint> = listOf(
            Checkpoint(
                id = 1,
                orderIndex = 1,
                title = "Parada 1: Casa Botines & Gaudí",
                landmarkName = "Casa Botines (Pl. San Marcelo)",
                latitude = 42.5985,
                longitude = -5.5700,
                clueDescription = "Buscad el emblemático edificio modernista de Antoni Gaudí y su estatua de bronce sentada.",
                challengeTitle = "Selfie Modelo con Gaudí & Piropo Solemne",
                challengeDescription = "Sentarse junto a la estatua de Gaudí en el banco, posar todo el equipo en plan 'top model' para un selfie de grupo y que el capitán grite un piropo épico a los novios.",
                challengeType = ChallengeType.PHOTO_MISSION,
                pointsReward = 150,
                isUnlocked = true,
                isCompleted = false
            ),
            Checkpoint(
                id = 2,
                orderIndex = 2,
                title = "Parada 2: Bar El Rebote (Barrio Húmedo)",
                landmarkName = "Bar El Rebote (Pl. San Martín)",
                latitude = 42.5966,
                longitude = -5.5684,
                clueDescription = "El templo de las croquetas variadas de cecina, morcilla y queso en el corazón del Húmedo.",
                challengeTitle = "El Baile de la Croqueta Caliente",
                challengeDescription = "En la terraza o entrada del bar, todo el equipo debe hacer una coreografía de 10 segundos ('el meneíto de la croqueta') antes de brindar con su corto o caña.",
                challengeType = ChallengeType.SINGING_DANCE,
                pointsReward = 200,
                isUnlocked = false,
                isCompleted = false
            ),
            Checkpoint(
                id = 3,
                orderIndex = 3,
                title = "Parada 3: Catedral de León (Pulchra)",
                landmarkName = "Catedral de León (Plaza de Regla)",
                latitude = 42.5994,
                longitude = -5.5668,
                clueDescription = "Frente a las vidrieras góticas más espectaculares de España en la Plaza de Regla.",
                challengeTitle = "Selfie con Transeúnte: Consejo Matrimonial",
                challengeDescription = "Parad a una persona o pareja que pase por la plaza, pedidle un consejo de oro para el matrimonio de Aitor y Amaia y sacad un selfie grupal con el pulgar arriba diciendo: '¡Amén y viva León!'.",
                challengeType = ChallengeType.STREET_CHALLENGE,
                pointsReward = 250,
                isUnlocked = false,
                isCompleted = false
            ),
            Checkpoint(
                id = 4,
                orderIndex = 4,
                title = "Parada 4: Taberna Los Cazurros & Pl. del Grano",
                landmarkName = "Taberna Los Cazurros (Pl. del Grano)",
                latitude = 42.5955,
                longitude = -5.5678,
                clueDescription = "La plaza empedrada más bonita y tradicional de León y la mítica taberna cazurra.",
                challengeTitle = "Trivia Secreta & Juramento Cazurro",
                challengeDescription = "Resolved el acertijo sobre la historia de amor de Aitor y Amaia y recitad en corro el juramento de la boda leonesa.",
                challengeType = ChallengeType.TRIVIA,
                pointsReward = 200,
                isUnlocked = false,
                isCompleted = false,
                triviaQuestion = TriviaQuestion(
                    question = "¿Quién es más probable que pierda las llaves o el móvil en plena noche de fiesta?",
                    options = listOf(
                        "Aitor, pero culpará a los bolsillos del pantalón",
                        "Amaia, aunque jura que lo tenía 'en la mano hace un segundo'",
                        "Los dos por igual, tienen un radar para olvidar cosas",
                        "Ninguno, son organizados hasta con tres cañas encima"
                    ),
                    correctIndex = 0,
                    spicyFunFact = "¡Aitor siempre revisa 6 veces sus bolsillos antes de salir de cualquier bar!"
                )
            ),
            Checkpoint(
                id = 5,
                orderIndex = 5,
                title = "Parada 5: Barrio Romántico & Parque del Cid",
                landmarkName = "Parque del Cid & Taberna del Sil",
                latitude = 42.5990,
                longitude = -5.5715,
                clueDescription = "La zona de pinchos y vinos del Barrio Romántico, entre jardines y ambiente distendido.",
                challengeTitle = "El Trenecito Humano Nupcial",
                challengeDescription = "Haced un trenecito humano recorriendo 10 metros entre los árboles o mesas cantando el estribillo '¡Chucu chucu chucu, que se nos casan!' agitando una servilleta como bandera.",
                challengeType = ChallengeType.SINGING_DANCE,
                pointsReward = 250,
                isUnlocked = false,
                isCompleted = false
            ),
            Checkpoint(
                id = 6,
                orderIndex = 6,
                title = "Parada 6: Basílica de San Isidoro",
                landmarkName = "Basílica de San Isidoro (Panteón Real)",
                latitude = 42.6006,
                longitude = -5.5708,
                clueDescription = "Cuna del parlamentarismo medieval y joya del arte románico europeo.",
                challengeTitle = "Foto de Reverencia Real al Novio/Novia",
                challengeDescription = "Todo el equipo se arrodilla en solemne reverencia mientras el portavoz 'corona' al novio o a la novia con un sombrero o vaso de fiesta. ¡Pedid a un peatón que haga la foto!",
                challengeType = ChallengeType.PHOTO_MISSION,
                pointsReward = 250,
                isUnlocked = false,
                isCompleted = false
            ),
            Checkpoint(
                id = 7,
                orderIndex = 7,
                title = "Parada 7: Plaza Mayor & Bar El Flechazo",
                landmarkName = "Plaza Mayor & El Flechazo",
                latitude = 42.5972,
                longitude = -5.5670,
                clueDescription = "Bajo los soportales barrocos de la Plaza Mayor y sus famosas patatas picantes.",
                challengeTitle = "Brindis con Desconocidos & Canto Cazurro",
                challengeDescription = "Encontrad a otra mesa o grupo en la Plaza Mayor, levantad los vasos y brindad juntos diciendo: '¡Por Aitor y Amaia y que nunca falte la fiesta!', cantando una estrofa a coro.",
                challengeType = ChallengeType.DRINK_TOAST,
                pointsReward = 300,
                isUnlocked = false,
                isCompleted = false
            ),
            Checkpoint(
                id = 8,
                orderIndex = 8,
                title = "Parada 8: Marcela Brasa y Vinos (Comida Final)",
                landmarkName = "Marcela Brasa y Vinos",
                latitude = 42.5979,
                longitude = -5.5708,
                clueDescription = "Pl. de S. Marcelo, 9, 24003 León. ¡El banquete de celebración final donde se decide el equipo ganador!",
                challengeTitle = "Meta Triunfal & Banquete en Marcela Brasa y Vinos",
                challengeDescription = "¡Llegada al restaurante de la comida! Foto de todo el equipo en la puerta de Marcela Brasa y Vinos, fichad la llegada y coronad al bando campeón.",
                challengeType = ChallengeType.RESTAURANT_FINALE,
                pointsReward = 500,
                isUnlocked = false,
                isCompleted = false
            )
        )
    }
}
