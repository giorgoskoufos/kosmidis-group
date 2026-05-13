const express = require('express');
const router = express.Router();
const googleController = require('../controllers/googleController');
const auth = require('../middleware/auth');

// Note: /auth/google/callback is NOT protected by JWT as it is a 
// standard OAuth redirect from the browser/Google.
router.get('/auth/google', googleController.getAuthUrl);
router.get('/auth/google/callback', googleController.handleCallback);
router.get('/users/:userId/integrations/google/status', auth, googleController.getStatus);
router.post('/integrations/google/disconnect', auth, googleController.disconnect);

module.exports = router;
