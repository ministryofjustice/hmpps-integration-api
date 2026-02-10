CREATE UNIQUE INDEX IF NOT EXISTS idx_event_notification_url_event_type ON EVENT_NOTIFICATION(URL, EVENT_TYPE) WHERE STATUS = 'PENDING' OR STATUS = NULL;

ALTER TABLE EVENT_NOTIFICATION
    DROP CONSTRAINT IF EXISTS idx_event_notification_url_event_type_status;

DROP INDEX IF EXISTS idx_event_notification_url_event_type_status;