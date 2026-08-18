/* =========================================================
   Gymkana León: PWA App Core Engine (JavaScript ES6+)
   Multi-Device Real-Time Synchronization via MQTT WebSockets
   Independent Per-Team Challenge Tracking (Aitor vs Amaia)
   ========================================================= */

// Shared Room Configuration
const SYNC_CONFIG = {
  roomTopic: 'gymkana_leon_2026_party_clash/sync_v2',
  brokers: [
    'wss://broker.emqx.io:8084/mqtt',
    'wss://broker.hivemq.com:8884/mqtt'
  ],
  deviceId: 'device_' + Math.random().toString(36).substring(2, 9)
};

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
    completedByAitor: false,
    photoProofAitor: null,
    completedByAmaia: false,
    photoProofAmaia: null,
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
    completedByAitor: false,
    photoProofAitor: null,
    completedByAmaia: false,
    photoProofAmaia: null,
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
    completedByAitor: false,
    photoProofAitor: null,
    completedByAmaia: false,
    photoProofAmaia: null,
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
    completedByAitor: false,
    photoProofAitor: null,
    completedByAmaia: false,
    photoProofAmaia: null,
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
    completedByAitor: false,
    photoProofAitor: null,
    completedByAmaia: false,
    photoProofAmaia: null,
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
    completedByAitor: false,
    photoProofAitor: null,
    completedByAmaia: false,
    photoProofAmaia: null,
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
  checkpoints: JSON.parse(JSON.stringify(INITIAL_CHECKPOINTS)),
  notifications: [],
  userCoords: {
    latitude: 42.5987,
    longitude: -5.5671,
    isLive: false
  },
  currentModalCheckpoint: null,
  selectedQuizAnswer: null,
  capturedPhotoBase64: null,
  isMqttConnected: false
};

// Helpers for Team Checkpoint State
function isCpCompletedByTeam(cp, team) {
  if (!cp) return false;
  return team === 'AITOR' ? !!cp.completedByAitor : !!cp.completedByAmaia;
}

function getCpPhotoByTeam(cp, team) {
  if (!cp) return null;
  return team === 'AITOR' ? cp.photoProofAitor : cp.photoProofAmaia;
}

function setCpCompletedByTeam(cp, team, photoUri) {
  if (!cp) return;
  if (team === 'AITOR') {
    cp.completedByAitor = true;
    if (photoUri) cp.photoProofAitor = photoUri;
  } else {
    cp.completedByAmaia = true;
    if (photoUri) cp.photoProofAmaia = photoUri;
  }
}

function recalculateScores() {
  state.aitorScore = state.checkpoints.reduce((sum, c) => sum + (c.completedByAitor ? c.pointsReward : 0), 0);
  state.amaiaScore = state.checkpoints.reduce((sum, c) => sum + (c.completedByAmaia ? c.pointsReward : 0), 0);
}

// Leaflet Map Handles
let mapInstance = null;
let userMarker = null;
let checkpointMarkers = [];
let routePolyline = null;

// Multi-Tab local broadcast
const localBroadcast = typeof BroadcastChannel !== 'undefined' ? new BroadcastChannel('gymkana_leon_local') : null;

// MQTT Client
let mqttClient = null;

// =========================================================
// Storage / Persistence Engine (localStorage + Memory)
// =========================================================

const STORAGE_KEY = 'GYMKANA_LEON_STATE_V3';

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
    console.warn('Storage quota exceeded or private mode, state kept in memory:', err);
  }
}

