-- Per-(consumer, shard) feed consumption cursors; one row per (consumer, shard).
CREATE TABLE feed_cursors (
  id BIGINT NOT NULL AUTO_INCREMENT,
  consumer VARCHAR(255) NOT NULL,
  shard INT NOT NULL DEFAULT 0,
  shard_count INT NOT NULL DEFAULT 1,
  token VARCHAR(255) DEFAULT NULL,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  last_fetched_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY u_consumer_shard (consumer, shard)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
