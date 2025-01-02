CREATE TABLE metrics (
    id SERIAL PRIMARY KEY,
    transcript_id BIGINT NOT NULL REFERENCES transcripts(id),
    summary_id BIGINT NOT NULL REFERENCES summaries(id),
    rouge1 FLOAT,
    rouge2 FLOAT,
    rougeL FLOAT,
    bert_precision FLOAT,
    bert_recall FLOAT,
    bert_f1 FLOAT,
    bleu FLOAT,
    meteor FLOAT,
    length_ratio FLOAT,
    redundancy FLOAT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
