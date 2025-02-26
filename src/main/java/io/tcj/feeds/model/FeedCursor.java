package io.tcj.feeds.model;

/**
 * stores the current position in the feed-published table for the <em>API</em> layer.
 *
 * <p>
 */
public record FeedCursor(String name, Long feedSyncId, int shard, int numShards) {

}
