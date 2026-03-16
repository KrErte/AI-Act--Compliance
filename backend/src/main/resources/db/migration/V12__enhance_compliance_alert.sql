-- Enhance compliance_alert table
ALTER TABLE compliance_alert ADD COLUMN IF NOT EXISTS source VARCHAR(100);
ALTER TABLE compliance_alert ADD COLUMN IF NOT EXISTS link_url VARCHAR(500);
ALTER TABLE compliance_alert ADD COLUMN IF NOT EXISTS dismissed BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE compliance_alert ADD COLUMN IF NOT EXISTS email_sent BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_compliance_alert_read ON compliance_alert(organization_id, read);
