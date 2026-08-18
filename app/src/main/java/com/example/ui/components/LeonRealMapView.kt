package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.Checkpoint
import com.example.data.model.TeamSide
import com.example.service.UserCoordinates
import com.example.ui.theme.BoldThemeBorder
import com.example.ui.theme.BoldThemeSurface
import com.example.ui.theme.BoldThemeTextMuted
import com.example.ui.theme.BoldThemeTextPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldCelebration
import com.example.ui.theme.PurpleDeep
import com.example.ui.theme.PurpleLightContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.RosePrimary

class WebAppInterface(
    private val onCheckpointClicked: (Int) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onMarkerClick(checkpointId: Int) {
        mainHandler.post {
            onCheckpointClicked(checkpointId)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LeonRealMapView(
    checkpoints: List<Checkpoint>,
    activeCheckpoint: Checkpoint?,
    userCoordinates: UserCoordinates,
    userTeam: TeamSide,
    onCheckpointSelected: (Checkpoint) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isMapLoaded by remember { mutableStateOf(false) }

    // Helper to open Google Maps External App or Web Navigation
    fun openInGoogleMaps(lat: Double, lng: Double, label: String) {
        try {
            val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
                context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
            }
        } catch (e: Exception) {
            val fallbackUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng&travelmode=walking")
            context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
        }
    }

    // Update GPS position on the real map when coordinates change
    LaunchedEffect(userCoordinates.latitude, userCoordinates.longitude, userCoordinates.accuracy, isMapLoaded) {
        if (isMapLoaded && webViewRef != null) {
            val js = "updateUserPos(${userCoordinates.latitude}, ${userCoordinates.longitude}, ${userCoordinates.accuracy});"
            webViewRef?.evaluateJavascript(js, null)
        }
    }

    // Update markers when checkpoints status changes
    LaunchedEffect(checkpoints, activeCheckpoint, isMapLoaded) {
        if (isMapLoaded && webViewRef != null) {
            val js = "renderCheckpoints();"
            webViewRef?.evaluateJavascript(js, null)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .border(1.5.dp, BoldThemeBorder, RoundedCornerShape(26.dp))
            .testTag("leon_real_google_map_container")
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = false
                        displayZoomControls = false
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }
                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isMapLoaded = true
                        }
                    }

                    addJavascriptInterface(
                        WebAppInterface { id ->
                            val clicked = checkpoints.firstOrNull { it.id == id }
                            if (clicked != null) {
                                onCheckpointSelected(clicked)
                            }
                        },
                        "AndroidBridge"
                    )

                    val htmlContent = buildLeafletMapHtml(checkpoints, activeCheckpoint, userCoordinates, userTeam)
                    loadDataWithBaseURL("https://www.openstreetmap.org", htmlContent, "text/html", "UTF-8", null)
                    webViewRef = this
                }
            },
            onRelease = { webView ->
                webView.stopLoading()
                webView.removeJavascriptInterface("AndroidBridge")
                webView.destroy()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top-left HUD: Real Street Map Indicator & Real GPS Status
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.95f),
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.08f)),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (userCoordinates.hasRealGpsFix) EmeraldSuccess else GoldCelebration)
                )
                Text(
                    text = if (userCoordinates.hasRealGpsFix) "GPS REAL (±${userCoordinates.accuracy.toInt()}m)" else "LEÓN (8 PUNTOS)",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = BoldThemeTextPrimary
                )
            }
        }

        // Top-right Action: Open Active Point in Google Maps App
        Surface(
            shape = CircleShape,
            color = PurplePrimary,
            shadowElevation = 4.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = {
                        val target = activeCheckpoint ?: checkpoints.firstOrNull()
                        if (target != null) {
                            openInGoogleMaps(target.latitude, target.longitude, target.landmarkName)
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Abrir en Google Maps",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "GOOGLE MAPS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Bottom-right Map Controls (Zoom in, Zoom out, Fit Route, Center Real GPS)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Zoom In
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.size(36.dp)
            ) {
                IconButton(
                    onClick = {
                        webViewRef?.evaluateJavascript("map.zoomIn();", null)
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Zoom In", tint = BoldThemeTextPrimary, modifier = Modifier.size(18.dp))
                }
            }

            // Zoom Out
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.size(36.dp)
            ) {
                IconButton(
                    onClick = {
                        webViewRef?.evaluateJavascript("map.zoomOut();", null)
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Zoom Out", tint = BoldThemeTextPrimary, modifier = Modifier.size(18.dp))
                }
            }

            // Fit León Checkpoints Route
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.size(36.dp)
            ) {
                IconButton(
                    onClick = {
                        webViewRef?.evaluateJavascript("fitAllCheckpoints();", null)
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(imageVector = Icons.Default.FitScreen, contentDescription = "Ver toda la ruta de León", tint = BoldThemeTextPrimary, modifier = Modifier.size(18.dp))
                }
            }

            // Center on Real GPS User Position
            Surface(
                shape = CircleShape,
                color = if (userCoordinates.hasRealGpsFix) Color(0xFF2563EB) else PurplePrimary,
                shadowElevation = 4.dp,
                modifier = Modifier.size(36.dp)
            ) {
                IconButton(
                    onClick = {
                        val js = "centerOnUser(${userCoordinates.latitude}, ${userCoordinates.longitude});"
                        webViewRef?.evaluateJavascript(js, null)
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = if (userCoordinates.hasRealGpsFix) Icons.Default.GpsFixed else Icons.Default.MyLocation,
                        contentDescription = "Centrar en mi ubicación GPS",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun buildLeafletMapHtml(
    checkpoints: List<Checkpoint>,
    activeCheckpoint: Checkpoint?,
    userCoordinates: UserCoordinates,
    userTeam: TeamSide
): String {
    val activeId = activeCheckpoint?.id ?: 1
    val isAitor = userTeam == TeamSide.AITOR
    val teamPrimaryHex = if (isAitor) "#7C3AED" else "#E11D48"

    // Build checkpoints JSON array
    val cpsJson = checkpoints.joinToString(separator = ",") { cp ->
        """
        {
            "id": ${cp.id},
            "order": ${cp.orderIndex},
            "lat": ${cp.latitude},
            "lng": ${cp.longitude},
            "name": "${escapeJs(cp.landmarkName)}",
            "title": "${escapeJs(cp.title)}",
            "points": ${cp.pointsReward},
            "isCompleted": ${cp.isCompleted},
            "isActive": ${cp.id == activeId},
            "emoji": "${cp.challengeType.iconEmoji}"
        }
        """.trimIndent()
    }

    return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        <style>
            html, body, #map {
                width: 100%;
                height: 100%;
                margin: 0;
                padding: 0;
                background-color: #f3f0f7;
                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            }
            .leaflet-control-attribution { display: none !important; }
            .leaflet-control-zoom { display: none !important; }
            
            /* Custom Pin Markers */
            .custom-pin {
                display: flex;
                align-items: center;
                justify-content: center;
                border-radius: 50%;
                color: white;
                font-weight: 900;
                font-size: 13px;
                box-shadow: 0 4px 10px rgba(0,0,0,0.35);
                border: 2px solid white;
                cursor: pointer;
                transition: transform 0.2s ease;
            }
            .pin-completed {
                background: #10B981;
            }
            .pin-active {
                background: $teamPrimaryHex;
                box-shadow: 0 0 0 6px rgba(124, 58, 237, 0.35);
                transform: scale(1.15);
                animation: pulse 1.5s infinite;
            }
            .pin-unlocked {
                background: #6D28D9;
            }
            .pin-locked {
                background: #9CA3AF;
            }
            
            /* User Real GPS Pulse Dot */
            .user-gps-dot {
                background: #2563EB;
                border: 3px solid white;
                border-radius: 50%;
                box-shadow: 0 0 0 8px rgba(37, 99, 235, 0.35);
                animation: userPulse 1.8s infinite;
                cursor: pointer;
            }
            
            @keyframes pulse {
                0% { box-shadow: 0 0 0 0 rgba(124, 58, 237, 0.5); }
                70% { box-shadow: 0 0 0 14px rgba(124, 58, 237, 0); }
                100% { box-shadow: 0 0 0 0 rgba(124, 58, 237, 0); }
            }
            
            @keyframes userPulse {
                0% { box-shadow: 0 0 0 0 rgba(37, 99, 235, 0.7); }
                70% { box-shadow: 0 0 0 16px rgba(37, 99, 235, 0); }
                100% { box-shadow: 0 0 0 0 rgba(37, 99, 235, 0); }
            }
            
            /* Popup styles */
            .leaflet-popup-content-wrapper {
                border-radius: 16px;
                padding: 4px;
                box-shadow: 0 8px 24px rgba(0,0,0,0.25);
            }
            .popup-card {
                font-family: inherit;
                min-width: 180px;
            }
            .popup-card h4 {
                margin: 0 0 4px 0;
                font-size: 14px;
                font-weight: 800;
                color: #1E1B24;
            }
            .popup-card p {
                margin: 0 0 8px 0;
                font-size: 12px;
                color: #6B7280;
            }
            .popup-btn {
                background: $teamPrimaryHex;
                color: white;
                border: none;
                border-radius: 20px;
                padding: 6px 14px;
                font-size: 11px;
                font-weight: 800;
                width: 100%;
                cursor: pointer;
            }
        </style>
    </head>
    <body>
        <div id="map"></div>

        <script>
            var checkpoints = [ $cpsJson ];
            var map = L.map('map', {
                center: [${userCoordinates.latitude}, ${userCoordinates.longitude}],
                zoom: 16,
                minZoom: 2,
                maxZoom: 19
            });

            // High-clarity CartoDB Positron / OSM tiles showing real streets
            L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
                maxZoom: 19
            }).addTo(map);

            var markersGroup = L.layerGroup().addTo(map);
            var polylineGroup = L.layerGroup().addTo(map);
            var userMarker = null;
            var accuracyCircle = null;

            function renderCheckpoints() {
                markersGroup.clearLayers();
                polylineGroup.clearLayers();

                var latlngs = [];

                checkpoints.forEach(function(cp) {
                    var latlng = [cp.lat, cp.lng];
                    latlngs.push(latlng);

                    var pinClass = "pin-unlocked";
                    if (cp.isCompleted) pinClass = "pin-completed";
                    else if (cp.isActive) pinClass = "pin-active";

                    var icon = L.divIcon({
                        className: 'custom-pin ' + pinClass,
                        html: '<span>' + cp.order + '</span>',
                        iconSize: [28, 28],
                        iconAnchor: [14, 14],
                        popupAnchor: [0, -14]
                    });

                    var popupHtml = '<div class="popup-card">' +
                        '<h4>' + cp.emoji + ' ' + cp.name + '</h4>' +
                        '<p>' + cp.title + ' (+' + cp.points + ' pts)</p>' +
                        '<button class="popup-btn" onclick="triggerCheckpointClick(' + cp.id + ')">' +
                        (cp.isCompleted ? '✓ SUPERADO' : '⚡ ABRIR RETO') +
                        '</button>' +
                        '</div>';

                    L.marker(latlng, { icon: icon })
                        .bindPopup(popupHtml)
                        .addTo(markersGroup);
                });

                // Route trajectory polyline
                if (latlngs.length > 1) {
                    L.polyline(latlngs, {
                        color: '$teamPrimaryHex',
                        weight: 3.5,
                        opacity: 0.8,
                        dashArray: '6, 8',
                        lineJoin: 'round'
                    }).addTo(polylineGroup);
                }
            }

            function updateUserPos(lat, lng, accuracy) {
                if (!userMarker) {
                    var userIcon = L.divIcon({
                        className: 'user-gps-dot',
                        iconSize: [20, 20],
                        iconAnchor: [10, 10],
                        popupAnchor: [0, -10]
                    });
                    userMarker = L.marker([lat, lng], { icon: userIcon, zIndexOffset: 1000 })
                        .bindPopup("<b>📍 Tu Ubicación Real</b><br>GPS Activo")
                        .addTo(map);
                } else {
                    userMarker.setLatLng([lat, lng]);
                }

                if (accuracy && accuracy > 0) {
                    if (!accuracyCircle) {
                        accuracyCircle = L.circle([lat, lng], {
                            radius: accuracy,
                            color: '#2563EB',
                            fillColor: '#2563EB',
                            fillOpacity: 0.12,
                            weight: 1
                        }).addTo(map);
                    } else {
                        accuracyCircle.setLatLng([lat, lng]);
                        accuracyCircle.setRadius(accuracy);
                    }
                }
            }

            function centerOnUser(lat, lng) {
                map.flyTo([lat, lng], 17, { animate: true, duration: 1.0 });
                updateUserPos(lat, lng, 10);
            }

            function fitAllCheckpoints() {
                if (checkpoints.length > 0) {
                    var bounds = L.latLngBounds(checkpoints.map(function(c) { return [c.lat, c.lng]; }));
                    if (userMarker) {
                        bounds.extend(userMarker.getLatLng());
                    }
                    map.fitBounds(bounds, { padding: [40, 40], animate: true });
                }
            }

            function triggerCheckpointClick(id) {
                if (window.AndroidBridge) {
                    window.AndroidBridge.onMarkerClick(id);
                }
            }

            // Initial render
            renderCheckpoints();
            updateUserPos(${userCoordinates.latitude}, ${userCoordinates.longitude}, ${userCoordinates.accuracy});
        </script>
    </body>
    </html>
    """.trimIndent()
}

private fun escapeJs(str: String): String {
    return str.replace("\"", "\\\"").replace("\n", " ")
}
