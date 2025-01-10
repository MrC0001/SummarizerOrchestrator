import express from 'express';
import path, { dirname } from 'path';
import { fileURLToPath } from 'url';
import dotenv from 'dotenv';
import logger from './config/logger.js';
import transcriptRoutes from './routes/transcriptRoutes.js';
import summaryRoutes from './routes/summaryRoutes.js';

/**
 * Load environment variables from .env file.
 */
dotenv.config();

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const app = express();
const PORT = process.env.PORT || 3000;

// Set Pug as the view engine
app.set('view engine', 'pug');
app.set('views', path.join(__dirname, 'views'));

// Middleware to parse URL-encoded bodies and JSON
app.use(express.static('public'));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));
app.use(express.json({ limit: '10mb' }));

/**
 * Main application routes.
 */
app.use('/', transcriptRoutes);
app.use('/', summaryRoutes);

/**
 * Global error handler. 
 * Catches unhandled errors and returns a generic message in production.
 */
app.use((err, req, res, next) => {
  logger.error(`Unhandled error: ${err.message}`);
  const message = process.env.NODE_ENV === 'production'
    ? 'An internal server error occurred'
    : err.message;
  res.status(500).render('error', { error: message });
});

/**
 * Start the Express server.
 */
app.listen(PORT, () => {
  logger.info(`Frontend server running at http://localhost:${PORT}`);
});