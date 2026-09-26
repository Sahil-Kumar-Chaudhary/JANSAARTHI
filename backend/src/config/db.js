const mongoose = require('mongoose');

const connectDB = async () => {
    try {
        const uri = process.env.MONGODB_URI;

        if (!uri) {
            console.error('MONGODB_URI is not defined in environment variables.');
            process.exit(1);
        }

        await mongoose.connect(uri, {
            dbName: 'jansaarthi'
        });
        console.log('MongoDB Connected successfully to database: jansaarthi');
    } catch (error) {
        console.error(`Error connecting to MongoDB: ${error.message}`);
        process.exit(1);
    }
};

module.exports = connectDB;
