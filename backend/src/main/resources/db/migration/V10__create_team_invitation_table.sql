-- Team invitation system
CREATE TABLE team_invitation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'VIEWER',
    token VARCHAR(255) NOT NULL UNIQUE,
    organization_id UUID NOT NULL REFERENCES organization(id),
    invited_by UUID NOT NULL REFERENCES app_user(id),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    accepted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_team_invitation_token ON team_invitation(token);
CREATE INDEX idx_team_invitation_org ON team_invitation(organization_id);
CREATE INDEX idx_team_invitation_email ON team_invitation(email);
