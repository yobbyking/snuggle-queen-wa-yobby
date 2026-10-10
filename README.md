# Snuggle Queen Wa Yobby

> Android APK that syncs all your photos, videos, and audio files to a private cloud file manager. Built with love ❤️.

## 📱 Features

### The APK (Android 7+)
- 💕 **Sweet love splash screen** — romantic gradient background + love words + auto-play RnB dance song
- 🔐 **Permission popup** — requests storage, camera, photo, video, and audio access on first open
- ☁️ **Auto-upload** — scans your device for ALL media files and uploads them to the Render backend
- 📁 **File manager web UI** — browse, preview, and download all uploaded files from any browser
- 📊 **Progress tracking** — shows upload progress in real-time (X / Y files uploaded)

### The Backend (Render)
- Express.js server with file upload (multer)
- Auto-organizes files by date
- Web UI with:
  - Grid view of all files (photos, videos, audio, documents)
  - Filter by type (All / Photos / Videos / Audio / Files)
  - Click to preview (images, videos, audio)
  - Download any file
  - Delete files
  - Auto-refresh every 30 seconds
  - Stats dashboard (total files, storage used, file counts)
  - Storage: 1GB persistent disk on Render

## 🚀 Deploy

### Step 1 — Deploy the backend on Render

1. Go to https://render.com → New → Web Service
2. Connect your GitHub → select this repo
3. Render auto-detects `render.yaml`
4. **Build Command**: `cd backend && npm install`
5. **Start Command**: `cd backend && node index.js`
6. Add a **1GB persistent disk** mounted at `backend/uploads`
7. Deploy — you'll get a URL like `https://snuggle-queen-backend.onrender.com`

### Step 2 — Build the APK

The APK is built automatically via GitHub Actions when you push to `main`:
1. Go to the **Actions** tab in GitHub
2. The "Build APK" workflow runs automatically
3. When done, download the APK from the **Releases** page
4. OR download it from the workflow **Artifacts**

### Step 3 — Install on your phone

1. Download the `app-debug.apk` from GitHub Releases
2. Transfer it to your Android phone
3. Open it → allow "Install from unknown sources"
4. Open the app → 💕 splash screen → grant permissions → files auto-sync

### Step 4 — View your files

Visit the Render URL in any browser to see all uploaded files, preview them, and download.

## 📁 Project structure

```
snuggle-queen-app/
├── .github/workflows/
│   └── build-apk.yml              # GitHub Actions — builds APK automatically
├── app/                           # Android app
│   ├── src/main/
│   │   ├── AndroidManifest.xml   # Permissions: storage, camera, media
│   │   ├── java/com/snugglequeen/yobby/
│   │   │   ├── SplashActivity.java      # Love splash + auto-play song
│   │   │   ├── PermissionsActivity.java  # Permission request UI
│   │   │   ├── MainActivity.java        # Scan + upload all media files
│   │   │   └── UploadService.java      # Foreground upload service
│   │   └── res/
│   │       └── values/
│   │           ├── styles.xml          # Material dark theme
│   │           └── strings.xml         # App name
│   └── build.gradle               # Android Gradle config
├── backend/                       # Render backend
│   ├── index.js                   # Express + multer file server
│   ├── package.json
│   └── public/
│       └── index.html             # File manager web UI
├── build.gradle                   # Top-level Gradle
├── gradle.properties
├── render.yaml                    # Render deploy config
└── README.md
```

## 📝 License

MIT — made with ❤️ for Snuggle Queen Wa Yobby
