// server.js

import express from 'express';
import path, { dirname } from 'path';
import { fileURLToPath } from 'url';
import fetch from 'node-fetch';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const app = express();

// Set Pug as the view engine
app.set('view engine', 'pug');
app.set('views', path.join(__dirname, 'views'));

// Middleware to parse URL-encoded bodies and JSON
app.use(express.urlencoded({ extended: true }));
app.use(express.json());

// Helper function to fetch transcripts from the Java backend
async function fetchTranscripts() {
  try {
    const response = await fetch('http://localhost:8080/api/transcripts');
    if (!response.ok) {
      console.error('Failed to fetch transcripts:', response.status, response.statusText);
      return [];
    }
    const transcripts = await response.json();
    return transcripts;
  } catch (error) {
    console.error('Error fetching transcripts:', error);
    return [];
  }
}

// Main page - Display list of transcripts
app.get('/', async (req, res) => {
  const transcripts = await fetchTranscripts();
  res.render('index', { title: 'Summarize a Transcript', transcripts: transcripts, error: null });
});

// New transcript page
app.get('/new', (req, res) => {
  res.render('new', { title: 'Create New Transcript', error: null });
});

// Handle creation of new transcript
app.post('/create', async (req, res) => {
  const dto = {
    scenario: req.body.scenario,
    transcript: req.body.transcript
  };

  try {
    const response = await fetch('http://localhost:8080/api/transcripts', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(dto)
    });

    if (!response.ok) {
      console.error('Failed to create transcript:', response.status, response.statusText);
      return res.render('new', { title: 'Create New Transcript', error: 'Failed to create transcript. Please try again.' });
    }

    res.redirect('/');
  } catch (error) {
    console.error('Error creating transcript:', error);
    res.render('new', { title: 'Create New Transcript', error: 'An error occurred while creating the transcript. Please try again.' });
  }
});

// Update transcript page
app.get('/update', async (req, res) => {
  const transcripts = await fetchTranscripts();
  res.render('update', { title: 'Update Transcript', transcripts: transcripts, error: null });
});

// Handle updating a transcript
app.post('/update', async (req, res) => {
  const { id, scenario, transcript } = req.body;

  try {
    const response = await fetch(`http://localhost:8080/api/transcripts/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ scenario, transcript })
    });

    if (!response.ok) {
      console.error('Failed to update transcript:', response.status, response.statusText);
      const t = await fetchTranscripts();
      return res.render('update', { title: 'Update Transcript', transcripts: t, error: 'Failed to update transcript. Please try again.' });
    }

    res.redirect('/');
  } catch (error) {
    console.error('Error updating transcript:', error);
    const t = await fetchTranscripts();
    res.render('update', { title: 'Update Transcript', transcripts: t, error: 'An error occurred while updating the transcript.' });
  }
});

// Delete transcript page
app.get('/delete', async (req, res) => {
  const transcripts = await fetchTranscripts();
  res.render('delete', { title: 'Delete Transcript', transcripts: transcripts, error: null });
});

// Handle deleting a transcript
app.post('/delete', async (req, res) => {
  const { id } = req.body;

  try {
    const response = await fetch(`http://localhost:8080/api/transcripts/${id}`, {
      method: 'DELETE'
    });

    if (!response.ok) {
      console.error('Failed to delete transcript:', response.status, response.statusText);
      const t = await fetchTranscripts();
      return res.render('delete', { title: 'Delete Transcript', transcripts: t, error: 'Failed to delete transcript. Please try again.' });
    }

    res.redirect('/');
  } catch (error) {
    console.error('Error deleting transcript:', error);
    const t = await fetchTranscripts();
    res.render('delete', { title: 'Delete Transcript', transcripts: t, error: 'An error occurred while deleting the transcript.' });
  }
});

// Summarize transcript
app.post('/summarizeAll', async (req, res) => {
  const transcript = req.body.transcript;

  // Construct the JSON payload for the Java API
  const payload = {
    prompt: "Summarize the following conversation:",
    context: transcript,
    parameters: {
      max_tokens: 200,
      temperature: 0.7
    }
  };

  try {
    const response = await fetch('http://localhost:8080/api/summarize', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      throw new Error(`Java API returned status ${response.status}`);
    }

    const summaries = await response.json();
    // summaries is an array of objects: [{ providerName: "...", summary: "..." }, ...]

    // Render results.pug with summaries data
    res.render('results', { title: 'Summarization Results', summaries: summaries });
  } catch (err) {
    console.error('Error calling Java API:', err);
    const transcripts = await fetchTranscripts();
    res.render('index', { title: 'Summarize a Transcript', transcripts: transcripts, error: 'Error summarizing transcript. Please try again.' });
  }
});

// Start the server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Frontend running at http://localhost:${PORT}`);
});
