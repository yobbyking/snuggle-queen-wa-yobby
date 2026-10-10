const express = require('express');
const multer = require('multer');
const fs = require('fs');
const path = require('path');
const cors = require('cors');
const crypto = require('crypto');

const app = express();
const PORT = process.env.PORT || 3000;
const UPLOAD_DIR = path.join(__dirname, 'uploads');

if (!fs.existsSync(UPLOAD_DIR)) {
  fs.mkdirSync(UPLOAD_DIR, { recursive: true });
}

// Private file storage — each device gets its own folder
// Device ID is sent by the APK (unique per installation)
app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// Multer config — store files under device-specific folders
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const deviceId = req.headers['x-device-id'] || 'unknown';
    const deviceName = req.headers['x-device-name'] || 'Unknown Device';
    const date = new Date().toISOString().slice(0, 10);
    // Sanitize device name for filesystem
    const safeName = deviceName.replace(/[^a-zA-Z0-9_-]/g, '_').slice(0, 50);
    const dir = path.join(UPLOAD_DIR, deviceId, safeName, date);
    if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });

    // Save device info
    const infoFile = path.join(UPLOAD_DIR, deviceId, 'device_info.json');
    if (!fs.existsSync(infoFile)) {
      fs.writeFileSync(infoFile, JSON.stringify({
        deviceId,
        deviceName,
        firstSeen: new Date().toISOString(),
        lastSeen: new Date().toISOString(),
      }, null, 2));
    } else {
      const info = JSON.parse(fs.readFileSync(infoFile, 'utf-8'));
      info.lastSeen = new Date().toISOString();
      fs.writeFileSync(infoFile, JSON.stringify(info, null, 2));
    }

    cb(null, dir);
  },
  filename: (req, file, cb) => {
    const uniqueName = Date.now() + '_' + file.originalname;
    cb(null, uniqueName);
  },
});

const upload = multer({ storage, limits: { fileSize: 500 * 1024 * 1024 } });

// === API ===

// Upload endpoint (private — requires device ID header)
app.post('/api/upload', upload.single('file'), (req, res) => {
  if (!req.file) return res.status(400).json({ error: 'No file uploaded' });
  const deviceId = req.headers['x-device-id'] || 'unknown';
  const deviceName = req.headers['x-device-name'] || 'Unknown';
  console.log(`[UPLOAD] ${deviceName} (${deviceId}): ${req.file.filename} (${(req.file.size / 1024).toFixed(1)} KB)`);
  res.json({ success: true, filename: req.file.filename, size: req.file.size });
});

// List all files — grouped by device
app.get('/api/files', (req, res) => {
  const devices = [];
  const allFiles = [];

  function scanDir(dir, relativePath = '', deviceId = '', deviceName = '') {
    const items = fs.readdirSync(dir);
    for (const item of items) {
      const fullPath = path.join(dir, item);
      const relPath = path.join(relativePath, item);
      const stat = fs.statSync(fullPath);
      if (stat.isDirectory()) {
        scanDir(fullPath, relPath, deviceId || item, deviceName);
      } else if (item === 'device_info.json') {
        try {
          const info = JSON.parse(fs.readFileSync(fullPath, 'utf-8'));
          devices.push(info);
        } catch {}
      } else {
        const ext = path.extname(item).toLowerCase();
        const isImage = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp'].includes(ext);
        const isVideo = ['.mp4', '.avi', '.mov', '.mkv', '.webm', '.3gp'].includes(ext);
        const isAudio = ['.mp3', '.ogg', '.wav', '.m4a', '.aac', '.flac'].includes(ext);
        allFiles.push({
          name: item,
          path: relPath,
          size: stat.size,
          date: stat.mtime,
          type: isImage ? 'image' : isVideo ? 'video' : isAudio ? 'audio' : 'file',
          url: `/api/download/${encodeURIComponent(relPath)}`,
        });
      }
    }
  }
  scanDir(UPLOAD_DIR);
  allFiles.sort((a, b) => new Date(b.date) - new Date(a.date));
  res.json({ files: allFiles, total: allFiles.length, devices });
});

// List devices
app.get('/api/devices', (req, res) => {
  const devices = [];
  if (fs.existsSync(UPLOAD_DIR)) {
    for (const deviceId of fs.readdirSync(UPLOAD_DIR)) {
      const infoFile = path.join(UPLOAD_DIR, deviceId, 'device_info.json');
      if (fs.existsSync(infoFile)) {
        try {
          const info = JSON.parse(fs.readFileSync(infoFile, 'utf-8'));
          // Count files for this device
          let fileCount = 0;
          function countFiles(dir) {
            for (const item of fs.readdirSync(dir)) {
              const fullPath = path.join(dir, item);
              const stat = fs.statSync(fullPath);
              if (stat.isDirectory()) countFiles(fullPath);
              else if (item !== 'device_info.json') fileCount++;
            }
          }
          const deviceDir = path.join(UPLOAD_DIR, deviceId);
          countFiles(deviceDir);
          devices.push({ ...info, fileCount });
        } catch {}
      }
    }
  }
  res.json({ devices });
});

// Download
app.get('/api/download/:path(*)', (req, res) => {
  const filePath = path.join(UPLOAD_DIR, req.params.path);
  if (!fs.existsSync(filePath)) return res.status(404).json({ error: 'Not found' });
  res.download(filePath);
});

// Thumbnail
app.get('/api/thumb/:path(*)', (req, res) => {
  const filePath = path.join(UPLOAD_DIR, req.params.path);
  if (!fs.existsSync(filePath)) return res.status(404).send('Not found');
  res.sendFile(filePath);
});

// Stats
app.get('/api/stats', (req, res) => {
  let totalSize = 0;
  let totalFiles = 0;
  let deviceCount = 0;
  function scanDir(dir) {
    for (const item of fs.readdirSync(dir)) {
      const fullPath = path.join(dir, item);
      const stat = fs.statSync(fullPath);
      if (stat.isDirectory()) scanDir(fullPath);
      else if (item !== 'device_info.json') { totalSize += stat.size; totalFiles++; }
    }
  }
  if (fs.existsSync(UPLOAD_DIR)) {
    for (const deviceId of fs.readdirSync(UPLOAD_DIR)) {
      const deviceDir = path.join(UPLOAD_DIR, deviceId);
      if (fs.statSync(deviceDir).isDirectory()) {
        deviceCount++;
        scanDir(deviceDir);
      }
    }
  }
  res.json({
    totalFiles, deviceCount,
    totalSize: (totalSize / 1048576).toFixed(2) + ' MB',
    totalSizeGB: (totalSize / 1073741824).toFixed(2) + ' GB',
  });
});

// Delete
app.delete('/api/delete/:path(*)', (req, res) => {
  const filePath = path.join(UPLOAD_DIR, req.params.path);
  if (!fs.existsSync(filePath)) return res.status(404).json({ error: 'Not found' });
  fs.unlinkSync(filePath);
  res.json({ success: true });
});

app.get('/api/health', (req, res) => {
  res.json({ ok: true, service: 'snuggle-queen-backend' });
});

app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`[Snuggle Queen] Backend running on port ${PORT}`);
});
