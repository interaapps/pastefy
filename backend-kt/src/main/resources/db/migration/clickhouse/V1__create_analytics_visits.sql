CREATE TABLE IF NOT EXISTS `${analyticsDatabase}`.`${analyticsTable}`
(
    paste_key FixedString(8),
    paste_visibility LowCardinality(String),
    paste_user_id Nullable(FixedString(8)),
    visit_type LowCardinality(String),
    visited_at DateTime64(3, 'UTC') CODEC(DoubleDelta, ZSTD(1)),
    country LowCardinality(String),
    region LowCardinality(String),
    city LowCardinality(String),
    visitor_user_id Nullable(FixedString(8)),
    browser LowCardinality(String),
    device_type LowCardinality(String),
    os LowCardinality(String),
    ip_hash Nullable(UInt64),
    referer_host String,
    acquisition LowCardinality(String),
    is_bot UInt8
)
ENGINE = MergeTree
PARTITION BY toYYYYMM(visited_at)
ORDER BY (paste_key, visited_at)
TTL visited_at + INTERVAL ${retentionDays} DAY DELETE;
