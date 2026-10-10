const express = require('express');
const multer = require('multer');
const fs = require('fs');
const path = require('path');
const cors = require('cors');

const app = express();
const PORT = process.env.PORT || 3000;
const UPLOAD_DIR = path.join(__dirname, 'uploads');

// Ensure uploads directory exists
if (!fs.existsSync(UPLOAD_DIR)) {
  fs.mkdirSync(UPLOAD_DIR, { recursive: true });
}

app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// Multer config — handle file uploads
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    // Create subdirectory by date
    const date = new Date().toISOString().slice(0, 10);
    const dir = path.join(UPLOAD_DIR, date);
    if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
    cb(null, dir);
  },
  filename: (req, file, cb) => {
    // Unique filename with timestamp
    const uniqueName = Date.now() + '_' + file.originalname;
    cb(null, uniqueName);
  },
});

const upload = multer({ storage, limits: { fileSize: 500 * 1024 * 1024 } }); // 500MB max

// === API ENDPOINTS ===

// Upload endpoint
app.post('/api/upload', upload.single('file'), (req, res) => {
  if (!req.file) {
    return res.status(400).json({ error: 'No file uploaded' });
  }
  console.log(`[UPLOAD] ${req.file.filename} (${(req.file.size / 1024).toFixed(1)} KB)`);
  res.json({
    success: true,
    filename: req.file.filename,
    size: req.file.size,
    path: req.file.path,
  });
});

// List all files
app.get('/api/files', (req, res) => {
  const allFiles = [];
  function scanDir(dir, relativePath = '') {
    const items = fs.readdirSync(dir);
    for (const item of items) {
      const fullPath = path.join(dir, item);
      const relPath = path.join(relativePath, item);
      const stat = fs.statSync(fullPath);
      if (stat.isDirectory()) {
        scanDir(fullPath, relPath);
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
  res.json({ files: allFiles, total: allFiles.length });
});

// Download endpoint
app.get('/api/download/:path(*)', (req, res) => {
  const filePath = path.join(UPLOAD_DIR, req.params.path);
  if (!fs.existsSync(filePath)) {
    return res.status(404).json({ error: 'File not found' });
  }
  res.download(filePath);
});

// Thumbnail/preview endpoint (for images)
app.get('/api/thumb/:path(*)', (req, res) => {
  const filePath = path.join(UPLOAD_DIR, req.params.path);
  if (!fs.existsSync(filePath)) {
    return res.status(404).send('Not found');
  }
  res.sendFile(filePath);
});

// Stats endpoint
app.get('/api/stats', (req, res) => {
  let totalSize = 0;
  let totalFiles = 0;
  function scanDir(dir) {
    const items = fs.readdirSync(dir);
    for (const item of items) {
      const fullPath = path.join(dir, item);
      const stat = fs.statSync(fullPath);
      if (stat.isDirectory()) {
        scanDir(fullPath);
      } else {
        totalSize += stat.size;
        totalFiles++;
      }
    }
  }
  scanDir(UPLOAD_DIR);
  res.json({
    totalFiles,
    totalSize: (totalSize / (1024 * 1024)).toFixed(2) + ' MB',
    totalSizeGB: (totalSize / (1024 * 1024 * 1024)).toFixed(2) + ' GB',
  });
});

// Delete file endpoint
app.delete('/api/delete/:path(*)', (req, res) => {
  const filePath = path.join(UPLOAD_DIR, req.params.path);
  if (!fs.existsSync(filePath)) {
    return res.status(404).json({ error: 'File not found' });
  }
  fs.unlinkSync(filePath);
  res.json({ success: true, deleted: req.params.path });
});

// Health check
app.get('/api/health', (req, res) => {
  res.json({ ok: true, service: 'snuggle-queen-backend' });
});

// Serve the web UI
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`[Snuggle Queen] Backend running on port ${PORT}`);
  console.log(`[Snuggle Queen] Web UI: http://localhost:${PORT}`);
});
