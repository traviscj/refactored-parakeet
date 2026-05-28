package io.tcj.feeds.model;

/**
 * Marker for the per-feed definition records bound into the engine. Definition
 * records such as {@link PublishedFeedDef}, {@link FeedConsumerDef} and
 * {@link FeedTableDef} each configure one feed; this marker is the hook for
 * treating "a feed's configuration" uniformly across the publisher/source/consumer
 * modules.
 */
public interface FeedDef {
}
