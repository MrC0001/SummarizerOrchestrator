import express from 'express';
import { body, validationResult } from 'express-validator';
import logger from '../config/logger.js';
import {
  fetchTranscripts,
  createTranscript,
  updateTranscript,
  deleteTranscript
} from '../services/transcriptService.js';

const router = express.Router();

/**
 * Renders the main page with a list of transcripts.
 */
router.get('/', async (req, res) => {
  try {
    const transcripts = await fetchTranscripts();
    res.render('index', { title: 'Summarize a Transcript', transcripts, error: null });
  } catch (error) {
    logger.error(`Error rendering main page: ${error.message}`);
    res.render('index', { title: 'Summarize a Transcript', transcripts: [], error: 'Failed to load transcripts.' });
  }
});

/**
 * Renders page to create a new transcript.
 */
router.get('/new', (req, res) => {
  res.render('new', { title: 'Create New Transcript', error: null });
});

/**
 * Handle creation of a new transcript.
 */
router.post(
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
      const response = await createTranscript(scenario, transcript);

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
 * Renders page to update a transcript, listing available transcripts.
 */
router.get('/update', async (req, res) => {
  try {
    const transcripts = await fetchTranscripts();
    res.render('update', { title: 'Update Transcript', transcripts, error: null });
  } catch (error) {
    logger.error(`Error rendering update page: ${error.message}`);
    res.render('update', { title: 'Update Transcript', transcripts: [], error: 'Failed to load transcripts.' });
  }
});

/**
 * Handles updating a transcript.
 */
router.post(
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
      const response = await updateTranscript(id, scenario, transcript);
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
 * Renders page to delete a transcript, listing available transcripts.
 */
router.get('/delete', async (req, res) => {
  try {
    const transcripts = await fetchTranscripts();
    res.render('delete', { title: 'Delete Transcript', transcripts, error: null });
  } catch (error) {
    logger.error(`Error rendering delete page: ${error.message}`);
    res.render('delete', { title: 'Delete Transcript', transcripts: [], error: 'Failed to load transcripts.' });
  }
});

/**
 * Handles deleting a transcript.
 */
router.post(
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
      const response = await deleteTranscript(id);
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

export default router;