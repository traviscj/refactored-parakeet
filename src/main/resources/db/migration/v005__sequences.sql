-- Named monotonic sequences (e.g. allocation of feed_sync_ids); optimistic-locked via version.
CREATE TABLE sequences (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  value BIGINT NOT NULL DEFAULT 0,
  version BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY u_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
