const User = require('../models/User');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

const JWT_SECRET = process.env.JWT_SECRET || 'jansaarthi_secret';

exports.register = async (req, res) => {
    try {
        const { name, mobile, email, password } = req.body;
        
        if (!name || !mobile || !email || !password) {
            return res.status(400).json({ message: 'Name, mobile, email, and password are required.' });
        }

        const existingUser = await User.findOne({ $or: [{ mobile }, { email }] });
        if (existingUser) {
            return res.status(409).json({ message: 'Account with this mobile or email already exists.' });
        }

        const passwordHash = await bcrypt.hash(password, 10);

        const newUser = new User({
            name,
            mobile,
            email,
            passwordHash
        });

        await newUser.save();

        const token = jwt.sign({ userId: newUser._id }, JWT_SECRET, { expiresIn: '7d' });

        const userObj = newUser.toObject();
        delete userObj.passwordHash;

        res.status(201).json({
            message: 'Registration successful',
            token,
            user: userObj
        });
    } catch (error) {
        console.error('Register error:', error);
        res.status(500).json({ message: 'Internal server error' });
    }
};

exports.login = async (req, res) => {
    try {
        const { mobile, password } = req.body;

        if (!mobile || !password) {
            return res.status(400).json({ message: 'Mobile and password are required.' });
        }

        const user = await User.findOne({ mobile });
        if (!user) {
            return res.status(401).json({ message: 'Invalid mobile number or password.' });
        }

        const isMatch = await bcrypt.compare(password, user.passwordHash);
        if (!isMatch) {
            return res.status(401).json({ message: 'Invalid mobile number or password.' });
        }

        const token = jwt.sign({ userId: user._id }, JWT_SECRET, { expiresIn: '7d' });

        const userObj = user.toObject();
        delete userObj.passwordHash;

        res.status(200).json({
            message: 'Login successful',
            token,
            user: userObj
        });
    } catch (error) {
        console.error('Login error:', error);
        res.status(500).json({ message: 'Internal server error' });
    }
};

exports.getMe = async (req, res) => {
    try {
        const user = await User.findById(req.userId).select('-passwordHash');
        if (!user) {
            return res.status(404).json({ message: 'User not found' });
        }
        res.status(200).json(user);
    } catch (error) {
        console.error('GetMe error:', error);
        res.status(500).json({ message: 'Internal server error' });
    }
};

exports.updateProfile = async (req, res) => {
    try {
        const updates = req.body;
        // Don't allow password updates via this route
        delete updates.passwordHash;
        delete updates.password;
        
        const user = await User.findByIdAndUpdate(req.userId, updates, { new: true }).select('-passwordHash');
        if (!user) {
            return res.status(404).json({ message: 'User not found' });
        }

        res.status(200).json({
            message: 'Profile updated successfully',
            user
        });
    } catch (error) {
        console.error('Update profile error:', error);
        res.status(500).json({ message: 'Internal server error' });
    }
};