function loadStateFromStorage() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY) || localStorage.getItem('GYMKANA_LEON_STATE_V2');
    if (saved) {
      const parsed = JSON.parse(saved);
      state.isGameStarted = parsed.isGameStarted ?? false;
      state.selectedTeam = parsed.selectedTeam ?? 'AITOR';
      state.patrolName = parsed.patrolName || (state.selectedTeam === 'AITOR' ? 'Los Cazurros' : 'Las Reinas');

      if (parsed.checkpoints && parsed.checkpoints.length > 0) {
        state.checkpoints = INITIAL_CHECKPOINTS.map(initCp => {
          const savedCp = parsed.checkpoints.find(c => c.id === initCp.id);
          if (!savedCp) return { ...initCp };

          const merged = { ...initCp, ...savedCp };

          // Backward compatibility migration if coming from legacy single isCompleted
          if (savedCp.isCompleted && savedCp.completedByTeam) {
            if (savedCp.completedByTeam === 'AITOR') {
              merged.completedByAitor = true;
              if (savedCp.photoProofUri && !merged.photoProofAitor) merged.photoProofAitor = savedCp.photoProofUri;
            } else if (savedCp.completedByTeam === 'AMAIA') {
              merged.completedByAmaia = true;
              if (savedCp.photoProofUri && !merged.photoProofAmaia) merged.photoProofAmaia = savedCp.photoProofUri;
            }
          }
          return merged;
        });
      }

      recalculateScores();

      if (parsed.notifications && parsed.notifications.length > 0) {
        state.notifications = parsed.notifications;
      }
    }
  } catch (err) {
    console.error('Error reading saved state:', err);
  }
}

// =========================================================
// Multi-Device Real-Time Sync via MQTT WebSockets
// =========================================================

function initMultiDeviceSync() {
  updateSyncStatus(false, '🟡 Conectando...');

  if (typeof mqtt === 'undefined') {
    console.warn('[Sync] MQTT library not available, fallback to local broadcast');
    updateSyncStatus(true, '🟢 LOCAL (Sin internet)');
    return;
  }

  tryConnectMqtt(0);

  // Listen to same-device tabs
  if (localBroadcast) {
    localBroadcast.onmessage = (event) => {
      handleIncomingSyncMessage(event.data);
    };
  }
}

function tryConnectMqtt(brokerIndex) {
  if (brokerIndex >= SYNC_CONFIG.brokers.length) {
    console.warn('[Sync] All MQTT brokers failed, retrying in 5 seconds...');
    updateSyncStatus(false, '🔴 Reconectando...');
    setTimeout(() => tryConnectMqtt(0), 5000);
    return;
  }

  const brokerUrl = SYNC_CONFIG.brokers[brokerIndex];
  console.log(`[Sync] Connecting to live multi-device broker: ${brokerUrl}`);

  try {
    mqttClient = mqtt.connect(brokerUrl, {
      clientId: `${SYNC_CONFIG.deviceId}_${Date.now()}`,
      clean: true,
      connectTimeout: 5000,
      reconnectPeriod: 4000
    });

    mqttClient.on('connect', () => {
      console.log('[Sync] Connected to Realtime Cloud Relay!');
      state.isMqttConnected = true;
      updateSyncStatus(true, '🟢 EN VIVO (Multijugador)');

      // Subscribe to shared party topic
      mqttClient.subscribe(SYNC_CONFIG.roomTopic, (err) => {
        if (!err) {
          console.log(`[Sync] Subscribed to topic: ${SYNC_CONFIG.roomTopic}`);
          // Ask other connected phones for the latest state
          broadcastSyncMessage({
            type: 'INIT_SYNC_REQUEST',
            senderId: SYNC_CONFIG.deviceId
          });
        }
      });
    });

    mqttClient.on('message', (topic, message) => {
      try {
        const payload = JSON.parse(message.toString());
        if (payload && payload.senderId !== SYNC_CONFIG.deviceId) {
          handleIncomingSyncMessage(payload);
        }
      } catch (e) {
        console.warn('[Sync] Error parsing message:', e);
      }
    });

    mqttClient.on('error', (err) => {
      console.warn('[Sync] MQTT connection error:', err);
      mqttClient.end();
      tryConnectMqtt(brokerIndex + 1);
    });

    mqttClient.on('close', () => {
      state.isMqttConnected = false;
      updateSyncStatus(false, '🟡 Reconectando...');
    });
  } catch (err) {
    console.warn('[Sync] Exception connecting to MQTT broker:', err);
    tryConnectMqtt(brokerIndex + 1);
  }
}

