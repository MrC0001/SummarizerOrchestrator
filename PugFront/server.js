import express from 'express';
import path, { dirname } from 'path';
import { fileURLToPath } from 'url';
import fetch from 'node-fetch';
import { body, validationResult } from 'express-validator';
import dotenv from 'dotenv';
import winston from 'winston';

// Initialize dotenv
dotenv.config();

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const app = express();
const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080';
const PORT = process.env.PORT || 3000;

// Configure Winston Logger
const logger = winston.createLogger({
    level: 'info',
    format: winston.format.combine(
        winston.format.timestamp(),
        winston.format.printf(({ timestamp, level, message }) => `[${timestamp}] ${level}: ${message}`)
    ),
    transports: [new winston.transports.Console()],
});

// Set Pug as the view engine
app.set('view engine', 'pug');
app.set('views', path.join(__dirname, 'views'));

// Middleware to parse URL-encoded bodies and JSON
app.use(express.urlencoded({ extended: true, limit: "10mb" }));
app.use(express.json({ limit: "10mb" }));

/**
 * Fetches transcripts from the backend.
 * @returns {Promise<Array>} Resolves to an array of transcripts.
 */
async function fetchTranscripts() {
    try {
        const response = await fetch(`${API_BASE_URL}/api/transcripts`);
        if (!response.ok) {
            logger.error(`Failed to fetch transcripts: ${response.status} ${response.statusText}`);
            return [];
        }
        return await response.json();
    } catch (error) {
        logger.error(`Error fetching transcripts: ${error.message}`);
        return [];
    }
}

/**
 * Main page: Displays the list of transcripts.
 */
app.get('/', async (req, res) => {
    try {
        const transcripts = await fetchTranscripts();
        res.render('index', { title: 'Summarize a Transcript', transcripts, error: null });
    } catch (error) {
        logger.error(`Error rendering main page: ${error.message}`);
        res.render('index', { title: 'Summarize a Transcript', transcripts: [], error: 'Failed to load transcripts.' });
    }
});

/**
 * New transcript page.
 */
app.get('/new', (req, res) => {
    res.render('new', { title: 'Create New Transcript', error: null });
});

/**
 * Handle creation of a new transcript.
 */
app.post(
    '/create',
    [
        body('scenario').notEmpty().withMessage('Scenario is required').trim(),
        body('transcript').notEmpty().withMessage('Transcript is required').trim(),
    ],
    async (req, res) => {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            logger.warn(`Validation errors: ${JSON.stringify(errors.array())}`);
            return res.render('new', { title: 'Create New Transcript', error: errors.array()[0].msg });
        }

        const { scenario, transcript } = req.body;

        try {
            const response = await fetch(`${API_BASE_URL}/api/transcripts`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ scenario, transcript }),
            });

            if (!response.ok) {
                logger.error(`Failed to create transcript: ${response.status} ${response.statusText}`);
                return res.render('new', { title: 'Create New Transcript', error: 'Failed to create transcript.' });
            }

            res.redirect('/');
        } catch (error) {
            logger.error(`Error creating transcript: ${error.message}`);
            res.render('new', { title: 'Create New Transcript', error: 'An error occurred.' });
        }
    }
);

/**
 * Update transcript page.
 */
app.get('/update', async (req, res) => {
    try {
        const transcripts = await fetchTranscripts();
        res.render('update', { title: 'Update Transcript', transcripts, error: null });
    } catch (error) {
        logger.error(`Error rendering update page: ${error.message}`);
        res.render('update', { title: 'Update Transcript', transcripts: [], error: 'Failed to load transcripts.' });
    }
});

/**
 * Handle updating a transcript.
 */
