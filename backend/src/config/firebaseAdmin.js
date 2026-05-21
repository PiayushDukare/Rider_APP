const admin = require('firebase-admin');
const path = require('path');

// Initialize with the downloaded service account key
const serviceAccount = require('../../riderapp-5ce68-firebase-adminsdk-fbsvc-7c77e002fa.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

module.exports = admin;
