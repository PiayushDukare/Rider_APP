const admin  = require('../config/firebaseAdmin')
const prisma  = require('../db')

class NotificationService {

    /**
     * Sends a data-only FCM message to all registered devices for a user.
     * Cleans up invalid/expired tokens after sending.
     */
    async sendToUser(userId, payload) {
        try {
            const tokens = await prisma.deviceToken.findMany({ where: { userId } })
            if (tokens.length === 0) return

            const messages = tokens.map(t => ({
                token:   t.token,
                data:    payload.data,
                android: { priority: 'high' }
            }))

            const response = await admin.messaging().sendEach(messages)
            console.log(`FCM: ${response.successCount}/${messages.length} delivered to ${userId}`)

            // ── Token cleanup ─────────────────────────────────────────────────
            // BUG FIX 1: forEach with async was fire-and-forget — use for...of instead
            // BUG FIX 2: response.error can be undefined on successful sends — guard it
            const staleTokens = []
            for (let i = 0; i < response.responses.length; i++) {
                const r = response.responses[i]
                if (!r.success && r.error) {
                    const code = r.error.code ?? ''
                    if (
                        code === 'messaging/registration-token-not-registered' ||
                        code === 'messaging/invalid-registration-token'
                    ) {
                        staleTokens.push(messages[i].token)
                    }
                }
            }

            if (staleTokens.length > 0) {
                await prisma.deviceToken.deleteMany({
                    where: { token: { in: staleTokens } }
                })
                console.log(`FCM: removed ${staleTokens.length} stale token(s) for ${userId}`)
            }

        } catch (error) {
            console.error('FCM sendToUser error:', error.message)
        }
    }

    /**
     * Sends a data-only ride invite push so the Android app can fire a
     * Full-Screen Intent (lock screen takeover) via TacticalMessagingService.
     */
    async sendRideInvite(inviterHandle, inviteeId, roomName) {
        await this.sendToUser(inviteeId, {
            data: {
                type:          'RIDE_INVITE',
                inviterHandle: String(inviterHandle),
                roomName:      String(roomName),
                channelId:     'CHANNEL_CONVOY'
            }
        })
    }

    /**
     * Broadcasts an emergency alert to a list of userIds (the squad).
     */
    async sendEmergencyAlert(userIds, alertType, lat, lng) {
        const payload = {
            data: {
                type:      'EMERGENCY',
                alertType: String(alertType),
                lat:       String(lat  ?? ''),
                lng:       String(lng  ?? ''),
                channelId: 'CHANNEL_EMERGENCY'
            }
        }
        // Send to all squad members in parallel
        await Promise.allSettled(
            userIds.map(uid => this.sendToUser(uid, payload))
        )
    }
}

module.exports = new NotificationService()
