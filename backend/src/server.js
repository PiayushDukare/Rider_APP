require('dotenv').config()

const express = require('express')
const cors = require('cors')

const roomRoutes = require('./routes/roomRoutes')
const healthRoute = require('./routes/healthRoute')
const authMiddleware = require('./middleware/authMiddleware')
const rateLimiter = require('./middleware/rateLimiter')
const errorMiddleware = require('./middleware/errorMiddleware')
const profileRoutes = require('./routes/profileRoutes');
const friendRoutes = require('./routes/friendRoutes');
const inviteRoutes = require('./routes/inviteRoutes');
const rideRoutes = require('./routes/rideRoutes');

const app = express()

app.use(cors())
app.use(express.json({ limit: '10mb' }))

// Rate limiter applied globally BEFORE routes
app.use(rateLimiter)

// Routes
app.use('/api/health', healthRoute);
app.use('/api/rooms', authMiddleware, roomRoutes);
app.use('/api/users', authMiddleware, profileRoutes);
app.use('/api/friends', authMiddleware, friendRoutes);
app.use('/api/rides', authMiddleware, rideRoutes);
app.use('/api/invites', authMiddleware, inviteRoutes);

// Health check - no auth needed
app.use('/', healthRoute)

// Global error handler (must be last)
app.use(errorMiddleware)

const PORT = process.env.PORT || 3000

app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`)
})
