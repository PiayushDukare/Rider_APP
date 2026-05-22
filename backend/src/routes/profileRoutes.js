const express = require('express');
const router = express.Router();
const prisma = require('../db');

// POST /api/users/profile - Create or Update Rider Profile
router.post('/profile', async (req, res) => {
    const { id, handle, displayName, bikeModel, bio, email, phone } = req.body;

    if (!id) {
        return res.status(400).json({ error: "Missing Firebase UID (id)" });
    }

    if (req.user && req.user.uid !== id) {
        return res.status(403).json({ error: "Forbidden: Cannot modify another user's profile" });
    }

    try {
        const user = await prisma.user.upsert({
            where: { id },
            update: {
                handle,
                displayName,
                bikeModel,
                bio,
                ...(email && { email }),
                ...(phone && { phone })
            },
            create: {
                id,
                handle,
                displayName,
                bikeModel,
                bio,
                email,
                phone
            }
        });
        res.json(user);
    } catch (error) {
        console.error("Profile Upsert Error:", error);
        res.status(500).json({ error: "Database error updating profile." });
    }
});

// GET /api/users/search?handle=... - Search for a rider
router.get('/search', async (req, res) => {
    const { handle } = req.query;

    if (!handle) {
        return res.status(400).json({ error: "Handle query parameter is required." });
    }

    try {
        const user = await prisma.user.findUnique({
            where: { handle },
            select: {
                id: true,
                handle: true,
                displayName: true,
                bikeModel: true,
                bio: true
            } // NEVER return email/phone for privacy
        });

        if (!user) {
            return res.status(404).json({ error: "Rider not found." });
        }
        res.json(user);
    } catch (error) {
        res.status(500).json({ error: "Search failed." });
    }
});

// POST /api/users/fcm-token
router.post('/fcm-token', async (req, res) => {
    const { userId, token, platform = 'android' } = req.body;

    if (!userId || !token) {
        return res.status(400).json({ error: "Missing userId or token" });
    }

    if (req.user && req.user.uid !== userId) {
        return res.status(403).json({ error: "Forbidden: Cannot register token for another user" });
    }

    try {
        const device = await prisma.deviceToken.upsert({
            where: { token },
            update: { userId, platform, updatedAt: new Date() },
            create: { userId, token, platform }
        });
        res.json(device);
    } catch (error) {
        console.error("Token save error:", error);
        res.status(500).json({ error: "Failed to save FCM token" });
    }
});

module.exports = router;
