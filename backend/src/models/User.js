const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
    name: { type: String, required: true },
    mobile: { type: String, required: true, unique: true, index: true },
    email: { type: String, required: true, unique: true, index: true },
    passwordHash: { type: String, required: true },
    
    // Profile Fields (Optional initially)
    age: { type: Number },
    scCategory: { type: String },
    annualFamilyIncome: { type: Number },
    occupation: { type: String },
    businessType: { type: String },
    purpose: { type: String },
    requestedAmount: { type: Number },
    projectCost: { type: Number },
    state: { type: String },
    district: { type: String },
    
    // Settings
    language: { type: String, default: 'EN' },
    theme: { type: String, default: 'SYSTEM' },
    notificationsEnabled: { type: Boolean, default: true }
}, {
    timestamps: true, // Adds createdAt and updatedAt
    collection: 'users'
});

const User = mongoose.model('User', userSchema);
module.exports = User;
