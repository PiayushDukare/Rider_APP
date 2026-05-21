const express = require('express');
const router = express.Router();
const prisma = require('../db');

// POST /api/friends/request
router.post('/request', async (req, res) => {
    const { requesterId, addresseeId } = req.body;

    if (!requesterId || !addresseeId) return res.status(400).json({ error: "Missing IDs" });

    try {
        const request = await prisma.friendship.create({
            data: {
                requesterId,
                addresseeId,
                status: 'PENDING'
            }
        });
        res.json(request);
    } catch (error) {
        res.status(500).json({ error: "Failed to send friend request." });
    }
});

// POST /api/friends/accept
router.post('/accept', async (req, res) => {
    const { requesterId, addresseeId } = req.body;

    try {
        const friendship = await prisma.friendship.update({
            where: {
                requesterId_addresseeId: { requesterId, addresseeId }
            },
            data: { status: 'ACCEPTED' }
        });
        res.json(friendship);
    } catch (error) {
        res.status(500).json({ error: "Failed to accept request." });
    }
});

// GET /api/friends/list/:userId
router.get('/list/:userId', async (req, res) => {
    const { userId } = req.params;

    try {
        const friends = await prisma.friendship.findMany({
            where: {
                status: 'ACCEPTED',
                OR: [
                    { requesterId: userId },
                    { addresseeId: userId }
                ]
            },
            include: {
                requester: { select: { id: true, handle: true, displayName: true, bikeModel: true } },
                addressee: { select: { id: true, handle: true, displayName: true, bikeModel: true } }
            }
        });
        
        // Flatten list
        const friendList = friends.map(f => f.requesterId === userId ? f.addressee : f.requester);
        res.json(friendList);
    } catch (error) {
        res.status(500).json({ error: "Failed to fetch friends." });
    }
});

module.exports = router;
