CREATE TABLE ai_system (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    vendor              VARCHAR(255),
    version             VARCHAR(100),
    purpose             TEXT,
    deployment_context  VARCHAR(50),
    organization_role   VARCHAR(50),
    status              VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    risk_level          VARCHAR(50),
    compliance_score    INTEGER DEFAULT 0,
    compliance_status   VARCHAR(50) NOT NULL DEFAULT 'NOT_STARTED',
    organization_id     UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    classified_at       TIMESTAMP WITH TIME ZONE,
    classified_by       UUID REFERENCES app_user(id),
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ai_system_organization ON ai_system(organization_id);
CREATE INDEX idx_ai_system_risk_level ON ai_system(risk_level);
CREATE INDEX idx_ai_system_status ON ai_system(status);
CREATE INDEX idx_ai_system_not_deleted ON ai_system(organization_id) WHERE deleted = FALSE;
