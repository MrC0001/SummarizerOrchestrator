import express from 'express';
import { body, validationResult } from 'express-validator';
import logger from '../config/logger.js';
import {
  fetchTranscripts
} from '../services/transcriptService.js';
import {
  summarizeAll,
  fetchControlSummary,
  calculateMetrics,
  fetchTranscriptAndSummaries,
  overwriteSummaries
} from '../services/summaryService.js';

const router = express.Router();

/**
 * Summarizes the content of a transcript.
 */
router.post(
  '/summarizeAll',
  [
    body('transcriptSelect')
      .notEmpty()
      .withMessage('Transcript ID is required')
      .isNumeric()
      .withMessage('Invalid ID format'),
    body('transcript').notEmpty().withMessage('Transcript content is required'),
  ],
  async (req, res) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      logger.warn(`Validation errors: ${JSON.stringify(errors.array())}`);
      const transcripts = await fetchTranscripts();
      return res.render('index', {
        title: 'Summarize a Transcript',
        transcripts,
        error: errors.array()[0].msg,
      });
    }

    const { transcriptSelect, transcript } = req.body;
    const payload = {
      transcriptId: parseInt(transcriptSelect, 10),
      prompt: 'Summarize the following conversation:',
      context: transcript,
    };

    logger.info(`Generated summarization request payload: ${JSON.stringify(payload)}`);

    try {
      const response = await summarizeAll(payload);
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
 * Calculates metrics for a transcript.
 */
router.post(
  '/metrics/calculate',
  [body('transcriptId').notEmpty().withMessage('Transcript ID is required').isNumeric().withMessage('Invalid ID format')],
  async (req, res) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      logger.warn(`Validation errors in /metrics/calculate: ${JSON.stringify(errors.array())}`);
      const transcripts = await fetchTranscripts();
      return res.render('index', { title: 'Summarize a Transcript', transcripts, error: errors.array()[0].msg });
    }

    const { transcriptId } = req.body;
    logger.info(`Metrics calculation request for transcript ID: ${transcriptId}`);

    try {
      const controlSummaryResponse = await fetchControlSummary(transcriptId);
      if (!controlSummaryResponse.ok) {
        const errorMessage = await controlSummaryResponse.text();
        logger.error(`Failed to fetch control summary: ${errorMessage}`);
        throw new Error(`Failed to fetch control summary: ${errorMessage}`);
      }
      const controlSummaryData = await controlSummaryResponse.json();

      const payload = {
        transcriptId,
        controlSummary: controlSummaryData.summaryText,
      };

      const metricsResponse = await calculateMetrics(payload);
      if (!metricsResponse.ok) {
        const errorMessage = await metricsResponse.text();
        logger.error(`Backend error on metrics calculation: ${errorMessage}`);
        throw new Error(`Backend error: ${errorMessage}`);
      }

      logger.info(`Metrics successfully calculated for transcript ID: ${transcriptId}`);
      res.render('metrics-confirmation', { title: 'Metrics Calculation Completed' });
    } catch (error) {
      logger.error(`Error calculating metrics: ${error.message}`);
      const transcripts = await fetchTranscripts();
      res.render('index', {
        title: 'Summarize a Transcript',
        transcripts,
        error: 'Metrics calculation failed. Please try again.',
      });
    }
  }
);

/**
 * Renders summaries page with list of transcripts.
 */
router.get('/summaries', async (req, res) => {
  try {
    const transcripts = await fetchTranscripts();
    res.render('summaries', { title: 'Stored Summaries', transcripts, summaries: null, error: null });
  } catch (error) {
    logger.error(`Error fetching transcripts for summaries page: ${error.message}`);
    res.render('summaries', {
      title: 'Stored Summaries',
      transcripts: [],
      summaries: null,
      error: 'Failed to fetch transcripts.'
    });
  }
});

/**
 * Displays summaries for the selected transcript.
 */
router.post(
  '/summaries/view',
  [body('transcriptId').notEmpty().withMessage('Transcript ID is required').isNumeric().withMessage('Invalid ID format')],
  async (req, res) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      logger.warn(`Validation errors in /summaries/view: ${JSON.stringify(errors.array())}`);
      const transcripts = await fetchTranscripts();
      return res.render('summaries', {
        title: 'Stored Summaries',
        transcripts,
        summaries: null,
        error: errors.array()[0].msg
      });
    }

    const { transcriptId } = req.body;
    try {
      const response = await fetchTranscriptAndSummaries(transcriptId);
      if (!response.ok) {
        throw new Error('Failed to fetch transcript and summaries for the selected transcript.');
      }
      const data = await response.json();
      logger.info(`Fetched data for transcript ID ${transcriptId}: ${JSON.stringify(data)}`);

      const summariesWithMetrics = (data.summaries || []).map(summary => {
        summary.metrics = summary.metrics || {
          rouge1: 'N/A',
          rouge2: 'N/A',
          rougeL: 'N/A',
          bertPrecision: 'N/A',
          bertRecall: 'N/A',
          bertF1: 'N/A',
          bleu: 'N/A',
          meteor: 'N/A',
          lengthRatio: 'N/A',
          redundancy: 'N/A',
          createdAt: 'N/A'
        };
        summary.metricsJson = JSON.stringify(summary.metrics);
        return summary;
      });

      const transcripts = await fetchTranscripts();
      res.render('summaries', {
        title: 'Stored Summaries',
        transcripts,
        transcript: data.transcript,
        summaries: summariesWithMetrics,
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
 * Overwrites summaries for a selected transcript.
 */
router.post(
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
      const parsedNewSummaries = JSON.parse(newSummaries);
      logger.info(`Parsed new summaries: ${JSON.stringify(parsedNewSummaries)}`);

      const response = await overwriteSummaries(transcriptId, parsedNewSummaries);
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

export default router;