function updateSyncStatus(isLive, labelText) {
  const badge = document.getElementById('sync-status-badge');
  const dot = badge ? badge.querySelector('.sync-dot') : null;
  const text = document.getElementById('sync-status-text');

  if (text) text.innerText = labelText;
  if (dot) {
    if (isLive) {
      dot.className = 'sync-dot pulsing';
    } else {
      dot.className = 'sync-dot offline';
    }
  }
}

function broadcastSyncMessage(data) {
  data.senderId = SYNC_CONFIG.deviceId;
  data.timestamp = Date.now();

  const payloadStr = JSON.stringify(data);

  if (mqttClient && state.isMqttConnected) {
    mqttClient.publish(SYNC_CONFIG.roomTopic, payloadStr);
  }

  if (localBroadcast) {
    localBroadcast.postMessage(data);
  }
}

function handleIncomingSyncMessage(data) {
  if (!data || !data.type) return;

  console.log('[Sync] Received incoming event:', data.type);

  switch (data.type) {
    case 'INIT_SYNC_REQUEST':
      // If we have already completed challenges or points, send our full state to sync the new device
      if (state.aitorScore > 0 || state.amaiaScore > 0 || state.notifications.length > 0) {
        broadcastSyncMessage({
          type: 'FULL_STATE_SYNC',
          aitorScore: state.aitorScore,
          amaiaScore: state.amaiaScore,
          checkpoints: state.checkpoints,
          notifications: state.notifications
        });
      }
      break;

    case 'FULL_STATE_SYNC':
      if (data.checkpoints && Array.isArray(data.checkpoints)) {
        data.checkpoints.forEach(incomingCp => {
          const localCp = state.checkpoints.find(c => c.id === incomingCp.id);
          if (localCp) {
            if (incomingCp.completedByAitor) {
              localCp.completedByAitor = true;
              if (incomingCp.photoProofAitor) localCp.photoProofAitor = incomingCp.photoProofAitor;
            }
            if (incomingCp.completedByAmaia) {
              localCp.completedByAmaia = true;
              if (incomingCp.photoProofAmaia) localCp.photoProofAmaia = incomingCp.photoProofAmaia;
            }
          }
        });
      }

      recalculateScores();

      if (data.notifications && Array.isArray(data.notifications)) {
        data.notifications.forEach(incNotif => {
          if (!state.notifications.some(n => n.id === incNotif.id)) {
            state.notifications.push(incNotif);
          }
        });
        state.notifications.sort((a, b) => b.id - a.id);
      }

      saveStateToStorage();
      refreshAllGameUI();
      break;

    case 'CHALLENGE_COMPLETED': {
      // Another device completed a challenge for their team!
      const cp = state.checkpoints.find(c => c.id === data.checkpointId);
      if (cp) {
        setCpCompletedByTeam(cp, data.teamSide, data.photoProofUri);
      }

      recalculateScores();

      // Add to notifications feed
      if (data.notification) {
        if (!state.notifications.some(n => n.id === data.notification.id)) {
          state.notifications.unshift(data.notification);
        }
        // Trigger alert banner on this phone!
        triggerPushBanner(data.notification);
        // Play alert audio sound
        playPushChimeAudio();
      }

      saveStateToStorage();
      refreshAllGameUI();
      break;
    }

    case 'RESET_GAME':
      state.aitorScore = 0;
      state.amaiaScore = 0;
      state.checkpoints = JSON.parse(JSON.stringify(INITIAL_CHECKPOINTS));
      state.notifications = [];
      saveStateToStorage();
      refreshAllGameUI();
      break;
  }
}

// =========================================================
// Audio Alert Synthesizer (Web Audio API)
// =========================================================

