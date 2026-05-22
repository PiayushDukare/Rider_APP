const admin = require('../config/firebaseAdmin')

module.exports = async (req, res, next) => {
    const authHeader = req.headers.authorization

    if (!authHeader) {
        return res.status(401).json({ error: 'Unauthorized: missing Authorization header' })
    }

    const parts = authHeader.split(' ')
    if (parts.length !== 2 || parts[0] !== 'Bearer') {
        return res.status(401).json({ error: 'Unauthorized: malformed Authorization header' })
    }

    const token = parts[1]

    try {
        const decodedToken = await admin.auth().verifyIdToken(token)
        req.user = decodedToken // Inject user info into the request
        next()
    } catch (error) {
        console.error('Firebase Auth Error:', error.message)
        return res.status(401).json({ error: 'Unauthorized: invalid Firebase token' })
    }
}
