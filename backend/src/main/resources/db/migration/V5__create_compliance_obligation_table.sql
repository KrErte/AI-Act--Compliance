CREATE TABLE compliance_obligation (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ai_system_id    UUID NOT NULL REFERENCES ai_system(id) ON DELETE CASCADE,
    article_ref     VARCHAR(50) NOT NULL,
    article_title   VARCHAR(500) NOT NULL,
    description     TEXT,
    status          VARCHAR(50) NOT NULL DEFAULT 'NOT_STARTED',
    assigned_to     UUID REFERENCES app_user(id),
    due_date        DATE,
    notes           TEXT,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_compliance_obligation_ai_system ON compliance_obligation(ai_system_id);
CREATE INDEX idx_compliance_obligation_status ON compliance_obligation(status);
