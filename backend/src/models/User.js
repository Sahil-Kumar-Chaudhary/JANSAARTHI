const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
    name: { type: String, required: true },
    mobile: { type: String, required: true, unique: true, index: true },
    email: { type: String, sparse: true, index: true },
    passwordHash: { type: String, required: true },
    
    // Mandatory for new users
    age: { type: Number },
    gender: { type: String },
    state: { type: String },
    district: { type: String },
    verificationDocType: { type: String },
    verificationDocUrl: { type: String },
    profilePhoto: { type: String },
    
    // Profile Fields (Optional)
    scCategory: { type: String },
    annualFamilyIncome: { type: Number },
    occupation: { type: String },
    businessType: { type: String },
    businessRegistrationStatus: { type: String },
    purpose: { type: String },
    requestedAmount: { type: Number },
    projectCost: { type: Number },
    
    // Settings
    language: { type: String, default: 'EN' },
    theme: { type: String, default: 'SYSTEM' },
    notificationsEnabled: { type: Boolean, default: true }
}, {
    timestamps: true,
    collection: 'users'
});

const User = mongoose.model('User', userSchema);
module.exports = User;


