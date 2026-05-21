const express = require('express');
const { PrismaClient } = require('@prisma/client');

const router = express.Router();
;

// POST /api/rides/sync
// Receives compressed ride JSON and saves to the database
router.post('/sync', async (req, res) => {
    try {
        const { riderId, roomName, startTime, endTime, distanceKm, privacyState, routeJson, events } = req.body;

        if (!riderId || !startTime) {
            return res.status(400).json({ error: 'Missing required fields' });
        }

        // Create the Ride Session
        const ride = await prisma.rideSession.create({
            data: {
                riderId,
                startTime: new Date(startTime),
                endTime: endTime ? new Date(endTime) : null,
                distanceKm: distanceKm || 0,
                privacyState: privacyState || 'PRIVATE',
                routeJson: routeJson || null
            }
        });

        // Create the Convoy Events
        if (events && Array.isArray(events) && events.length > 0) {
            const eventPayload = events.map(e => ({
                rideId: ride.id,
                type: e.eventType,
                lat: e.lat,
                lng: e.lng,
                timestamp: new Date(e.timestamp)
            }));
            
            await prisma.convoyEvent.createMany({
                data: eventPayload
            });
        }

        res.status(201).json({ success: true, rideId: ride.id });

    } catch (error) {
        console.error('Ride Sync Error:', error);
        res.status(500).json({ error: 'Internal server error during ride sync' });
    }
});

module.exports = router;
