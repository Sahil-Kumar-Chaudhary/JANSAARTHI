const User = require('../models/User');

exports.getRecommendations = async (req, res) => {
    try {
        const user = await User.findById(req.userId);
        if (!user) {
            return res.status(404).json({ message: 'User not found' });
        }

        // Logic based on user profile
        let match1 = 70;
        let match2 = 60;
        let match3 = 50;
        let reasons1 = ["Matches your location"];

        if (user.annualFamilyIncome < 300000) {
            match1 += 15;
            reasons1.push("Income criteria matched");
        }
        if (user.scCategory && user.scCategory.toUpperCase() !== 'GENERAL') {
            match2 += 25;
        }
        if (user.purpose && user.purpose.toLowerCase().includes('shop')) {
            match3 += 35;
        }

        const schemes = [
            {
                id: "1",
                name: "PM Mudra Yojana (Tarun)",
                ministry: "Ministry of MSME",
                purpose: "Business Expansion",
                maxAmountText: "₹10 Lakh",
                matchPercentage: Math.min(match1, 99),
                interestRate: "8.6% p.a.",
                subsidyText: "Up to 25%",
                reasons: reasons1,
                eligibilityStatus: "Eligible"
            },
            {
                id: "2",
                name: "Stand-Up India Scheme",
                ministry: "Ministry of Finance",
                purpose: "Greenfield Enterprise",
                maxAmountText: "₹1 Crore",
                matchPercentage: Math.min(match2, 99),
                interestRate: "10.2% p.a.",
                subsidyText: "Interest Rebate",
                reasons: ["Supports new business"],
                eligibilityStatus: "Eligible"
            },
            {
                id: "3",
                name: "PMEGP",
                ministry: "Ministry of MSME",
                purpose: "Micro Enterprise Setup",
                maxAmountText: "₹50 Lakh",
                matchPercentage: Math.min(match3, 99),
                interestRate: "9.5% p.a.",
                subsidyText: "15-35%",
                reasons: ["Good for micro enterprise setup"],
                eligibilityStatus: "Eligible"
            }
        ];

        // Sort by match percentage descending
        schemes.sort((a, b) => b.matchPercentage - a.matchPercentage);

        res.status(200).json(schemes);
    } catch (error) {
        console.error('Recommendation error:', error);
        res.status(500).json({ message: 'Internal server error' });
    }
};
