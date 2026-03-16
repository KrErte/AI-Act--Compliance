ALTER TABLE app_user ADD COLUMN IF NOT EXISTS language_preference VARCHAR(5) DEFAULT 'en';
