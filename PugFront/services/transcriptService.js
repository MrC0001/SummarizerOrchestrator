import fetch from 'node-fetch';
import logger from '../config/logger.js';

const API_BASE_URL = process.env.API_BASE_URL || 'http://localhost:8080';

/**
 * Fetches all transcripts from the backend service.
 * @returns {Promise<Array>} A promise resolving to a list of transcripts.
 */
export async function fetchTranscripts() {
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
 * Creates a new transcript in the backend service.
 * @param {string} scenario The transcript scenario/context.
 * @param {string} transcript The transcript textual content.
 * @returns {Promise<Response>} The backend response.
 */
export async function createTranscript(scenario, transcript) {
  return fetch(`${API_BASE_URL}/api/transcripts`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ scenario, transcript })
  });
}

/**
 * Updates an existing transcript in the backend service.
 * @param {number|string} id The ID of the transcript to update.
 * @param {string} scenario Updated scenario text.
 * @param {string} transcript Updated transcript text.
 * @returns {Promise<Response>} The backend response.
 */
export async function updateTranscript(id, scenario, transcript) {
  return fetch(`${API_BASE_URL}/api/transcripts/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ scenario, transcript })
  });
}

/**
 * Deletes an existing transcript in the backend service.
 * @param {number|string} id The transcript ID to delete.
 * @returns {Promise<Response>} The backend response.
 */
export async function deleteTranscript(id) {
  return fetch(`${API_BASE_URL}/api/transcripts/${id}`, { method: 'DELETE' });
}