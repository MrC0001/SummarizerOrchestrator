CREATE TABLE transcripts (
    id SERIAL PRIMARY KEY,
    scenario VARCHAR(255) NOT NULL,
    transcript TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