function playPushChimeAudio() {
  try {
    const AudioContext = window.AudioContext || window.webkitAudioContext;
    if (!AudioContext) return;

    const ctx = new AudioContext();
    const now = ctx.currentTime;

    // Pleasant two-tone chime (E5 -> B5)
    const osc1 = ctx.createOscillator();
    const gain1 = ctx.createGain();
    osc1.type = 'sine';
    osc1.frequency.setValueAtTime(659.25, now); // E5
    osc1.frequency.exponentialRampToValueAtTime(987.77, now + 0.15); // B5

    gain1.gain.setValueAtTime(0.3, now);
    gain1.gain.exponentialRampToValueAtTime(0.001, now + 0.5);

    osc1.connect(gain1);
    gain1.connect(ctx.destination);

    osc1.start(now);
    osc1.stop(now + 0.5);
  } catch (e) {
    console.log('Audio playback prevented or unsupported:', e);
  }
}

// =========================================================
// Service Worker Registration (PWA Offline Capability)
// =========================================================

if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('sw.js')
      .then(reg => console.log('[PWA] ServiceWorker registered:', reg.scope))
      .catch(err => console.log('[PWA] ServiceWorker error:', err));
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

  // Setup Multi-device Real-Time Sync
  initMultiDeviceSync();
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
  refreshAllGameUI();
  
  // Init Leaflet Map if not created
  setTimeout(() => {
    initLeafletMap();
  }, 120);
}

function refreshAllGameUI() {
  updateScoreboardUI();
  updateTeamBadgeUI();
  renderCheckpointsList();
  renderNotificationsList();
  updateMapMarkers();
  updateActiveTargetCard();
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
  const aitorEl = document.getElementById('score-aitor-num');
  const amaiaEl = document.getElementById('score-amaia-num');
  if (aitorEl) aitorEl.innerHTML = `${state.aitorScore} <small>pts</small>`;
  if (amaiaEl) amaiaEl.innerHTML = `${state.amaiaScore} <small>pts</small>`;
}

function updateTeamBadgeUI() {
  const isAitor = state.selectedTeam === 'AITOR';
  const teamEmoji = isAitor ? '🦁' : '👑';
  const teamName = isAitor ? 'TEAM AITOR' : 'TEAM AMAIA';

  const hEmoji = document.getElementById('header-team-emoji');
  const hText = document.getElementById('header-team-text');
  const nIcon = document.getElementById('nav-team-icon');

  if (hEmoji) hEmoji.innerText = teamEmoji;
  if (hText) hText.innerText = teamName;
  if (nIcon) nIcon.innerText = teamEmoji;

  // My Team Screen elements
  const bBadge = document.getElementById('my-team-banner-badge');
  const bTitle = document.getElementById('my-team-name-title');
  const bPatrol = document.getElementById('my-patrol-label');

  if (bBadge) bBadge.innerText = `${teamEmoji} TU ESCUADRÓN`;
  if (bTitle) bTitle.innerText = teamName;
  if (bPatrol) bPatrol.innerText = `Patrulla: ${state.patrolName}`;

  // Progress Bar for CURRENT team
  const completedCount = state.checkpoints.filter(c => isCpCompletedByTeam(c, state.selectedTeam)).length;
  const totalCount = state.checkpoints.length;
  const percentage = totalCount > 0 ? (completedCount / totalCount) * 100 : 0;

  const progText = document.getElementById('route-progress-text');
  const progFill = document.getElementById('route-progress-fill');

  if (progText) progText.innerText = `${completedCount} de ${totalCount} completados`;
  if (progFill) progFill.style.width = `${percentage}%`;
}

// =========================================================
// Leaflet Map & GPS Radar Engine
// =========================================================

