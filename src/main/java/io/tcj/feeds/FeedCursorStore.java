package io.tcj.feeds;

interface FeedCursorStore {
    FeedCursor get(String feedName, int shard);

    void put(String feedName, int shard, String cursor);
}
