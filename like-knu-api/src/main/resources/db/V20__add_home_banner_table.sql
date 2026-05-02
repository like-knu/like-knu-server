CREATE TABLE IF NOT EXISTS home_banner
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    title           VARCHAR(50)  NOT NULL,
    body            VARCHAR(200) NOT NULL,
    link_path       VARCHAR(200),
    min_app_version VARCHAR(20),
    starts_at       DATETIME     NOT NULL,
    ends_at         DATETIME     NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_home_banner_active_period ON home_banner (active, starts_at, ends_at);
