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
    return await response.json();
  } catch (error) {
    console.error('Error fetching transcripts:', error);
    return [];
  }
}

// Main page - Display list of transcripts
app.get('/', async (req, res) => {
  const transcripts = await fetchTranscripts();
  res.render('index', { title: 'Summarize a Transcript', transcripts, error: null });
});

// New transcript page
app.get('/new', (req, res) => {
  res.render('new', { title: 'Create New Transcript', error: null });
});

// Handle creation of new transcript
app.post('/create', async (req, res) => {
  const { scenario, transcript } = req.body;

  try {
    const response = await fetch('http://localhost:8080/api/transcripts', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ scenario, transcript }),
    });

    if (!response.ok) {
      console.error('Failed to create transcript:', response.status, response.statusText);
      return res.render('new', { title: 'Create New Transcript', error: 'Failed to create transcript.' });
    }

    res.redirect('/');
  } catch (error) {
    console.error('Error creating transcript:', error);
    res.render('new', { title: 'Create New Transcript', error: 'An error occurred.' });
  }
});

// Update transcript page
app.get('/update', async (req, res) => {
  const transcripts = await fetchTranscripts();
  res.render('update', { title: 'Update Transcript', transcripts, error: null });
});

// Handle updating a transcript
app.post('/update', async (req, res) => {
  const { id, scenario, transcript } = req.body;

  try {
    const response = await fetch(`http://localhost:8080/api/transcripts/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ scenario, transcript }),
    });

    if (!response.ok) {
      throw new Error('Failed to update transcript.');
    }

    res.redirect('/');
  } catch (error) {
    console.error('Error updating transcript:', error);
    const transcripts = await fetchTranscripts();
    res.render('update', { title: 'Update Transcript', transcripts, error: error.message });
  }
});

// Delete transcript page
app.get('/delete', async (req, res) => {
  const transcripts = await fetchTranscripts();
  res.render('delete', { title: 'Delete Transcript', transcripts, error: null });
});

// Handle deleting a transcript
app.post('/delete', async (req, res) => {
  const { id } = req.body;

  try {
    const response = await fetch(`http://localhost:8080/api/transcripts/${id}`, { method: 'DELETE' });

    if (!response.ok) {
      throw new Error('Failed to delete transcript.');
    }

    res.redirect('/');
  } catch (error) {
    console.error('Error deleting transcript:', error);
    const transcripts = await fetchTranscripts();
    res.render('delete', { title: 'Delete Transcript', transcripts, error: error.message });
  }
});

app.post('/summarizeAll', async (req, res) => {
  const { transcriptSelect, transcript } = req.body;

  const payload = {
    prompt: "Summarize the following conversation:",
    context: transcript,
    parameters: { max_tokens: 200, temperature: 0.7 },
    transcriptId: parseInt(transcriptSelect), // Ensure this is valid
  };

  try {
    const response = await fetch('http://localhost:8080/api/summarize', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      throw new Error(`Summarize API failed with status ${response.status}`);
    }

    const responseData = await response.json();
    const newSummaries = responseData.newSummaries || [];
    const oldSummaries = responseData.oldSummaries || [];

    console.log("Transcript ID being passed to results.pug:", transcriptSelect);

    res.render('results', { 
      title: 'Summarization Results', 
      newSummaries, 
      oldSummaries, 
      transcriptId: transcriptSelect // Pass transcriptId to template
    });
  } catch (error) {
    console.error('Error summarizing transcript:', error);
    const transcripts = await fetchTranscripts();
    res.render('index', { 
      title: 'Summarize a Transcript', 
      transcripts, 
      error: 'Summarization failed.' 
    });
  }
});


// New route to display summaries page
app.get('/summaries', async (req, res) => {
  try {
    const transcripts = await fetchTranscripts();
    res.render('summaries', { title: 'Stored Summaries', transcripts, summaries: null, error: null });
  } catch (error) {
    console.error('Error fetching transcripts:', error);
    res.render('summaries', { title: 'Stored Summaries', transcripts: [], summaries: null, error: 'Failed to fetch transcripts.' });
  }
});


app.post('/summaries/view', async (req, res) => {
  const { transcriptId } = req.body;

  try {
    const response = await fetch(`http://localhost:8080/api/${transcriptId}`);
    if (!response.ok) {
      throw new Error('Failed to fetch transcript and summaries for the selected transcript.');
    }

    const data = await response.json();
    console.log("Fetched data for transcript and summaries:", data);

    const transcripts = await fetchTranscripts(); // Keep transcript list for dropdown
    res.render('summaries', { 
      title: 'Stored Summaries', 
      transcripts, 
      transcript: data.transcript, 
      summaries: data.summaries, 
      error: null 
    });
  } catch (error) {
    console.error('Error fetching transcript and summaries:', error);
    const transcripts = await fetchTranscripts();
    res.render('summaries', { 
      title: 'Stored Summaries', 
      transcripts, 
      transcript: null, 
      summaries: null, 
      error: 'Failed to fetch transcript and summaries for the selected transcript.' 
    });
  }
});





// Handle summarization request
app.post('/summaries/generate', async (req, res) => {
  const transcriptId = req.body.transcriptId;

  const transcripts = await fetchTranscripts();
  const transcript = transcripts.find(t => t.id === parseInt(transcriptId))?.transcript;

  if (!transcript) {
    return res.redirect('/summaries?error=Transcript not found');
  }

  const payload = {
    prompt: "Summarize the following conversation:",
    context: transcript,
    parameters: { max_tokens: 200, temperature: 0.7 },
  };

  try {
    const response = await fetch('http://localhost:8080/api/summarize', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      throw new Error(`Java API returned status ${response.status}`);
    }

    const summaries = await response.json();
    res.render('summaries', { title: 'Summaries', summaries, transcriptId });
  } catch (error) {
    console.error('Error generating summaries:', error);
    res.redirect(`/summaries?error=${encodeURIComponent(error.message)}`);
  }
});

// Handle overwrite request
app.post('/summaries/overwrite', async (req, res) => {
  const { transcriptId, newSummaries } = req.body;

  try {
    console.log("Received request body:", req.body); // Debugging log for full request body

    if (!transcriptId) {
      throw new Error('Transcript ID is missing.');
    }

    console.log("Transcript ID:", transcriptId);
    console.log("New Summaries:", newSummaries);

    // Parse newSummaries from JSON string to an array
    const parsedNewSummaries = JSON.parse(newSummaries);

    const response = await fetch(`http://localhost:8080/api/overwrite/${transcriptId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(parsedNewSummaries),
    });

    if (!response.ok) {
      const errorMessage = await response.text();
      throw new Error(`Backend error: ${errorMessage}`);
    }

    res.redirect('/summaries');
  } catch (error) {
    console.error('Error overwriting summaries:', error);
    res.redirect(`/summaries?error=${encodeURIComponent(error.message)}`);
  }
});



// Start the server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Frontend running at http://localhost:${PORT}`);
});
