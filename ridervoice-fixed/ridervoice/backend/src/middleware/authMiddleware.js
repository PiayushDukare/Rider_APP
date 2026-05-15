module.exports = (req, res, next) => {

    const authHeader = req.headers.authorization

    if (!authHeader) {
        return res.status(401).json({ error: 'Unauthorized: missing Authorization header' })
    }

    // Expect: Authorization: Bearer <API_SECRET>
    const parts = authHeader.split(' ')
    if (parts.length !== 2 || parts[0] !== 'Bearer') {
        return res.status(401).json({ error: 'Unauthorized: malformed Authorization header' })
    }

    const token = parts[1]
    const expectedSecret = process.env.API_SECRET

    if (!expectedSecret) {
        console.error('API_SECRET env var is not set')
        return res.status(500).json({ error: 'Server configuration error' })
    }

    if (token !== expectedSecret) {
        return res.status(401).json({ error: 'Unauthorized: invalid token' })
    }

    next()
}