app.post(
    '/update',
    [
        body('id').notEmpty().withMessage('Transcript ID is required').isNumeric().withMessage('Invalid ID format'),
        body('scenario').notEmpty().withMessage('Scenario is required').trim(),
        body('transcript').notEmpty().withMessage('Transcript is required').trim(),
    ],
    async (req, res) => {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            logger.warn(`Validation errors: ${JSON.stringify(errors.array())}`);
            const transcripts = await fetchTranscripts();
            return res.render('update', { title: 'Update Transcript', transcripts, error: errors.array()[0].msg });
        }

        const { id, scenario, transcript } = req.body;

        try {
            const response = await fetch(`${API_BASE_URL}/api/transcripts/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ scenario, transcript }),
            });

            if (!response.ok) {
                logger.error(`Failed to update transcript: ${response.status} ${response.statusText}`);
                throw new Error('Failed to update transcript.');
            }

            res.redirect('/');
        } catch (error) {
            logger.error(`Error updating transcript: ${error.message}`);
            const transcripts = await fetchTranscripts();
            res.render('update', { title: 'Update Transcript', transcripts, error: error.message });
        }
    }
);

/**
 * Delete transcript page.
 */
app.get('/delete', async (req, res) => {
    try {
        const transcripts = await fetchTranscripts();
        res.render('delete', { title: 'Delete Transcript', transcripts, error: null });
    } catch (error) {
        logger.error(`Error rendering delete page: ${error.message}`);
        res.render('delete', { title: 'Delete Transcript', transcripts: [], error: 'Failed to load transcripts.' });
    }
});

/**
 * Handle deleting a transcript.
 */
app.post(
    '/delete',
    [body('id').notEmpty().withMessage('Transcript ID is required').isNumeric().withMessage('Invalid ID format')],
    async (req, res) => {
        const errors = validationResult(req);
        if (!errors.isEmpty()) {
            logger.warn(`Validation errors: ${JSON.stringify(errors.array())}`);
            const transcripts = await fetchTranscripts();
            return res.render('delete', { title: 'Delete Transcript', transcripts, error: errors.array()[0].msg });
        }

        const { id } = req.body;

        try {
            const response = await fetch(`${API_BASE_URL}/api/transcripts/${id}`, { method: 'DELETE' });

            if (!response.ok) {
                logger.error(`Failed to delete transcript: ${response.status} ${response.statusText}`);
                throw new Error('Failed to delete transcript.');
            }

            res.redirect('/');
        } catch (error) {
            logger.error(`Error deleting transcript: ${error.message}`);
            const transcripts = await fetchTranscripts();
            res.render('delete', { title: 'Delete Transcript', transcripts, error: error.message });
        }
    }
);

/**
 * Summarization page.
 */
app.post(
  '/summarizeAll',
  [
      body('transcriptSelect').notEmpty().withMessage('Transcript ID is required').isNumeric().withMessage('Invalid ID format'),
      body('transcript').notEmpty().withMessage('Transcript content is required'),
  ],
  async (req, res) => {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
          logger.warn(`Validation errors: ${JSON.stringify(errors.array())}`);
          const transcripts = await fetchTranscripts();
          return res.render('index', { title: 'Summarize a Transcript', transcripts, error: errors.array()[0].msg });
      }

      const { transcriptSelect, transcript } = req.body;

      const payload = {
          prompt: "Summarize the following conversation:",
          context: transcript,
          parameters: { max_tokens: 200, temperature: 0.7 },
          transcriptId: parseInt(transcriptSelect),
      };

      logger.info(`Summarization payload: ${JSON.stringify(payload)}`);

      try {
          const response = await fetch(`${API_BASE_URL}/api/summarize`, {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(payload),
          });

          if (!response.ok) {
              const errorDetails = await response.text();
              logger.error(`Summarization failed. Status: ${response.status}, Error: ${errorDetails}`);
              throw new Error('Summarization failed.');
          }

          const responseData = await response.json();
          const { newSummaries = [], oldSummaries = [] } = responseData;

          logger.info(`Summarization response: ${JSON.stringify(responseData)}`);

          res.render('results', {
              title: 'Summarization Results',
              newSummaries,
              oldSummaries,
              transcriptId: transcriptSelect,
          });
      } catch (error) {
          logger.error(`Error summarizing transcript: ${error.message}`);
          const transcripts = await fetchTranscripts();
          res.render('index', {
              title: 'Summarize a Transcript',
              transcripts,
              error: 'Summarization failed. Please try again.',
          });
      }
  }
);


/**
 * Displays summaries page with the list of transcripts.
 */
app.get('/summaries', async (req, res) => {
  try {
    const transcripts = await fetchTranscripts();
    res.render('summaries', { title: 'Stored Summaries', transcripts, summaries: null, error: null });
  } catch (error) {
    logger.error(`Error fetching transcripts for summaries page: ${error.message}`);
    res.render('summaries', { title: 'Stored Summaries', transcripts: [], summaries: null, error: 'Failed to fetch transcripts.' });
  }
});

/**
 * Displays summaries for a selected transcript.
 */
app.post(
  '/summaries/view',
  [body('transcriptId').notEmpty().withMessage('Transcript ID is required').isNumeric().withMessage('Invalid ID format')],
  async (req, res) => {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
          logger.warn(`Validation errors in /summaries/view: ${JSON.stringify(errors.array())}`);
          const transcripts = await fetchTranscripts();
          return res.render('summaries', { title: 'Stored Summaries', transcripts, summaries: null, error: errors.array()[0].msg });
      }

      const { transcriptId } = req.body;

      try {
          const response = await fetch(`${API_BASE_URL}/api/${transcriptId}`);
          if (!response.ok) {
              throw new Error('Failed to fetch transcript and summaries for the selected transcript.');
          }

          const data = await response.json();
          logger.info(`Fetched data for transcript ID ${transcriptId}: ${JSON.stringify(data)}`);

          const transcripts = await fetchTranscripts();
          res.render('summaries', { 
              title: 'Stored Summaries', 
              transcripts, 
              transcript: data.transcript, 
              summaries: data.summaries, 
              error: null 
          });
      } catch (error) {
          logger.error(`Error fetching transcript and summaries: ${error.message}`);
          const transcripts = await fetchTranscripts();
          res.render('summaries', { 
              title: 'Stored Summaries', 
              transcripts, 
              transcript: null, 
              summaries: null, 
              error: 'Failed to fetch transcript and summaries for the selected transcript.' 
          });
      }
  }
);


/**
 * Generates summaries for a selected transcript.
 */
app.post(
  '/summaries/generate',
  [body('transcriptId').notEmpty().withMessage('Transcript ID is required').isNumeric().withMessage('Invalid ID format')],
  async (req, res) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      logger.warn(`Validation errors in /summaries/generate: ${JSON.stringify(errors.array())}`);
      return res.redirect('/summaries?error=Validation failed');
    }

    const { transcriptId } = req.body;

    const transcripts = await fetchTranscripts();
    const transcript = transcripts.find((t) => t.id === parseInt(transcriptId))?.transcript;

    if (!transcript) {
      logger.warn(`Transcript not found for ID ${transcriptId}`);
      return res.redirect('/summaries?error=Transcript not found');
    }

    const payload = {
      prompt: "Summarize the following conversation:",
      context: transcript,
      parameters: { max_tokens: 200, temperature: 0.7 },
    };

    try {
      const response = await fetch(`${API_BASE_URL}/api/summarize`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        logger.error(`Summarization failed for transcript ID ${transcriptId}: Status ${response.status}`);
        throw new Error(`Java API returned status ${response.status}`);
      }

      const summaries = await response.json();
      logger.info(`Generated summaries for transcript ID ${transcriptId}`);
      res.render('summaries', { title: 'Summaries', summaries, transcriptId });
    } catch (error) {
      logger.error(`Error generating summaries: ${error.message}`);
      res.redirect(`/summaries?error=${encodeURIComponent(error.message)}`);
    }
  }
);

/**
 * Overwrites summaries for a selected transcript.
 */
app.post(
  '/summaries/overwrite',
  [
    body('transcriptId').notEmpty().withMessage('Transcript ID is required').isNumeric().withMessage('Invalid ID format'),
    body('newSummaries').notEmpty().withMessage('New summaries data is required'),
  ],
  async (req, res) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      logger.warn(`Validation errors in /summaries/overwrite: ${JSON.stringify(errors.array())}`);
      return res.redirect('/summaries?error=Validation failed');
    }

    const { transcriptId, newSummaries } = req.body;

    try {
      logger.info(`Received overwrite request for transcript ID ${transcriptId}`);

      // Parse newSummaries from JSON string to an array
      const parsedNewSummaries = JSON.parse(newSummaries);
      logger.info(`Parsed new summaries: ${JSON.stringify(parsedNewSummaries)}`);

      const response = await fetch(`${API_BASE_URL}/api/overwrite/${transcriptId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(parsedNewSummaries),
      });

      if (!response.ok) {
        const errorMessage = await response.text();
        logger.error(`Backend error on overwrite: ${errorMessage}`);
        throw new Error(`Backend error: ${errorMessage}`);
      }

      logger.info(`Successfully overwritten summaries for transcript ID ${transcriptId}`);
      res.redirect('/summaries');
    } catch (error) {
      logger.error(`Error overwriting summaries: ${error.message}`);
      res.redirect(`/summaries?error=${encodeURIComponent(error.message)}`);
    }
  }
);

// Start the server
app.listen(PORT, () => {
    logger.info(`Frontend server running at http://localhost:${PORT}`);
});
