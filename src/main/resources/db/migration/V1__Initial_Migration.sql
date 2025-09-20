-- Can be language or dialect
CREATE TABLE IF NOT EXISTS saban_language
(
    id            integer primary key generated always as identity,
    rtl           BOOLEAN     NOT NULL,
    language_name TEXT UNIQUE NOT NULL,
    language_code TEXT UNIQUE NOT NULL
);

-- Can be word or sentence
CREATE TABLE IF NOT EXISTS word
(
    id          integer primary key generated always as identity,
    language_id INT  NOT NULL,
    word_text   TEXT NOT NULL,
    CONSTRAINT fk_word_language_id FOREIGN KEY (language_id) REFERENCES saban_language (id) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS saban_user
(
    id            integer primary key generated always as identity,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE,
    username      TEXT                     NOT NULL,
    password_hash TEXT                     NOT NULL,
    email         TEXT                     NOT NULL,
    api_token     TEXT
);

CREATE TABLE IF NOT EXISTS pronunciation
(
    id          integer primary key generated always as identity,
    user_id     INT                      NOT NULL,
    word_id     INT                      NOT NULl,
    is_approved BOOLEAN                  NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    s3_key      TEXT                     NOT NULL,
    CONSTRAINT fk_pronunciation_user_id FOREIGN KEY (user_id) REFERENCES saban_user (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_pronunciation_word_id FOREIGN KEY (word_id) REFERENCES word (id) ON DELETE CASCADE ON UPDATE CASCADE
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

-- Indexes
CREATE INDEX word_name_idx on word (word_text);
CREATE UNIQUE INDEX word_language_name_idx on word (language_id, word_text);
CREATE INDEX user_name_idx on saban_user (username);
CREATE INDEX user_email_idx on saban_user (email);

-- Fulltext index