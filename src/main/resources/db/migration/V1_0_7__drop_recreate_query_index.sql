DROP INDEX IF EXISTS idx_status_claim_id_last_modified_datetime;
CREATE INDEX IF NOT EXISTS idx_event_notification_status_last_modified_datetime ON EVENT_NOTIFICATION(STATUS, LAST_MODIFIED_DATETIME);
CREATE INDEX IF NOT EXISTS idx_event_notification_claim_id ON EVENT_NOTIFICATION(CLAIM_ID);
