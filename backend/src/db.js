const { PrismaClient } = require('@prisma/client');

// Use a singleton to prevent connection exhaustion in dev
const prisma = new PrismaClient();

module.exports = prisma;
