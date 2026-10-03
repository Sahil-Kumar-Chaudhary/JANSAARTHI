const User = require('../models/User');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

const JWT_SECRET = process.env.JWT_SECRET || 'jansaarthi_secret';

exports.register = async (req, res) => {
    try {
        const { 
            name, mobile, email, password,
            age, gender, state, district,
            verificationDocType, verificationDocUrl,
            scCategory, annualFamilyIncome, occupation,
            businessType, businessRegistrationStatus,
            purpose, requestedAmount, projectCost,
            educationLevel, areaType, dob
        } = req.body;
        
        if (!name || !mobile || !password || !age || !gender || !state || !district || !verificationDocType || !verificationDocUrl) {
            return res.status(400).json({ message: 'Missing mandatory fields.' });
        }

        const query = [{ mobile }];
        if (email) {
            query.push({ email });
        }

        const existingUser = await User.findOne({ $or: query });
        if (existingUser) {
            return res.status(409).json({ message: 'Account with this mobile or email already exists.' });
        }

        const passwordHash = await bcrypt.hash(password, 10);

        const newUser = new User({
            name, mobile, email, passwordHash,
            age, gender, state, district,
            verificationDocType, verificationDocUrl,
            scCategory, annualFamilyIncome, occupation,
            businessType, businessRegistrationStatus,
            purpose, requestedAmount, projectCost,
            educationLevel, areaType, dob
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

exports.forgotPassword = async (req, res) => {
    try {
        const { mobile } = req.body;

        if (!mobile || !mobile.trim()) {
            return res.status(400).json({ message: 'Mobile number is required.' });
        }

        const trimmedMobile = mobile.trim();
        if (!/^[0-9]{10}$/.test(trimmedMobile)) {
            return res.status(400).json({ message: 'Please enter a valid 10-digit mobile number.' });
        }

        const user = await User.findOne({ mobile: trimmedMobile });
        if (!user) {
            return res.status(404).json({ message: 'No account registered with this mobile number.' });
        }

        // Generate secure 6-digit OTP
        const otp = Math.floor(100000 + Math.random() * 900000).toString();
        const otpExpires = new Date(Date.now() + 10 * 60 * 1000); // 10 minutes

        user.resetOtp = otp;
        user.resetOtpExpires = otpExpires;
        await user.save();

        console.log(`[Auth] Password reset OTP generated for ${user.mobile}: ${otp}`);

        res.status(200).json({
            message: 'OTP sent successfully to registered mobile number.',
            mobile: user.mobile,
            otp: otp // Provided in response for testing/demo/hackathon verification
        });
    } catch (error) {
        console.error('Forgot password error:', error);
        res.status(500).json({ message: 'Internal server error' });
    }
};

exports.verifyResetOtp = async (req, res) => {
    try {
        const { mobile, otp } = req.body;

        if (!mobile || !otp) {
            return res.status(400).json({ message: 'Mobile number and OTP are required.' });
        }

        const trimmedMobile = mobile.trim();
        const trimmedOtp = otp.trim();

        const user = await User.findOne({ mobile: trimmedMobile });
        if (!user) {
            return res.status(404).json({ message: 'User not found.' });
        }

        if (!user.resetOtp || user.resetOtp !== trimmedOtp) {
            return res.status(400).json({ message: 'Invalid OTP. Please check and try again.' });
        }

        if (!user.resetOtpExpires || user.resetOtpExpires < new Date()) {
            return res.status(400).json({ message: 'OTP has expired. Please request a new verification code.' });
        }

        const resetToken = jwt.sign(
            { userId: user._id, mobile: user.mobile, purpose: 'pwd_reset' },
            JWT_SECRET,
            { expiresIn: '15m' }
        );

        res.status(200).json({
            message: 'OTP verified successfully.',
            resetToken,
            mobile: user.mobile
        });
    } catch (error) {
        console.error('Verify reset OTP error:', error);
        res.status(500).json({ message: 'Internal server error' });
    }
};

exports.resetPassword = async (req, res) => {
    try {
        const { mobile, otp, resetToken, newPassword } = req.body;

        if (!newPassword) {
            return res.status(400).json({ message: 'New password is required.' });
        }

        if (newPassword.length < 8) {
            return res.status(400).json({ message: 'Password must be at least 8 characters long.' });
        }

        if (!/[A-Z]/.test(newPassword) || !/[a-z]/.test(newPassword) || !/[0-9]/.test(newPassword)) {
            return res.status(400).json({ message: 'Password must include uppercase, lowercase letters, and at least one number.' });
        }

        let user = null;

        if (resetToken) {
            try {
                const decoded = jwt.verify(resetToken, JWT_SECRET);
                if (decoded.purpose !== 'pwd_reset') {
                    return res.status(401).json({ message: 'Invalid reset token.' });
                }
                user = await User.findById(decoded.userId);
            } catch (err) {
                return res.status(401).json({ message: 'Reset session expired or invalid. Please verify OTP again.' });
            }
        } else if (mobile && otp) {
            const trimmedMobile = mobile.trim();
            const trimmedOtp = otp.trim();
            user = await User.findOne({ mobile: trimmedMobile });
            if (!user) {
                return res.status(404).json({ message: 'User not found.' });
            }
            if (!user.resetOtp || user.resetOtp !== trimmedOtp) {
                return res.status(400).json({ message: 'Invalid OTP. Please verify your OTP.' });
            }
            if (!user.resetOtpExpires || user.resetOtpExpires < new Date()) {
                return res.status(400).json({ message: 'OTP has expired. Please request a new one.' });
            }
        } else {
            return res.status(400).json({ message: 'Verification details missing.' });
        }

        if (!user) {
            return res.status(404).json({ message: 'User account not found.' });
        }

        const passwordHash = await bcrypt.hash(newPassword, 10);
        user.passwordHash = passwordHash;
        user.resetOtp = undefined;
        user.resetOtpExpires = undefined;
        await user.save();

        res.status(200).json({
            message: 'Password reset successful! You can now log in with your new password.'
        });
    } catch (error) {
        console.error('Reset password error:', error);
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

exports.uploadPhoto = async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({ message: 'No photo provided.' });
        }

        const base64Image = `data:${req.file.mimetype};base64,${req.file.buffer.toString('base64')}`;
        
        const user = await User.findByIdAndUpdate(
            req.userId, 
            { profilePhoto: base64Image }, 
            { new: true }
        ).select('-passwordHash');

        if (!user) {
            return res.status(404).json({ message: 'User not found' });
        }

        res.status(200).json({
            message: 'Profile photo updated successfully',
            user
        });
    } catch (error) {
        console.error('Upload photo error:', error);
        res.status(500).json({ message: 'Internal server error' });
    }
};

exports.getPhoto = async (req, res) => {
    try {
        const user = await User.findById(req.userId).select('profilePhoto');
        if (!user || !user.profilePhoto) {
            return res.status(404).json({ message: 'Photo not found' });
        }
        
        res.status(200).json({ photo: user.profilePhoto });
    } catch (error) {
        console.error('Get photo error:', error);
        res.status(500).json({ message: 'Internal server error' });
    }
};

exports.deletePhoto = async (req, res) => {
    try {
        const user = await User.findByIdAndUpdate(
            req.userId,
            { $unset: { profilePhoto: 1 } },
            { new: true }
        ).select('-passwordHash');

        if (!user) {
            return res.status(404).json({ message: 'User not found' });
        }

        res.status(200).json({
            message: 'Profile photo deleted successfully',
            user
        });
    } catch (error) {
        console.error('Delete photo error:', error);
        res.status(500).json({ message: 'Internal server error' });
    }
};
