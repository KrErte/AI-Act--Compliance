CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE organization (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name            VARCHAR(255) NOT NULL,
    industry        VARCHAR(100),
    country         VARCHAR(100),
    subscription_plan   VARCHAR(50) NOT NULL DEFAULT 'STARTER',
    subscription_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    lemonsqueezy_customer_id    VARCHAR(255),
    lemonsqueezy_subscription_id VARCHAR(255),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_organization_subscription_plan ON organization(subscription_plan);
