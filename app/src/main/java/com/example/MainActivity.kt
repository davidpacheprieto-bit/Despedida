package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Checkpoint
import com.example.data.model.TeamSide
import com.example.service.LocationHelper
import com.example.service.LocationTrackerHandle
import com.example.service.PhotoHelper
import com.example.ui.components.ConfettiEffect
import com.example.ui.components.HeadsUpNotificationBanner
import com.example.ui.components.LeonGpsMapRadar
import com.example.ui.components.PhotoPreviewDialog
import com.example.ui.components.ScoreboardBanner
import com.example.ui.screens.ChallengeDetailDialog
import com.example.ui.screens.LiveFeedScreen
import com.example.ui.screens.MyTeamScreen
import com.example.ui.screens.TeamSelectionScreen
import com.example.ui.theme.BoldThemeBackground
import com.example.ui.theme.BoldThemeBorder
import com.example.ui.theme.BoldThemeSurface
import com.example.ui.theme.BoldThemeSurfaceVariant
import com.example.ui.theme.BoldThemeTextMuted
import com.example.ui.theme.BoldThemeTextPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PurpleDeep
import com.example.ui.theme.PurpleLightContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.RoseDark
import com.example.ui.theme.RoseLightContainer
import com.example.ui.theme.RosePrimary
import com.example.ui.viewmodel.PartyUiState
import com.example.ui.viewmodel.PartyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PartyAppRoot()
            }
        }
    }
}

@Composable
fun PartyAppRoot(partyViewModel: PartyViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by partyViewModel.uiState.collectAsState()
    val checkpoints by partyViewModel.checkpoints.collectAsState()
    val notifications by partyViewModel.notifications.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }

    // GPS runtime permission launcher
    var trackerHandle by remember { mutableStateOf<LocationTrackerHandle?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            trackerHandle?.stop()
            trackerHandle = LocationHelper.startRealtimeLocationUpdates(context) { coords ->
                partyViewModel.updateRealGpsLocation(coords)
            }
        }
    }

    // Auto-request location permissions on launch
    LaunchedEffect(Unit) {
        if (!LocationHelper.hasLocationPermission(context)) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Continuous Real GPS Tracking
    DisposableEffect(context) {
        if (LocationHelper.hasLocationPermission(context)) {
            trackerHandle = LocationHelper.startRealtimeLocationUpdates(context) { coords ->
                partyViewModel.updateRealGpsLocation(coords)
            }
        }
        onDispose {
            trackerHandle?.stop()
            trackerHandle = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BoldThemeBackground)
    ) {
        if (!uiState.isInitialized) {
            // Smooth loading screen while Room database loads saved session and checkpoints
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = PurplePrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
        } else if (!uiState.isGameStarted) {
            TeamSelectionScreen(
                onTeamSelected = { team, patrolName ->
                    partyViewModel.selectTeamAndStart(team, patrolName)
                }
            )
        } else {
            MainGameScreen(
                uiState = uiState,
                checkpoints = checkpoints,
                notifications = notifications,
                onCheckpointClicked = { partyViewModel.openChallengeModal(it) },
                onNotificationClicked = { notif ->
                    if (!notif.photoProofUri.isNullOrBlank()) {
                        partyViewModel.openPhotoForNotification(notif)
                    }
                },
                onTeleportToTarget = { partyViewModel.teleportToActiveCheckpoint() },
                onResetClicked = { showResetDialog = true },
                onSwitchTeam = { newTeam, newPatrol ->
                    partyViewModel.selectTeamAndStart(newTeam, newPatrol)
                }
            )
        }

        // Active Challenge Modal
        val activeModalCheckpoint = uiState.activeChallengeModalCheckpoint
        if (activeModalCheckpoint != null) {
            ChallengeDetailDialog(
                checkpoint = activeModalCheckpoint,
                userTeam = uiState.selectedTeam,
                onCompleteChallenge = { earnedPoints, photoUri ->
                    partyViewModel.completeActiveChallenge(activeModalCheckpoint, earnedPoints, photoUri)
                },
                onDismiss = { partyViewModel.closeChallengeModal() }
            )
        }

        // Floating Heads-Up Banner for Live Push Notifications
        val pushBanner = uiState.latestPushBanner
        AnimatedVisibility(
            visible = pushBanner != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 8.dp)
        ) {
            if (pushBanner != null) {
                HeadsUpNotificationBanner(
                    notification = pushBanner,
                    onDismiss = { partyViewModel.dismissPushBanner() },
                    onClick = { notif ->
                        partyViewModel.dismissPushBanner()
                        if (!notif.photoProofUri.isNullOrBlank()) {
                            partyViewModel.openPhotoForNotification(notif)
                        }
                    }
                )
            }
        }

        // Dedicated Fullscreen Photo Viewer Dialog for Notifications or Checkpoints
        val notifPhoto = uiState.viewingPhotoNotification
        if (notifPhoto != null) {
            PhotoPreviewDialog(
                title = notifPhoto.checkpointName ?: notifPhoto.title,
                subtitle = notifPhoto.message,
                photoPathOrUri = notifPhoto.photoProofUri,
                teamSide = notifPhoto.teamSide,
                onDismiss = { partyViewModel.closePhotoViewer() },
                onSaveToGallery = {
                    val path = notifPhoto.photoProofUri ?: ""
                    PhotoHelper.savePhotoToSystemGallery(
                        context,
                        path,
                        notifPhoto.checkpointName ?: "Reto_Leon"
                    )
                }
            )
        }

        val cpPhoto = uiState.viewingPhotoCheckpoint
        if (cpPhoto != null) {
            PhotoPreviewDialog(
                title = cpPhoto.landmarkName,
                subtitle = "Reto: ${cpPhoto.challengeTitle}",
                photoPathOrUri = cpPhoto.photoProofUri,
                teamSide = cpPhoto.completedByTeam ?: uiState.selectedTeam,
                onDismiss = { partyViewModel.closePhotoViewer() },
                onSaveToGallery = {
                    val path = cpPhoto.photoProofUri ?: ""
                    PhotoHelper.savePhotoToSystemGallery(
                        context,
                        path,
                        cpPhoto.landmarkName
                    )
                }
            )
        }

        // Confetti celebration overlay
        ConfettiEffect(
            isVisible = uiState.isShowingConfetti,
            modifier = Modifier.fillMaxSize()
        )

        // Reset Game Confirmation Dialog
        if (showResetDialog) {
            ResetConfirmDialog(
                onConfirm = {
                    showResetDialog = false
                    partyViewModel.resetWholeGame()
                },
                onDismiss = { showResetDialog = false }
            )
        }
    }
}

