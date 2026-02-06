CREATE UNIQUE INDEX IF NOT EXISTS idx_event_notification_url_event_type_status ON EVENT_NOTIFICATION(URL, EVENT_TYPE, STATUS);

ALTER TABLE EVENT_NOTIFICATION
    ADD CONSTRAINT idx_event_notification_url_event_type_status UNIQUE
    USING INDEX idx_event_notification_url_event_type_status;

ALTER TABLE EVENT_NOTIFICATION
    DROP CONSTRAINT IF EXISTS idx_event_notification_url_event_type;

DROP INDEX IF EXISTS idx_event_notification_url_event_type;