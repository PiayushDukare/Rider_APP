const { AccessToken } = require('livekit-server-sdk')

async function createToken(roomName, participantName) {

    if (!process.env.LIVEKIT_API_KEY || !process.env.LIVEKIT_API_SECRET) {
        throw new Error('LIVEKIT_API_KEY and LIVEKIT_API_SECRET must be set')
    }

    const at = new AccessToken(
        process.env.LIVEKIT_API_KEY,
        process.env.LIVEKIT_API_SECRET,
        {
            identity: participantName,
            ttl: '6h'
        }
    )

    at.addGrant({
        roomJoin: true,
        room: roomName,
        canPublish: true,
        canSubscribe: true
    })

    return await at.toJwt()
}

module.exports = { createToken }
