-- Enhance gpai_model and create gpai_obligation table
ALTER TABLE gpai_model ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE gpai_model ADD COLUMN IF NOT EXISTS version VARCHAR(100);
ALTER TABLE gpai_model ADD COLUMN IF NOT EXISTS open_source BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE gpai_obligation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gpai_model_id UUID NOT NULL REFERENCES gpai_model(id) ON DELETE CASCADE,
    article_ref VARCHAR(100) NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'NOT_STARTED',
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_gpai_obligation_model ON gpai_obligation(gpai_model_id);
