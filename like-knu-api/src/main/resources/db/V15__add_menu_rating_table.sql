CREATE TABLE IF NOT EXISTS menu_rating
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    rating   TINYINT      NOT NULL,
    rated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    device   VARCHAR(36)  NOT NULL,
    menu     VARCHAR(60)  NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (device) REFERENCES device (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (menu) REFERENCES menu (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_menu_rating_value CHECK (rating BETWEEN 1 AND 5)
);

ALTER TABLE menu_rating
    ADD UNIQUE INDEX device_menu_uix (device, menu);
