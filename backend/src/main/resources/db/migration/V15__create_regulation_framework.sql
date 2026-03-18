-- Regulation Framework Tables

CREATE TABLE regulation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    effective_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE regulation_domain (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    regulation_id UUID NOT NULL REFERENCES regulation(id) ON DELETE CASCADE,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    weight DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_regulation_domain_regulation ON regulation_domain(regulation_id);

CREATE TABLE regulation_question (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    domain_id UUID NOT NULL REFERENCES regulation_domain(id) ON DELETE CASCADE,
    question_en TEXT NOT NULL,
    question_et TEXT,
    article_ref VARCHAR(100),
    explanation_en TEXT,
    explanation_et TEXT,
    recommendation_en TEXT,
    recommendation_et TEXT,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_regulation_question_domain ON regulation_question(domain_id);

CREATE TABLE regulation_assessment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    regulation_id UUID NOT NULL REFERENCES regulation(id) ON DELETE CASCADE,
    overall_score DOUBLE PRECISION,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_regulation_assessment_org ON regulation_assessment(organization_id);

CREATE TABLE regulation_answer (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    assessment_id UUID NOT NULL REFERENCES regulation_assessment(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES regulation_question(id) ON DELETE CASCADE,
    answer INT NOT NULL CHECK (answer BETWEEN 0 AND 4),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_regulation_answer_assessment ON regulation_answer(assessment_id);

-- Seed initial regulations
INSERT INTO regulation (id, code, name, description, effective_date) VALUES
    ('00000000-0000-0000-0001-000000000001', 'AI_ACT', 'EU AI Act', 'Regulation (EU) 2024/1689 laying down harmonised rules on artificial intelligence.', '2024-08-01'),
    ('00000000-0000-0000-0001-000000000002', 'DORA', 'Digital Operational Resilience Act', 'Regulation (EU) 2022/2554 on digital operational resilience for the financial sector.', '2025-01-17');
