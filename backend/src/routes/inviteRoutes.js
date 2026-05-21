const express = require('express');
const router = express.Router();
const prisma = require('../db');
const notificationService = require('../services/notificationService');

// POST /api/rides/invite
router.post('/invite', async (req, res) => {
    const { roomId, inviterId, inviteeId } = req.body;

    if (!roomId || !inviterId || !inviteeId) return res.status(400).json({ error: "Missing fields." });

    try {
        // 1. Check if they are friends
        const isFriend = await prisma.friendship.findFirst({
            where: {
                status: 'ACCEPTED',
                OR: [
                    { requesterId: inviterId, addresseeId: inviteeId },
                    { requesterId: inviteeId, addresseeId: inviterId }
                ]
            }
        });

        if (!isFriend) {
            return res.status(403).json({ error: "Cannot invite non-friends. Trust boundary violation." });
        }

        // 2. Create Invite
        const invite = await prisma.rideInvite.create({
            data: {
                roomId,
                inviterId,
                inviteeId,
                status: 'PENDING'
            }
        });
        
        // 3. Get Inviter details for the push notification
        const inviter = await prisma.user.findUnique({ where: { id: inviterId } });
        const room = await prisma.room.findUnique({ where: { id: roomId } });

        // 4. Trigger FCM Push Notification!
        if (inviter && room) {
            await notificationService.sendRideInvite(inviter.handle || inviter.displayName, inviteeId, room.name);
        }

        res.json(invite);
    } catch (error) {
        res.status(500).json({ error: "Failed to dispatch invite." });
    }
});

// GET /api/rides/invites/:userId
router.get('/invites/:userId', async (req, res) => {
    const { userId } = req.params;

    try {
        const invites = await prisma.rideInvite.findMany({
            where: { inviteeId: userId, status: 'PENDING' },
            include: {
                inviter: { select: { handle: true, displayName: true } },
                room: { select: { name: true } }
            }
        });
        res.json(invites);
    } catch (error) {
        res.status(500).json({ error: "Failed to fetch invites." });
    }
});

module.exports = router;
