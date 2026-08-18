/* =========================================================
   Gymkana León: PWA App Core Engine (JavaScript ES6+)
   ========================================================= */

// Initial Default Route Checkpoints in León, Spain
const INITIAL_CHECKPOINTS = [
  {
    id: 1,
    landmarkName: "1. Catedral de León",
    subtitle: "Pulchra Leonina • Plaza de Regla",
    challengeTitle: "Foto de Grupo Medieval",
    challengeDescription: "Haz una foto de todo tu equipo posando con cara de gárgolas o reyes leoneses frente a la fachada.",
    type: "PHOTO",
    latitude: 42.5987,
    longitude: -5.5671,
    pointsReward: 200,
    isCompleted: false,
    completedByTeam: null,
    photoProofUri: null,
    icon: "⛪"
  },
  {
    id: 2,
    landmarkName: "2. Plaza Mayor & Barrio Húmedo",
    subtitle: "El epicentro del tapeo cazurro",
    challengeTitle: "Brindis Cazurro Obligatorio",
    challengeDescription: "¿Cuántas plazas o soportales famosos componen el corazón del Húmedo tradicional?",
    type: "QUIZ",
    quizQuestion: "¿Qué famosa plaza con soportales y antiguo consistorio es el centro de fiestas en el Húmedo?",
    quizOptions: ["Plaza Mayor de León", "Plaza de San Marcelo", "Plaza del Grano", "Plaza de Santa María"],
    correctQuizIndex: 0,
    latitude: 42.5968,
    longitude: -5.5682,
    pointsReward: 150,
    isCompleted: false,
    completedByTeam: null,
    photoProofUri: null,
    icon: "🍻"
  },
  {
    id: 3,
    landmarkName: "3. Casa Botines (Gaudí)",
    subtitle: "Obra maestra modernista de Gaudí",
    challengeTitle: "Selfie con el Dragón de San Jorge",
    challengeDescription: "Busca la escultura de San Jorge y el Dragón en la puerta y saca una foto de victoria.",
    type: "PHOTO",
    latitude: 42.5982,
    longitude: -5.5714,
    pointsReward: 250,
    isCompleted: false,
    completedByTeam: null,
    photoProofUri: null,
    icon: "🏰"
  },
  {
    id: 4,
    landmarkName: "4. Basílica de San Isidoro",
    subtitle: "Panteón Real y Santo Grial",
    challengeTitle: "El Santo Grial Leonés",
    challengeDescription: "¿Qué célebre cáliz conservado aquí se postula internacionalmente como el Santo Grial?",
    type: "QUIZ",
    quizQuestion: "¿Cómo se llama el cáliz histórico conservado en San Isidoro?",
    quizOptions: ["Cáliz de Doña Urraca", "Cáliz de Alfonso VI", "Copa de Don Pelayo", "Cáliz de San Froilán"],
    correctQuizIndex: 0,
    latitude: 42.6006,
    longitude: -5.5708,
    pointsReward: 200,
    isCompleted: false,
    completedByTeam: null,
    photoProofUri: null,
    icon: "👑"
  },
  {
    id: 5,
    landmarkName: "5. Muralla Romana & Torre del Gallo",
    subtitle: "Fortificación de la Legio VII Gemina",
    challengeTitle: "Grito de Guerra Cazurro",
    challengeDescription: "Llega al perímetro de la muralla romana y realiza el check-in de patrulla.",
    type: "CHECKIN",
    latitude: 42.6015,
    longitude: -5.5680,
    pointsReward: 200,
    isCompleted: false,
    completedByTeam: null,
    photoProofUri: null,
    icon: "🛡️"
  },
  {
    id: 6,
    landmarkName: "6. Restaurante Final de Celebración",
    subtitle: "Gran Banquete de la Despedida",
    challengeTitle: "Gran Brindis de Victoria",
    challengeDescription: "Sube la foto final de los dos bandos unidos celebrando la boda.",
    type: "PHOTO",
    latitude: 42.5975,
    longitude: -5.5695,
    pointsReward: 300,
    isCompleted: false,
    completedByTeam: null,
    photoProofUri: null,
    icon: "🎉"
  }
];

// App State
const state = {
  isGameStarted: false,
  selectedTeam: 'AITOR', // 'AITOR' or 'AMAIA'
  patrolName: 'Los Cazurros',
  aitorScore: 0,
  amaiaScore: 0,
  checkpoints: [...INITIAL_CHECKPOINTS],
  notifications: [],
  userCoords: {
    latitude: 42.5987,
    longitude: -5.5671,
    isLive: false
  },
  currentModalCheckpoint: null,
  selectedQuizAnswer: null,
  capturedPhotoBase64: null
};

