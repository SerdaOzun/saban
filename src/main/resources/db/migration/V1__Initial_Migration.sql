CREATE EXTENSION IF NOT EXISTS unaccent;

CREATE TEXT SEARCH CONFIGURATION simple_unaccent (COPY = simple);
ALTER TEXT SEARCH CONFIGURATION simple_unaccent
  ALTER MAPPING FOR hword, hword_part, word
  WITH unaccent, simple;

CREATE TABLE IF NOT EXISTS session_storage (
    id text primary key,
    session_value text
);

-- Can be language or dialect
CREATE TABLE IF NOT EXISTS saban_language
(
    id            integer primary key generated always as identity,
    rtl           BOOLEAN     NOT NULL,
    language_name TEXT UNIQUE NOT NULL,
    language_code TEXT UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS saban_user
(
    id            integer primary key generated always as identity,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE,
    username      TEXT                     NOT NULL,
    password_hash TEXT                     NOT NULL,
    email         TEXT                     NOT NULL,
    api_token     TEXT,
    country       TEXT
);

CREATE TABLE IF NOT EXISTS pronunciation
(
    id          integer primary key generated always as identity,
    user_id     INT                      NOT NULL,
    language_id INT                      NOT NULL,
    phrase_text TEXT                     NOT NULL,
    search_tsv  tsvector                 NOT NULL,
    is_approved BOOLEAN                  NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    s3_key      TEXT                     NOT NULL,
    CONSTRAINT  fk_pronunciation_user_id  FOREIGN KEY (user_id) REFERENCES saban_user (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT  fk_pronunciation_language_id FOREIGN KEY (language_id) REFERENCES saban_language (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS votes
(
    user_id          INT                      NOT NULL,
    pronunciation_id INT                      NOT NULL,
    vote_value       INT                      NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (pronunciation_id, user_id),
    CONSTRAINT fk_votes_user_id__id FOREIGN KEY (user_id) REFERENCES saban_user (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_votes_recording_id__id FOREIGN KEY (pronunciation_id) REFERENCES pronunciation (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS request
(
    id           integer primary key generated always as identity,
    requested_by INT                      NOT NULL,
    language_id  INT                      NOT NULL,
    phrase_text  TEXT                     NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    done         BOOLEAN                  NOT NULL,
    CONSTRAINT   fk_pronunciation_user_id  FOREIGN KEY (requested_by) REFERENCES saban_user (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT   fk_pronunciation_language_id FOREIGN KEY (language_id) REFERENCES saban_language (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

-- Indexes
CREATE INDEX request_language_date_idx ON request (language_id, created_at);
CREATE INDEX request_date_idx ON request (created_at);
CREATE INDEX pronunciation_search_tsv_idx ON pronunciation USING gin (search_tsv);
CREATE INDEX pronunciation_language_name_idx on pronunciation (language_id, phrase_text);
CREATE INDEX user_name_idx on saban_user (username);
CREATE INDEX user_email_idx on saban_user (email);

-- Trigger
CREATE OR REPLACE FUNCTION pronunciation_search_tsv_trigger()
RETURNS trigger AS $$
BEGIN
  NEW.search_tsv := to_tsvector('simple_unaccent', NEW.phrase_text);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER pronunciation_search_tsv_update
BEFORE INSERT OR UPDATE ON pronunciation
FOR EACH ROW EXECUTE FUNCTION pronunciation_search_tsv_trigger();