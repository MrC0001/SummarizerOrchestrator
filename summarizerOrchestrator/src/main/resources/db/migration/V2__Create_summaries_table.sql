CREATE TABLE summaries (
    id BIGSERIAL PRIMARY KEY,
    transcript_id BIGINT NOT NULL REFERENCES transcripts(id),
    provider_name VARCHAR(255) NOT NULL,
    summary_text TEXT NOT NULL,
    UNIQUE(transcript_id, provider_name) -- Ensures one summary per provider per transcript
);
