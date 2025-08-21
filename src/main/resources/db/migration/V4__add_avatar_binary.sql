ALTER TABLE users
    ADD COLUMN IF NOT EXISTS avatar_data bytea,
    ADD COLUMN IF NOT EXISTS avatar_mime text;
