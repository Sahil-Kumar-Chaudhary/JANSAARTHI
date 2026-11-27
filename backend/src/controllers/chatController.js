
const RAG_API_URL = process.env.RAG_API_URL || 'http://127.0.0.1:8000';

exports.chat = async (req, res) => {
    try {
        const { question } = req.body;

        if (typeof question !== 'string' || !question.trim()) {
            return res.status(400).json({
                message: 'Please provide a question.'
            });
        }

        const response = await fetch(`${RAG_API_URL}/chat`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                question: question.trim()
            }),
            signal: AbortSignal.timeout(60000)
        });

        if (!response.ok) {
            console.error('RAG API returned status:', response.status);
            return res.status(502).json({
                message: 'The AI service could not process your question.'
            });
        }

        const data = await response.json();

        return res.status(200).json({
            answer: data.answer,
            sources: data.sources || []
        });
    } catch (error) {
        console.error('Chat error:', error.message);

        return res.status(502).json({
            message: 'Unable to reach the AI service. Please try again.'
        });
    }
};
