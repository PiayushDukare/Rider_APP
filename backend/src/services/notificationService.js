const admin = require('../config/firebaseAdmin');
const prisma = require('../db');

class NotificationService {
    async sendToUser(userId, payload) {
        try {
            const tokens = await prisma.deviceToken.findMany({
                where: { userId }
            });

            if (tokens.length === 0) return;

            const messages = tokens.map(t => ({
                token: t.token,
                data: payload.data, // Custom data payload for background handling
                android: {
                    priority: 'high',
                }
            }));

            const response = await admin.messaging().sendEach(messages);
            console.log('FCM Dispatch:', response.successCount, 'successes');
            
            // Clean up invalid tokens
            response.responses.forEach(async (res, idx) => {
                if (!res.success && res.error.code === 'messaging/registration-token-not-registered') {
                    await prisma.deviceToken.delete({ where: { token: messages[idx].token } });
                }
            });
        } catch (error) {
            console.error('FCM Send Error:', error);
        }
    }

    async sendRideInvite(inviterHandle, inviteeId, roomName) {
        // Send a data-only message so the Android app can trigger a Full-Screen Intent
        await this.sendToUser(inviteeId, {
            data: {
                type: 'RIDE_INVITE',
                inviterHandle,
                roomName,
                channelId: 'CHANNEL_CONVOY'
            }
        });
    }

    async sendEmergencyAlert(userId, alertType) {
        // Stub for future IMU implementation. Will broadcast to squad.
        console.log(`EMERGENCY: ${alertType} for user ${userId}`);
    }
}

module.exports = new NotificationService();
