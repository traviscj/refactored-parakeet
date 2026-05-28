package io.tcj.feeds.impl;

import io.tcj.feeds.api.FeedEntry;
import org.jooq.TableField;
import org.jooq.UpdatableRecord;

/**
 * Table-agnostic {@link FeedEntry}: wraps any jOOQ record and reads/writes its
 * {@code feed_sync_id} through the column handle supplied by the feed's
 * {@code FeedTableDef}. Replaces the per-table hand-written envelope.
 *
 * @param <Rec> the jOOQ record type backing this feed.
 */
public class GenericFeedEntry<Rec extends UpdatableRecord<Rec>> implements FeedEntry<Rec> {

    private final Rec record;
    private final TableField<Rec, Long> feedSyncId;

    public GenericFeedEntry(Rec record, TableField<Rec, Long> feedSyncId) {
        this.record = record;
        this.feedSyncId = feedSyncId;
    }

    @Override
    public Rec record() {
        return record;
    }

    @Override
    public long feedSyncId() {
        return record.get(feedSyncId);
    }

    @Override
    public FeedEntry<Rec> setFeedSyncId(long feedSyncId) {
        record.set(this.feedSyncId, feedSyncId);
        return this;
    }
}
