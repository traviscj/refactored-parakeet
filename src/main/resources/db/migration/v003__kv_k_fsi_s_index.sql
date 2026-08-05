-- Covering index for feed reads: scan by (feed_sync_id, shard).
-- Semantics: feed_sync_id NULL = pending publish; shard = -1 = unpublished/hidden from feed.
ALTER TABLE kv ADD KEY k_fsi_s (feed_sync_id, shard);
