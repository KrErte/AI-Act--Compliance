-- Enhance generated_document table for full document generation feature
ALTER TABLE generated_document ADD COLUMN IF NOT EXISTS status VARCHAR(50) NOT NULL DEFAULT 'COMPLETED';
ALTER TABLE generated_document ADD COLUMN IF NOT EXISTS version INTEGER NOT NULL DEFAULT 1;
ALTER TABLE generated_document ADD COLUMN IF NOT EXISTS parent_id UUID REFERENCES generated_document(id);
ALTER TABLE generated_document ADD COLUMN IF NOT EXISTS metadata JSONB;
ALTER TABLE generated_document ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();

CREATE INDEX IF NOT EXISTS idx_generated_document_type ON generated_document(document_type);
