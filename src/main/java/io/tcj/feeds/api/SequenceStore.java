package io.tcj.feeds.api;

import io.tcj.feeds.model.Sequence;

/**
 * Persists the publisher's monotonically increasing position per named feed, so
 * successive publish batches assign strictly increasing {@code feed_sync_id}s. The
 * default implementation stores sequences in the dedicated {@code sequences} table
 * (shared with the feedz Python tooling), where {@code value} is the <em>last
 * already-assigned</em> id for the sequence.
 */
public interface SequenceStore {
    /**
     * Looks up the current sequence for a feed, returning a zero-valued sequence
     * when none has been stored yet. Read-only: does not create the row.
     *
     * @param sequenceName the feed name.
     * @return the current {@link Sequence} ({@code value} = last assigned id, 0 if none).
     */
    Sequence lookup(String sequenceName);

    /**
     * Atomically reserves {@code count} ids from the named sequence, creating the
     * sequence on first use. The reserved ids are the contiguous range
     * {@code [returned - count + 1, returned]}.
     *
     * @param sequenceName the feed name.
     * @param count        how many ids to reserve; must be &gt; 0.
     * @return the last (highest) id reserved.
     */
    long allocate(String sequenceName, int count);
}
