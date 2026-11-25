CREATE DATABASE IF NOT EXISTS logs;

CREATE TABLE IF NOT EXISTS logs.access_log_all
(
    `timestamp` DateTime64(3, 'UTC'),
    `microservice` String,
    `api_endpoint` String,
    `http_method` String,
    `status_code` UInt16,
    `latency_ms` UInt32,
    `trace_id` String,
    `client_ip` String,
    `user_agent` String,
    `user_id` String DEFAULT '',
    `downstream_service` String DEFAULT '',
    `downstream_status_code` UInt16 DEFAULT 0,
    `downstream_duration_ms` UInt32 DEFAULT 0,
    `downstream_error_type` String DEFAULT ''
)
ENGINE = MergeTree
PARTITION BY toYYYYMM(timestamp)
ORDER BY (microservice, api_endpoint, timestamp, trace_id)
TTL toDateTime(timestamp) + INTERVAL 90 DAY
SETTINGS index_granularity = 8192;
