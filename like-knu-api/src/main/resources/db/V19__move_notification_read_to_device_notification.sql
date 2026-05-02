ALTER TABLE device_notification
    ADD COLUMN `read` TINYINT(1) DEFAULT 0 NOT NULL;

ALTER TABLE notification
    DROP COLUMN `read`;