// Leaflet Map Handles
let mapInstance = null;
let userMarker = null;
let checkpointMarkers = [];
let routePolyline = null;

// =========================================================
// Storage / Persistence Engine (IndexedDB + localStorage)
// =========================================================

const STORAGE_KEY = 'GYMKANA_LEON_STATE_V1';

function saveStateToStorage() {
  try {
    const dataToSave = {
      isGameStarted: state.isGameStarted,
      selectedTeam: state.selectedTeam,
      patrolName: state.patrolName,
      aitorScore: state.aitorScore,
      amaiaScore: state.amaiaScore,
      checkpoints: state.checkpoints,
      notifications: state.notifications
    };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(dataToSave));
  } catch (err) {
    console.error('Error saving state:', err);
  }
}

function loadStateFromStorage() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      const parsed = JSON.parse(saved);
      state.isGameStarted = parsed.isGameStarted ?? false;
      state.selectedTeam = parsed.selectedTeam ?? 'AITOR';
      state.patrolName = parsed.patrolName || (state.selectedTeam === 'AITOR' ? 'Los Cazurros' : 'Las Reinas');
      state.aitorScore = parsed.aitorScore || 0;
      state.amaiaScore = parsed.amaiaScore || 0;
      if (parsed.checkpoints && parsed.checkpoints.length > 0) {
        state.checkpoints = parsed.checkpoints;
      }
      if (parsed.notifications && parsed.notifications.length > 0) {
        state.notifications = parsed.notifications;
      }
    }
  } catch (err) {
    console.error('Error reading saved state:', err);
  }
}

// =========================================================
// Service Worker Registration (PWA Offline Capability)
// =========================================================

if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('sw.js')
      .then(reg => console.log('[PWA] ServiceWorker registered with scope:', reg.scope))
      .catch(err => console.log('[PWA] ServiceWorker registration failed:', err));
  });
}

// =========================================================
// Initialization on DOM Ready
// =========================================================

document.addEventListener('DOMContentLoaded', () => {
  loadStateFromStorage();
  
  if (state.isGameStarted) {
    enterGameScreen();
  } else {
    showScreen('screen-team-selection');
  }

  // Setup Live GPS Geolocation
  initGeolocationTracker();
});

// =========================================================
// Navigation & Screen Management
// =========================================================

function showScreen(screenId) {
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  const target = document.getElementById(screenId);
  if (target) target.classList.add('active');
}

function selectTeam(team) {
  state.selectedTeam = team;
  const customPatrol = document.getElementById('patrol-name-input').value.trim();
  if (customPatrol) {
    state.patrolName = customPatrol;
  } else {
    state.patrolName = team === 'AITOR' ? 'Los Cazurros' : 'Las Reinas';
  }

  state.isGameStarted = true;
  saveStateToStorage();
  enterGameScreen();
}

function enterGameScreen() {
  showScreen('screen-game');
  updateScoreboardUI();
  updateTeamBadgeUI();
  renderCheckpointsList();
  renderNotificationsList();
  
  // Init Leaflet Map if not created
  setTimeout(() => {
    initLeafletMap();
  }, 100);
}

