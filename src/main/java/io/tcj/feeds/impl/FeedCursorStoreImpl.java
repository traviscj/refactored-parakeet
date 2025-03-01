package io.tcj.feeds.impl;

import io.tcj.feeds.api.FeedCursorStore;
import io.tcj.feeds.model.FeedCursor;
import io.tcj.jooq.tables.records.KvRecord;
import io.tcj.keyvalue.KvStore;

import javax.inject.Inject;

public class FeedCursorStoreImpl implements FeedCursorStore {
    @Inject
    KvStore kvStore;

    @Override
    public FeedCursor get(String feedName, int shard) {
        // TODO: need to respect FeedConsumerDef
        KvRecord feedCursor = kvStore.getOptional("FEED_CURSOR", feedName + "::" + shard)
                .orElseGet(() -> {
                    KvRecord kvRecord = new KvRecord();
                    kvRecord.setNs("FEED_CURSOR");
                    kvRecord.setK(feedName + "::" + shard);
                    kvRecord.setV("0");
                    return kvRecord;
                });
        return new FeedCursor(feedName, Long.parseLong(feedCursor.getV()
        ), shard, -1);
    }

    @Override
    public void put(String feedName, int shard, String cursor) {
        kvStore.put("FEED_CURSOR", feedName + "::" + shard, cursor);
    }
}
