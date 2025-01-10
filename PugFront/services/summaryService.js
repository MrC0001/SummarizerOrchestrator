import fetch from 'node-fetch';
import logger from '../config/logger.js';

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080';

/**
 * Sends a summarize request to the backend.
 * @param {Object} payload Summarization payload containing transcriptId, prompt, context, etc.
 * @returns {Promise<Response>} The backend response.
 */
export async function summarizeAll(payload) {
  return fetch(`${API_BASE_URL}/api/summarizeAll`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
}

/**
 * Retrieves control summary for a given transcript.
 * @param {number|string} transcriptId The transcript ID.
 * @returns {Promise<Response>} The backend response.
 */
export async function fetchControlSummary(transcriptId) {
  return fetch(`${API_BASE_URL}/api/metrics/control-summary/${transcriptId}`);
}

/**
 * Calculates metrics by sending a POST request to the backend.
 * @param {Object} payload Contains transcriptId and controlSummary.
 * @returns {Promise<Response>} The backend response.
 */
export async function calculateMetrics(payload) {
  return fetch(`${API_BASE_URL}/api/metrics`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
}

/**
 * Fetches transcript and its summaries from the backend.
 * @param {number|string} transcriptId The transcript ID.
 * @returns {Promise<Response>} The backend response.
 */
export async function fetchTranscriptAndSummaries(transcriptId) {
  return fetch(`${API_BASE_URL}/api/${transcriptId}`);
}

/**
 * Overwrites existing summaries with the given data.
 * @param {number|string} transcriptId The transcript ID.
 * @param {Object[]} newSummaries New summaries to overwrite existing ones.
 * @returns {Promise<Response>} The backend response.
 */
export async function overwriteSummaries(transcriptId, newSummaries) {
  return fetch(`${API_BASE_URL}/api/overwrite/${transcriptId}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(newSummaries),
  });
}