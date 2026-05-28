package io.tcj.feeds.api;

import io.tcj.feeds.model.FeedCursor;

/**
 * Persists a consumer's position within a (possibly sharded) feed so each
 * {@code FeedConsumer.processBatch()} resumes after the last entry it handled. The
 * default implementation stores cursors in the {@code kv} table (ns {@code FEED_CURSOR}).
 */
public interface FeedCursorStore {
    /**
     * Reads the cursor for a feed/shard, returning a zero-valued cursor when none exists.
     *
     * @param feedName the feed name.
     * @param shard    the consumer's shard.
     * @return the stored {@link FeedCursor}.
     */
    FeedCursor get(String feedName, int shard);

    /**
     * Saves the cursor (the {@code feed_sync_id} high-water mark) for a feed/shard.
     *
     * @param feedName the feed name.
     * @param shard    the consumer's shard.
     * @param cursor   the new cursor value (a stringified {@code feed_sync_id}).
     */
    void put(String feedName, int shard, String cursor);
}
