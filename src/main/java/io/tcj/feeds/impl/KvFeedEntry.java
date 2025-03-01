package io.tcj.feeds.impl;

import io.tcj.feeds.api.FeedEntry;
import io.tcj.jooq.tables.records.KvRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KvFeedEntry implements FeedEntry<KvRecord> {

    private final KvRecord record;

    public KvFeedEntry(KvRecord record) {
        this.record = record;
    }

    @Override
    public KvRecord record() {
        return record;
    }

    @Override
    public long feedSyncId() {
        return record.getFeedSyncId();
    }

    @Override
    public FeedEntry<KvRecord> setFeedSyncId(long feedSyncId) {
        logger.info(String.format("kv.id=%s fsi=%s", record.getId(), feedSyncId));
        record.setFeedSyncId(feedSyncId);
        return this;
    }

    private static final Logger logger = LoggerFactory.getLogger(KvFeedEntry.class);
}