function switchTab(tabName) {
  document.querySelectorAll('.tab-pane').forEach(tab => tab.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach(item => item.classList.remove('active'));

  const tabElem = document.getElementById(`tab-${tabName}`);
  const navElem = document.getElementById(`nav-${tabName}`);

  if (tabElem) tabElem.classList.add('active');
  if (navElem) navElem.classList.add('active');

  if (tabName === 'radar' && mapInstance) {
    setTimeout(() => {
      mapInstance.invalidateSize();
      updateMapMarkers();
    }, 150);
  }

  if (tabName === 'feed') {
    document.getElementById('feed-unread-badge').classList.add('hidden');
  }
}

// =========================================================
// Scoreboard & Header Updates
// =========================================================

function updateScoreboardUI() {
  document.getElementById('score-aitor-num').innerHTML = `${state.aitorScore} <small>pts</small>`;
  document.getElementById('score-amaia-num').innerHTML = `${state.amaiaScore} <small>pts</small>`;
}

function updateTeamBadgeUI() {
  const isAitor = state.selectedTeam === 'AITOR';
  const teamEmoji = isAitor ? '🦁' : '👑';
  const teamName = isAitor ? 'TEAM AITOR' : 'TEAM AMAIA';

  document.getElementById('header-team-emoji').innerText = teamEmoji;
  document.getElementById('header-team-text').innerText = teamName;
  document.getElementById('nav-team-icon').innerText = teamEmoji;

  // My Team Screen elements
  document.getElementById('my-team-banner-badge').innerText = `${teamEmoji} TU ESCUADRÓN`;
  document.getElementById('my-team-name-title').innerText = teamName;
  document.getElementById('my-patrol-label').innerText = `Patrulla: ${state.patrolName}`;

  // Progress Bar
  const completedCount = state.checkpoints.filter(c => c.isCompleted).length;
  const totalCount = state.checkpoints.length;
  const percentage = totalCount > 0 ? (completedCount / totalCount) * 100 : 0;

  document.getElementById('route-progress-text').innerText = `${completedCount} de ${totalCount} completados`;
  document.getElementById('route-progress-fill').style.width = `${percentage}%`;
}

// =========================================================
// Leaflet Map & GPS Radar Engine
// =========================================================

function initLeafletMap() {
  if (mapInstance) {
    mapInstance.invalidateSize();
    return;
  }

  // Leon center coordinates
  const initialLat = state.userCoords.latitude || 42.5987;
  const initialLng = state.userCoords.longitude || -5.5671;

  mapInstance = L.map('leon-map', {
    zoomControl: false,
    attributionControl: false
  }).setView([initialLat, initialLng], 16);

  // High quality OpenStreetMap tiles
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19
  }).addTo(mapInstance);

  updateMapMarkers();
  updateActiveTargetCard();
}

function initGeolocationTracker() {
  if ('geolocation' in navigator) {
    navigator.geolocation.watchPosition(
      (position) => {
        state.userCoords.latitude = position.coords.latitude;
        state.userCoords.longitude = position.coords.longitude;
        state.userCoords.isLive = true;

        document.getElementById('hud-gps-text').innerText = 'GPS REAL ACTIVO';
        
        if (mapInstance) {
          updateUserMapMarker();
          updateActiveTargetCard();
        }
      },
      (error) => {
        console.warn('Geolocation access issue:', error);
        document.getElementById('hud-gps-text').innerText = 'GPS SIMULADO (LEÓN)';
      },
      {
        enableHighAccuracy: true,
        maximumAge: 3000,
        timeout: 10000
      }
    );
  }
}

function updateUserMapMarker() {
  const lat = state.userCoords.latitude;
  const lng = state.userCoords.longitude;

  const customIcon = L.divIcon({
    className: 'user-gps-marker',
    iconSize: [20, 20],
    iconAnchor: [10, 10]
  });

  if (!userMarker) {
    userMarker = L.marker([lat, lng], { icon: customIcon }).addTo(mapInstance);
  } else {
    userMarker.setLatLng([lat, lng]);
  }
}

function updateMapMarkers() {
  if (!mapInstance) return;

  // Clear existing checkpoint markers
  checkpointMarkers.forEach(m => mapInstance.removeLayer(m));
  checkpointMarkers = [];

  const routePoints = [];

  state.checkpoints.forEach((cp, index) => {
    routePoints.push([cp.latitude, cp.longitude]);

    const isCompleted = cp.isCompleted;
    const badgeBg = isCompleted ? '#10B981' : (index === getNextTargetIndex() ? '#7C3AED' : '#334155');
    
    const iconHtml = `
      <div style="background:${badgeBg}; color:#FFF; border:2px solid #FFF; border-radius:50%; width:32px; height:32px; display:flex; align-items:center; justify-content:center; font-weight:900; font-size:14px; box-shadow:0 4px 10px rgba(0,0,0,0.5);">
        ${isCompleted ? '✓' : cp.icon}
      </div>
    `;

    const markerIcon = L.divIcon({
      className: 'cp-map-marker',
      html: iconHtml,
      iconSize: [32, 32],
      iconAnchor: [16, 16]
    });

    const marker = L.marker([cp.latitude, cp.longitude], { icon: markerIcon })
      .addTo(mapInstance)
      .on('click', () => openChallengeModal(cp));

    checkpointMarkers.push(marker);
  });

  // Polyline for the route
  if (routePolyline) {
    mapInstance.removeLayer(routePolyline);
  }
  routePolyline = L.polyline(routePoints, {
    color: '#7C3AED',
    weight: 3,
    opacity: 0.7,
    dashArray: '6, 6'
  }).addTo(mapInstance);

  updateUserMapMarker();
}

