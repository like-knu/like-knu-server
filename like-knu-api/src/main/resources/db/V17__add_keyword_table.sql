CREATE TABLE IF NOT EXISTS keyword
(
    id         VARCHAR(60) NOT NULL PRIMARY KEY,
    device_id  VARCHAR(36) NOT NULL,
    keyword    VARCHAR(50) NOT NULL,
    created_at DATETIME    NOT NULL,
    CONSTRAINT uk_keyword_device_keyword UNIQUE (device_id, keyword),
    CONSTRAINT fk_keyword_device FOREIGN KEY (device_id) REFERENCES device (id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX idx_keyword_keyword ON keyword (keyword);
