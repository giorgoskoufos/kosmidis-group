const { OpenAI } = require('openai');
require('dotenv').config();

const openai = new OpenAI({
    apiKey: process.env.AI_API_KEY, 
});

module.exports = openai;
