const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');
const authMiddleware = require('../middleware/authMiddleware');
const multer = require('multer');

// Configure multer for memory storage
const storage = multer.memoryStorage();
const upload = multer({ 
    storage: storage,
    limits: { fileSize: 5 * 1024 * 1024 }, // 5 MB limit
    fileFilter: (req, file, cb) => {
        if (file.mimetype === 'image/jpeg' || file.mimetype === 'image/png') {
            cb(null, true);
        } else {
            cb(new Error('Invalid file type. Only JPG and PNG are allowed.'), false);
        }
    }
});

router.get('/', authMiddleware, authController.getMe);
router.put('/', authMiddleware, authController.updateProfile);

// Profile Photo Endpoints
router.post('/photo', authMiddleware, upload.single('photo'), authController.uploadPhoto);
router.get('/photo', authMiddleware, authController.getPhoto);
router.delete('/photo', authMiddleware, authController.deletePhoto);

module.exports = router;
