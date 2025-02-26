package io.tcj.feeds.api;

import io.tcj.feeds.model.FeedCursor;

public interface FeedCursorStore {
    FeedCursor get(String feedName, int shard);

    void put(String feedName, int shard, String cursor);
}