function initLeafletMap() {
  if (mapInstance) {
    mapInstance.invalidateSize();
    return;
  }

  const initialLat = state.userCoords.latitude || 42.5987;
  const initialLng = state.userCoords.longitude || -5.5671;

  mapInstance = L.map('leon-map', {
    zoomControl: false,
    attributionControl: false
  }).setView([initialLat, initialLng], 16);

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

        const hudText = document.getElementById('hud-gps-text');
        if (hudText) hudText.innerText = 'GPS REAL ACTIVO';
        
        if (mapInstance) {
          updateUserMapMarker();
          updateActiveTargetCard();
        }
      },
      (error) => {
        console.warn('Geolocation access issue:', error);
        const hudText = document.getElementById('hud-gps-text');
        if (hudText) hudText.innerText = 'GPS SIMULADO (LEÓN)';
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
  if (!mapInstance) return;
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

  checkpointMarkers.forEach(m => mapInstance.removeLayer(m));
  checkpointMarkers = [];

  const routePoints = [];
  const nextTargetIdx = getNextTargetIndex();

  state.checkpoints.forEach((cp, index) => {
    routePoints.push([cp.latitude, cp.longitude]);

    const isCompletedByMe = isCpCompletedByTeam(cp, state.selectedTeam);
    const rivalTeam = state.selectedTeam === 'AITOR' ? 'AMAIA' : 'AITOR';
    const isCompletedByRival = isCpCompletedByTeam(cp, rivalTeam);
    const isNextTarget = (index === nextTargetIdx) && !isCompletedByMe;

    const badgeBg = isCompletedByMe ? '#10B981' : (isNextTarget ? (state.selectedTeam === 'AITOR' ? '#7C3AED' : '#E11D48') : '#334155');
    
    let rivalDotHtml = '';
    if (isCompletedByRival) {
      const rivalEmoji = rivalTeam === 'AITOR' ? '🦁' : '👑';
      rivalDotHtml = `<span title="${rivalTeam} ya completó este reto" style="position:absolute; top:-6px; right:-6px; background:#F59E0B; border:1.5px solid #FFF; border-radius:50%; width:16px; height:16px; font-size:9px; display:flex; align-items:center; justify-content:center; box-shadow:0 2px 4px rgba(0,0,0,0.5);">${rivalEmoji}</span>`;
    }

    const iconHtml = `
      <div style="position:relative; background:${badgeBg}; color:#FFF; border:2px solid #FFF; border-radius:50%; width:34px; height:34px; display:flex; align-items:center; justify-content:center; font-weight:900; font-size:14px; box-shadow:0 4px 10px rgba(0,0,0,0.5);">
        ${isCompletedByMe ? '✓' : cp.icon}
        ${rivalDotHtml}
      </div>
    `;

    const markerIcon = L.divIcon({
      className: 'cp-map-marker',
      html: iconHtml,
      iconSize: [34, 34],
      iconAnchor: [17, 17]
    });

    const marker = L.marker([cp.latitude, cp.longitude], { icon: markerIcon })
      .addTo(mapInstance)
      .on('click', () => openChallengeModal(cp));

    checkpointMarkers.push(marker);
  });

  if (routePolyline) {
    mapInstance.removeLayer(routePolyline);
  }
  routePolyline = L.polyline(routePoints, {
    color: state.selectedTeam === 'AITOR' ? '#7C3AED' : '#E11D48',
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
  const index = state.checkpoints.findIndex(c => !isCpCompletedByTeam(c, state.selectedTeam));
  return index !== -1 ? index : 0;
}

function updateActiveTargetCard() {
  const nextCp = state.checkpoints.find(c => !isCpCompletedByTeam(c, state.selectedTeam)) || state.checkpoints[state.checkpoints.length - 1];
  if (!nextCp) return;

  const isCompletedByMyTeam = isCpCompletedByTeam(nextCp, state.selectedTeam);

  const tName = document.getElementById('target-name');
  const tChal = document.getElementById('target-challenge');
  const tDist = document.getElementById('target-dist-badge');
  const tStatus = document.getElementById('target-status-badge');
  const tBtn = document.getElementById('btn-open-target-challenge');

  if (tName) tName.innerText = nextCp.landmarkName;
  if (tChal) tChal.innerText = `${nextCp.icon} ${nextCp.challengeTitle}`;

  const distMeters = calculateDistance(
    state.userCoords.latitude,
    state.userCoords.longitude,
    nextCp.latitude,
    nextCp.longitude
  );

  if (tDist) tDist.innerText = `A ${Math.round(distMeters)} m`;
  
  if (tStatus) {
    tStatus.innerText = isCompletedByMyTeam ? '✓ RUTA SUPERADA' : '🎯 SIGUIENTE OBJETIVO';
  }

  if (tBtn) {
    tBtn.innerText = isCompletedByMyTeam ? 'VER RETO SUPERADO ✓' : `ABRIR RETO (+${nextCp.pointsReward} pts)`;
  }
}

function calculateDistance(lat1, lon1, lat2, lon2) {
  const R = 6371e3;
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
  if (!container) return;
  container.innerHTML = '';

  const rivalTeam = state.selectedTeam === 'AITOR' ? 'AMAIA' : 'AITOR';
  const rivalEmoji = rivalTeam === 'AITOR' ? '🦁' : '👑';
  const rivalName = rivalTeam === 'AITOR' ? 'Team Aitor' : 'Team Amaia';

  state.checkpoints.forEach(cp => {
    const isCompletedByMe = isCpCompletedByTeam(cp, state.selectedTeam);
    const isCompletedByRival = isCpCompletedByTeam(cp, rivalTeam);

    const card = document.createElement('div');
    card.className = `checkpoint-card ${isCompletedByMe ? 'completed' : ''}`;
    card.onclick = () => openChallengeModal(cp);

    let rivalStatusTag = '';
    if (isCompletedByRival) {
      rivalStatusTag = `<span class="rival-done-tag">${rivalEmoji} ${rivalName} ya lo superó</span>`;
    }

    card.innerHTML = `
      <div class="cp-icon-box">${isCompletedByMe ? '✓' : cp.icon}</div>
      <div class="cp-info">
        <h4 class="cp-title">${cp.landmarkName}</h4>
        <p class="cp-challenge">${cp.challengeTitle}</p>
        ${rivalStatusTag}
      </div>
      <div class="cp-badge">
        ${isCompletedByMe ? '✓ SUPERADO' : `+${cp.pointsReward} pts`}
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
  if (!container) return;
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

  if (!banner) return;

  if (bannerEmoji) bannerEmoji.innerText = notification.emoji || '🎉';
  if (bannerMsg) bannerMsg.innerText = notification.message;

  if (photoBadge) {
    if (notification.photoProofUri) {
      photoBadge.classList.remove('hidden');
    } else {
      photoBadge.classList.add('hidden');
    }
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
  const badge = document.getElementById('feed-unread-badge');
  if (badge) badge.classList.remove('hidden');

  // Auto dismiss after 6 seconds
  setTimeout(() => {
    banner.classList.add('hidden');
  }, 6000);
}

// =========================================================
// Challenge Modal & Completion Flow
// =========================================================

function openActiveTargetModal() {
  const target = state.checkpoints.find(c => !isCpCompletedByTeam(c, state.selectedTeam)) || state.checkpoints[0];
  openChallengeModal(target);
}

function openChallengeModal(checkpoint) {
  state.currentModalCheckpoint = checkpoint;
  state.selectedQuizAnswer = null;

  const isCompletedByMe = isCpCompletedByTeam(checkpoint, state.selectedTeam);
  const myPhoto = getCpPhotoByTeam(checkpoint, state.selectedTeam);
  state.capturedPhotoBase64 = myPhoto || null;

  document.getElementById('modal-landmark-title').innerText = checkpoint.landmarkName;
  document.getElementById('modal-challenge-name').innerText = `Reto: ${checkpoint.challengeTitle}`;
  document.getElementById('modal-challenge-desc').innerText = checkpoint.challengeDescription;
  document.getElementById('modal-points-chip').innerText = `+${checkpoint.pointsReward} PUNTOS`;

  document.getElementById('area-photo-challenge').classList.add('hidden');
  document.getElementById('area-quiz-challenge').classList.add('hidden');
  document.getElementById('area-checkin-challenge').classList.add('hidden');

  if (checkpoint.type === 'PHOTO') {
    const photoArea = document.getElementById('area-photo-challenge');
    photoArea.classList.remove('hidden');

    const previewContainer = document.getElementById('photo-preview-thumbnail-container');
    const previewImg = document.getElementById('photo-preview-img');
    if (myPhoto) {
      previewImg.src = myPhoto;
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
    document.getElementById('area-checkin-challenge').classList.remove('hidden');
  }

  const submitBtn = document.getElementById('btn-submit-challenge');
  if (isCompletedByMe) {
    submitBtn.innerText = 'RETO YA SUPERADO POR TU EQUIPO ✓';
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

function triggerNativeCamera() {
  document.getElementById('native-camera-input').click();
}

// Compress High-Res Phone Camera image using HTML5 Canvas
function handlePhotoSelected(event) {
  const file = event.target.files[0];
  if (!file) return;

  const reader = new FileReader();
  reader.onload = (e) => {
    const img = new Image();
    img.onload = () => {
      const canvas = document.createElement('canvas');
      const maxDim = 800;
      let width = img.width;
      let height = img.height;

      if (width > height) {
        if (width > maxDim) {
          height = Math.round((height * maxDim) / width);
          width = maxDim;
        }
      } else {
        if (height > maxDim) {
          width = Math.round((width * maxDim) / height);
          height = maxDim;
        }
      }

      canvas.width = width;
      canvas.height = height;
      const ctx = canvas.getContext('2d');
      ctx.drawImage(img, 0, 0, width, height);

      // Lightweight compressed JPEG data URL (< 70KB)
      const compressedDataUrl = canvas.toDataURL('image/jpeg', 0.72);
      state.capturedPhotoBase64 = compressedDataUrl;

      const previewContainer = document.getElementById('photo-preview-thumbnail-container');
      const previewImg = document.getElementById('photo-preview-img');
      previewImg.src = compressedDataUrl;
      previewContainer.classList.remove('hidden');
    };
    img.src = e.target.result;
  };
  reader.readAsDataURL(file);
}

function submitCurrentChallenge() {
  const cp = state.currentModalCheckpoint;
  if (!cp) return;

  const isCompletedByMe = isCpCompletedByTeam(cp, state.selectedTeam);
  if (isCompletedByMe) {
    closeChallengeModal();
    return;
  }

  if (cp.type === 'QUIZ' && state.selectedQuizAnswer === null) {
    alert('Por favor selecciona una respuesta');
    return;
  }

  // Mark Completed specifically for THIS team!
  setCpCompletedByTeam(cp, state.selectedTeam, state.capturedPhotoBase64);

  const earned = cp.pointsReward;
  recalculateScores();

  const timeStr = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  const isAitor = state.selectedTeam === 'AITOR';
  const notif = {
    id: Date.now(),
    teamSide: state.selectedTeam,
    title: isAitor ? '🦁 ¡Team Aitor sumó puntos!' : '👑 ¡Team Amaia a la cabeza!',
    message: `Superaron ${cp.landmarkName} (+${earned} pts)${state.capturedPhotoBase64 ? ' 📸 [Foto adjunta]' : ''}`,
    pointsDelta: earned,
    checkpointName: cp.landmarkName,
    photoProofUri: state.capturedPhotoBase64,
    emoji: isAitor ? '🦁' : '👑',
    time: timeStr
  };

  state.notifications.unshift(notif);

  // Save to local storage
  saveStateToStorage();

  // Broadcast to ALL OTHER PHONES in real time!
  broadcastSyncMessage({
    type: 'CHALLENGE_COMPLETED',
    checkpointId: cp.id,
    teamSide: state.selectedTeam,
    patrolName: state.patrolName,
    pointsReward: earned,
    photoProofUri: state.capturedPhotoBase64,
    notification: notif
  });

  closeChallengeModal();
  refreshAllGameUI();

  triggerPushBanner(notif);
  playPushChimeAudio();

  if (window.confetti) {
    confetti({
      particleCount: 100,
      spread: 70,
      origin: { y: 0.6 }
    });
  }

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
  
  broadcastSyncMessage({
    type: 'RESET_GAME'
  });

  closeResetModal();
  showScreen('screen-team-selection');
}

function switchTeamModal() {
  const newTeam = state.selectedTeam === 'AITOR' ? 'AMAIA' : 'AITOR';
  state.selectedTeam = newTeam;
  state.patrolName = newTeam === 'AITOR' ? 'Los Cazurros' : 'Las Reinas';
  saveStateToStorage();
  refreshAllGameUI();
}
