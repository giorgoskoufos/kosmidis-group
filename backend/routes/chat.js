const express = require('express');
const router = express.Router();
const multer = require('multer');

const chatController = require('../controllers/chatController');
const auth = require('../middleware/auth');

const upload = multer({ storage: multer.memoryStorage() });

// All chat routes are protected by JWT
router.get('/conversations/:userId', auth, chatController.getConversations);
router.post('/conversations', auth, chatController.createConversation);
router.get('/messages/:conversationId', auth, chatController.getMessages);
router.post('/chat', auth, chatController.sendMessage);

// NEW VISION ROUTE
router.post(
    '/chat/vision',
    auth,
    upload.array('images', 5),
    chatController.sendVisionMessage
);

router.post(
    '/chat/files',
    auth,
    upload.array('files', 2),
    chatController.sendFileMessage
);

module.exports = router;