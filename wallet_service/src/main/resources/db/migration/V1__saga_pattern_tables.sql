-- =====================================================
-- SAGA PATTERN - DATABASE MIGRATIONS
-- Wallet Service (PostgreSQL)
-- =====================================================

-- 1. Outbox Events Table
CREATE TABLE IF NOT EXISTS outbox_events (
    id BIGSERIAL PRIMARY KEY,
    aggregate_id VARCHAR(100) NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retry INTEGER NOT NULL DEFAULT 5,
    last_error TEXT,
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    next_retry_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_outbox_status_created
    ON outbox_events(status, created_at);

CREATE INDEX IF NOT EXISTS idx_outbox_event_type
    ON outbox_events(event_type);

COMMENT ON TABLE outbox_events IS 'Outbox Pattern - Events pending publication to Kafka';

-- 2. Processed Events Table
CREATE TABLE IF NOT EXISTS processed_events (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    source_service VARCHAR(50),
    payload_hash VARCHAR(64),
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processing_result VARCHAR(20),
    result_details TEXT,
    CONSTRAINT uq_event_id_type UNIQUE (event_id, event_type)
);

CREATE INDEX IF NOT EXISTS idx_processed_at
    ON processed_events(processed_at);

COMMENT ON TABLE processed_events IS 'Idempotency - Tracks processed events to prevent duplicates';

-- Sample monitoring queries
-- SELECT COUNT(*) FROM outbox_events WHERE status='PENDING';
-- SELECT * FROM processed_events WHERE processing_result='FAILED' AND processed_at > NOW() - INTERVAL '1 hour';

