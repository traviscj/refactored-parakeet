package io.tcj.feeds.model;

/**
 * In-process feed fetch request (the wire equivalent is the {@code FeedFetchRequest}
 * proto). Built by a co-located {@code FeedConsumer} to pull from a local feed source.
 */
public record FetchRequest(String feedName, String after, int limit, int shard, int numShards) {
}
