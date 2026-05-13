const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');

// @route   POST api/register
// @desc    Register a new user
router.post('/register', authController.register);

// @route   POST api/login
// @desc    Login user & get token
router.post('/login', authController.login);

module.exports = router;
