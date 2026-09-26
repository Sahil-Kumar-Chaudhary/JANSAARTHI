const express = require('express');
const jwt = require('jsonwebtoken');
const cors = require('cors');

const app = express();
app.use(express.json());
app.use(cors());

const PORT = 3000;
const SECRET_KEY = 'jansaarthi_secret_key';

// In-memory user store
const users = [];

app.post('/api/auth/register', (req, res) => {
    const { fullName, mobile, email, password } = req.body;
    
    if (!fullName || !mobile || !email || !password) {
        return res.status(400).json({ message: 'All fields are required.' });
    }

    const existingUser = users.find(u => u.mobile === mobile);
    if (existingUser) {
        return res.status(409).json({ message: 'An account with this mobile number already exists.' });
    }

    const newUser = { id: Date.now().toString(), fullName, mobile, email, password };
    users.push(newUser);
    
    // Create token
    const token = jwt.sign({ id: newUser.id, mobile: newUser.mobile }, SECRET_KEY, { expiresIn: '1h' });
    
    return res.status(201).json({
        message: 'Account created successfully',
        token,
        user: { id: newUser.id, fullName: newUser.fullName, mobile: newUser.mobile }
    });
});

app.post('/api/auth/login', (req, res) => {
    const { mobile, password } = req.body;
    
    if (!mobile || !password) {
        return res.status(400).json({ message: 'Mobile and password are required.' });
    }

    const user = users.find(u => u.mobile === mobile && u.password === password);
    if (!user) {
        return res.status(401).json({ message: 'Invalid mobile number or password.' });
    }

    const token = jwt.sign({ id: user.id, mobile: user.mobile }, SECRET_KEY, { expiresIn: '1h' });
    
    return res.status(200).json({
        message: 'Login successful',
        token,
        user: { id: user.id, fullName: user.fullName, mobile: user.mobile }
    });
});

app.listen(PORT, () => {
    console.log(`Mock auth server running on http://localhost:${PORT}`);
});
