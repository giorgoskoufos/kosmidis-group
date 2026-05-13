const express = require('express');
const router = express.Router();
const profileController = require('../controllers/profileController');
const auth = require('../middleware/auth');

router.get('/profile/:userId', auth, profileController.getProfile);
router.post('/profile', auth, profileController.updateProfile);

module.exports = router;