function centerMapOnUser() {
  if (mapInstance && state.userCoords) {
    mapInstance.flyTo([state.userCoords.latitude, state.userCoords.longitude], 17, {
      duration: 1.2
    });
  }
}

function getNextTargetIndex() {
  const index = state.checkpoints.findIndex(c => !c.isCompleted);
  return index !== -1 ? index : 0;
}

function updateActiveTargetCard() {
  const nextCp = state.checkpoints.find(c => !c.isCompleted) || state.checkpoints[state.checkpoints.length - 1];
  if (!nextCp) return;

  document.getElementById('target-name').innerText = nextCp.landmarkName;
  document.getElementById('target-challenge').innerText = `${nextCp.icon} ${nextCp.challengeTitle}`;

  // Calculate distance in meters
  const distMeters = calculateDistance(
    state.userCoords.latitude,
    state.userCoords.longitude,
    nextCp.latitude,
    nextCp.longitude
  );

  document.getElementById('target-dist-badge').innerText = `A ${Math.round(distMeters)} m`;
  document.getElementById('btn-open-target-challenge').innerText = `ABRIR RETO (+${nextCp.pointsReward} pts)`;
}

function calculateDistance(lat1, lon1, lat2, lon2) {
  const R = 6371e3; // Earth radius in meters
  const φ1 = lat1 * Math.PI / 180;
  const φ2 = lat2 * Math.PI / 180;
  const Δφ = (lat2 - lat1) * Math.PI / 180;
  const Δλ = (lon2 - lon1) * Math.PI / 180;

  const a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
            Math.cos(φ1) * Math.cos(φ2) *
            Math.sin(Δλ/2) * Math.sin(Δλ/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  return R * c;
}

// =========================================================
// Checklist & My Team Screen
// =========================================================

function renderCheckpointsList() {
  const container = document.getElementById('checkpoints-list-container');
  container.innerHTML = '';

  state.checkpoints.forEach(cp => {
    const card = document.createElement('div');
    card.className = `checkpoint-card ${cp.isCompleted ? 'completed' : ''}`;
    card.onclick = () => openChallengeModal(cp);

    card.innerHTML = `
      <div class="cp-icon-box">${cp.icon}</div>
      <div class="cp-info">
        <h4 class="cp-title">${cp.landmarkName}</h4>
        <p class="cp-challenge">${cp.challengeTitle}</p>
      </div>
      <div class="cp-badge">
        ${cp.isCompleted ? '✓ SUPERADO' : `+${cp.pointsReward} pts`}
      </div>
    `;

    container.appendChild(card);
  });
}

// =========================================================
// Live Feed Screen & Real Push Notifications
// =========================================================

function renderNotificationsList() {
  const container = document.getElementById('notifications-list-container');
  container.innerHTML = '';

  if (state.notifications.length === 0) {
    container.innerHTML = `
      <div style="text-align:center; padding:30px; color:var(--text-muted);">
        <p>Aún no hay retos completados. ¡Sé el primero en superar una prueba!</p>
      </div>
    `;
    return;
  }

  state.notifications.forEach(notif => {
    const isRival = notif.teamSide !== state.selectedTeam;
    const item = document.createElement('div');
    item.className = `feed-item-card ${isRival ? 'rival' : ''}`;
    item.onclick = () => {
      if (notif.photoProofUri) {
        openPhotoViewer(notif.checkpointName || notif.title, notif.message, notif.photoProofUri, notif.teamSide);
      }
    };

    item.innerHTML = `
      <div class="feed-item-top">
        <div class="feed-avatar">${notif.emoji || '🎉'}</div>
        <div class="feed-item-body">
          <div class="feed-title-row">
            <span class="feed-title">${notif.title}</span>
            <span class="feed-time">${notif.time || 'Ahora'}</span>
          </div>
          <p class="feed-msg">${notif.message}</p>
          <div class="feed-tags-row">
            ${notif.pointsDelta > 0 ? `<span class="tag-points">+${notif.pointsDelta} PUNTOS</span>` : ''}
            ${notif.photoProofUri ? `<span class="tag-photo-avail">📸 VER FOTO</span>` : ''}
          </div>
        </div>
      </div>
      ${notif.photoProofUri ? `<img src="${notif.photoProofUri}" class="feed-photo-thumb" alt="Foto reto">` : ''}
    `;

    container.appendChild(item);
  });
}

function triggerPushBanner(notification) {
  const banner = document.getElementById('heads-up-banner');
  const bannerEmoji = document.getElementById('banner-emoji');
  const bannerMsg = document.getElementById('banner-message');
  const photoBadge = document.getElementById('banner-photo-badge');

  bannerEmoji.innerText = notification.emoji || '🎉';
  bannerMsg.innerText = notification.message;

  if (notification.photoProofUri) {
    photoBadge.classList.remove('hidden');
  } else {
    photoBadge.classList.add('hidden');
  }

  banner.onclick = () => {
    banner.classList.add('hidden');
    if (notification.photoProofUri) {
      openPhotoViewer(
        notification.checkpointName || notification.title,
        notification.message,
        notification.photoProofUri,
        notification.teamSide
      );
    }
  };

  banner.classList.remove('hidden');

  // Trigger unread badge on tab
  document.getElementById('feed-unread-badge').classList.remove('hidden');

  // Auto dismiss after 6 seconds
  setTimeout(() => {
    banner.classList.add('hidden');
  }, 6000);
}

// =========================================================
// Challenge Modal & Completion Flow
// =========================================================

function openActiveTargetModal() {
  const target = state.checkpoints.find(c => !c.isCompleted) || state.checkpoints[0];
  openChallengeModal(target);
}

function openChallengeModal(checkpoint) {
  state.currentModalCheckpoint = checkpoint;
  state.selectedQuizAnswer = null;
  state.capturedPhotoBase64 = checkpoint.photoProofUri || null;

  document.getElementById('modal-landmark-title').innerText = checkpoint.landmarkName;
  document.getElementById('modal-challenge-name').innerText = `Reto: ${checkpoint.challengeTitle}`;
  document.getElementById('modal-challenge-desc').innerText = checkpoint.challengeDescription;
  document.getElementById('modal-points-chip').innerText = `+${checkpoint.pointsReward} PUNTOS`;

  // Hide all challenge areas
  document.getElementById('area-photo-challenge').classList.add('hidden');
  document.getElementById('area-quiz-challenge').classList.add('hidden');
  document.getElementById('area-checkin-challenge').classList.add('hidden');

  // Populate by type
  if (checkpoint.type === 'PHOTO') {
    const photoArea = document.getElementById('area-photo-challenge');
    photoArea.classList.remove('hidden');

    const previewContainer = document.getElementById('photo-preview-thumbnail-container');
    const previewImg = document.getElementById('photo-preview-img');
    if (checkpoint.photoProofUri) {
      previewImg.src = checkpoint.photoProofUri;
      previewContainer.classList.remove('hidden');
    } else {
      previewContainer.classList.add('hidden');
    }
  } else if (checkpoint.type === 'QUIZ') {
    const quizArea = document.getElementById('area-quiz-challenge');
    quizArea.classList.remove('hidden');
    document.getElementById('quiz-question-text').innerText = checkpoint.quizQuestion || 'Pregunta de León:';

    const optionsContainer = document.getElementById('quiz-options-container');
    optionsContainer.innerHTML = '';
    (checkpoint.quizOptions || []).forEach((opt, idx) => {
      const btn = document.createElement('button');
      btn.className = 'quiz-option-btn';
      btn.innerText = opt;
      btn.onclick = () => selectQuizAnswer(idx, btn);
      optionsContainer.appendChild(btn);
    });
  } else {
    // Check-in
    document.getElementById('area-checkin-challenge').classList.remove('hidden');
  }

  // Update submit button text if already completed
  const submitBtn = document.getElementById('btn-submit-challenge');
  if (checkpoint.isCompleted) {
    submitBtn.innerText = 'RETO YA SUPERADO ✓';
    submitBtn.style.opacity = '0.6';
  } else {
    submitBtn.innerText = 'COMPLETAR RETO ✨';
    submitBtn.style.opacity = '1';
  }

  document.getElementById('modal-challenge').classList.remove('hidden');
}

function closeChallengeModal() {
  document.getElementById('modal-challenge').classList.add('hidden');
}

function selectQuizAnswer(index, buttonElem) {
  state.selectedQuizAnswer = index;
  document.querySelectorAll('.quiz-option-btn').forEach(b => b.classList.remove('selected'));
  buttonElem.classList.add('selected');
}

// Trigger Mobile HTML5 Camera
function triggerNativeCamera() {
  document.getElementById('native-camera-input').click();
}

function handlePhotoSelected(event) {
  const file = event.target.files[0];
  if (!file) return;

  const reader = new FileReader();
  reader.onload = (e) => {
    const dataUrl = e.target.result;
    state.capturedPhotoBase64 = dataUrl;

    const previewContainer = document.getElementById('photo-preview-thumbnail-container');
    const previewImg = document.getElementById('photo-preview-img');
    previewImg.src = dataUrl;
    previewContainer.classList.remove('hidden');
  };
  reader.readAsDataURL(file);
}

function submitCurrentChallenge() {
  const cp = state.currentModalCheckpoint;
  if (!cp) return;

  if (cp.isCompleted) {
    closeChallengeModal();
    return;
  }

  // Validation
  if (cp.type === 'QUIZ' && state.selectedQuizAnswer === null) {
    alert('Por favor selecciona una respuesta');
    return;
  }

  // Mark Completed
  cp.isCompleted = true;
  cp.completedByTeam = state.selectedTeam;
  if (state.capturedPhotoBase64) {
    cp.photoProofUri = state.capturedPhotoBase64;
  }

  // Add Points
  const earned = cp.pointsReward;
  if (state.selectedTeam === 'AITOR') {
    state.aitorScore += earned;
  } else {
    state.amaiaScore += earned;
  }

  // Create Real Push Notification
  const timeStr = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  const isAitor = state.selectedTeam === 'AITOR';
  const notif = {
    id: Date.now(),
    teamSide: state.selectedTeam,
    title: isAitor ? '🦁 ¡Team Aitor sumó puntos!' : '👑 ¡Team Amaia a la cabeza!',
    message: `Superaron ${cp.landmarkName} (+${earned} pts)${cp.photoProofUri ? ' 📸 [Foto adjunta]' : ''}`,
    pointsDelta: earned,
    checkpointName: cp.landmarkName,
    photoProofUri: cp.photoProofUri,
    emoji: isAitor ? '🦁' : '👑',
    time: timeStr
  };

  state.notifications.unshift(notif);

  // Save to persistence
  saveStateToStorage();

  // Close modal and refresh UI
  closeChallengeModal();
  updateScoreboardUI();
  updateTeamBadgeUI();
  renderCheckpointsList();
  renderNotificationsList();
  updateMapMarkers();
  updateActiveTargetCard();

  // Trigger celebration effects
  triggerPushBanner(notif);
  if (window.confetti) {
    confetti({
      particleCount: 100,
      spread: 70,
      origin: { y: 0.6 }
    });
  }

  // Haptic feedback if supported
  if ('vibrate' in navigator) {
    navigator.vibrate([100, 50, 100]);
  }
}

// =========================================================
// Fullscreen Photo Viewer & Gallery Downloader
// =========================================================

function openPhotoViewer(title, subtitle, photoUrl, teamSide) {
  document.getElementById('viewer-photo-title').innerText = title;
  document.getElementById('viewer-photo-subtitle').innerText = subtitle;
  document.getElementById('viewer-full-image').src = photoUrl;

  const isAitor = teamSide === 'AITOR';
  document.getElementById('viewer-team-badge').innerText = isAitor ? '🦁 TEAM AITOR' : '👑 TEAM AMAIA';

  const downloadBtn = document.getElementById('btn-download-photo');
  downloadBtn.href = photoUrl;
  downloadBtn.download = `gymkana_leon_${title.toLowerCase().replace(/\s+/g, '_')}.jpg`;

  document.getElementById('modal-photo-viewer').classList.remove('hidden');
}

function closePhotoViewer() {
  document.getElementById('modal-photo-viewer').classList.add('hidden');
}

// =========================================================
// Reset Game & Switch Team Modals
// =========================================================

function openResetModal() {
  document.getElementById('modal-reset').classList.remove('hidden');
}

function closeResetModal() {
  document.getElementById('modal-reset').classList.add('hidden');
}

function confirmResetGame() {
  state.isGameStarted = false;
  state.aitorScore = 0;
  state.amaiaScore = 0;
  state.checkpoints = JSON.parse(JSON.stringify(INITIAL_CHECKPOINTS));
  state.notifications = [];

  localStorage.removeItem(STORAGE_KEY);
  closeResetModal();
  showScreen('screen-team-selection');
}

function switchTeamModal() {
  const newTeam = state.selectedTeam === 'AITOR' ? 'AMAIA' : 'AITOR';
  state.selectedTeam = newTeam;
  state.patrolName = newTeam === 'AITOR' ? 'Los Cazurros' : 'Las Reinas';
  saveStateToStorage();
  updateTeamBadgeUI();
  updateScoreboardUI();
}
