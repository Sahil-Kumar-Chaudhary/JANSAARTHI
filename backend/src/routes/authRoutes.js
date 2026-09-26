const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');
const authMiddleware = require('../middleware/authMiddleware');

router.post('/register', authController.register);
router.post('/login', authController.login);
router.get('/me', authMiddleware, authController.getMe);
router.post('/logout', authMiddleware, (req, res) => {
    // In a stateless JWT setup, logout is handled client-side by dropping the token.
    // We can just return success here.
    res.status(200).json({ message: 'Logged out successfully' });
});

module.exports = router;
