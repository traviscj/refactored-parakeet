package io.tcj.feeds.model;

public record FeedConsumerDef(String feedName, int shard, int limit, int numShards) {

}
