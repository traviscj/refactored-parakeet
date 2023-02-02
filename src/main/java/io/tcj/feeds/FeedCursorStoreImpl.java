package io.tcj.feeds;

import io.tcj.jooq.tables.records.KvRecord;
import io.tcj.keyvalue.KvStore;

import javax.inject.Inject;

public class FeedCursorStoreImpl implements FeedCursorStore {
    @Inject
    KvStore kvStore;

    @Override
    public FeedCursor get(String feedName, int shard) {
        KvRecord feedCursor = kvStore.get("FEED_CURSOR", feedName + "::" + shard);
        return new FeedCursor(feedName, Long.parseLong(feedCursor.getV()), shard, -1);
    }

    @Override
    public void put(String feedName, int shard, String cursor) {
        kvStore.put("FEED_CURSOR", feedName + "::" + shard, cursor);
    }
}
