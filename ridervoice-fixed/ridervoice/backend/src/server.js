require('dotenv').config()

const express = require('express')
const cors = require('cors')

const roomRoutes = require('./routes/roomRoutes')
const healthRoute = require('./routes/healthRoute')
const authMiddleware = require('./middleware/authMiddleware')
const rateLimiter = require('./middleware/rateLimiter')
const errorMiddleware = require('./middleware/errorMiddleware')

const app = express()

app.use(cors())
app.use(express.json())

// Rate limiter applied globally
app.use(rateLimiter)

// Health check - no auth needed
app.use('/', healthRoute)

// All room routes require auth
app.use('/', authMiddleware, roomRoutes)

// Global error handler (must be last)
app.use(errorMiddleware)

const PORT = process.env.PORT || 3000

app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`)
})
