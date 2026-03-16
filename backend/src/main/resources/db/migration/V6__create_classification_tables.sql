CREATE TABLE classification_question (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    question_key    VARCHAR(100) NOT NULL UNIQUE,
    question_text   TEXT NOT NULL,
    question_type   VARCHAR(50) NOT NULL DEFAULT 'YES_NO',
    options         JSONB,
    category        VARCHAR(100) NOT NULL,
    help_text       TEXT,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    depends_on      VARCHAR(100),
    depends_on_answer VARCHAR(100),
    active          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE classification_response (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ai_system_id    UUID NOT NULL REFERENCES ai_system(id) ON DELETE CASCADE,
    question_id     UUID NOT NULL REFERENCES classification_question(id),
    answer          VARCHAR(500) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_classification_response_ai_system ON classification_response(ai_system_id);
CREATE UNIQUE INDEX idx_classification_response_unique ON classification_response(ai_system_id, question_id);