@Composable
fun MainGameScreen(
    uiState: PartyUiState,
    checkpoints: List<Checkpoint>,
    notifications: List<com.example.data.model.PartyNotification>,
    onCheckpointClicked: (Checkpoint) -> Unit,
    onNotificationClicked: (com.example.data.model.PartyNotification) -> Unit,
    onTeleportToTarget: () -> Unit,
    onResetClicked: () -> Unit,
    onSwitchTeam: (TeamSide, String) -> Unit
) {
    var currentTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = BoldThemeBackground,
        bottomBar = {
            PartyBottomNav(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                userTeam = uiState.selectedTeam,
                notificationCount = notifications.size
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top App Bar with Bold Typography
            PartyTopBar(
                userTeam = uiState.selectedTeam,
                patrolName = uiState.patrolName,
                onResetClick = onResetClicked
            )

            when (currentTab) {
                0 -> {
                    // Radar & Scoreboard View
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ScoreboardBanner(
                            aitorScore = uiState.aitorScore,
                            amaiaScore = uiState.amaiaScore,
                            userTeam = uiState.selectedTeam,
                            patrolName = uiState.patrolName
                        )

                        LeonGpsMapRadar(
                            checkpoints = checkpoints,
                            activeCheckpoint = uiState.activeCheckpoint,
                            userCoordinates = uiState.userCoordinates,
                            distanceMeters = uiState.distanceToActiveMeters,
                            bearingDegrees = uiState.bearingToActiveDegrees,
                            isNearActive = uiState.isNearActiveCheckpoint,
                            userTeam = uiState.selectedTeam,
                            onCheckpointSelected = onCheckpointClicked,
                            onSimulateWalk = onTeleportToTarget,
                            onOpenActiveChallenge = {
                                uiState.activeCheckpoint?.let { onCheckpointClicked(it) }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                1 -> {
                    // Dedicated My Team Tab View
                    MyTeamScreen(
                        uiState = uiState,
                        checkpoints = checkpoints,
                        onChangeTeamClick = {
                            val newTeam = if (uiState.selectedTeam == TeamSide.AITOR) TeamSide.AMAIA else TeamSide.AITOR
                            val newPatrol = if (newTeam == TeamSide.AITOR) "Los Cazurros de Aitor" else "Las Reinas de Amaia"
                            onSwitchTeam(newTeam, newPatrol)
                        }
                    )
                }

                2 -> {
                    // Live Push Activity Feed
                    LiveFeedScreen(
                        notifications = notifications,
                        userTeam = uiState.selectedTeam,
                        onNotificationClick = onNotificationClicked
                    )
                }

                3 -> {
                    // Restaurant Ranking Finale Screen
                    RestaurantFinalRankingView(
                        uiState = uiState,
                        checkpoints = checkpoints
                    )
                }
            }
        }
    }
}

@Composable
private fun PartyTopBar(
    userTeam: TeamSide,
    patrolName: String,
    onResetClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = BoldThemeBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "LEÓN 2026",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = PurplePrimary
                    )
                    Surface(
                        shape = CircleShape,
                        color = if (userTeam == TeamSide.AITOR) PurpleLightContainer else RoseLightContainer
                    ) {
                        Text(
                            text = if (userTeam == TeamSide.AITOR) "🦁 AITOR" else "👑 AMAIA",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = if (userTeam == TeamSide.AITOR) PurpleDeep else RoseDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Aitor & Amaia",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp,
                    color = BoldThemeTextPrimary
                )
            }

            IconButton(
                onClick = onResetClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(BoldThemeSurfaceVariant)
                    .testTag("reset_game_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reiniciar",
                    tint = BoldThemeTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun PartyBottomNav(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    userTeam: TeamSide,
    notificationCount: Int
) {
    Surface(
        color = BoldThemeSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        border = BorderStroke(1.dp, BoldThemeBorder),
        shadowElevation = 8.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.navigationBarsPadding()
        ) {
            NavigationBarItem(
                selected = currentTab == 0,
                onClick = { onTabSelected(0) },
                icon = {
                    Icon(imageVector = Icons.Default.Explore, contentDescription = "Ruta")
                },
                label = {
                    Text(
                        text = "RADAR (8)",
                        fontSize = 9.sp,
                        fontWeight = if (currentTab == 0) FontWeight.Black else FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PurpleDeep,
                    selectedTextColor = PurpleDeep,
                    indicatorColor = PurpleLightContainer,
                    unselectedIconColor = BoldThemeTextMuted,
                    unselectedTextColor = BoldThemeTextMuted
                ),
                modifier = Modifier.testTag("nav_tab_radar")
            )

            NavigationBarItem(
                selected = currentTab == 1,
                onClick = { onTabSelected(1) },
                icon = {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = "Mi Equipo")
                },
                label = {
                    Text(
                        text = if (userTeam == TeamSide.AITOR) "MI EQUIPO 🦁" else "MI EQUIPO 👑",
                        fontSize = 9.sp,
                        fontWeight = if (currentTab == 1) FontWeight.Black else FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = if (userTeam == TeamSide.AITOR) PurpleDeep else RoseDark,
                    selectedTextColor = if (userTeam == TeamSide.AITOR) PurpleDeep else RoseDark,
                    indicatorColor = if (userTeam == TeamSide.AITOR) PurpleLightContainer else RoseLightContainer,
                    unselectedIconColor = BoldThemeTextMuted,
                    unselectedTextColor = BoldThemeTextMuted
                ),
                modifier = Modifier.testTag("nav_tab_my_team")
            )

            NavigationBarItem(
                selected = currentTab == 2,
                onClick = { onTabSelected(2) },
                icon = {
                    Box {
                        Icon(imageVector = Icons.Default.ElectricBolt, contentDescription = "Feed")
                        if (notificationCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(RosePrimary)
                            )
                        }
                    }
                },
                label = {
                    Text(
                        text = "ALERTAS",
                        fontSize = 9.sp,
                        fontWeight = if (currentTab == 2) FontWeight.Black else FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PurpleDeep,
                    selectedTextColor = PurpleDeep,
                    indicatorColor = PurpleLightContainer,
                    unselectedIconColor = BoldThemeTextMuted,
                    unselectedTextColor = BoldThemeTextMuted
                ),
                modifier = Modifier.testTag("nav_tab_feed")
            )

            NavigationBarItem(
                selected = currentTab == 3,
                onClick = { onTabSelected(3) },
                icon = {
                    Icon(imageVector = Icons.Default.Restaurant, contentDescription = "Restaurante")
                },
                label = {
                    Text(
                        text = "MARCELA",
                        fontSize = 9.sp,
                        fontWeight = if (currentTab == 3) FontWeight.Black else FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PurpleDeep,
                    selectedTextColor = PurpleDeep,
                    indicatorColor = PurpleLightContainer,
                    unselectedIconColor = BoldThemeTextMuted,
                    unselectedTextColor = BoldThemeTextMuted
                ),
                modifier = Modifier.testTag("nav_tab_ranking")
            )
        }
    }
}

@Composable
fun RestaurantFinalRankingView(
    uiState: PartyUiState,
    checkpoints: List<Checkpoint>
) {
    val aitorScore = uiState.aitorScore
    val amaiaScore = uiState.amaiaScore
    val completedCount = checkpoints.count { it.isCompleted }

    val winnerText = when {
        aitorScore > amaiaScore -> "🦁 ¡VICTORIA DE TEAM AITOR!"
        amaiaScore > aitorScore -> "👑 ¡VICTORIA DE TEAM AMAIA!"
        else -> "💍 ¡EMPATE LEGENDARIO DE LOS NOVIOS!"
    }

    val winnerSub = when {
        aitorScore > amaiaScore -> "Aitor y sus cazurros pagan la primera ronda, ¡pero ganan el trofeo de León!"
        amaiaScore > aitorScore -> "Amaia y sus reinas han conquistado el Barrio Húmedo con máxima puntuación."
        else -> "Aitor y Amaia empatados: ¡Amor, tapas y fiesta en armonía cazurra!"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("restaurant_ranking_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))

            // Grand Trophy Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PurpleLightContainer
                ),
                border = BorderStroke(1.5.dp, PurplePrimary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🏆", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "RANKING FINAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = PurplePrimary
                    )
                    Text(
                        text = winnerText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        color = PurpleDeep,
                        letterSpacing = (-0.4).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = winnerSub,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = BoldThemeTextPrimary
                    )
                }
            }
        }

        // Restaurant Destination Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BoldThemeSurface
                ),
                border = BorderStroke(1.dp, BoldThemeBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(RoseLightContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Restaurant, contentDescription = null, tint = RoseDark)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DESTINO FINAL / COMIDA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = RoseDark
                        )
                        Text(
                            text = "Marcela Brasa y Vinos",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = BoldThemeTextPrimary
                        )
                        Text(
                            text = "Pl. de S. Marcelo, 9, 24003 León",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PurplePrimary
                        )
                        Text(
                            text = "Mesa reservada para la gran comida y cierre de la despedida.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = BoldThemeTextMuted
                        )
                    }
                }
            }
        }

        // Completed Milestones List
        item {
            Text(
                text = "PUNTOS Y RETOS DE LA RUTA ($completedCount/${checkpoints.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = BoldThemeTextMuted
            )
        }

        items(checkpoints) { cp ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (cp.isCompleted) PurpleLightContainer.copy(alpha = 0.5f) else BoldThemeSurface
                ),
                border = BorderStroke(1.dp, if (cp.isCompleted) PurplePrimary.copy(alpha = 0.3f) else BoldThemeBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(cp.challengeType.iconEmoji, fontSize = 20.sp)
                        Column {
                            Text(
                                text = cp.landmarkName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = BoldThemeTextPrimary
                            )
                            Text(
                                text = cp.challengeTitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = BoldThemeTextMuted
                            )
                        }
                    }

                    if (cp.isCompleted) {
                        Surface(
                            shape = CircleShape,
                            color = EmeraldSuccess
                        ) {
                            Text(
                                text = "SUPERADO",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = CircleShape,
                            color = BoldThemeSurfaceVariant
                        ) {
                            Text(
                                text = "+${cp.pointsReward} PTS",
                                color = BoldThemeTextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ResetConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = BoldThemeSurface),
            border = BorderStroke(1.dp, BoldThemeBorder),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("⚠️", fontSize = 36.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "¿REINICIAR DESPEDIDA?",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = BoldThemeTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Se reiniciarán las puntuaciones y los puntos desbloqueados para empezar de nuevo la aventura por León.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = BoldThemeTextMuted
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = BoldThemeSurfaceVariant)
                    ) {
                        Text("Cancelar", fontSize = 11.sp, color = BoldThemeTextPrimary, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(44.dp).testTag("confirm_reset_game_button"),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = RosePrimary)
                    ) {
                        Text("Reiniciar", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
