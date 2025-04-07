-- Migration to add "location" column as jsonb to "user" table
ALTER TABLE "user"
ADD COLUMN location JSONB;
