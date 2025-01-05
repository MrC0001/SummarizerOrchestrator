CREATE TABLE reference_summaries (
    id BIGSERIAL PRIMARY KEY,
    transcript_id BIGINT NOT NULL REFERENCES transcripts(id),
    summary_text TEXT NOT NULL
);
