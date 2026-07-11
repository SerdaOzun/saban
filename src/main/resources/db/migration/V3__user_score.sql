CREATE TABLE IF NOT EXISTS saban_scoring
(
    id               integer primary key generated always as identity,
    user_id          INT                      NOT NULL,
    pronunciation_id INT                      NOT NULL,
    points           INT                      NOT NULL, -- points gained
    reason           TEXT                     NOT NULL,
    points_from      INT,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_sabanscoring_user_id FOREIGN KEY (user_id) REFERENCES saban_user (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_sabanscoring_points_from_user_id FOREIGN KEY (points_from) REFERENCES saban_user (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_sabanscoring_pronunciation_id FOREIGN KEY (pronunciation_id) REFERENCES pronunciation (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX saban_scoring_user_idx ON saban_scoring (user_id);
CREATE INDEX saban_scoring_timestamp_idx ON saban_scoring (created_at);
-- For checking whether a user has already voted on a pronunciation or similar
CREATE unique index saban_scoring_points_from_idx ON saban_scoring (pronunciation_id, points_from);
