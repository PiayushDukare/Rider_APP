const express = require('express')
const router = express.Router()
const prisma = require('../db')
const { AccessToken } = require('livekit-server-sdk')

/**
 * POST /api/lobby/create
 * Host creates a named convoy with trip details.
 * Returns the room record. LiveKit connection does NOT happen here —
 * it only happens when the host taps "Start Ride".
 */
router.post('/create', async (req, res, next) => {
    const hostId = req.user.uid
    const { convoyName, origin, destination, estimatedDurationMin, meetupPoint } = req.body

    if (!convoyName) {
        return res.status(400).json({ error: 'convoyName is required' })
    }

    try {
        // Room name is the convoy name — must be unique
        const existing = await prisma.room.findUnique({ where: { name: convoyName } })
        if (existing) {
            return res.status(409).json({ error: 'A convoy with that name already exists' })
        }

        const room = await prisma.room.create({
            data: {
                name: convoyName,
                ownerId: hostId,
                // Store trip metadata in a JSON blob on the room
                // (add tripMeta Json? to schema, or encode in name for now)
            }
        })

        res.status(201).json({
            roomId: room.id,
            convoyName: room.name,
            hostId,
        })
    } catch (error) {
        next(error)
    }
})

/**
 * GET /api/lobby/:roomName/status
 * Host polls this to see which invitees have accepted / declined / are pending.
 * Only the room owner can see this.
 */
router.get('/:roomName/status', async (req, res, next) => {
    const { roomName } = req.params
    const requesterId = req.user.uid

    try {
        const room = await prisma.room.findUnique({
            where: { name: roomName },
            include: {
                invites: {
                    include: {
                        invitee: {
                            select: { id: true, handle: true, displayName: true, bikeModel: true }
                        }
                    }
                }
            }
        })

        if (!room) return res.status(404).json({ error: 'Room not found' })

        // Only the host sees the full lobby status
        if (room.ownerId !== requesterId) {
            return res.status(403).json({ error: 'Only the host can view lobby status' })
        }

        const summary = {
            roomId: room.id,
            convoyName: room.name,
            invites: room.invites.map(inv => ({
                inviteId: inv.id,
                status: inv.status,
                invitee: inv.invitee,
            })),
            acceptedCount: room.invites.filter(i => i.status === 'ACCEPTED').length,
            pendingCount:  room.invites.filter(i => i.status === 'PENDING').length,
            declinedCount: room.invites.filter(i => i.status === 'DECLINED').length,
            canStart: room.invites.some(i => i.status === 'ACCEPTED'),
        }

        res.json(summary)
    } catch (error) {
        next(error)
    }
})

/**
 * POST /api/lobby/:roomName/start
 * Host taps "Start Ride". THIS is where LiveKit token is generated.
 * At least one accepted invite is required.
 */
router.post('/:roomName/start', async (req, res, next) => {
    const { roomName } = req.params
    const user = req.user

    try {
        const room = await prisma.room.findUnique({
            where: { name: roomName },
            include: {
                invites: { where: { status: 'ACCEPTED' } }
            }
        })

        if (!room) return res.status(404).json({ error: 'Room not found' })
        if (room.ownerId !== user.uid) return res.status(403).json({ error: 'Only the host can start the ride' })
        if (room.invites.length === 0) return res.status(400).json({ error: 'At least one rider must accept before starting' })

        // Generate LiveKit token for the host
        const at = new AccessToken(
            process.env.LIVEKIT_API_KEY,
            process.env.LIVEKIT_API_SECRET,
            { identity: user.uid, name: user.name || 'Host', ttl: '6h' }
        )
        at.addGrant({ roomJoin: true, room: roomName, canPublish: true, canSubscribe: true })
        const token = await at.toJwt()

        res.json({ token, roomName, livekitUrl: process.env.LIVEKIT_URL })
    } catch (error) {
        next(error)
    }
})

/**
 * POST /api/lobby/join-token
 * Joiner calls this AFTER accepting an invite.
 * Validates the invite is ACCEPTED before issuing a LiveKit token.
 */
router.post('/join-token', async (req, res, next) => {
    const { roomName } = req.body
    const user = req.user

    if (!roomName) return res.status(400).json({ error: 'roomName is required' })

    try {
        // Verify this user has an accepted invite to the room
        const room = await prisma.room.findUnique({ where: { name: roomName } })
        if (!room) return res.status(404).json({ error: 'Room not found' })

        const invite = await prisma.rideInvite.findFirst({
            where: {
                roomId: room.id,
                inviteeId: user.uid,
                status: 'ACCEPTED'
            }
        })

        // Also allow the host to get a token via this endpoint
        const isHost = room.ownerId === user.uid

        if (!invite && !isHost) {
            return res.status(403).json({ error: 'No accepted invite found for this room' })
        }

        const at = new AccessToken(
            process.env.LIVEKIT_API_KEY,
            process.env.LIVEKIT_API_SECRET,
            { identity: user.uid, name: user.name || 'Rider', ttl: '6h' }
        )
        at.addGrant({ roomJoin: true, room: roomName, canPublish: true, canSubscribe: true })
        const token = await at.toJwt()

        res.json({ token, roomName, livekitUrl: process.env.LIVEKIT_URL })
    } catch (error) {
        next(error)
    }
})

module.exports = router
