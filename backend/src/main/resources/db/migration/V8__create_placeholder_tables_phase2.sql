-- Phase 2 placeholder tables — will be fully implemented later

CREATE TABLE generated_document (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ai_system_id    UUID NOT NULL REFERENCES ai_system(id) ON DELETE CASCADE,
    document_type   VARCHAR(100) NOT NULL,
    title           VARCHAR(500) NOT NULL,
    content         TEXT,
    format          VARCHAR(50) DEFAULT 'PDF',
    generated_by    UUID REFERENCES app_user(id),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE compliance_alert (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    ai_system_id    UUID REFERENCES ai_system(id) ON DELETE CASCADE,
    alert_type      VARCHAR(100) NOT NULL,
    title           VARCHAR(500) NOT NULL,
    message         TEXT,
    severity        VARCHAR(50) NOT NULL DEFAULT 'INFO',
    read            BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE gpai_model (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name            VARCHAR(255) NOT NULL,
    provider        VARCHAR(255),
    model_type      VARCHAR(100),
    has_systemic_risk BOOLEAN NOT NULL DEFAULT FALSE,
    training_compute_flops NUMERIC,
    organization_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_generated_document_ai_system ON generated_document(ai_system_id);
CREATE INDEX idx_compliance_alert_organization ON compliance_alert(organization_id);
CREATE INDEX idx_gpai_model_organization ON gpai_model(organization_id);
