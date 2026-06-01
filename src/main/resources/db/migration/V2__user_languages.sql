CREATE TABLE IF NOT EXISTS user_language
(
    user_id     INT                      NOT NULL,
    language_id INT                      NOT NULL,
    CONSTRAINT fk_userlang_user_id FOREIGN KEY (user_id) REFERENCES saban_user (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_userlang_language_id FOREIGN KEY (language_id) REFERENCES saban_language (id) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY (user_id, language_id)
)