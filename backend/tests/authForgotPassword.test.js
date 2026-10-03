const assert = require('assert');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

// Mock User Model
const mockUsers = [
    {
        _id: 'user_123',
        name: 'Test Citizen',
        mobile: '9876543210',
        email: 'test@example.com',
        passwordHash: '$2a$10$oldHash1234567890123456',
        resetOtp: undefined,
        resetOtpExpires: undefined,
        save: async function() { return this; }
    }
];

// Test the authController handlers with mock req/res
const authController = require('../src/controllers/authController');
const User = require('../src/models/User');

// Stub User.findOne and User.findById
const originalFindOne = User.findOne;
const originalFindById = User.findById;

User.findOne = async function(query) {
    if (query.mobile) {
        return mockUsers.find(u => u.mobile === query.mobile) || null;
    }
    return null;
};

User.findById = async function(id) {
    return mockUsers.find(u => u._id === id) || null;
};

function createMockRes() {
    return {
        statusCode: null,
        body: null,
        status: function(code) {
            this.statusCode = code;
            return this;
        },
        json: function(data) {
            this.body = data;
            return this;
        }
    };
}

async function runForgotPasswordTests() {
    console.log('=== JANSAARTHI Auth & Forgot Password Unit Tests ===\n');
    let passed = 0;
    let failed = 0;

    async function test(name, fn) {
        try {
            process.stdout.write(`• ${name}... `);
            await fn();
            console.log('PASSED');
            passed++;
        } catch (err) {
            console.log('FAILED');
            console.error(`  Error: ${err.message}`);
            failed++;
        }
    }

    try {
        // 1. forgotPassword - missing mobile
        await test('forgotPassword rejects missing mobile with 400', async () => {
            const req = { body: {} };
            const res = createMockRes();
            await authController.forgotPassword(req, res);
            assert.strictEqual(res.statusCode, 400);
            assert.strictEqual(res.body.message.includes('required'), true);
        });

        // 2. forgotPassword - invalid 10-digit mobile
        await test('forgotPassword rejects non-10-digit mobile with 400', async () => {
            const req = { body: { mobile: '123' } };
            const res = createMockRes();
            await authController.forgotPassword(req, res);
            assert.strictEqual(res.statusCode, 400);
            assert.strictEqual(res.body.message.includes('valid 10-digit'), true);
        });

        // 3. forgotPassword - user not found
        await test('forgotPassword returns 404 for non-existent mobile', async () => {
            const req = { body: { mobile: '9999999999' } };
            const res = createMockRes();
            await authController.forgotPassword(req, res);
            assert.strictEqual(res.statusCode, 404);
            assert.strictEqual(res.body.message.includes('No account registered'), true);
        });

        // 4. forgotPassword - valid mobile generates 6-digit OTP
        let generatedOtp = null;
        await test('forgotPassword generates 6-digit OTP and returns 200', async () => {
            const req = { body: { mobile: '9876543210' } };
            const res = createMockRes();
            await authController.forgotPassword(req, res);
            assert.strictEqual(res.statusCode, 200);
            assert.strictEqual(typeof res.body.otp, 'string');
            assert.strictEqual(res.body.otp.length, 6);
            assert.strictEqual(res.body.mobile, '9876543210');
            generatedOtp = res.body.otp;
        });

        // 5. verifyResetOtp - wrong OTP
        await test('verifyResetOtp rejects incorrect OTP with 400', async () => {
            const req = { body: { mobile: '9876543210', otp: '000000' } };
            const res = createMockRes();
            await authController.verifyResetOtp(req, res);
            assert.strictEqual(res.statusCode, 400);
            assert.strictEqual(res.body.message.includes('Invalid OTP'), true);
        });

        // 6. verifyResetOtp - valid OTP generates resetToken
        let validResetToken = null;
        await test('verifyResetOtp accepts valid OTP and issues resetToken with 200', async () => {
            const req = { body: { mobile: '9876543210', otp: generatedOtp } };
            const res = createMockRes();
            await authController.verifyResetOtp(req, res);
            assert.strictEqual(res.statusCode, 200);
            assert.strictEqual(typeof res.body.resetToken, 'string');
            validResetToken = res.body.resetToken;
        });

        // 7. resetPassword - rejects weak password
        await test('resetPassword rejects password under 8 chars with 400', async () => {
            const req = { body: { resetToken: validResetToken, newPassword: 'abc' } };
            const res = createMockRes();
            await authController.resetPassword(req, res);
            assert.strictEqual(res.statusCode, 400);
            assert.strictEqual(res.body.message.includes('8 characters'), true);
        });

        await test('resetPassword rejects password lacking number or uppercase with 400', async () => {
            const req = { body: { resetToken: validResetToken, newPassword: 'passwordonly' } };
            const res = createMockRes();
            await authController.resetPassword(req, res);
            assert.strictEqual(res.statusCode, 400);
            assert.strictEqual(res.body.message.includes('uppercase'), true);
        });

        // 8. resetPassword - successful reset via resetToken
        await test('resetPassword successfully updates password hash with 200', async () => {
            const req = { body: { resetToken: validResetToken, newPassword: 'NewPassword@2026' } };
            const res = createMockRes();
            await authController.resetPassword(req, res);
            assert.strictEqual(res.statusCode, 200);
            assert.strictEqual(res.body.message.includes('successful'), true);

            // Verify password was hashed and user hash matches new password
            const user = mockUsers[0];
            const isMatch = await bcrypt.compare('NewPassword@2026', user.passwordHash);
            assert.strictEqual(isMatch, true);
            assert.strictEqual(user.resetOtp, undefined);
        });

        // 9. resetPassword - can also reset directly via mobile and OTP
        await test('resetPassword works directly with mobile and OTP', async () => {
            // Set OTP again
            mockUsers[0].resetOtp = '654321';
            mockUsers[0].resetOtpExpires = new Date(Date.now() + 600000);

            const req = { body: { mobile: '9876543210', otp: '654321', newPassword: 'AnotherStrongPassword1' } };
            const res = createMockRes();
            await authController.resetPassword(req, res);
            assert.strictEqual(res.statusCode, 200);

            const isMatch = await bcrypt.compare('AnotherStrongPassword1', mockUsers[0].passwordHash);
            assert.strictEqual(isMatch, true);
        });

    } finally {
        // Restore mocks
        User.findOne = originalFindOne;
        User.findById = originalFindById;
    }

    console.log(`\nForgot Password Test Summary: ${passed} passed, ${failed} failed`);
    if (failed > 0) {
        process.exit(1);
    }
}

runForgotPasswordTests();
