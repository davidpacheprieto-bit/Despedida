# Gymkana León: Progressive Web App (PWA)

Aplicación Web Progresiva Full-Stack (HTML5, CSS3, ES6 JavaScript moderno) adaptada para Despedida de Solteros en León (Aitor vs Amaia) con GPS en tiempo real, mapa interactivo Leaflet, retos multimedia con cámara y visor de fotos.

---

## 📱 Archivos del Proyecto PWA

```
/pwa
├── index.html        # Estructura principal con meta-tags para iOS Safari y PWA
├── style.css         # Sistema de diseño Material 3 + iOS Native con Safe-Area Insets
├── app.js            # Lógica completa, persistencia (localStorage/IndexedDB), Leaflet y Retos
├── sw.js             # Service Worker con precaché offline y gestión de recursos
├── manifest.json     # Web App Manifest configurado en modo standalone
├── icons/
│   └── icon.svg      # Icono vectorial escalable para App Icon y Splash Screen
└── README.md         # Guía de despliegue e instalación
```

---

## 🚀 Despliegue Rápido

Puedes alojar estos archivos en cualquier servidor HTTPS (GitHub Pages, Netlify, Vercel, Firebase Hosting o Cloudflare Pages):

1. Sube los archivos de la carpeta `/pwa` a tu servidor.
2. Abre la URL en tu navegador móvil.

### 🍏 En iPhone (iOS Safari):
1. Abre la web en **Safari**.
2. Pulsa el botón **Compartir** (icono cuadrado con flecha hacia arriba).
3. Selecciona **"Añadir a la pantalla de inicio"** (*Add to Home Screen*).
4. La app se abrirá como una aplicación nativa independiente a pantalla completa con status bar translúcida.

### 🤖 En Android (Google Chrome / Pixel 8):
1. Abre la web en **Chrome**.
2. Pulsa el banner emergente **"Instalar aplicación"** o en los 3 puntos del menú > **"Instalar app"**.
3. Se integrará en el cajón de aplicaciones con soporte offline.

---

## ✨ Características Incluidas
- **Persistencia Completa**: Tu equipo elegido, puntuación, checkpoints y fotos se conservan permanentemente aunque cierres la pestaña o la app.
- **GPS en Directo con Radar**: Mapa Leaflet interactivo con orientación, distancia en metros en tiempo real y zoom centrado.
- **Cámara & Fotos**: Subida y captura con cámara nativa, almacenamiento de pruebas y descarga a galería.
- **Notificaciones Push Reales**: Avisos emergentes inmediatos al superar pruebas con visor de foto ampliable.
