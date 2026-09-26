const jwt = require('jsonwebtoken');
const JWT_SECRET = process.env.JWT_SECRET || 'jansaarthi_secret';

module.exports = (req, res, next) => {
    try {
        const authHeader = req.header('Authorization');
        if (!authHeader || !authHeader.startsWith('Bearer ')) {
            return res.status(401).json({ message: 'No authentication token, authorization denied.' });
        }

        const token = authHeader.replace('Bearer ', '');
        const decoded = jwt.verify(token, JWT_SECRET);
        
        req.userId = decoded.userId;
        next();
    } catch (error) {
        res.status(401).json({ message: 'Token verification failed, authorization denied.' });
    }
};
