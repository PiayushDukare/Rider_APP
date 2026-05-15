const express = require('express')
const router = express.Router()
const { createToken } = require('../services/tokenService')

router.get('/token', async (req, res) => {

    try {
        const { room, user } = req.query

        // Validate required params
        if (!room || !user) {
            return res.status(400).json({
                error: 'Missing required query params: room and user'
            })
        }

        if (room.trim() === '' || user.trim() === '') {
            return res.status(400).json({
                error: 'room and user must not be empty'
            })
        }

        const token = await createToken(room.trim(), user.trim())

        res.json({
            roomName: room.trim(),
            token
        })
    } catch (error) {
        console.error('Token generation error:', error)
        res.status(500).json({ error: error.message })
    }
})

module.exports = router
