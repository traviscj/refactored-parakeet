package io.tcj.feeds.api;

import io.tcj.feeds.model.Sequence;

/**
 * Persists the publisher's monotonically increasing position per named feed, so
 * successive publish batches assign strictly increasing {@code feed_sync_id}s. The
 * default implementation stores sequences in the {@code kv} table (ns {@code FEED_SEQ}).
 */
public interface SequenceStore {
    /**
     * Looks up the current sequence for a feed, returning a zero-valued sequence
     * when none has been stored yet.
     *
     * @param sequenceName the feed name.
     * @return the current {@link Sequence}.
     */
    Sequence lookup(String sequenceName);

    /**
     * Stores the next (high-water) sequence value for a feed.
     *
     * @param sequenceName      the feed name.
     * @param nextSequenceValue the new high-water sequence value.
     */
    void put(String sequenceName, long nextSequenceValue);
}
